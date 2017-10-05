package domala.internal.macros.reflect

import domala.internal.macros.DaoParamClass

import scala.reflect.macros.blackbox

class ReflectionHelper[C <: blackbox.Context](val c: C) {
  import c.universe._

  def paramTypes(params: Seq[c.Expr[DaoParamClass[_]]]): ParamMap = params.map(p => {
    val Literal(Constant(pName: String)) = p.tree.children(1)
    val tpe: c.Type = p.tree.children(2).tpe match {
      case ConstantType(Constant(x: c.Type)) => x
      case x => x
    }
    (pName, tpe)
  }).toMap
  type ParamMap = Map[String, C#Type]
  type ParamType = C#Type
}
