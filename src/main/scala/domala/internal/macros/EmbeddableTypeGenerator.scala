package domala.internal.macros

import scala.collection.immutable.Seq
import scala.meta._
import scala.meta.contrib._
import org.scalameta.logger

/**
 * @see [[https://github.com/domaframework/doma/blob/master/src/main/java/org/seasar/doma/internal/apt/EmbeddableTypeGenerator.java]]
 */
object EmbeddableTypeGenerator {
  def generate(cls: Defn.Class): Term.Block = {
    val internalClass = makeInternalClass(cls.name, cls.ctor)

    val obj = q"""
    object ${Term.Name(cls.name.value)} {
      private val __singleton = new Internal()

      def getSingletonInternal() = __singleton

      $internalClass
    }
    """

    logger.debug(obj)
    Term.Block(Seq(
      cls,
      obj
    ))
  }

  protected def makeInternalClass(clsName: Type.Name, ctor: Ctor.Primary): Defn.Class = {
    val methods = makeMethods(clsName, ctor)
    
    q"""
    class Internal private[$clsName] ()
    extends org.seasar.doma.jdbc.entity.EmbeddableType[$clsName] {
      ..$methods
    }
    """
  }

  protected def makeMethods(clsName: Type.Name, ctor: Ctor.Primary): Seq[Defn.Def] = {
    Seq(
      {
        val params = ctor.paramss.flatten.map { p =>
          val Term.Param(mods, name, Some(decltpe), default) = p
          val nameStr = name.value
          val tpe = Type.Name(decltpe.toString)
          val (basicTpe, newWrapperExpr, domainTpe) = MacroUtil.convertPropertyType(decltpe)

          q"""
          new domala.jdbc.entity.DefaultPropertyType(
              entityClass,
              classOf[$tpe],
              $basicTpe,
              $newWrapperExpr,
              null,
              null,
              embeddedPropertyName + "." + $nameStr,
              "",
              namingType,
              true,
              true,
              false
            )
          """
        }
        q"""
        override def getEmbeddablePropertyTypes[ENTITY](embeddedPropertyName: String, entityClass: Class[ENTITY], namingType: org.seasar.doma.jdbc.entity.NamingType) = {
          java.util.Arrays.asList(..$params)
        }
        """
      },
      {
        val params = ctor.paramss.flatten.map { p =>
          val Term.Param(mods, name, Some(decltpe), default) = p
          val nameStr = name.value
          val tpe = Type.Name(decltpe.toString)
          val (basicTpe, newWrapperExpr, domainTpe) = MacroUtil.convertPropertyType(tpe)
          q"""
          { (if(__args.get(embeddedPropertyName + "." + $nameStr) != null )
              __args.get(embeddedPropertyName + "." + $nameStr)
            else null).get().asInstanceOf[$tpe] }
          """
        }

        q"""
        override def newEmbeddable[ENTITY](embeddedPropertyName: String,  __args: java.util.Map[String, org.seasar.doma.jdbc.entity.Property[ENTITY, _]]) = {
          ${Term.Name(clsName.value)}(..$params)
        }
        """
      }
    )
  }
}
