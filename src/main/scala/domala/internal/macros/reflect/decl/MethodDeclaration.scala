package domala.internal.macros.reflect.decl

import scala.reflect.macros.blackbox

class MethodDeclaration[C <: blackbox.Context](val c: C)(
  methodElement: C#Symbol,
  typeParameterDeclarations: Seq[TypeParameterDeclaration[C]]
) {
  import c.universe._
  methodElement.asMethod
  val methodSymbos: MethodSymbol = methodElement match {
    case x: MethodSymbol => x
  }

  def getElement: MethodSymbol = methodSymbos
  def getReturnTypeDeclaration: TypeDeclaration[C] = {
    val returnType: C#Type = resolveTypeParameter(methodSymbos.returnType)
    TypeDeclaration.newTypeDeclaration(c)(returnType)
  }
  def isStatic: Boolean = methodSymbos.isStatic

  protected def resolveTypeParameter(formalType: C#Type): C#Type = {
    for (typeParameterDecl <- typeParameterDeclarations) {
      // TODO: unchecked since it is eliminated by erasure
      if (formalType.toString == typeParameterDecl.formalTypeName) return typeParameterDecl.actualType
    }
    formalType
  }

}