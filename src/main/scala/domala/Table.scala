package domala

/** Indicates a database table.
  *
  * This annotation must be used in conjunction with the [[domala.Entity Entity]]
  * annotation.
  *
  * {{{
  *@literal @Entity
  *@literal @Table(name = "EMP")
  * case class Employee {
  *   ...
  * }
  * }}}
  */
class Table(
  catalog: String = "",
  schema: String = "",
  name: String = "",
  quote: Boolean = false
) extends scala.annotation.StaticAnnotation
