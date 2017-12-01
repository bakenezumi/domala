package domala.internal.macros

import domala.internal.macros.helper.LiteralConverters._
import domala.internal.macros.helper.{CaseClassMacroHelper, MacrosHelper, TypeHelper}
import domala.message.Message

import scala.collection.immutable.Seq
import scala.meta._

/**
  * @see [[https://github.com/domaframework/doma/blob/master/src/main/java/org/seasar/doma/internal/apt/EmbeddableTypeGenerator.java]]
  */
object EmbeddableTypeGenerator {
  def generate(cls: Defn.Class, maybeOriginalCompanion: Option[Defn.Object]): Defn.Object = {
    if(cls.tparams.nonEmpty)
      MacrosHelper.abort(Message.DOMALA4285, cls.name.syntax)
    val methods = makeMethods(cls.name, cls.ctor)
    val newCompanion = q"""
    object ${Term.Name(cls.name.syntax)} extends org.seasar.doma.jdbc.entity.EmbeddableType[${cls.name}] {
      ..${Seq(CaseClassMacroHelper.generateApply(cls, maybeOriginalCompanion), CaseClassMacroHelper.generateUnapply(cls, maybeOriginalCompanion))}
      ..$methods
    }
    """
    MacrosHelper.mergeObject(maybeOriginalCompanion, newCompanion)
  }

  protected def makeMethods(clsName: Type.Name, ctor: Ctor.Primary): Seq[Defn.Def] = {
    val properties: Seq[EmbeddableProperties] = ctor.paramss.head.map { p =>
      val Term.Param(mods, name, Some(decltpe), _) = p
      val tpe = Type.Name(decltpe.toString)
      val columnSetting = ColumnSetting.read(mods)
      val (isBasic, isOption, nakedTpe, newWrapperExpr) = TypeHelper.convertToEntityDomaType(decltpe) match {
        case DomaType.Basic(_, convertedType, wrapperSupplier, _) => (true, false, convertedType, wrapperSupplier)
        case DomaType.Option(DomaType.Basic(_, convertedType, wrapperSupplier, _), _) => (true, true, convertedType, wrapperSupplier)
        case DomaType.EntityOrHolderOrEmbeddable(otherType) => (false, false, otherType, q"null")
        case DomaType.Option(DomaType.EntityOrHolderOrEmbeddable(otherType), _) => (false, true, otherType,  q"null")
        case _ => MacrosHelper.abort(Message.DOMALA4096, decltpe.syntax, clsName.syntax, name.syntax)
      }
      mods.collect {
        case mod"@Id" | mod"@domala.dId" | mod"@Id()" | mod"@domala.Id()" =>
          MacrosHelper.abort(Message.DOMALA4289, decltpe.syntax, name.syntax)
      }

      //noinspection ScalaUnusedSymbol
      if(mods.exists {
        case mod"@GeneratedValue($_)" => true
        case mod"@domala.GeneratedValue($_)" => true
        case _ => false
      }) MacrosHelper.abort(Message.DOMALA4291, decltpe.syntax, name.syntax)

      mods.collect {
        case mod"@Version" | mod"@domala.Version" | mod"@Version()" | mod"@domala.Version()" =>
          MacrosHelper.abort(Message.DOMALA4290, decltpe.syntax, name.syntax)
      }

      mods.collect {
        case mod"@TenantId" | mod"@domala.TenantId" | mod"@TenantId()" | mod"@domala.TenantId()"=>
          MacrosHelper.abort(Message.DOMALA4443, decltpe.syntax, name.syntax)
      }
      EmbeddableProperties(name, columnSetting, isBasic, isOption, tpe, nakedTpe, newWrapperExpr)
    }

    Seq({
      val params = properties.map { p =>
        q"""
      domala.internal.macros.reflect.EmbeddableReflectionMacros.generatePropertyType[${p.tpe}, ENTITY, ${p.nakedTpe}](
        entityClass,
        embeddedPropertyName + "." + ${p.name.literal},
        namingType,
        ${if(p.isBasic) q"true" else q"false"},
        ${p.newWrapperExpr},
        ${p.columnSetting.name},
        ${p.columnSetting.insertable},
        ${p.columnSetting.updatable},
        ${p.columnSetting.quote},
        domala.internal.macros.reflect.EntityCollections[ENTITY]()
      ).asInstanceOf[org.seasar.doma.jdbc.entity.EntityPropertyType[ENTITY, _]]
      """
      }
      q"""
    override def getEmbeddablePropertyTypes[ENTITY](embeddedPropertyName: String, entityClass: Class[ENTITY], namingType: org.seasar.doma.jdbc.entity.NamingType): java.util.List[org.seasar.doma.jdbc.entity.EntityPropertyType[ENTITY, _]] = {
      java.util.Arrays.asList(..$params)
    }
    """
    }, {
      val params = properties.map { p =>
          q"""
          { Option(__args.get(embeddedPropertyName + "." + ${p.name.literal})).map(_.get()).orNull.asInstanceOf[${p.tpe}] }
          """
      }
      q"""
      override def newEmbeddable[ENTITY](embeddedPropertyName: String,  __args: java.util.Map[String, org.seasar.doma.jdbc.entity.Property[ENTITY, _]]): $clsName = {
        ${Term.Name(clsName.syntax)}(..$params)
      }
      """
    })
  }

  private[macros] case class EmbeddableProperties(
    name: Term.Param.Name,
    columnSetting: ColumnSetting,
    isBasic: Boolean,
    isOption: Boolean,
    tpe: Type,
    nakedTpe: Type,
    newWrapperExpr: Term)

}

