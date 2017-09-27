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

