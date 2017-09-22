package domala

import org.seasar.doma.jdbc.id.{BuiltinSequenceIdGenerator, BuiltinTableIdGenerator, SequenceIdGenerator, TableIdGenerator}

class Id extends scala.annotation.StaticAnnotation

class GeneratedValue(strategy: GenerationType) extends scala.annotation.StaticAnnotation

sealed trait GenerationType

object GenerationType {
  object IDENTITY extends GenerationType
  object SEQUENCE extends GenerationType
  object TABLE extends GenerationType
}

class SequenceGenerator(
  catalog: String = "",
  schema: String = "",
  sequence: String,
  initialValue: Long = 1,
  allocationSize: Long = 1,
  implementer:Class[_ <:  SequenceIdGenerator] = classOf[BuiltinSequenceIdGenerator]
) extends scala.annotation.StaticAnnotation

class TableGenerator(
  catalog: String = "",
  schema: String = "",
  table: String = "ID_GENERATOR",
  pkColumnName: String = "PK",
  valueColumnName: String = "VALUE",
  pkColumnValue: String,
  initialValue: Long = 1,
  allocationSize: Long = 1,
  implementer:Class[_ <:  TableIdGenerator] = classOf[BuiltinTableIdGenerator]
) extends scala.annotation.StaticAnnotation

package internal { package macros {

  import scala.meta._

  case class SequenceGeneratorSetting(
    catalog: Term.Arg,
    schema: Term.Arg,
    sequence: Term.Arg,
    initialValue: Term.Arg,
    allocationSize: Term.Arg,
    implementer: Term.Arg
  )

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
        case _ => abort(org.seasar.doma.message.Message.DOMA4034.getMessage(className, propertyName))
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
        case _ => abort(org.seasar.doma.message.Message.DOMA4035.getMessage(className, propertyName))
      }
    }
  }

}}
