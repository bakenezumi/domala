package domala.internal.macros.generator

import domala.GenerationType
import domala.internal.macros.DomaType
import domala.internal.macros.args.{ColumnArgs, SequenceGeneratorArgs, TableArgs, TableGeneratorArgs}
import domala.internal.macros.util.NameConverters._
import domala.internal.macros.util.TypeUtil.toType
import domala.internal.macros.util.{MacrosHelper, TypeUtil}
import domala.message.Message
import org.seasar.doma.internal.apt.meta.MetaConstants

import scala.collection.immutable.Seq
import scala.meta._

/**
  * @see [[https://github.com/domaframework/doma/tree/master/src/main/java/org/seasar/doma/internal/apt/EntityTypeGenerator.java]]
  */
object EntityDescGenerator {

  case class EntityArgs(
    listener: Type,
    naming: Term with Pat
  )

  def generate(cls: Defn.Class, maybeOriginalCompanion: Option[Defn.Object], args: Seq[Term.Arg]): Term.Block = {
    if(cls.tparams.nonEmpty)
      MacrosHelper.abort(Message.DOMALA4051, cls.name.syntax)
    validateFieldAnnotation(cls.name, cls.ctor)
    val entityArgs = EntityArgs(
      args.collectFirst {
        case arg"listener = classOf[$x]" => x
        case arg"classOf[$x]" => x
      }.getOrElse(t"org.seasar.doma.jdbc.entity.NullEntityListener[${cls.name}]"),
      args.collectFirst { case arg"naming = $x" => Term.Name(x.syntax) }.getOrElse(q"null")
    )
    val tableSetting = TableArgs.of(cls.mods)
    val fields = generateFields(cls.name, cls.ctor, entityArgs)
    val constructor = generateConstructor(cls.name, cls.ctor, entityArgs, tableSetting)
    val methods = generateMethods(cls.name, cls.ctor, entityArgs)

    val generatedCompanion = q"""
    object ${Term.Name(cls.name.syntax)} extends domala.jdbc.entity.EntityCompanion[${cls.name}] {
      val entityDesc: domala.jdbc.entity.EntityDesc[${cls.name}] = EntityDesc
      object EntityDesc extends domala.jdbc.entity.AbstractEntityDesc[${cls.name}] {
        object ListenerHolder {
          domala.internal.macros.reflect.EntityReflectionMacros.validateListener(classOf[${cls.name}], classOf[${entityArgs.listener}])
          val listener =
            new ${entityArgs.listener.syntax.parse[Ctor.Call].get}()
        }
        ..${fields ++ constructor ++ methods}
      }
      ..${Seq(CaseClassGenerator.generateApply(cls, maybeOriginalCompanion), CaseClassGenerator.generateUnapply(cls, maybeOriginalCompanion))}
    }
    """
    val newCompanion = MacrosHelper.mergeObject(maybeOriginalCompanion, generatedCompanion)

    //logger.debug(newCompanion)

    Term.Block(Seq(
      // 警告抑制のため一部アノテーションを除去
      // https://github.com/scala/bug/issues/9612
      cls.copy(
        mods = cls.mods.filter {
          case mod"@Table(..$_)" => false
          case _ => true
        },
        ctor = cls.ctor.copy(paramss = cls.ctor.paramss.map(ps => ps.map(p => p.copy(mods = p.mods.filter {
          case mod"@Column(..$_)" => false
          case mod"@GeneratedValue(..$_)" => false
          case mod"@SequenceGenerator(..$_)" => false
          case mod"@TableGenerator(..$_)" => false
          case _ => true
        }))))
      ),
      newCompanion
    ))

  }

  protected def validateFieldAnnotation(clsName: Type.Name, ctor: Ctor.Primary): Unit = {
    ctor.paramss.flatten.foreach { p =>
      p.mods.collect {
        case mod"@Id" | mod"@Id()" | mod"@domala.Id" | mod"@domala.Id()"=> "@Id"
        case mod"@TenantId" | mod"@TenantId()" | mod"@domala.TenantId" | mod"@domala.TenantId()" => "@TenantId"
        case mod"@Transient" | mod"@Transient()" | mod"@domala.Transient" | mod"@domala.Transient()" => "@Transient"
        case mod"@Version" | mod"@Version()" | mod"@domala.Version" | mod"@domala.Version()" => "@Version"
      } match {
        case x :: y :: _ => MacrosHelper.abort(Message.DOMALA4086, x, y, clsName.syntax, p.name.syntax)
        case _ => ()
      }
    }
  }

