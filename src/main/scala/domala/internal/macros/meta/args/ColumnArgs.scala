package domala.internal.macros.meta.args

import scala.meta._

case class ColumnArgs(
  name: Term.Arg,
  insertable: Term.Arg,
  updatable: Term.Arg,
  quote: Term.Arg
)

object ColumnArgs {
  def of(mods: Seq[Mod]): ColumnArgs = {
    val column = mods.collectFirst {
      case mod"@Column(..$args)" => args
    }
    val blank = q""" "" """
    column match {
      case Some(args) =>
        val name = {
          if(args.nonEmpty && args.head.syntax.startsWith("\""))
            args.head
          else
            args.collectFirst { case arg"name = $x" => x }.getOrElse(blank)
        }
        val insertable = args
          .collectFirst { case arg"insertable = $x" => x }
          .getOrElse(q"true")
        val updatable =
          args.collectFirst { case arg"updatable = $x" => x }.getOrElse(q"true")
        val quote =
          args.collectFirst { case arg"quote = $x" => x }.getOrElse(q"false")
        ColumnArgs(name, insertable, updatable, quote)
      case None => ColumnArgs(blank, q"true", q"true", q"false")
    }
  }
}
