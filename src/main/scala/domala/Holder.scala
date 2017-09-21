package domala

import domala.internal.macros.HolderTypeGenerator
import scala.meta._

class Holder extends scala.annotation.StaticAnnotation {
  inline def apply(defn: Any): Any = meta {
    defn match {
      case cls: Defn.Class => HolderTypeGenerator.generate(cls)
      case _ => abort("@Domain most annotate a class")
    }
  }
}

package internal { package macros {

  import org.scalameta.logger
  import scala.collection.immutable.Seq

  /**
    * @see [[https://github.com/domaframework/doma/blob/master/src/main/java/org/seasar/doma/internal/apt/DomainTypeGenerator.java]]
    */
  object HolderTypeGenerator {
    def generate(cls: Defn.Class): Term.Block = {

      if (cls.ctor.paramss.flatten.length != 1) abort(cls.pos, domala.message.Message.DOMALA6001.getMessage())
      val valueParam = cls.ctor.paramss.flatten.headOption.getOrElse(abort(cls.pos, domala.message.Message.DOMALA6001.getMessage()))
      if (valueParam.name.syntax != "value") abort(cls.pos, domala.message.Message.DOMALA6002.getMessage())
      val (basicTpe, wrapperSupplier) = TypeHelper.convertToDomaType(valueParam.decltpe.get) match {
        case DomaType.Basic(_, convertedType, wrapperSupplier) => (convertedType, wrapperSupplier)
        case _ => abort(cls.pos, domala.message.Message.DOMALA4096.getMessage(valueParam.decltpe.get.toString(), cls.name.syntax, valueParam.name.syntax))
      }

      val methods = makeMethods(cls.name, cls.ctor, basicTpe)

      val obj =
        q"""
        object ${Term.Name(cls.name.syntax)} extends
          domala.jdbc.holder.AbstractHolderDesc[
            $basicTpe, ${cls.name}](
            $wrapperSupplier: java.util.function.Supplier[org.seasar.doma.wrapper.Wrapper[$basicTpe]]) {
          def getSingletonInternal() = this
         override def wrapper: java.util.function.Supplier[org.seasar.doma.wrapper.Wrapper[$basicTpe]] = $wrapperSupplier
          ..$methods
        }
        """

      logger.debug(obj)
      Term.Block(Seq(
        cls,
        obj
      ))
    }

    protected def makeMethods(clsName: Type.Name, ctor: Ctor.Primary, basicTpe: Type): Seq[Stat] = {
      q"""
      override protected def newDomain(value: $basicTpe): $clsName = {
        if (value == null) null else ${Term.Name(clsName.toString)}(value)
      }

      override protected def getBasicValue(domain: $clsName): $basicTpe = {
        if (domain == null) null else domain.value
      }

      override def getBasicClass() = {
        classOf[$basicTpe]
      }

      override def getDomainClass() = {
        classOf[$clsName]
      }
      """.stats
    }
  }
}}