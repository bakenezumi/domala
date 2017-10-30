package domala

/** Indicates a database column.
  *
  * The annotated field must be a member of an [[domala.Entity Entity]] annotated class.
  *
  * {{{
  *@literal @Entity
  * case class Employee(
  *
  *  @literal @Column(name = "EMPLOYEE_NAME")
  *   employeeName: String,
  *
  *  @literal @Column(name = "SALARY")
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
