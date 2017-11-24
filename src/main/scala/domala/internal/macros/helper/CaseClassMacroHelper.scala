package domala.internal.macros.helper

import scala.meta._

object CaseClassMacroHelper {

  // for IntelliJ IDEA
  def generateApply(cls: Defn.Class): Stat = {
    val paramss = cls.ctor.paramss.map(ps => ps.map(_.copy(mods = Nil)))
    val argss = paramss.map(ps => ps.map(p => Term.Name(p.name.syntax)))
    val tparams = TypeHelper.convertDefTypeParams(cls.tparams)
    val typeNames = cls.tparams.map(tp => Type.Name(tp.name.syntax))
    if (typeNames.nonEmpty)
      q"def apply[..{$tparams}](...$paramss): ${cls.name}[..{$typeNames}] = new ${Ctor.Ref.Name(cls.name.syntax)}[..{$typeNames}](...$argss)"
    else
      q"def apply(...$paramss): ${cls.name} = new ${Ctor.Ref.Name(cls.name.syntax)}(...$argss)"
  }

  // for IntelliJ IDEA
  def generateUnapply(cls: Defn.Class): Stat = {
    val params = cls.ctor.paramss.head.map(_.copy(mods = Nil))
    val tparams = TypeHelper.convertDefTypeParams(cls.tparams)
    val typeNames = cls.tparams.map(tp => Type.Name(tp.name.syntax))
    if (params.size > 1) {
      val typeTuple =
        Type.Tuple(params.map(p => Type.Name(p.decltpe.get.toString)))
      val unapplyBody = params.map(p => q"x.${Term.Name(p.name.syntax)}")
      if (typeNames.nonEmpty)
        q"def unapply[..{$tparams}](x: ${cls.name}[..$typeNames]): Option[$typeTuple] = if (x == null) None else Some((..$unapplyBody))"
      else
        q"def unapply(x: ${cls.name}): Option[$typeTuple] = if (x == null) None else Some((..$unapplyBody))"
    } else {
      if (typeNames.nonEmpty)
        q"def unapply[..{$tparams}](x: ${cls.name}[..$typeNames]): Option[${Type.Name(
          params.head.decltpe.get.toString)}] = if (x == null) None else Some(x.${Term.Name(params.head.name.syntax)})"
      else
        q"def unapply(x: ${cls.name}): Option[${Type.Name(
          params.head.decltpe.get.toString)}] = if (x == null) None else Some(x.${Term.Name(params.head.name.syntax)})"
    }
  }

}
