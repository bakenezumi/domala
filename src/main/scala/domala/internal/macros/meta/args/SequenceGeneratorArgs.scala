package domala.internal.macros.meta.args

import scala.meta._

case class SequenceGeneratorArgs(
  catalog: Term.Arg,
  schema: Term.Arg,
  sequence: Term.Arg,
  initialValue: Term.Arg,
  allocationSize: Term.Arg,
  implementer: Type
)

object SequenceGeneratorArgs {
  def of(mods: Seq[Mod], className: String): Option[SequenceGeneratorArgs] = {
    val blank = q""" "" """
    mods.collectFirst {
      case mod"@SequenceGenerator(..$args)" =>
        val catalog = args.collectFirst { case arg"catalog = $x" => x }.getOrElse(blank)
        val schema = args.collectFirst { case arg"schema = $x" => x }.getOrElse(blank)
        val sequence = args.collectFirst { case arg"sequence = $x" => x }.get
        val initialValue = args.collectFirst { case arg"initialValue = $x" => x }.getOrElse(q"1")
        val allocationSize = args.collectFirst { case arg"allocationSize = $x" => x }.getOrElse(q"1")
        val implementer = args.collectFirst { case arg"implementer = classOf[$x]" => x }.getOrElse(t"org.seasar.doma.jdbc.id.BuiltinSequenceIdGenerator")
        SequenceGeneratorArgs(catalog, schema, sequence, initialValue, allocationSize, implementer)
    }
  }
}
