package domala

import org.seasar.doma.jdbc.id.{BuiltinSequenceIdGenerator, BuiltinTableIdGenerator, SequenceIdGenerator, TableIdGenerator}

/** Indicates an entity identifier that is mapped to a primary key of a database
  * table.
  * The annotated field must be a member of an [[Entity]] annotated class.
  *
  * {{{
  * @Entity
  * case class Employee(
  *
  *   @Id
  *   @Column(name = "ID")
  *   id: String,
  *   ...
  * )
  * }}}
  *
  * @see [[GeneratedValue]]
  */
class Id extends scala.annotation.StaticAnnotation

/** Indicates a strategy to generate identifiers.
  *
  * The annotated field must be a member of an [[Entity]] annotated class and
  * the field must be annotated with [[Id]].
  *
  * The additional annotation is required according to the `strategy`
  * value:
  * - [[SequenceGenerator]] annotation is required, if
  * [[GenerationType.SEQUENCE]] is specified.
  * - the [[TableGenerator]] annotation is required, if
  * [[GenerationType.TABLE]] is specified.
  *
  * {{{
  * @Entity
  * case class Employee (
  *
  *   @Id
  *   @GeneratedValue(strategy = GenerationType.SEQUENCE)
  *   @SequenceGenerator(sequence = "EMPLOYEE_SEQ")
  *   id: Int,
  *
  *   ...
  * )
  * }}}
  *
  * @see [[GenerationType]]
  * @see [[SequenceGenerator]]
  * @see [[TableGenerator]]
  */
class GeneratedValue(strategy: GenerationType) extends scala.annotation.StaticAnnotation

/** Defines strategies to generate identifiers.
  *
  * @see [[GeneratedValue]]
  */
sealed trait GenerationType
object GenerationType {
  object IDENTITY extends GenerationType
  object SEQUENCE extends GenerationType
  object TABLE extends GenerationType
}

/** Indicates an identifier generator that uses a sequence.
  *
  * The annotated field must be a member of an [[Entity]] annotated class.
  * This annotation must be used in conjunction with the [[Id]] annotation
  * and the [[GeneratedValue]] annotation.
  *
  * {{{
  * @Entity
  * case class Employee(
  *
  *   @Id
  *   @GeneratedValue(strategy = GenerationType.SEQUENCE)
  *   @SequenceGenerator(sequence = "EMPLOYEE_SEQ")
  *   id: Int,
  *
  *   ...
  * )
  * }}}
  */
class SequenceGenerator(
  catalog: String = "",
  schema: String = "",
  sequence: String,
  initialValue: Long = 1,
  allocationSize: Long = 1,
  implementer:Class[_ <:  SequenceIdGenerator] = classOf[BuiltinSequenceIdGenerator]
) extends scala.annotation.StaticAnnotation

/** Indicates an identifier generator that uses a table.
  *
  * The annotated field must be a member of an [[Entity]] annotated class.
  * This annotation must be used in conjunction with the [[Id]] annotation
  * and the [[GeneratedValue]] annotation.
  *
  * {{{
  * @Entity
  * case class Employee(
  *
  *   @Id
  *   @GeneratedValue(strategy = GenerationType.TABLE)
  *   @TableGenerator(pkColumnValue = "EMPLOYEE_ID")
  *   id: Integer,
  *
  *   ...
  * }
  * }}}
  */
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

