package domala.internal.macros

import scala.meta._

case class ColumnSetting(
  name: Term.Arg,
  insertable: Term.Arg,
  updatable: Term.Arg,
  quote: Term.Arg
)

object ColumnSetting {
  def read(mods: Seq[Mod]): ColumnSetting = {
    val column = mods.collect {
      case mod"@Column(..$args)" => args
    }.headOption
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
        ColumnSetting(name, insertable, updatable, quote)
      case None => ColumnSetting(blank, q"true", q"true", q"false")
    }
  }
}
