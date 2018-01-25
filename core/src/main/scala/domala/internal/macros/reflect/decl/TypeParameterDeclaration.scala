package domala.internal.macros.reflect.decl

import scala.reflect.macros.blackbox

class TypeParameterDeclaration[C <: blackbox.Context](val c: C)(
  val formalType: C#Symbol,
  val actualType: C#Type
) {
  def formalTypeName: String = formalType.name.toString

}
