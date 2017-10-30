package domala

import org.seasar.doma.DomaNullPointerException
import org.seasar.doma.jdbc.{JdbcException, OptimisticLockException, SqlLogType}

/** Indicates a batch delete.
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
  *   @BatchDelete
  *   def delete(employee: Seq[Employee]): BatchResult[Employee]
  * }
  * }}}
  *
  * The method may throw following exceptions:
  * @throws DomaNullPointerException if any of the method parameters are
  * `null`
  * @throws OptimisticLockException  if optimistic locking is enabled and an
  * update count is 0 for each entity
  * @throws JdbcException if a JDBC related error occurs
  *
  */
class BatchDelete(
    sql: String = "",
    queryTimeOut: Int = -1,
    batchSize: Int = -1,
    ignoreVersion: Boolean = false,
    suppressOptimisticLockException: Boolean = false,
    sqlLog: SqlLogType = SqlLogType.FORMATTED
) extends scala.annotation.StaticAnnotation
