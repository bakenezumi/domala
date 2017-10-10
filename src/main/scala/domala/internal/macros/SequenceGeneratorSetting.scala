package domala.internal.macros

import scala.meta._

object SequenceGeneratorSetting {
  def read(mods: Seq[Mod], className: String, propertyName: String): SequenceGeneratorSetting = {
    val sequenceGenerator = mods.collect {
      case mod"@SequenceGenerator(..$args)" => args
    }.headOption
    val blank = q""" "" """
    sequenceGenerator match {
      case Some(args) =>
        val catalog = args.collectFirst { case arg"catalog = $x" => x }.getOrElse(blank)
        val schema = args.collectFirst { case arg"schema = $x" => x }.getOrElse(blank)
        val sequence = args.collectFirst { case arg"sequence = $x" => x }.get
        val initialValue = args.collectFirst { case arg"initialValue = $x" => x }.getOrElse(q"1")
        val allocationSize = args.collectFirst { case arg"allocationSize = $x" => x }.getOrElse(q"1")
        val implementer = args.collectFirst { case arg"implementer = $x" => x }.getOrElse(q"classOf[org.seasar.doma.jdbc.id.BuiltinSequenceIdGenerator]")
        SequenceGeneratorSetting(catalog, schema, sequence, initialValue, allocationSize, implementer)
      case _ => abort(domala.message.Message.DOMALA4034.getMessage(className, propertyName))
    }
  }
}

case class SequenceGeneratorSetting(
  catalog: Term.Arg,
  schema: Term.Arg,
  sequence: Term.Arg,
  initialValue: Term.Arg,
  allocationSize: Term.Arg,
  implementer: Term.Arg
)