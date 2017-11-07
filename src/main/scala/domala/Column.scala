package domala

/** Indicates a database column.
  *
  * The annotated field must be a member of an [[domala.Entity Entity]]
  * annotated class.
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
  *
  * @param name The name of the column. If not specified, the name is
  *  resolved by [[domala.Entity Entity#naming]].
  * @param insertable Whether the column is included in SQL INSERT statements.
  * @param updatable Whether the column is included in SQL UPDATE statements.
  * @param quote Whether the column name is enclosed by quotation marks in
  *  SQL statements.
  */
class Column(
  name: String = "",
  insertable: Boolean = true,
  updatable: Boolean = true,
  quote: Boolean = false
) extends scala.annotation.StaticAnnotation
