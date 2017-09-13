package domala

import scala.meta._

class Table(
  catalog: String = "",
  schema: String = "",
  name: String = "",
  quote: Boolean = false
) extends scala.annotation.StaticAnnotation

package internal { package macros {
  case class TableSetting(
     catalog: Term.Arg,
     schema: Term.Arg,
     name: Term.Arg,
     quote: Term
  )

  object TableSetting {
    def read(mods: Seq[Mod]): TableSetting = {
      val table = mods.collect {
        case mod"@Table(..$args)" => args
      }.headOption
      val blank = q""" "" """
      table match {
        case Some(args) =>
          val catalog = args.collectFirst { case arg"catalog = $x" => x }.getOrElse(blank)
          val schema = args.collectFirst { case arg"schema = $x" => x }.getOrElse(blank)
          val name = args.collectFirst { case arg"name = $x" => x }.getOrElse(blank)
          val quote = args.collectFirst { case arg"quote = $x" => x.syntax.parse[Term].get }.getOrElse("false".parse[Term].get)
          TableSetting(catalog, schema, name, quote)
        case None => TableSetting(blank, blank, blank, "false".parse[Term].get)
      }
    }
  }
}}