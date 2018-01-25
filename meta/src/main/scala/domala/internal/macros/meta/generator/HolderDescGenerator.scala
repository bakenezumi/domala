package domala.internal.macros.meta.generator

import domala.internal.macros.meta.Types
import domala.internal.macros.meta.util.{MetaHelper, TypeUtil}
import domala.message.Message

import scala.collection.immutable.Seq
import scala.meta._

/**
  * @see [[https://github.com/domaframework/doma/blob/master/src/main/java/org/seasar/doma/internal/apt/DomainTypeGenerator.java]]
  */
object HolderDescGenerator {
  def generate(cls: Defn.Class, maybeOriginalCompanion: Option[Defn.Object]): Defn.Object = {

    if (cls.ctor.paramss.flatten.length != 1)  MetaHelper.abort(Message.DOMALA6001)
    val valueParam = cls.ctor.paramss.flatten.headOption.getOrElse(MetaHelper.abort(Message.DOMALA6001))
    val (basicTpe: Type, wrapperSupplier: Term.Function, isNumeric) = Types.of(valueParam.decltpe.get) match {
      case Types.Basic(_, convertedType, function, numeric) => (convertedType, function, numeric)
      case _ => MetaHelper.abort(Message.DOMALA4102, valueParam.decltpe.get.toString(), cls.name.syntax, valueParam.name.syntax)
    }

    val isCase = cls.mods.exists(_.syntax == Mod.Case().syntax)
    // IntelliJ can not see abstract modifier now
    val isEnum = cls.mods.exists(_.syntax == Mod.Abstract().syntax) && cls.mods.exists(_.syntax == Mod.Sealed().syntax)
    if (isCase && isEnum) MetaHelper.abort(Message.DOMALA6005)

    if(
      isEnum &&
      !valueParam.mods.exists {
        case Mod.ValParam() => true
        case _ => false
      } ||
      valueParam.mods.exists {
        case Mod.Private(_) => true
        case _ => false
      }) MetaHelper.abort(Message.DOMALA6006)

    val erasedHolderType =
      if(cls.tparams.nonEmpty) {
        val tparams = cls.tparams.map(_ => t"_")
        t"${cls.name}[..$tparams]"
      } else {
        t"${cls.name}"
      }

    val methods = makeMethods(cls.name, cls.ctor, basicTpe, erasedHolderType, valueParam)
    val tparams = TypeUtil.toDefTypeParams(cls.tparams)

    val applyDef =
      if(isEnum && !CaseClassGenerator.hasApplyDef(cls, maybeOriginalCompanion)) {
        val paramss = cls.ctor.paramss.map(ps => ps.map(_.copy(mods = Nil)))
        val argss = paramss.map(ps => ps.map(p => Term.Name(p.name.syntax)))
        val typeNames = cls.tparams.map(tp => Type.Name(tp.name.syntax))
        val assertSubclasses = q"domala.internal.macros.reflect.HolderReflectionMacros.assertSubclasses(classOf[${cls.name}])"
        val matchSubclasses = q"domala.internal.macros.reflect.HolderReflectionMacros.matchSubclasses(classOf[$basicTpe], classOf[${cls.name}])(...$argss)"
        Seq(assertSubclasses,
          if (typeNames.nonEmpty)
            q"private def apply[..{$tparams}](...$paramss): ${cls.name}[..{$typeNames}] = $matchSubclasses"
          else
            q"private def apply(...$paramss): ${cls.name} = $matchSubclasses"
        )
      } else Seq(CaseClassGenerator.generateApply(cls, maybeOriginalCompanion))

    val numericImplicitVal =
      if(isNumeric && !isNumericDefined(maybeOriginalCompanion)) Seq(generateNumericImplicitVal(cls.name, basicTpe, tparams, Term.Name(valueParam.name.syntax)))
      else Nil

    val generatedCompanion = q"""
    object ${Term.Name(cls.name.syntax) } extends domala.jdbc.holder.HolderCompanion[$basicTpe, $erasedHolderType] {
      val holderDesc: domala.jdbc.holder.HolderDesc[$basicTpe, $erasedHolderType] = HolderDesc
      object HolderDesc extends domala.jdbc.holder.AbstractHolderDesc[
          $basicTpe, $erasedHolderType](
          $wrapperSupplier: java.util.function.Supplier[org.seasar.doma.wrapper.Wrapper[$basicTpe]]) {
        ..$methods
      }
      ..$numericImplicitVal
      ..${applyDef ++ Seq(CaseClassGenerator.generateUnapply(cls, maybeOriginalCompanion))}
    }
    """
    MetaHelper.mergeObject(maybeOriginalCompanion, generatedCompanion)
  }

  protected def makeMethods(clsName: Type.Name, ctor: Ctor.Primary, basicTpe: Type, erasedHolderType: Type, valueParam: Term.Param): Seq[Stat] = {
    q"""
    override protected def newDomain(value: $basicTpe): $erasedHolderType = {
      if (value == null) null else ${Ctor.Name(clsName.toString)}(value)
    }

    override protected def getBasicValue(holder: $erasedHolderType): $basicTpe = {
      if (holder == null) null else holder.${Term.Name(valueParam.name.syntax)}
    }

    override def getBasicClass: Class[$basicTpe] = {
      classOf[$basicTpe]
    }

    override def getDomainClass: Class[$erasedHolderType] = {
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
          if(name.startsWith("__")) MetaHelper.abort(Message.DOMALA4025, "__", companion.name, name))
        stats.exists(stat => stat.syntax.contains("Numeric"))
      })
    })
  }

  def generateNumericImplicitVal(clsName: Type.Name, basicTpe: Type, tparams: Seq[Type.Param], valueParamName: Term.Name): Stat = {
    def numericMethods(typedClsName: Type) = {
      q"""
      override def plus(x: $typedClsName, y: $typedClsName): $typedClsName = new ${Ctor.Name(typedClsName.syntax)}(x.$valueParamName + y.$valueParamName)

      override def minus(x: $typedClsName, y: $typedClsName): $typedClsName = new ${Ctor.Name(typedClsName.syntax)}(x.$valueParamName - y.$valueParamName)

      override def times(x: $typedClsName, y: $typedClsName): $typedClsName = new ${Ctor.Name(typedClsName.syntax)}(x.$valueParamName * y.$valueParamName)

      override def negate(x: $typedClsName): $typedClsName = new ${Ctor.Name(clsName.syntax)}(-x.$valueParamName)

      override def fromInt(x: Int): $typedClsName = new ${Ctor.Name(clsName.syntax)}(x)

      override def toInt(x: $typedClsName): Int = x.$valueParamName.toInt

      override def toLong(x: $typedClsName): Long = x.$valueParamName.toLong

      override def toFloat(x: $typedClsName): Float = x.$valueParamName.toFloat

      override def toDouble(x: $typedClsName): Double = x.$valueParamName.toDouble

      override def compare(x: $typedClsName, y: $typedClsName): Int = x.$valueParamName compare y.$valueParamName
      """
    }

    if (tparams.isEmpty) {
      q"""
      implicit val __numeric: Numeric[$clsName] = new Numeric[$clsName] {
        ..${numericMethods(clsName).stats}
      }
      """
    } else {
      val typeParamNames = tparams.map(t => Type.Name(t.name.syntax))
      val typedClsName: Type = t"$clsName[..${typeParamNames.map(t => Type.Name(t.syntax))}]"
      q"""
      implicit def __numeric[..$tparams]: Numeric[$typedClsName] = new Numeric[$typedClsName] {
        ..${numericMethods(typedClsName).stats}
      }
      """
    }
  }

}
