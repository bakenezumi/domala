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

    val basicTpeStr = cls.ctor.paramss.flatten.head.decltpe.get.toString
    // TODO: その他の型
    val (wrapper, basicTpe) = basicTpeStr match {
      case "String" => (q"new org.seasar.doma.wrapper.StringWrapper()", Type.Name("String"))
      case "Int" => (q"new org.seasar.doma.wrapper.IntegerWrapper()", Type.Name("Integer"))
    }

    val wrapperSupplier = q"""
    new java.util.function.Supplier[org.seasar.doma.wrapper.Wrapper[$basicTpe]]() {
      def get = $wrapper
    }
    """

    val methods = makeMethods(cls.name, cls.ctor, basicTpe)

    val obj = q"""
    object ${Term.Name(cls.name.value)} extends
      org.seasar.doma.jdbc.domain.AbstractDomainType[
        $basicTpe, ${cls.name}](
        $wrapperSupplier) {
      def getSingletonInternal() = this
      val wrapper = $wrapperSupplier
      ..$methods
    }
    """

    logger.debug(obj)
    Term.Block(Seq(
      cls,
      obj
    ))
  }

  protected def makeMethods(clsName: Type.Name, ctor: Ctor.Primary, basicTpe: Type.Name) = {
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
