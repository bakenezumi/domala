package domala

import org.seasar.doma.DomaNullPointerException
import org.seasar.doma.jdbc.{JdbcException, SqlLogType, UniqueConstraintException}

/** Indicates a batch insert.
  *
  * The annotated method must be a member of a [[Dao]] annotated trait.
  *
  * {{{
  * @Entity
  * case class Employee(
  *   ...
  * )
  *
  * @Dao
  * trait EmployeeDao {
  *
  *   @BatchInsert
  *   def insert(employee: List[Employee]): BatchResult[Employee]
  * }
  * }}}
  *
  * The method may throw following exceptions:
  * @throws DomaNullPointerException if any of the method parameters are
  * `null`
  * @throws UniqueConstraintException if an unique constraint is violated
  * @throws JdbcException if a JDBC related error occurs
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
