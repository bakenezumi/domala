package domala.internal.macros

import domala.GenerationType
import domala.internal.macros.TypeHelper.toType
import domala.message.Message
import org.seasar.doma.internal.apt.meta.MetaConstants

import scala.collection.immutable.Seq
import scala.meta._

case class EntitySetting(
  listener: Type,
  naming: Term with Pat
)

/**
  * @see [[https://github.com/domaframework/doma/tree/master/src/main/java/org/seasar/doma/internal/apt/EntityTypeGenerator.java]]
  */
object EntityTypeGenerator {

  def generate(cls: Defn.Class, args: Seq[Term.Arg]): Defn.Object = {
    if(cls.tparams.nonEmpty)
      abort(Message.DOMALA4051.getMessage(cls.name.syntax))
    validateFieldAnnotation(cls.name, cls.ctor)
    val entitySetting = EntitySetting(
      args.collectFirst { case arg"listener = classOf[$x]" => x }.getOrElse(t"org.seasar.doma.jdbc.entity.NullEntityListener[${cls.name}]"),
      args.collectFirst { case arg"naming = $x" => Term.Name(x.syntax) }.getOrElse(q"null")
    )
    val tableSetting = TableSetting.read(cls.mods)
    val fields = generateFields(cls.name, cls.ctor, entitySetting)
    val constructor = generateConstructor(cls.name, cls.ctor, entitySetting, tableSetting)
    val methods = generateMethods(cls.name, cls.ctor, entitySetting)

    q"""
    object ${Term.Name(cls.name.syntax)} extends org.seasar.doma.jdbc.entity.AbstractEntityType[${cls.name}] {

      object ListenerHolder {
        val listener =
          new ${entitySetting.listener.syntax.parse[Ctor.Call].get}()
      }

      ..${Seq(CaseClassMacroHelper.generateApply(cls), CaseClassMacroHelper.generateUnapply(cls))}

      ..${fields ++ constructor ++ methods}
    }
    """
  }

  protected def validateFieldAnnotation(clsName: Type.Name, ctor: Ctor.Primary): Unit = {
    ctor.paramss.flatten.foreach { p =>
      p.mods.collect {
        case mod"@Id" | mod"@Id()" | mod"@domala.Id" | mod"@domala.Id()"=> "@Id"
        case mod"@TenantId" | mod"@TenantId()" | mod"@domala.TenantId" | mod"@domala.TenantId()" => "@TenantId"
        case mod"@Transient" | mod"@Transient()" | mod"@domala.Transient" | mod"@domala.Transient()" => "@Transient"
        case mod"@Version" | mod"@Version()" | mod"@domala.Version" | mod"@domala.Version()" => "@Version"
      } match {
        case x :: y :: _ => abort(Message.DOMALA4086.getMessage(x, y, clsName.syntax, p.name.syntax))
        case _ => ()
      }
    }
  }

  protected def generateFields(clsName: Type.Name, ctor: Ctor.Primary, entitySetting: EntitySetting): Seq[Stat] = {
    val propertySize = ctor.paramss.flatten.size

    val namingTypeField =
      q"private val __namingType: org.seasar.doma.jdbc.entity.NamingType = ${entitySetting.naming}"
    val idGeneratorField = generateIdGeneratorFields(clsName, ctor)

    val propertyTypesFields =
      q"""
      val __idList = new java.util.ArrayList[org.seasar.doma.jdbc.entity.EntityPropertyType[$clsName, _]]()
      val __list = new java.util.ArrayList[org.seasar.doma.jdbc.entity.EntityPropertyType[$clsName, _]]($propertySize)
      val __map = new java.util.HashMap[String, org.seasar.doma.jdbc.entity.EntityPropertyType[$clsName, _]]($propertySize)
      val __collections = domala.internal.macros.reflect.EntityCollections[$clsName](__list, __map, __idList)
      """.stats

    val propertyTypeFields = generatePropertyTypeFields(clsName, ctor)

    namingTypeField +: (
    idGeneratorField ++
    propertyTypesFields ++
    propertyTypeFields )
  }

