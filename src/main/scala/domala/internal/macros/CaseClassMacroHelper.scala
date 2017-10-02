package domala.internal.macros

import scala.meta._

object CaseClassMacroHelper {

  // for IntelliJ IDEA
  def generateApply(cls: Defn.Class): Stat = {
    val paramss = cls.ctor.paramss.map(ps => ps.map(_.copy(mods = Nil)))
    val argss = paramss.map(ps => ps.map(p => Term.Name(p.name.syntax)))
    q"def apply(...$paramss): ${cls.name} = new ${Ctor.Ref.Name(cls.name.syntax)}(...$argss)"
  }

  // for IntelliJ IDEA
  def generateUnapply(cls: Defn.Class): Stat = {
    val params = cls.ctor.paramss.head.map(_.copy(mods = Nil))
    if (params.size > 1) {
      val typeTuple =
        Type.Tuple(params.map(p => Type.Name(p.decltpe.get.toString)))
      val unapplyBody = params.map(p => q"x.${Term.Name(p.name.syntax)}")
      q"def unapply(x: ${cls.name}): Option[$typeTuple] = Some((..$unapplyBody))"
    } else {
      q"def unapply(x: ${cls.name}): Option[${Type.Name(
        params.head.decltpe.get.toString)}] = Some(x.${Term.Name(params.head.name.syntax)})"
    }
  }

}
