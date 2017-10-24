package domala.internal.macros

import domala.message.Message
import scala.collection.immutable.Seq
import scala.meta._

/**
  * @see [[https://github.com/domaframework/doma/blob/master/src/main/java/org/seasar/doma/internal/apt/DomainTypeGenerator.java]]
  */
object HolderTypeGenerator {
  def generate(cls: Defn.Class): Defn.Object = {

    if (cls.ctor.paramss.flatten.length != 1) abort(cls.pos, Message.DOMALA6001.getMessage())
    val valueParam = cls.ctor.paramss.flatten.headOption.getOrElse(abort(cls.pos, Message.DOMALA6001.getMessage()))
    if (valueParam.name.syntax != "value") abort(cls.pos, Message.DOMALA6002.getMessage())
    val (basicTpe, wrapperSupplier) = TypeHelper.convertToDomaType(valueParam.decltpe.get) match {
      case DomaType.Basic(_, convertedType, wrapperSupplier) => (convertedType, wrapperSupplier)
      case _ => abort(cls.pos, Message.DOMALA4102.getMessage(valueParam.decltpe.get.toString(), cls.name.syntax, valueParam.name.syntax))
    }

    val erasedHolderType =
      if(cls.tparams.nonEmpty) {
        val tparams = cls.tparams.map(_ => t"_")
        t"${cls.name}[..$tparams]"
      } else {
        t"${cls.name}"
      }
    val methods = makeMethods(cls.name, cls.ctor, basicTpe, erasedHolderType)

    q"""
    object ${Term.Name(cls.name.syntax) } extends
      domala.jdbc.holder.AbstractHolderDesc[
        $basicTpe, $erasedHolderType](
        $wrapperSupplier: java.util.function.Supplier[org.seasar.doma.wrapper.Wrapper[$basicTpe]]) {
      def getSingletonInternal() = this
      override def wrapper: java.util.function.Supplier[org.seasar.doma.wrapper.Wrapper[$basicTpe]] = $wrapperSupplier
      ..${Seq(CaseClassMacroHelper.generateApply(cls), CaseClassMacroHelper.generateUnapply(cls))}
      ..$methods
    }
    """
  }

  protected def makeMethods(clsName: Type.Name, ctor: Ctor.Primary, basicTpe: Type, erasedHolderType: Type): Seq[Stat] = {
    q"""
    override protected def newDomain(value: $basicTpe): $erasedHolderType = {
      if (value == null) null else ${Term.Name(clsName.toString)}(value)
    }

    override protected def getBasicValue(domain: $erasedHolderType): $basicTpe = {
      if (domain == null) null else domain.value
    }

    override def getBasicClass() = {
      classOf[$basicTpe]
    }

    override def getDomainClass() = {
      classOf[$erasedHolderType]
    }
    """.stats
  }
}
