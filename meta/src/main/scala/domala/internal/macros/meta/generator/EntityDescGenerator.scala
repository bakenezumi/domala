package domala.internal.macros.meta.generator

import domala.GenerationType
import domala.internal.macros.meta.args.{ColumnArgs, SequenceGeneratorArgs, TableArgs, TableGeneratorArgs}
import domala.internal.macros.meta.Types
import domala.internal.macros.meta.util.{MetaHelper, TypeUtil}
import domala.internal.macros.meta.util.NameConverters._
import domala.internal.macros.meta.util.TypeUtil.toType
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
      MetaHelper.abort(Message.DOMALA4051, cls.name.syntax)
    validateFieldAnnotation(cls.name, cls.ctor)
    val entityArgs = EntityArgs(
      args.collectFirst {
        case arg"listener = classOf[$x]" => x
        case arg"classOf[$x]" => x
      }.getOrElse(t"org.seasar.doma.jdbc.entity.NullEntityListener[${cls.name}]"),
      args.collectFirst { case arg"naming = $x" => Term.Name(x.syntax) }.getOrElse(q"null")
    )
    val tableArgs = TableArgs.of(cls.mods)
    val fields = generateFields(cls.name, cls.ctor, entityArgs, tableArgs)
    val methods = generateMethods(cls.name, cls.ctor, entityArgs)

    val generatedCompanion = q"""
    object ${Term.Name(cls.name.syntax)} extends domala.jdbc.entity.EntityCompanion[${cls.name}] {
      val entityDesc: domala.jdbc.entity.EntityDesc[${cls.name}] = EntityDesc
      object EntityDesc extends domala.jdbc.entity.AbstractEntityDesc[${cls.name}] {
        ..${fields ++ methods}
      }
      ..${Seq(CaseClassGenerator.generateApply(cls, maybeOriginalCompanion), CaseClassGenerator.generateUnapply(cls, maybeOriginalCompanion))}
    }
    """
    val newCompanion = MetaHelper.mergeObject(maybeOriginalCompanion, generatedCompanion)

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
        case x :: y :: _ => MetaHelper.abort(Message.DOMALA4086, x, y, clsName.syntax, p.name.syntax)
        case _ => ()
      }
    }
  }

  protected def generateFields(clsName: Type.Name, ctor: Ctor.Primary, entityArgs: EntityArgs, tableArgs: TableArgs): Seq[Stat] = {
    val idGeneratorField = generateIdGeneratorFields(clsName, ctor)

    val idSet = q"Set(..${idParamsFilter(ctor.paramss.flatten).map(_.name.literal)})"
    val propertyTypesFields =
      q"""
      override type ENTITY_LISTENER = ${entityArgs.listener}
      override protected val table: domala.Table = domala.Table(
        ${Term.Name(tableArgs.name.syntax)},
        ${Term.Name(tableArgs.catalog.syntax)},
        ${Term.Name(tableArgs.schema.syntax)},
        ${tableArgs.quote})
      override protected val propertyDescMap: Map[String, domala.jdbc.entity.EntityPropertyDesc[$clsName, _]] = Seq(..${generatePropertyDescMap(clsName, ctor)}).flatten.toMap
      override protected val listener =
              new ${entityArgs.listener.syntax.parse[Ctor.Call].get}()
      override protected val idPropertyDescList: List[domala.jdbc.entity.EntityPropertyDesc[$clsName, _]] = propertyDescMap.filterKeys($idSet).values.toList
      """.stats

    idGeneratorField ++ propertyTypesFields
  }

  def idParamsFilter(params: Seq[Term.Param]): Seq[Term.Param] = {
    params.filter(p => p.mods.exists {
      case mod"@Id" | mod"@domala.Id" | mod"@Id()" | mod"@domala.Id()" => true
      case _ => false
    })
  }

  protected def generateIdGeneratorFields(clsName: Type.Name, ctor: Ctor.Primary): Seq[Stat] = {
    val params = ctor.paramss.flatten
    val idParams = idParamsFilter(params)
    //noinspection ScalaUnusedSymbol
    val generatedValueParams = params.filter(p => p.mods.exists {
      case mod"@GeneratedValue($_)" => true
      case _ => false
    })
    if(generatedValueParams.length > 1) {
      MetaHelper.abort(Message.DOMALA4037, clsName.syntax, idParams.head.name.syntax)
    }
    if(generatedValueParams.length == 1) {
      if(idParams.size > 1) {
        MetaHelper.abort(Message.DOMALA4036, clsName.syntax)
      }
      if(idParams.isEmpty || idParams.head.name.syntax != generatedValueParams.head.name.syntax) {
        MetaHelper.abort(Message.DOMALA4033, clsName.syntax, generatedValueParams.head.name.syntax)
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
          MetaHelper.abort(Message.DOMALA4030, clsName.syntax, p.name.syntax)
      case mod"@TableGenerator(..$_)" =>
        if(generatedValueParams.isEmpty || p.name.syntax != generatedValueParams.head.name.syntax || {
          strategy match {
            case  Some(GenerationType.TABLE) => false
            case _ => true
          }})
          MetaHelper.abort(Message.DOMALA4031, clsName.syntax, p.name.syntax)
    })

    strategy.map {
      case GenerationType.IDENTITY => Seq(q"private[this] val __idGenerator = new org.seasar.doma.jdbc.id.BuiltinIdentityIdGenerator()")
      case GenerationType.SEQUENCE =>
        val sequenceGeneratorSetting = SequenceGeneratorArgs.of(idParams.head.mods, clsName.syntax)
          .getOrElse(MetaHelper.abort(Message.DOMALA4034, clsName.syntax, idParams.head.name.syntax))
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
          .getOrElse(MetaHelper.abort(Message.DOMALA4035, clsName.syntax, idParams.head.name.syntax))
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

  protected def generatePropertyDescMap(clsName: Type.Name, ctor: Ctor.Primary): Seq[Term.Apply] = {
    ctor.paramss.flatten.map { p =>
      val Term.Param(mods, name, Some(decltpe), _) = p
      val columnArgs = ColumnArgs.of(mods)
      val tpe = Type.Name(decltpe.toString)
      if(name.syntax.startsWith(MetaConstants.RESERVED_NAME_PREFIX)) {
        MetaHelper.abort(Message.DOMALA4025, MetaConstants.RESERVED_NAME_PREFIX, clsName.syntax, name.syntax)
      }
      val nakedTpe = Types.ofEntityProperty(decltpe) match {
        case Types.Basic(_, convertedType, _, _) => convertedType
        case Types.Option(Types.Basic(_, convertedType, _, _), _) => convertedType
        case Types.EntityOrHolderOrEmbeddable(otherType) => otherType
        case Types.Option(Types.EntityOrHolderOrEmbeddable(otherType), _) => otherType
        case _ => MetaHelper.abort(Message.DOMALA4096, decltpe.syntax, clsName.syntax, name.syntax)
      }

      if(TypeUtil.isWildcardType(nakedTpe)) MetaHelper.abort(Message.DOMALA4205, nakedTpe.children.head, clsName.syntax, name.syntax)
      if(mods.exists {
        case Mod.VarParam() => true
        case _ => false
      }) MetaHelper.abort(Message.DOMALA4225, clsName.syntax, name.syntax)

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

      if(columnArgs.insertable.syntax == "false" && (isId || isVersion || isTenantId))
        MetaHelper.abort(Message.DOMALA4088, clsName.syntax, name.syntax)
      if(columnArgs.updatable.syntax == "false" && (isId || isVersion || isTenantId))
        MetaHelper.abort(Message.DOMALA4089, clsName.syntax, name.syntax)

      q"""
      domala.internal.macros.reflect.EntityReflectionMacros.generatePropertyDesc[$tpe, $clsName, $nakedTpe](
        classOf[$clsName],
        ${name.literal},
        getNamingType,
        ${if(isId) q"true" else q"false"},
        ${if(isIdGenerate) q"true" else q"false"},
        __idGenerator,
        ${if(isVersion) q"true" else q"false"},
        ${if(isTenantId) q"true" else q"false"},
        domala.Column(
          ${columnArgs.name},
          ${columnArgs.insertable},
          ${columnArgs.updatable},
          ${columnArgs.quote}
        )
      )
      """
    }
  }

  protected def generateGeneratedIdPropertyDesc(clsName: Type.Name, ctor: Ctor.Primary): Term = {
    ctor.paramss.flatten.collectFirst {
      case param if param.mods.exists(mod => mod.syntax.startsWith("@GeneratedValue")) =>
        q"propertyDescMap(${param.name.literal}).asInstanceOf[domala.jdbc.entity.GeneratedIdPropertyDesc[$clsName, $clsName, _ <: Number, _]]"
    }.getOrElse(q"null")
  }

  protected def generateVersionPropertyDesc(clsName: Type.Name, ctor: Ctor.Primary): Term = {
    val versionProperties = ctor.paramss.flatten.collect {
      case param if param.mods.exists(mod => mod.syntax.startsWith("@Version") || mod.syntax.startsWith("@domala.Version")) => param
    }
    if(versionProperties.length > 1) MetaHelper.abort(Message.DOMALA4024, clsName.syntax, versionProperties(1).name.syntax)
    versionProperties.headOption.map(param =>
      q"propertyDescMap(${param.name.literal}).asInstanceOf[domala.jdbc.entity.VersionPropertyDesc[$clsName, $clsName, _ <: Number, _]]"
    ).getOrElse(q"null")
  }

  protected def generateGetTenantIdPropertyTypeMethod(clsName: Type.Name, ctor: Ctor.Primary): Term = {
    val tenantIdProperties = ctor.paramss.flatten.collect {
      case param if param.mods.exists(mod => mod.syntax.startsWith("@TenantId") || mod.syntax.startsWith("@domala.TenantId")) => param
    }
    if(tenantIdProperties.length > 1) MetaHelper.abort(Message.DOMALA4442,  clsName.syntax, tenantIdProperties(1).name.syntax)
    tenantIdProperties.headOption.map(param =>
      q"propertyDescMap(${param.name.literal}).asInstanceOf[domala.jdbc.entity.TenantIdPropertyDesc[$clsName, $clsName, _, _]]"

    ).getOrElse(q"null")
  }

  protected def generateMethods(clsName: Type.Name, ctor: Ctor.Primary, entityArgs: EntityArgs): Seq[Stat] = {
    q"""

    override def getNamingType: domala.jdbc.entity.NamingType = ${entityArgs.naming}

    override def getGeneratedIdPropertyType: domala.jdbc.entity.GeneratedIdPropertyDesc[_ >: $clsName, $clsName, _, _] = ${generateGeneratedIdPropertyDesc(clsName, ctor)}

    override def getVersionPropertyType: domala.jdbc.entity.VersionPropertyDesc[_ >: $clsName, $clsName, _, _] = ${generateVersionPropertyDesc(clsName, ctor)}

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
    Seq(q"""
    override def getEntityClass: Class[$clsName] = classOf[$clsName]
    """)
  }
}
