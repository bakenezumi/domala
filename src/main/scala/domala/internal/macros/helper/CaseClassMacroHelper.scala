package domala.internal.macros.helper

import scala.meta._

object CaseClassMacroHelper {

  // for IntelliJ IDEA
  def generateApply(cls: Defn.Class): Stat = {
    val paramss = cls.ctor.paramss.map(ps => ps.map(_.copy(mods = Nil)))
    val argss = paramss.map(ps => ps.map(p => Term.Name(p.name.syntax)))
    val tparams = cls.tparams.map(tp => Type.Name(tp.name.syntax))
    if (tparams.nonEmpty)
      q"def apply[..{${cls.tparams}}](...$paramss): ${cls.name}[..{$tparams}] = new ${Ctor.Ref.Name(cls.name.syntax)}[..{$tparams}](...$argss)"
    else
      q"def apply(...$paramss): ${cls.name} = new ${Ctor.Ref.Name(cls.name.syntax)}(...$argss)"
  }

  // for IntelliJ IDEA
  def generateUnapply(cls: Defn.Class): Stat = {
    val params = cls.ctor.paramss.head.map(_.copy(mods = Nil))
    val tparams = cls.tparams.map(tp => Type.Name(tp.name.syntax))
    if (params.size > 1) {
      val typeTuple =
        Type.Tuple(params.map(p => Type.Name(p.decltpe.get.toString)))
      val unapplyBody = params.map(p => q"x.${Term.Name(p.name.syntax)}")
      if (tparams.nonEmpty)
        q"def unapply[..{${cls.tparams}}](x: ${cls.name}[..$tparams]): Option[$typeTuple] = if (x == null) None else Some((..$unapplyBody))"
      else
        q"def unapply(x: ${cls.name}): Option[$typeTuple] = if (x == null) None else Some((..$unapplyBody))"
    } else {
      if (tparams.nonEmpty)
        q"def unapply[..{${cls.tparams}}](x: ${cls.name}[..$tparams]): Option[${Type.Name(
          params.head.decltpe.get.toString)}] = if (x == null) None else Some(x.${Term.Name(params.head.name.syntax)})"
      else
        q"def unapply(x: ${cls.name}): Option[${Type.Name(
          params.head.decltpe.get.toString)}] = if (x == null) None else Some(x.${Term.Name(params.head.name.syntax)})"
    }
  }

}