  protected def generateIdGeneratorFields(clsName: Type.Name, ctor: Ctor.Primary): Seq[Stat] = {
    val params = ctor.paramss.flatten
    val idParams = params.filter(p => p.mods.exists {
      case mod"@Id" | mod"@domala.Id" | mod"@Id()" | mod"@domala.Id()" => true
      case _ => false
    })
    val generatedValueParams = params.filter(p => p.mods.exists {
      case mod"@GeneratedValue($_)" => true
      case _ => false
    })
    if(generatedValueParams.length > 1) {
      abort(Message.DOMALA4037.getMessage(clsName.syntax, idParams.head.name.syntax))
    }
    if(generatedValueParams.length == 1) {
      if(idParams.size > 1) {
        abort(Message.DOMALA4036.getMessage(clsName.syntax))
      }
      if(idParams.isEmpty || idParams.head.name.syntax != generatedValueParams.head.name.syntax) {
        abort(Message.DOMALA4033.getMessage(clsName.syntax, generatedValueParams.head.name.syntax))
      }
    }
    val strategy = generatedValueParams.flatMap(_.mods).collectFirst {
      case mod"@GeneratedValue(strategy = $strategy)" => strategy
      case mod"@GeneratedValue($strategy)" => strategy
    }.map {
      case q"GenerationType.IDENTITY" => GenerationType.IDENTITY
      case q"GenerationType.SEQUENCE" => GenerationType.SEQUENCE
      case q"GenerationType.TABLE" => GenerationType.TABLE
    }

    params.foreach(p => p.mods.collect {
      case mod"@SequenceGenerator(..$_)" =>
        if(strategy.isEmpty || p.name.syntax != generatedValueParams.head.name.syntax || {
          strategy match {
          case Some(GenerationType.SEQUENCE) => false
          case _ => true
          }})
          abort(Message.DOMALA4030.getMessage(clsName.syntax, p.name.syntax + ":" + strategy))
      case mod"@TableGenerator(..$_)" =>
        if(generatedValueParams.isEmpty || p.name.syntax != generatedValueParams.head.name.syntax || {
          strategy match {
            case  Some(GenerationType.TABLE) => false
            case _ => true
          }})
          abort(Message.DOMALA4031.getMessage(clsName.syntax, p.name.syntax))
    })

    strategy.map {
      case GenerationType.IDENTITY => Seq(q"private val __idGenerator = new org.seasar.doma.jdbc.id.BuiltinIdentityIdGenerator()")
      case GenerationType.SEQUENCE =>
        val sequenceGeneratorSetting = SequenceGeneratorSetting.read(idParams.head.mods, clsName.syntax)
          .getOrElse(abort(domala.message.Message.DOMALA4034.getMessage(clsName.syntax, idParams.head.name.syntax)))
        q"""
          private val __idGenerator = new org.seasar.doma.jdbc.id.BuiltinSequenceIdGenerator()
          __idGenerator.setQualifiedSequenceName(${sequenceGeneratorSetting.sequence})
          __idGenerator.setInitialValue(${sequenceGeneratorSetting.initialValue})
          __idGenerator.setAllocationSize(${sequenceGeneratorSetting.allocationSize})
          __idGenerator.initialize()
          """.stats
      case GenerationType.TABLE =>
        val tableGeneratorSetting = TableGeneratorSetting.read(idParams.head.mods, clsName.syntax)
          .getOrElse(abort(domala.message.Message.DOMALA4035.getMessage(clsName.syntax, idParams.head.name.syntax)))
        q"""
          private val __idGenerator = new org.seasar.doma.jdbc.id.BuiltinTableIdGenerator()
          __idGenerator.setQualifiedTableName(${tableGeneratorSetting.table})
          __idGenerator.setInitialValue(${tableGeneratorSetting.initialValue})
          __idGenerator.setAllocationSize(${tableGeneratorSetting.allocationSize})
          __idGenerator.setPkColumnName(${tableGeneratorSetting.pkColumnName})
          __idGenerator.setPkColumnValue(${tableGeneratorSetting.pkColumnValue})
          __idGenerator.setValueColumnName(${tableGeneratorSetting.valueColumnName})
          __idGenerator.initialize()
          """.stats
      }.getOrElse(
        Seq(q"private val __idGenerator = null")
      )
  }

