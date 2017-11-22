package domala.internal.macros.helper

import scala.meta._
object LiteralConverters {

  implicit class RichName(name: Name) {
    def literal: Term = Term.Name("\"" + name.syntax + "\"")
    def className = q"classOf[${Type.Name(name.syntax)}].getName"
  }

}
