package domala

/** Indicates a database table.
  *
  * This annotation must be used in conjunction with the [[domala.Entity Entity]]
  * annotation.
  *
  * {{{
  * @Entity
  * @Table(name = "EMP")
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
case class Table(
  catalog: String = "",
  schema: String = "",
  name: String = "",
  quote: Boolean = false
) extends scala.annotation.StaticAnnotation

object Table {
  import scala.reflect.api.Universe
  def reflect(u: Universe)(annotation: u.Annotation): Table = {
    import u._
    val args = annotation.tree.children.tail
    Table(
      args.head match {
        case Literal(Constant(v: String)) => v
        case _ => ""
      },
      args(1) match {
        case Literal(Constant(v: String)) => v
        case _ => ""
      },
      args(2) match {
        case Literal(Constant(v: String)) => v
        case _ => ""
      },
      args(3) match {
        case Literal(Constant(v: Boolean)) => v
        case _ => false
      }
    )
  }

}