  protected def generatePropertyTypeFields(clsName: Type.Name, ctor: Ctor.Primary): Seq[Defn.Val] = {
    if (ctor.paramss.flatten.flatMap(_.mods).count {
      case mod"@Version" | mod"@domala.Version" | mod"@Version()" | mod"@domala.Version()"=> true
      case _ => false
    } > 1) abort(Message.DOMALA4024.getMessage(clsName.syntax))

    ctor.paramss.flatten.map { p =>
      val Term.Param(mods, name, Some(decltpe), default) = p
      val columnSetting = ColumnSetting.read(mods)
      val tpe = Type.Name(decltpe.toString)
      if(name.syntax.startsWith(MetaConstants.RESERVED_NAME_PREFIX)) {
        abort(Message.DOMALA4025.getMessage(MetaConstants.RESERVED_NAME_PREFIX, clsName.syntax, name.syntax))
      }
      val propertyName = Pat.Var.Term(Term.Name("$" + name.syntax))

      val (isBasic, nakedTpe, newWrapperExpr) = TypeHelper.convertToEntityDomaType(decltpe) match {
        case DomaType.Basic(_, convertedType, wrapperSupplier) => (true, convertedType, wrapperSupplier)
        case DomaType.Option(DomaType.Basic(_, convertedType, wrapperSupplier), _) => (true, convertedType, wrapperSupplier)
        case DomaType.EntityOrHolderOrEmbeddable(otherType) => (false, otherType, q"null")
        case DomaType.Option(DomaType.EntityOrHolderOrEmbeddable(otherType), _) => (false, otherType,  q"null")
        case _ => abort(Message.DOMALA4096.getMessage(decltpe.syntax, clsName.syntax, name.syntax))
      }

      val isId = mods.exists {
        case mod"@Id" | mod"@domala.Id" | mod"@Id()" | mod"@domala.Id()"=> true
        case _ => false
      }

      val isIdGenerate = mods.exists {
        case mod"@GeneratedValue($_)" => true
        case _ => false
      }

      val isTenantId = false

      val isVersion = mods.exists {
        case mod"@Version" | mod"@domala.Version" | mod"@Version()" | mod"@domala.Version()" => true
        case _ => false
      }
      if(columnSetting.insertable.syntax == "false" && (isId || isVersion || isTenantId))
        abort(Message.DOMALA4088.getMessage(clsName.syntax, name.syntax))
      if(columnSetting.updatable.syntax == "false" && (isId || isVersion || isTenantId))
        abort(Message.DOMALA4089.getMessage(clsName.syntax, name.syntax))

      q"""
      val $propertyName = domala.internal.macros.reflect.EntityReflectionMacros.generatePropertyType[$tpe, $clsName, $nakedTpe](
        classOf[$clsName],
        ${name.syntax},
        __namingType,
        ${if(isId) q"true" else q"false"},
        ${if(isIdGenerate) q"true" else q"false"},
        __idGenerator,
        ${if(isVersion) q"true" else q"false"},
        ${if(isBasic) q"true" else q"false"},
        $newWrapperExpr,
        ${columnSetting.name},
        ${columnSetting.insertable},
        ${columnSetting.updatable},
        ${columnSetting.quote},
        __collections
      )
      """
    }
  }

  protected def generateConstructor(clsName: Type.Name, ctor: Ctor.Primary, entitySetting: EntitySetting, tableSetting: TableSetting): Seq[Stat] = {
    q"""
    val __listenerSupplier: java.util.function.Supplier[${entitySetting.listener}] =
      () => ListenerHolder.listener
    val __immutable = true
    val __name = ${Term.Name("\"" + clsName.syntax + "\"")}
    val __catalogName = ${Term.Name(tableSetting.catalog.syntax)}
    val __schemaName = ${Term.Name(tableSetting.schema.syntax)}
    val __tableName = ${Term.Name(tableSetting.name.syntax)}
    val __isQuoteRequired = ${tableSetting.quote}
    val __idPropertyTypes = java.util.Collections.unmodifiableList(__idList)
    val __entityPropertyTypes = java.util.Collections.unmodifiableList(__list)
    val __entityPropertyTypeMap: java.util.Map[
      String,
      org.seasar.doma.jdbc.entity.EntityPropertyType[$clsName, _]] =
      java.util.Collections.unmodifiableMap(__map)
    """.stats
  }

  protected def generateGeneratedIdPropertyType(clsName: Type.Name, ctor: Ctor.Primary): Term = {
    ctor.paramss.flatten.collect {
      case param if param.mods.exists(mod => mod.syntax.startsWith("@GeneratedValue")) =>
        ("$" + param.name.syntax + s".asInstanceOf[org.seasar.doma.jdbc.entity.GeneratedIdPropertyType[$clsName, $clsName, _ <: Number, _]]").parse[Term].get
    }.headOption.getOrElse(q"null")
  }

  protected def generateVersionPropertyType(clsName: Type.Name, ctor: Ctor.Primary): Term = {
    ctor.paramss.flatten.collect {
      case param if param.mods.exists(mod => mod.syntax.startsWith("@Version") || mod.syntax.startsWith("@domala.Version")) =>
        ("$" + param.name.syntax  + s".asInstanceOf[org.seasar.doma.jdbc.entity.VersionPropertyType[$clsName, $clsName, _ <: Number, _]]").parse[Term].get
    }.headOption.getOrElse(q"null")
  }

