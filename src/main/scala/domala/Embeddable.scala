package domala

import domala.internal.macros.{EmbeddableTypeGenerator, EntityTypeGenerator}
import org.scalameta.logger

import scala.collection.immutable.Seq
import scala.meta._

class Embeddable extends scala.annotation.StaticAnnotation {
  inline def apply(defn: Any): Any = meta {
    val (cls, newCompanion) = defn match {
      case Term.Block(Seq(cls: Defn.Class, companion: Defn.Object)) => {
        val newCompanion = EmbeddableTypeGenerator.generate(cls)
        (
          cls,
          newCompanion.copy(templ = newCompanion.templ.copy(
            stats = Some(newCompanion.templ.stats.getOrElse(Nil) ++ companion.templ.stats.getOrElse(Nil))
          ))
        )
      }
      case cls: Defn.Class => (cls, EmbeddableTypeGenerator.generate(cls))
      case _ => abort("@Embeddable most annotate a class")
    }
    logger.debug(newCompanion)
    Term.Block(Seq(
      cls,
      newCompanion
    ))
  }
}

package internal { package macros {

  import org.scalameta.logger
  import scala.collection.immutable.Seq

  /**
    * @see [[https://github.com/domaframework/doma/blob/master/src/main/java/org/seasar/doma/internal/apt/EmbeddableTypeGenerator.java]]
    */
  object EmbeddableTypeGenerator {
    def generate(cls: Defn.Class): Defn.Object = {
      val methods = makeMethods(cls.name, cls.ctor)
      q"""
      object ${Term.Name(cls.name.syntax)} extends org.seasar.doma.jdbc.entity.EmbeddableType[${cls.name}] {
        ..${Seq(CaseClassMacroHelper.generateApply(cls), CaseClassMacroHelper.generateUnapply(cls))}
        ..$methods
      }
      """
    }

    protected def makeMethods(clsName: Type.Name, ctor: Ctor.Primary): Seq[Defn.Def] = {
      Seq({
        val params = ctor.paramss.flatten.map { p =>
          val Term.Param(mods, name, Some(decltpe), default) = p
          val tpe = Type.Name(decltpe.toString)
          val columnSetting = ColumnSetting.read(mods)
          val (isBasic, basicTpe, newWrapperExpr) = TypeHelper.convertToEntityDomaType(decltpe) match {
            case DomaType.Basic(_, convertedType, wrapperSupplier) => (true, convertedType, wrapperSupplier)
            case DomaType.Option(DomaType.Basic(_, convertedType, wrapperSupplier), _) => (true, convertedType, wrapperSupplier)
            case DomaType.EntityOrHolderOrEmbeddable(otherType) => (false, otherType, q"null")
            case DomaType.Option(DomaType.EntityOrHolderOrEmbeddable(otherType), _) => (false, otherType,  q"null")
            case _ => abort(domala.message.Message.DOMALA4096.getMessage(decltpe.syntax, clsName.syntax, name.syntax))
          }
          q"""
          domala.internal.macros.EntityReflectionMacros.generatePropertyType(
            classOf[$tpe],
            entityClass,
            embeddedPropertyName + "." + ${name.syntax},
            namingType,
            false,
            false,
            null,
            false,
            ${if(isBasic) q"true" else q"false"},
            classOf[$basicTpe],
            $newWrapperExpr,
            ${columnSetting.name},
            ${columnSetting.insertable},
            ${columnSetting.updatable},
            ${columnSetting.quote},
            domala.internal.macros.EntityCollections[ENTITY]()
          ).asInstanceOf[org.seasar.doma.jdbc.entity.EntityPropertyType[ENTITY, _]]
          """
        }
        q"""
        override def getEmbeddablePropertyTypes[ENTITY](embeddedPropertyName: String, entityClass: Class[ENTITY], namingType: org.seasar.doma.jdbc.entity.NamingType) = {
          java.util.Arrays.asList(..$params)
        }
        """
      }, {
        val params = ctor.paramss.flatten.map { p =>
          val Term.Param(mods, name, Some(decltpe), default) = p
          val tpe = Type.Name(decltpe.toString)
          q"""
          { (if(__args.get(embeddedPropertyName + "." + ${name.syntax}) != null )
              __args.get(embeddedPropertyName + "." + ${name.syntax} )
            else null).get().asInstanceOf[$tpe] }
          """
        }
        q"""
        override def newEmbeddable[ENTITY](embeddedPropertyName: String,  __args: java.util.Map[String, org.seasar.doma.jdbc.entity.Property[ENTITY, _]]) = {
          ${Term.Name(clsName.syntax)}(..$params)
        }
        """
      })
    }
  }
}}