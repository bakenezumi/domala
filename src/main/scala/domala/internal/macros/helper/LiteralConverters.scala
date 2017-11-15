package domala.internal.macros.helper

import scala.meta.{Name, Term}
object LiteralConverters {

  implicit class RichName(name: Name) {
    def literal: Term = Term.Name("\"" + name.syntax + "\"")
  }

}