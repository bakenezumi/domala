package domala.internal.macros.meta.args

import scala.meta._

case class TableGeneratorArgs(
  catalog: Term.Arg,
  schema: Term.Arg,
  table: Term.Arg,
  pkColumnName: Term.Arg,
  valueColumnName: Term.Arg,
  pkColumnValue: Term.Arg,
  initialValue: Term.Arg,
  allocationSize: Term.Arg,
  implementer: Type
)

object TableGeneratorArgs {
  def of(mods: Seq[Mod], className: String): Option[TableGeneratorArgs] = {
    val blank = q""" "" """
    mods.collectFirst {
      case mod"@TableGenerator(..$args)" =>
        val catalog = args.collectFirst { case arg"catalog = $x" => x }.getOrElse(blank)
        val schema = args.collectFirst { case arg"schema = $x" => x }.getOrElse(blank)
        val table = args.collectFirst { case arg"table = $x" => x }.getOrElse(q""""ID_GENERATOR"""")
        val pkColumnName = args.collectFirst { case arg"pkColumnName = $x" => x }.getOrElse(q""""PK"""")
        val valueColumnName = args.collectFirst { case arg"valueColumnName = $x" => x }.getOrElse(q""""VALUE"""")
        val pkColumnValue = args.collectFirst { case arg"pkColumnValue = $x" => x }.get
        val initialValue = args.collectFirst { case arg"initialValue = $x" => x }.getOrElse(q"1")
        val allocationSize = args.collectFirst { case arg"allocationSize = $x" => x }.getOrElse(q"1")
        val implementer = args.collectFirst { case arg"implementer = classOf[$x]" => x }.getOrElse(t"org.seasar.doma.jdbc.id.BuiltinTableIdGenerator")
        TableGeneratorArgs(catalog, schema, table, pkColumnName, valueColumnName, pkColumnValue, initialValue, allocationSize, implementer)
    }
  }
}
