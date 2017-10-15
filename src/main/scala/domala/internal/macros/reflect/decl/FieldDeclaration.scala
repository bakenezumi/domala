package domala.internal.macros.reflect.decl

import scala.reflect.macros.blackbox

class FieldDeclaration[C <: blackbox.Context](val c: C)(
  methodElement: C#Symbol,
  typeParameterDeclarations: Seq[TypeParameterDeclaration[C]]
) {
  import c.universe._

  val fieldSymbol: c.universe.TermSymbol = methodElement match {
    case x: TermSymbol => x
  }

  def getTypeDeclaration: TypeDeclaration[C] = {
    TypeDeclaration.newTypeDeclaration(c)(fieldSymbol.typeSignature)
  }
}
