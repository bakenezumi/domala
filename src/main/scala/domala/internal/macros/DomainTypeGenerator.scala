package domala.internal.macros

import scala.collection.immutable.Seq
import scala.meta._
import scala.meta.contrib._
import org.scalameta.logger

/**
 * @see [[https://github.com/domaframework/doma/blob/master/src/main/java/org/seasar/doma/internal/apt/DomainTypeGenerator.java]]
 */
object DomainTypeGenerator {
  def generate(cls: Defn.Class) = {

    if (cls.ctor.paramss.flatten.length != 1) abort(cls.pos, domala.message.Message.DOMALA6001.getMessage())
    val valueParam = cls.ctor.paramss.flatten.headOption.getOrElse(abort(cls.pos, domala.message.Message.DOMALA6001.getMessage()))
    if(valueParam.name.syntax != "value") abort(cls.pos, domala.message.Message.DOMALA6002.getMessage())
    val (basicTpe, wrapperSupplier) = TypeHelper.convertToDomaType(valueParam.decltpe.get) match {
      case DomaType.Basic(_, convertedType, wrapperSupplier) => (convertedType, wrapperSupplier)
      case _ => abort(cls.pos, domala.message.Message.DOMALA4096.getMessage(valueParam.decltpe.get.toString(), cls.name.syntax, valueParam.name.syntax))
    }

    val methods = makeMethods(cls.name, cls.ctor, basicTpe)

    val obj = q"""
    object ${Term.Name(cls.name.syntax)} extends
      org.seasar.doma.jdbc.domain.AbstractDomainType[
        $basicTpe, ${cls.name}](
        $wrapperSupplier: java.util.function.Supplier[org.seasar.doma.wrapper.Wrapper[$basicTpe]]) {
      def getSingletonInternal() = this
      val wrapper: java.util.function.Supplier[org.seasar.doma.wrapper.Wrapper[$basicTpe]] = $wrapperSupplier
      ..$methods
    }
    """

    logger.debug(obj)
    Term.Block(Seq(
      cls,
      obj
    ))
  }

  protected def makeMethods(clsName: Type.Name, ctor: Ctor.Primary, basicTpe: Type) = {
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