  protected def generateFields(clsName: Type.Name, ctor: Ctor.Primary, entitySetting: EntityArgs): Seq[Stat] = {
    val propertySize = ctor.paramss.flatten.size

    val namingTypeField =
      q"private[this]  val __namingType: org.seasar.doma.jdbc.entity.NamingType = ${entitySetting.naming}"
    val idGeneratorField = generateIdGeneratorFields(clsName, ctor)

    val propertyTypesFields =
      q"""
      private[this] val __idList = new java.util.ArrayList[domala.jdbc.entity.EntityPropertyDesc[$clsName, _]]()
      private[this] val __list = new java.util.ArrayList[domala.jdbc.entity.EntityPropertyDesc[$clsName, _]](${Term.Name(propertySize.toString)})
      private[this] val __map = new java.util.HashMap[String, domala.jdbc.entity.EntityPropertyDesc[$clsName, _]](${Term.Name(propertySize.toString)})
      private[this] val __collections = domala.internal.macros.reflect.EntityCollections[$clsName](__list, __map, __idList)
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
    //noinspection ScalaUnusedSymbol
    val generatedValueParams = params.filter(p => p.mods.exists {
      case mod"@GeneratedValue($_)" => true
      case _ => false
    })
    if(generatedValueParams.length > 1) {
      MacrosHelper.abort(Message.DOMALA4037, clsName.syntax, idParams.head.name.syntax)
    }
    if(generatedValueParams.length == 1) {
      if(idParams.size > 1) {
        MacrosHelper.abort(Message.DOMALA4036, clsName.syntax)
      }
      if(idParams.isEmpty || idParams.head.name.syntax != generatedValueParams.head.name.syntax) {
        MacrosHelper.abort(Message.DOMALA4033, clsName.syntax, generatedValueParams.head.name.syntax)
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

    //noinspection ScalaUnusedSymbol
    params.foreach(p => p.mods.collect {
      case mod"@SequenceGenerator(..$_)" =>
        if(strategy.isEmpty || p.name.syntax != generatedValueParams.head.name.syntax || {
          strategy match {
          case Some(GenerationType.SEQUENCE) => false
          case _ => true
          }})
          MacrosHelper.abort(Message.DOMALA4030, clsName.syntax, p.name.syntax)
      case mod"@TableGenerator(..$_)" =>
        if(generatedValueParams.isEmpty || p.name.syntax != generatedValueParams.head.name.syntax || {
          strategy match {
            case  Some(GenerationType.TABLE) => false
            case _ => true
          }})
          MacrosHelper.abort(Message.DOMALA4031, clsName.syntax, p.name.syntax)
    })

    strategy.map {
      case GenerationType.IDENTITY => Seq(q"private[this] val __idGenerator = new org.seasar.doma.jdbc.id.BuiltinIdentityIdGenerator()")
      case GenerationType.SEQUENCE =>
        val sequenceGeneratorSetting = SequenceGeneratorArgs.of(idParams.head.mods, clsName.syntax)
          .getOrElse(MacrosHelper.abort(Message.DOMALA4034, clsName.syntax, idParams.head.name.syntax))
        q"""
          domala.internal.macros.reflect.EntityReflectionMacros.validateSequenceIdGenerator(classOf[$clsName], classOf[${sequenceGeneratorSetting.implementer}])
          private[this] val __idGenerator = new ${sequenceGeneratorSetting.implementer.syntax.parse[Ctor.Call].get}()
          __idGenerator.setQualifiedSequenceName(${sequenceGeneratorSetting.sequence})
          __idGenerator.setInitialValue(${sequenceGeneratorSetting.initialValue})
          __idGenerator.setAllocationSize(${sequenceGeneratorSetting.allocationSize})
          __idGenerator.initialize()
          """.stats
      case GenerationType.TABLE =>
        val tableGeneratorSetting = TableGeneratorArgs.of(idParams.head.mods, clsName.syntax)
          .getOrElse(MacrosHelper.abort(Message.DOMALA4035, clsName.syntax, idParams.head.name.syntax))
        q"""
          domala.internal.macros.reflect.EntityReflectionMacros.validateTableIdGenerator(classOf[$clsName], classOf[${tableGeneratorSetting.implementer}])
          private[this] val __idGenerator = new ${tableGeneratorSetting.implementer.syntax.parse[Ctor.Call].get}()
          __idGenerator.setQualifiedTableName(${tableGeneratorSetting.table})
          __idGenerator.setInitialValue(${tableGeneratorSetting.initialValue})
          __idGenerator.setAllocationSize(${tableGeneratorSetting.allocationSize})
          __idGenerator.setPkColumnName(${tableGeneratorSetting.pkColumnName})
          __idGenerator.setPkColumnValue(${tableGeneratorSetting.pkColumnValue})
          __idGenerator.setValueColumnName(${tableGeneratorSetting.valueColumnName})
          __idGenerator.initialize()
          """.stats
      }.getOrElse(
        Seq(q"private[this] val __idGenerator = null")
      )
  }

  protected def generatePropertyTypeFields(clsName: Type.Name, ctor: Ctor.Primary): Seq[Defn.Val] = {
    ctor.paramss.flatten.map { p =>
      val Term.Param(mods, name, Some(decltpe), _) = p
      val columnSetting = ColumnArgs.of(mods)
      val tpe = Type.Name(decltpe.toString)
      if(name.syntax.startsWith(MetaConstants.RESERVED_NAME_PREFIX)) {
        MacrosHelper.abort(Message.DOMALA4025, MetaConstants.RESERVED_NAME_PREFIX, clsName.syntax, name.syntax)
      }
      val propertyName = Pat.Var.Term(Term.Name("$" + name.syntax))

      val (isBasic, nakedTpe, newWrapperExpr) = TypeUtil.convertToEntityDomaType(decltpe) match {
        case DomaType.Basic(_, convertedType, wrapperSupplier, _) => (true, convertedType, wrapperSupplier)
        case DomaType.Option(DomaType.Basic(_, convertedType, wrapperSupplier, _), _) => (true, convertedType, wrapperSupplier)
        case DomaType.EntityOrHolderOrEmbeddable(otherType) => (false, otherType, q"null")
        case DomaType.Option(DomaType.EntityOrHolderOrEmbeddable(otherType), _) => (false, otherType,  q"null")
        case _ => MacrosHelper.abort(Message.DOMALA4096, decltpe.syntax, clsName.syntax, name.syntax)
      }

      if(TypeUtil.isWildcardType(nakedTpe)) MacrosHelper.abort(Message.DOMALA4205, nakedTpe.children.head, clsName.syntax, name.syntax)
      if(mods.exists {
        case Mod.VarParam() => true
        case _ => false
      }) MacrosHelper.abort(Message.DOMALA4225, clsName.syntax, name.syntax)

      val isId = mods.exists {
        case mod"@Id" | mod"@domala.Id" | mod"@Id()" | mod"@domala.Id()"=> true
        case _ => false
      }

      //noinspection ScalaUnusedSymbol
      val isIdGenerate = mods.exists {
        case mod"@GeneratedValue($_)" => true
        case mod"@domala.GeneratedValue($_)" => true
        case _ => false
      }

      val isVersion = mods.exists {
        case mod"@Version" | mod"@domala.Version" | mod"@Version()" | mod"@domala.Version()" => true
        case _ => false
      }

      val isTenantId = mods.exists {
        case mod"@TenantId" | mod"@domala.TenantId" | mod"@TenantId()" | mod"@domala.TenantId()"=> true
        case _ => false
      }

      if(columnSetting.insertable.syntax == "false" && (isId || isVersion || isTenantId))
        MacrosHelper.abort(Message.DOMALA4088, clsName.syntax, name.syntax)
      if(columnSetting.updatable.syntax == "false" && (isId || isVersion || isTenantId))
        MacrosHelper.abort(Message.DOMALA4089, clsName.syntax, name.syntax)

      q"""
      private[this] val $propertyName = domala.internal.macros.reflect.EntityReflectionMacros.generatePropertyDesc[$tpe, $clsName, $nakedTpe](
        classOf[$clsName],
        ${name.literal},
        __namingType,
        ${if(isId) q"true" else q"false"},
        ${if(isIdGenerate) q"true" else q"false"},
        __idGenerator,
        ${if(isVersion) q"true" else q"false"},
        ${if(isTenantId) q"true" else q"false"},
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

  protected def generateConstructor(clsName: Type.Name, ctor: Ctor.Primary, entitySetting: EntityArgs, tableSetting: TableArgs): Seq[Stat] = {
    q"""
    private[this] val __listenerSupplier: java.util.function.Supplier[${entitySetting.listener}] =
      () => ListenerHolder.listener
    private[this] val __immutable = true
    private[this] val __name = ${Term.Name("\"" + clsName.syntax + "\"")}
    private[this] val __catalogName = ${Term.Name(tableSetting.catalog.syntax)}
    private[this] val __schemaName = ${Term.Name(tableSetting.schema.syntax)}
    private[this] val __tableName = ${Term.Name(tableSetting.name.syntax)}
    private[this] val __isQuoteRequired = ${tableSetting.quote}
    private[this] val __idPropertyTypes = java.util.Collections.unmodifiableList(__idList)
    private[this] val __entityPropertyTypes = java.util.Collections.unmodifiableList(__list)
    private[this] val __entityPropertyTypeMap: java.util.Map[
      String,
      domala.jdbc.entity.EntityPropertyDesc[$clsName, _]] =
      java.util.Collections.unmodifiableMap(__map)
    """.stats
  }

  protected def generateGeneratedIdPropertyType(clsName: Type.Name, ctor: Ctor.Primary): Term = {
    ctor.paramss.flatten.collect {
      case param if param.mods.exists(mod => mod.syntax.startsWith("@GeneratedValue")) =>
        ("$" + param.name.syntax + s".asInstanceOf[domala.jdbc.entity.GeneratedIdPropertyDesc[$clsName, $clsName, _ <: Number, _]]").parse[Term].get
    }.headOption.getOrElse(q"null")
  }

  protected def generateVersionPropertyType(clsName: Type.Name, ctor: Ctor.Primary): Term = {
    val versionProperties = ctor.paramss.flatten.collect {
      case param if param.mods.exists(mod => mod.syntax.startsWith("@Version") || mod.syntax.startsWith("@domala.Version")) => param
    }
    if(versionProperties.length > 1) MacrosHelper.abort(Message.DOMALA4024, clsName.syntax, versionProperties(1).name.syntax)
    versionProperties.headOption.map(param =>
      ("$" + param.name.syntax  + s".asInstanceOf[domala.jdbc.entity.VersionPropertyDesc[$clsName, $clsName, _ <: Number, _]]").parse[Term].get
    ).getOrElse(q"null")
  }

  protected def generateGetTenantIdPropertyTypeMethod(clsName: Type.Name, ctor: Ctor.Primary): Term = {
    val tenantIdProperties = ctor.paramss.flatten.collect {
      case param if param.mods.exists(mod => mod.syntax.startsWith("@TenantId") || mod.syntax.startsWith("@domala.TenantId")) => param
    }
    if(tenantIdProperties.length > 1) MacrosHelper.abort(Message.DOMALA4442,  clsName.syntax, tenantIdProperties(1).name.syntax)
    tenantIdProperties.headOption.map(param =>
      ("$" + param.name.syntax  + s".asInstanceOf[domala.jdbc.entity.TenantIdPropertyDesc[$clsName, $clsName, _, _]]").parse[Term].get
    ).getOrElse(q"null")
  }

  protected def generateMethods(clsName: Type.Name, ctor: Ctor.Primary, entitySetting: EntityArgs): Seq[Stat] = {
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

    override def isQuoteRequired: Boolean = __isQuoteRequired

    override def preInsert(
        entity: $clsName,
        context: org.seasar.doma.jdbc.entity.PreInsertContext[$clsName]): Unit = {
      val __listenerClass =
        classOf[${entitySetting.listener}]
      val __listener =
        context.getConfig.getEntityListenerProvider
          .get[$clsName, ${entitySetting.listener}](__listenerClass, __listenerSupplier)
      __listener.preInsert(entity, context)
    }

    override def preUpdate(
        entity: $clsName,
        context: org.seasar.doma.jdbc.entity.PreUpdateContext[$clsName]): Unit = {
      val __listenerClass =
        classOf[${entitySetting.listener}]
      val __listener =
        context.getConfig.getEntityListenerProvider
          .get[$clsName, ${entitySetting.listener}](__listenerClass, __listenerSupplier)
      __listener.preUpdate(entity, context)
    }

    override def preDelete(
        entity: $clsName,
        context: org.seasar.doma.jdbc.entity.PreDeleteContext[$clsName]): Unit = {
      val __listenerClass =
        classOf[${entitySetting.listener}]
      val __listener =
        context.getConfig.getEntityListenerProvider
          .get[$clsName, ${entitySetting.listener}](__listenerClass, __listenerSupplier)
      __listener.preDelete(entity, context)
    }

    override def postInsert(
        entity: $clsName,
        context: org.seasar.doma.jdbc.entity.PostInsertContext[$clsName]): Unit = {
      val __listenerClass =
        classOf[${entitySetting.listener}]
      val __listener =
        context.getConfig.getEntityListenerProvider
          .get[$clsName,${entitySetting.listener}](__listenerClass, __listenerSupplier)
      __listener.postInsert(entity, context)
    }

    override def postUpdate(
        entity: $clsName,
        context: org.seasar.doma.jdbc.entity.PostUpdateContext[$clsName]): Unit = {
      val __listenerClass =
        classOf[${entitySetting.listener}]
      val __listener =
        context.getConfig.getEntityListenerProvider
          .get[$clsName, ${entitySetting.listener}](__listenerClass, __listenerSupplier)
      __listener.postUpdate(entity, context)
    }

    override def postDelete(
        entity: $clsName,
        context: org.seasar.doma.jdbc.entity.PostDeleteContext[$clsName]): Unit = {
      val __listenerClass =
        classOf[${entitySetting.listener}]
      val __listener =
        context.getConfig.getEntityListenerProvider
          .get[$clsName, ${entitySetting.listener}](__listenerClass, __listenerSupplier)
      __listener.postDelete(entity, context)
    }

    override def getEntityPropertyTypes: java.util.List[domala.jdbc.entity.EntityPropertyDesc[$clsName, _]] = __entityPropertyTypes

    override def getEntityPropertyType(__name: String): domala.jdbc.entity.EntityPropertyDesc[$clsName, _] = __entityPropertyTypeMap.get(__name)

    override def getIdPropertyTypes: java.util.List[domala.jdbc.entity.EntityPropertyDesc[$clsName, _]] = __idPropertyTypes

    override def getGeneratedIdPropertyType: domala.jdbc.entity.GeneratedIdPropertyDesc[_ >: $clsName, $clsName, _, _] = ${generateGeneratedIdPropertyType(clsName, ctor)}

    override def getVersionPropertyType: domala.jdbc.entity.VersionPropertyDesc[_ >: $clsName, $clsName, _, _] = ${generateVersionPropertyType(clsName, ctor)}

    override def getTenantIdPropertyType: domala.jdbc.entity.TenantIdPropertyDesc[_ >: $clsName, $clsName, _, _] = ${generateGetTenantIdPropertyTypeMethod(clsName, ctor)}

    """.stats ++ {
        val params = ctor.paramss.flatten.map { p =>
          q"domala.internal.macros.reflect.EntityReflectionMacros.readProperty[${toType(p.decltpe.get)}, $clsName](classOf[$clsName], __args, ${p.name.literal})"
        }
        Seq(
          q"""
          override def newEntity(
              __args: java.util.Map[
                String,
                domala.jdbc.entity.Property[$clsName, _]]) =
            new ${clsName.toString.parse[Ctor.Call].get}(
              ..$params
            )
          """)
    } ++
    q"""
    override def getEntityClass: Class[$clsName] = classOf[$clsName]

    override def getOriginalStates(__entity: $clsName): $clsName = null

    override def saveCurrentStates(__entity: $clsName): Unit = {}

    """.stats
  }
}
