package domala

import org.seasar.doma.jdbc.SqlLogType

/** Indicates a batch insert.
  *
  * The annotated method must be a member of a [[domala.Dao Dao]] annotated trait.
  *
  * {{{
  *@literal @Entity
  * case class Employee(
  *   ...
  * )
  *
  *@literal @Dao
  * trait EmployeeDao {
  *
  *  @literal @BatchInsert
  *   def insert(employee: List[Employee]): BatchResult[Employee]
  * }
  * }}}
  *
  * The method may throw following exceptions:
  * @throws org.seasar.doma.DomaNullPointerException if any of the method parameters are
  * `null`
  * @throws org.seasar.doma.jdbc.UniqueConstraintException if an unique constraint is violated
  * @throws org.seasar.doma.jdbc.JdbcException if a JDBC related error occurs
  *
  */
class BatchInsert(
    sql: String = "",
    queryTimeOut: Int = -1,
    batchSize: Int = -1,
    include: collection.Seq[String] = Nil,
    exclude: collection.Seq[String] = Nil,
    sqlLog: SqlLogType = SqlLogType.FORMATTED
) extends scala.annotation.StaticAnnotation
