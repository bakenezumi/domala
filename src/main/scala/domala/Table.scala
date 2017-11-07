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
  *
  * @param catalog The catalog name.
  * @param schema The schema name.
  * @param name The table name. If not specified, the name is
  *  resolved by [[domala.Entity Entity#naming]].
  * @param quote Whether quotation marks are used for the catalog name,
  *  the schema name and the table name.
  */
class Table(
  catalog: String = "",
  schema: String = "",
  name: String = "",
  quote: Boolean = false
) extends scala.annotation.StaticAnnotation
