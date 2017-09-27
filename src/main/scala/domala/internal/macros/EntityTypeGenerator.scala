package domala.internal.macros

import domala.internal.macros.TypeHelper.toType

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
    val idMods = params.filter(p => p.mods.exists {
      case  mod"@Id" | mod"@domala.Id" => true
      case _ => false
    })
    if (idMods.nonEmpty) { // has Id annotation
      val strategies = ctor.paramss.flatten.flatMap(_.mods.collect {
        case mod"@GeneratedValue(strategy = $strategy)" => strategy
      })
      if(strategies.size > 1) {
        abort(org.seasar.doma.message.Message.DOMA4037.getMessage(clsName.syntax, idMods.head.name.syntax))
      } else if(strategies.isEmpty) { // Id only
        Seq(q"private val __idGenerator = null")
      } else {
        if(idMods.size > 1) {
          abort(org.seasar.doma.message.Message.DOMA4036.getMessage(clsName.syntax))
        }
        strategies.head match {
          case q"GenerationType.IDENTITY" => Seq(q"private val __idGenerator = new org.seasar.doma.jdbc.id.BuiltinIdentityIdGenerator()")
          case q"GenerationType.SEQUENCE" =>
            val sequenceGeneratorSetting = SequenceGeneratorSetting.read(idMods.head.mods, clsName.syntax, idMods.head.name.syntax)
            q"""
            private val __idGenerator = new org.seasar.doma.jdbc.id.BuiltinSequenceIdGenerator()
            __idGenerator.setQualifiedSequenceName(${sequenceGeneratorSetting.sequence})
            __idGenerator.setInitialValue(${sequenceGeneratorSetting.initialValue})
            __idGenerator.setAllocationSize(${sequenceGeneratorSetting.allocationSize})
            __idGenerator.initialize()
            """.stats
          case q"GenerationType.TABLE" =>
            val tableGeneratorSetting = TableGeneratorSetting.read(idMods.head.mods, clsName.syntax, idMods.head.name.syntax)
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
        }
      }
    } else { // no Id
      Seq(q"private val __idGenerator = null")
    }
  }

  protected def generatePropertyTypeFields(clsName: Type.Name, ctor: Ctor.Primary): Seq[Defn.Val] = {
    if (ctor.paramss.flatten.flatMap(_.mods).count {
      case mod"@Version" | mod"@domala.Version" => true
      case _ => false
    } > 1) abort(org.seasar.doma.message.Message.DOMA4024.getMessage(clsName.syntax))

    ctor.paramss.flatten.map { p =>
      val Term.Param(mods, name, Some(decltpe), default) = p
      val columnSetting = ColumnSetting.read(mods)
      val tpe = Type.Name(decltpe.toString)
      val propertyName = Pat.Var.Term(Term.Name("$" + name.syntax))

      val (isBasic, nakedTpe, newWrapperExpr) = TypeHelper.convertToEntityDomaType(decltpe) match {
        case DomaType.Basic(_, convertedType, wrapperSupplier) => (true, convertedType, wrapperSupplier)
        case DomaType.Option(DomaType.Basic(_, convertedType, wrapperSupplier), _) => (true, convertedType, wrapperSupplier)
        case DomaType.EntityOrHolderOrEmbeddable(otherType) => (false, otherType, q"null")
        case DomaType.Option(DomaType.EntityOrHolderOrEmbeddable(otherType), _) => (false, otherType,  q"null")
        case _ => abort(domala.message.Message.DOMALA4096.getMessage(decltpe.syntax, clsName.syntax, name.syntax))
      }

      val isId = mods.exists {
        case mod"@Id" | mod"@domala.Id" => true
        case _ => false
      }

       val isIdGenerate = mods.exists {
         case mod"@GeneratedValue($_)" => true
         case _ => false
       }

      // TODO: IntelliJでなぜかエラー（アノテーションが読めてない模様）
      // if(isIdGenerate && !isId) abort(org.seasar.doma.message.Message.DOMA4033.getMessage(clsName.syntax, name.syntax))

      val isVersion = mods.exists {
        case mod"@Version" | mod"@domala.Version" => true
        case _ => false
      }

      q"""
      val $propertyName = domala.internal.macros.reflect.EntityReflectionMacros.generatePropertyType(
        classOf[$tpe],
        classOf[$clsName],
        classOf[$nakedTpe],
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
    """.stats ++ {
        val params = ctor.paramss.flatten.map { p =>
          q"domala.internal.macros.reflect.EntityReflectionMacros.readProperty(classOf[$clsName], __args, ${p.name.syntax}, classOf[${toType(p.decltpe.get)}])"
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
