package domala

import org.seasar.doma.DomaNullPointerException
import org.seasar.doma.jdbc.{JdbcException, OptimisticLockException, SqlLogType}

/** Indicates a delete.
  *
  * The annotated method must be a member of a [[Dao]] annotated interface.
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
  *   @Delete
  *   delete(employee: Employee): Int
  * }
  * }}}
  *
  * The method may throw following exceptions:
  * @throws DomaNullPointerException if any of the method parameters are
  * `null`
  * @throws OptimisticLockException if optimistic locking is enabled and an
  * update count is 0 for each entity
  * @throws JdbcException if a JDBC related error occurs
  */
class Delete(
  sql: String = "",
  queryTimeOut: Int = -1,
  ignoreVersion: Boolean = false,
  suppressOptimisticLockException: Boolean = false,
  sqlLog: SqlLogType = SqlLogType.FORMATTED
) extends scala.annotation.StaticAnnotation
