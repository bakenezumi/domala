package domala.internal.macros.meta.args

import scala.meta._

case class TableArgs(
  name: Term.Arg,
  catalog: Term.Arg,
  schema: Term.Arg,
  quote: Term
)

object TableArgs {
  def of(mods: Seq[Mod]): TableArgs = {
    val table = mods.collectFirst {
      case mod"@Table(..$args)" => args
    }
    val blank = q""" "" """
    table match {
      case Some(args) =>
        val name =
          if(args.nonEmpty && args.head.syntax.startsWith("\"")) args.head
          else args.collectFirst { case arg"name = $x" => x }.getOrElse(blank)
        val catalog = args.collectFirst { case arg"catalog = $x" => x }.getOrElse(blank)
        val schema = args.collectFirst { case arg"schema = $x" => x }.getOrElse(blank)
        val quote = args.collectFirst { case arg"quote = $x" => x.syntax.parse[Term].get }.getOrElse("false".parse[Term].get)
        TableArgs(name, catalog, schema, quote)
      case None => TableArgs(blank, blank, blank, "false".parse[Term].get)
    }
  }
}