  protected def generateMethods(clsName: Type.Name, ctor: Ctor.Primary, entitySetting: EntitySetting): Seq[Stat] = {
    q"""

    override def getNamingType: org.seasar.doma.jdbc.entity.NamingType = __namingType

    override def isImmutable: Boolean = __immutable

    override def getName: String = __name

    override def getCatalogName: String = __catalogName

    override def getSchemaName: String = __schemaName

    override def getTableName: String =
      getTableName(org.seasar.doma.jdbc.Naming.DEFAULT.apply _)

    override def getTableName(
      namingFunction: java.util.function.BiFunction[
        org.seasar.doma.jdbc.entity.NamingType,
        String,
        String]): String = {
      if (__tableName.isEmpty) {
        namingFunction.apply(__namingType, __name)
      } else {
        __tableName
      }
    }

    override def isQuoteRequired = __isQuoteRequired

    override def preInsert(
        entity: $clsName,
        context: org.seasar.doma.jdbc.entity.PreInsertContext[$clsName]) = {
      val __listenerClass =
        classOf[${entitySetting.listener}]
      val __listener =
        context.getConfig.getEntityListenerProvider
          .get[$clsName, ${entitySetting.listener}](__listenerClass, __listenerSupplier)
      __listener.preInsert(entity, context)
    }

    override def preUpdate(
        entity: $clsName,
        context: org.seasar.doma.jdbc.entity.PreUpdateContext[$clsName]) = {
      val __listenerClass =
        classOf[${entitySetting.listener}]
      val __listener =
        context.getConfig.getEntityListenerProvider
          .get[$clsName, ${entitySetting.listener}](__listenerClass, __listenerSupplier)
      __listener.preUpdate(entity, context)
    }

    override def preDelete(
        entity: $clsName,
        context: org.seasar.doma.jdbc.entity.PreDeleteContext[$clsName]) = {
      val __listenerClass =
        classOf[${entitySetting.listener}]
      val __listener =
        context.getConfig.getEntityListenerProvider
          .get[$clsName, ${entitySetting.listener}](__listenerClass, __listenerSupplier)
      __listener.preDelete(entity, context)
    }

    override def postInsert(
        entity: $clsName,
        context: org.seasar.doma.jdbc.entity.PostInsertContext[$clsName]) = {
      val __listenerClass =
        classOf[${entitySetting.listener}]
      val __listener =
        context.getConfig.getEntityListenerProvider
          .get[$clsName,${entitySetting.listener}](__listenerClass, __listenerSupplier)
      __listener.postInsert(entity, context)
    }

    override def postUpdate(
        entity: $clsName,
        context: org.seasar.doma.jdbc.entity.PostUpdateContext[$clsName]) = {
      val __listenerClass =
        classOf[${entitySetting.listener}]
      val __listener =
        context.getConfig.getEntityListenerProvider
          .get[$clsName, ${entitySetting.listener}](__listenerClass, __listenerSupplier)
      __listener.postUpdate(entity, context)
    }

    override def postDelete(
        entity: $clsName,
        context: org.seasar.doma.jdbc.entity.PostDeleteContext[$clsName]) = {
      val __listenerClass =
        classOf[${entitySetting.listener}]
      val __listener =
        context.getConfig.getEntityListenerProvider
          .get[$clsName, ${entitySetting.listener}](__listenerClass, __listenerSupplier)
      __listener.postDelete(entity, context)
    }

    override def getEntityPropertyTypes = __entityPropertyTypes

    override def getEntityPropertyType(__name: String) = __entityPropertyTypeMap.get(__name)

    override def getIdPropertyTypes = __idPropertyTypes

    override def getGeneratedIdPropertyType: org.seasar.doma.jdbc.entity.GeneratedIdPropertyType[_ >: $clsName, $clsName, _, _] = ${generateGeneratedIdPropertyType(clsName, ctor)}

    override def getVersionPropertyType: org.seasar.doma.jdbc.entity.VersionPropertyType[_ >: $clsName, $clsName, _, _] = ${generateVersionPropertyType(clsName, ctor)}

    override def getTenantIdPropertyType: org.seasar.doma.jdbc.entity.TenantIdPropertyType[_ >: $clsName, $clsName, _, _] = null

    """.stats ++ {
        val params = ctor.paramss.flatten.map { p =>
          q"domala.internal.macros.reflect.EntityReflectionMacros.readProperty[${toType(p.decltpe.get)}, $clsName](classOf[$clsName], __args, ${p.name.syntax})"
        }
        Seq(
          q"""
          override def newEntity(
              __args: java.util.Map[
                String,
                org.seasar.doma.jdbc.entity.Property[$clsName, _]]) =
            new ${clsName.toString.parse[Ctor.Call].get}(
              ..$params
            )
          """)
    } ++
    q"""
    override def getEntityClass = classOf[$clsName]

    override def getOriginalStates(__entity: $clsName): $clsName = null

    override def saveCurrentStates(__entity: $clsName): Unit = {}

    """.stats
  }
}
