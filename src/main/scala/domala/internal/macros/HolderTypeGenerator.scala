package domala.internal.macros

import domala.internal.macros.helper.{CaseClassMacroHelper, MacrosHelper, TypeHelper}
import domala.message.Message

import scala.collection.immutable.Seq
import scala.meta._

/**
  * @see [[https://github.com/domaframework/doma/blob/master/src/main/java/org/seasar/doma/internal/apt/DomainTypeGenerator.java]]
  */
object HolderTypeGenerator {
  def generate(cls: Defn.Class, companion: Option[Defn.Object]): Defn.Object = {

    if (cls.ctor.paramss.flatten.length != 1)  MacrosHelper.abort(Message.DOMALA6001)
    val valueParam = cls.ctor.paramss.flatten.headOption.getOrElse(MacrosHelper.abort(Message.DOMALA6001))
    if (valueParam.name.syntax != "value") MacrosHelper.abort(Message.DOMALA6002)
    val (basicTpe: Type, wrapperSupplier: Term.Function, isNumeric) = TypeHelper.convertToDomaType(valueParam.decltpe.get) match {
      case DomaType.Basic(_, convertedType, function, isNumeric) => (convertedType, function, isNumeric)
      case _ => MacrosHelper.abort(Message.DOMALA4102, valueParam.decltpe.get.toString(), cls.name.syntax, valueParam.name.syntax)
    }

    val isCase = cls.mods.exists(_.syntax == Mod.Case().syntax)
    val isEnum = cls.mods.exists(_.syntax == Mod.Abstract().syntax) && cls.mods.exists(_.syntax == Mod.Sealed().syntax)
    if (isCase && isEnum) MacrosHelper.abort(Message.DOMALA6005)

    if(isEnum && !valueParam.mods.exists(_.syntax == Mod.ValParam().syntax)) MacrosHelper.abort(Message.DOMALA6006)

    val erasedHolderType =
      if(cls.tparams.nonEmpty) {
        val tparams = cls.tparams.map(_ => t"_")
        t"${cls.name}[..$tparams]"
      } else {
        t"${cls.name}"
      }

    val methods = makeMethods(cls.name, cls.ctor, basicTpe, erasedHolderType)

    val applyDef =
      if(isEnum) {
        val paramss = cls.ctor.paramss.map(ps => ps.map(_.copy(mods = Nil)))
        val argss = paramss.map(ps => ps.map(p => Term.Name(p.name.syntax)))
        val tparams = cls.tparams.map(tp => Type.Name(tp.name.syntax))
        val matchSubclasses = q"domala.internal.macros.reflect.HolderReflectionMacros.matchSubclasses(classOf[$basicTpe], classOf[${cls.name}])(...$argss)"
        if (tparams.nonEmpty)
          q"private def apply[..{${cls.tparams}}](...$paramss): ${cls.name}[..{$tparams}] = $matchSubclasses"
        else
          q"private def apply(...$paramss): ${cls.name} = $matchSubclasses"
      } else CaseClassMacroHelper.generateApply(cls)

    val numericImplicitVal =
      if(isNumeric && !isNumericDefined(companion)) Seq(generateNumericImplicitVal(cls.name, basicTpe, cls.tparams))
      else Nil

    q"""
    object ${Term.Name(cls.name.syntax) } extends
      domala.jdbc.holder.AbstractHolderDesc[
        $basicTpe, $erasedHolderType](
        $wrapperSupplier: java.util.function.Supplier[org.seasar.doma.wrapper.Wrapper[$basicTpe]]) {
      def getSingletonInternal() = this
      override def wrapper: java.util.function.Supplier[org.seasar.doma.wrapper.Wrapper[$basicTpe]] = $wrapperSupplier
      ..${Seq(applyDef, CaseClassMacroHelper.generateUnapply(cls))}
      ..$numericImplicitVal
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

  def isNumericDefined(maybeCompanion: Option[Defn.Object]): Boolean = {
    maybeCompanion.exists(companion => {
      companion.templ.stats.exists(stats => {
        val names = stats.collect {
          case x: Defn.Val => x.pats.map(_.syntax)
          case x: Defn.Var => x.pats.map(_.syntax)
          case x: Defn.Def => Seq(x.name.syntax)
        }.flatten
        names.foreach(name =>
          if(name.startsWith("__")) MacrosHelper.abort(Message.DOMALA4025, "__", companion.name, name))
        stats.exists(stat => stat.syntax.contains("Numeric"))
      })
    })
  }

  def generateNumericImplicitVal(clsName: Type.Name, basicTpe: Type, tparams: Seq[Type.Param]): Stat = {
    if (tparams.isEmpty) {
      q"""
      implicit val __num: Numeric[$clsName] = new Numeric[$clsName] {
        override def plus(x: $clsName, y: $clsName): $clsName = new ${Ctor.Name(clsName.syntax)}(x.value + y.value)

        override def minus(x: $clsName, y: $clsName): $clsName = new ${Ctor.Name(clsName.syntax)}(x.value - y.value)

        override def times(x: $clsName, y: $clsName): $clsName = new ${Ctor.Name(clsName.syntax)}(x.value * y.value)

        override def negate(x: $clsName): $clsName = new ${Ctor.Name(clsName.syntax)}(-x.value)

        override def fromInt(x: Int): $clsName = new ${Ctor.Name(clsName.syntax)}(x)

        override def toInt(x: $clsName): Int = x.value.toInt

        override def toLong(x: $clsName): Long = x.value.toLong

        override def toFloat(x: $clsName): Float = x.value.toFloat

        override def toDouble(x: $clsName): Double = x.value.toDouble

        override def compare(x: $clsName, y: $clsName): Int = x.value compare y.value
      }
      """
    } else {
      val typeParamNames = tparams.map(t => Type.Name(t.name.syntax))
      val typedClsName = t"$clsName[..${typeParamNames.map(t => Type.Name(t.syntax))}]"
      q"""
      implicit def __num[..$tparams]: Numeric[$typedClsName] = new Numeric[$typedClsName] {
        override def plus(x: $typedClsName, y: $typedClsName): $typedClsName = new ${Ctor.Name(typedClsName.syntax)}(x.value + y.value)

        override def minus(x: $typedClsName, y: $typedClsName): $typedClsName = new ${Ctor.Name(typedClsName.syntax)}(x.value - y.value)

        override def times(x: $typedClsName, y: $typedClsName): $typedClsName = new ${Ctor.Name(typedClsName.syntax)}(x.value * y.value)

        override def negate(x: $typedClsName): $typedClsName = new ${Ctor.Name(typedClsName.syntax)}(-x.value)

        override def fromInt(x: Int): $typedClsName = new ${Ctor.Name(typedClsName.syntax)}(x)

        override def toInt(x: $typedClsName): Int = x.value.toInt

        override def toLong(x: $typedClsName): Long = x.value.toLong

        override def toFloat(x: $typedClsName): Float = x.value.toFloat

        override def toDouble(x: $typedClsName): Double = x.value.toDouble

        override def compare(x: $typedClsName, y: $typedClsName): Int = x.value compare y.value
      }
      """

    }
  }

}
