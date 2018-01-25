package domala.internal.macros.reflect.util

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

object ElementUtilTestDriver {
  def getTypeElement(className: String): Option[String] = macro getTypeElementImpl
  def getTypeElementImpl(c: blackbox.Context)(
    className: c.Expr[String]): c.Expr[Option[String]] = {
    import c.universe._
    val Literal(Constant(classNameLiteral: String)) = className.tree
    val r = ElementUtil.getTypeElement(c)(classNameLiteral).map(_.typeSymbol.fullName.toString)
    c.Expr(q"$r")
  }
}
