package domala

/** Indicates a database column.
  *
  * The annotated field must be a member of an [[domala.Entity Entity]]
  * annotated class.
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
  *
  * @param name The name of the column. If not specified, the name is
  *  resolved by [[domala.Entity Entity#naming]].
  * @param insertable Whether the column is included in SQL INSERT statements.
  * @param updatable Whether the column is included in SQL UPDATE statements.
  * @param quote Whether the column name is enclosed by quotation marks in
  *  SQL statements.
  */
case class Column(
  name: String = "",
  insertable: Boolean = true,
  updatable: Boolean = true,
  quote: Boolean = false
) extends scala.annotation.StaticAnnotation

object Column {
  import scala.reflect.api.Universe
  def reflect(u: Universe)(annotation: u.Annotation): Column = {
    import u._
    val args = annotation.tree.children.tail
    Column(
     args.head match {
       case Literal(Constant(v: String)) => v
       case _ => ""
     },
      args(1) match {
        case Literal(Constant(v: Boolean)) => v
        case _ => true
      },
      args(2) match {
        case Literal(Constant(v: Boolean)) => v
        case _ => true
      },
      args(3) match {
        case Literal(Constant(v: Boolean)) => v
        case _ => false
      }
    )
  }

}
