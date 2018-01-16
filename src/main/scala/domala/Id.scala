package domala

import domala.jdbc.id.{BuiltinSequenceIdGenerator, BuiltinTableIdGenerator, SequenceIdGenerator, TableIdGenerator}

/** Indicates an entity identifier that is mapped to a primary key of a database
  * table.
  * The annotated field must be a member of an [[domala.Entity Entity]] annotated class.
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
  * @see [[domala.GeneratedValue GeneratedValue]]
  */
class Id extends scala.annotation.StaticAnnotation

/** Indicates a strategy to generate identifiers.
  *
  * The annotated field must be a member of an [[domala.Entity Entity]] annotated class and
  * the field must be annotated with [[domala.Id Id]].
  *
  * The additional annotation is required according to the `strategy`
  * value:
  *   - [[domala.SequenceGenerator SequenceGenerator]] annotation is required, if
  *     [[domala.GenerationType.SEQUENCE GenerationType.SEQUENCE]] is specified. </li>
  *   - the [[domala.TableGenerator TableGenerator]] annotation is required, if
  *     [[domala.GenerationType.TABLE GenerationType.TABLE]] is specified. </li>
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
  * @see [[domala.GenerationType GenerationType]]
  * @see [[domala.SequenceGenerator SequenceGenerator]]
  * @see [[domala.TableGenerator TableGenerator]]
  */
class GeneratedValue(strategy: GenerationType) extends scala.annotation.StaticAnnotation

/** Defines strategies to generate identifiers.
  *
  * @see [[domala.GeneratedValue GeneratedValue]]
  */
sealed trait GenerationType
object GenerationType {
  object IDENTITY extends GenerationType
  object SEQUENCE extends GenerationType
  object TABLE extends GenerationType
}

/** Indicates an identifier generator that uses a sequence.
  *
  * The annotated field must be a member of an [[domala.Entity Entity]] annotated class.
  * This annotation must be used in conjunction with the [[domala.Id Id]] annotation
  * and the [[domala.GeneratedValue GeneratedValue]] annotation.
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
  * The annotated field must be a member of an [[domala.Entity Entity]] annotated class.
  * This annotation must be used in conjunction with the [[domala.Id Id]] annotation
  * and the [[domala.GeneratedValue GeneratedValue]] annotation.
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
