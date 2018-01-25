package domala.internal.macros.reflect.util

import scala.reflect.macros.blackbox
import org.seasar.doma.internal.util.AssertionUtil.assertNotNull

object ElementUtil {
  def getTypeElement[C <: blackbox.Context](c: C)(className: String): Option[c.Type] = {
    assertNotNull(className, c)
    val parts = className.split("\\$")
    if (parts.length > 1) {
      getTypeElement(c)(parts(0)).flatMap(topElement =>
        getEnclosedTypeElement(c)(topElement, parts.toSeq.tail)
      )
    } else {
      try {
        Some(c.mirror.staticClass(className).selfType)
      } catch {
        case _: ScalaReflectionException => None
      }
    }
  }

  def getEnclosedTypeElement[C <: blackbox.Context](c: C)(typeElement: c.Type, enclosedNames: Seq[String]): Option[c.Type] = {
    import c.universe._
    enclosedNames.foldLeft(Option(typeElement)){(enclosingOption, enclosedName) =>
      enclosingOption.flatMap(enclosing => enclosing.decls.collectFirst{
        case cs: ClassSymbol if cs.name.toString == enclosedName => cs.toType
      })
    }
  }
}
