package domala.internal.macros.meta.util

import scala.meta.Type

object TypeUtil {

  def toType(arg: Type.Arg): Type = arg match {
    case Type.Arg.Repeated(tpe) => tpe
    case Type.Arg.ByName(tpe) => tpe
    case tpe: Type => tpe
  }

  def isWildcardType(arg: Type.Arg): Boolean = arg.syntax.contains("[_")

  def toDefTypeParams(tparams: collection.immutable.Seq[Type.Param]): collection.immutable.Seq[Type.Param] = {
    tparams.map(_.copy(mods = Nil))
  }
}
