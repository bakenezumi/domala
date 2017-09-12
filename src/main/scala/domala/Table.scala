package domala

import scala.meta._

class Table(
  catalog: String = "",
  schema: String = "",
  name: String = "",
  quote: Boolean = false
) extends scala.annotation.StaticAnnotation

case class TableSetting(
  catalog: Option[Term.Arg],
  schema: Option[Term.Arg],
  name: Option[Term.Arg],
  quote: Option[Term.Arg]
)

object TableSetting {
  def read(mods: Seq[Mod]): TableSetting = {
    val table = mods.collect {
      case mod"@Table(..$args)" => args
    }.headOption
    table match {
      case Some(args)  =>
        val catalog = args.collectFirst{ case arg"catalog = $x" => Some(x) }.getOrElse(None)
        val schema = args.collectFirst{ case arg"schema = $x" => Some(x) }.getOrElse(None)
        val name = args.collectFirst{ case arg"name = $x" => Some(x) }.getOrElse(None)
        val quote = args.collectFirst{ case arg"quote = $x" => Some(x) }.getOrElse(None)
        TableSetting(catalog, schema, name, quote)
      case None => TableSetting(None, None, None, None)
    }
  }

}