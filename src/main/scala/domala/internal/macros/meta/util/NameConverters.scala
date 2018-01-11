package domala.internal.macros.meta.util

import scala.meta._

object NameConverters {

  implicit class RichName(name: Name) {
    def literal: Term = Term.Name("\"" + name.syntax + "\"")
    def className = q"classOf[${Type.Name(name.syntax)}].getName"
  }

}
