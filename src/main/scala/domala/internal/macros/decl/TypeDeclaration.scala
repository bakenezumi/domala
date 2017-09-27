package domala.internal.macros.decl
import scala.meta._

class TypeDeclaration {
  def getType: Type.Arg = t"T"
  def getBinaryName: String = "T"
  def isBooleanType: Boolean = true
}
