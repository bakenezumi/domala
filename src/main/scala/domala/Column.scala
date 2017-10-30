package domala

/** Indicates a database column.
  *
  * The annotated field must be a member of an [[Entity]] annotated class.
  *
  * {{{
  * @Entity
  * case class Employee(
  *
  *   @Column(name = "EMPLOYEE_NAME")
  *   employeeName: String,
  *
  *   @Column(name = "SALARY")
  *   salary: BigDecimal,
  *
  *   ...
  * )
  * }}}
  */
class Column(
  name: String = "",
  insertable: Boolean = true,
  updatable: Boolean = true,
  quote: Boolean = false
) extends scala.annotation.StaticAnnotation
