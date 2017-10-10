package domala.internal.macros

import scala.meta._

object TableGeneratorSetting {
  def read(mods: Seq[Mod], className: String, propertyName: String): TableGeneratorSetting = {
    val tableGenerator = mods.collect {
      case mod"@TableGenerator(..$args)" => args
    }.headOption
    val blank = q""" "" """
    tableGenerator match {
      case Some(args) =>
        val catalog = args.collectFirst { case arg"catalog = $x" => x }.getOrElse(blank)
        val schema = args.collectFirst { case arg"schema = $x" => x }.getOrElse(blank)
        val table = args.collectFirst { case arg"table = $x" => x }.getOrElse(q""""ID_GENERATOR"""")
        val pkColumnName = args.collectFirst { case arg"pkColumnName = $x" => x }.getOrElse(q""""PK"""")
        val valueColumnName = args.collectFirst { case arg"valueColumnName = $x" => x }.getOrElse(q""""VALUE"""")
        val pkColumnValue = args.collectFirst { case arg"pkColumnValue = $x" => x }.get
        val initialValue = args.collectFirst { case arg"initialValue = $x" => x }.getOrElse(q"1")
        val allocationSize = args.collectFirst { case arg"allocationSize = $x" => x }.getOrElse(q"1")
        val implementer = args.collectFirst { case arg"implementer = $x" => x }.getOrElse(q"classOf[org.seasar.doma.jdbc.id.BuiltinSequenceIdGenerator]")
        TableGeneratorSetting(catalog, schema, table, pkColumnName, valueColumnName, pkColumnValue, initialValue, allocationSize, implementer)
      case _ => abort(domala.message.Message.DOMALA4035.getMessage(className, propertyName))
    }
  }
}

case class TableGeneratorSetting(
  catalog: Term.Arg,
  schema: Term.Arg,
  table: Term.Arg,
  pkColumnName: Term.Arg,
  valueColumnName: Term.Arg,
  pkColumnValue: Term.Arg,
  initialValue: Term.Arg,
  allocationSize: Term.Arg,
  implementer: Term.Arg
)