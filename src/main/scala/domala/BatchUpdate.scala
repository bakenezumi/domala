package domala

import org.seasar.doma.jdbc.SqlLogType

/** Indicates a batch delete.
  *
  * The annotated method must be a member of a [[domala.Dao Dao]] annotated trait.
  *
  * {{{
  *@literal @Entity
  * case class Employee {
  *   ...
  * }
  *
  *@literal @Dao
  * trait EmployeeDao {
  *
  *  @literal @BatchDelete
  *   def delete(employee: List[Employee]): BatchResult[Employee]
  * }
  * }}}
  *
  * The method may throw following exceptions:
  * @throws org.seasar.doma.DomaNullPointerException if any of the method parameters are
  * `null`
  * @throws org.seasar.doma.jdbc.OptimisticLockException if optimistic locking is enabled and an
  * update count is 0 for each entity
  * @throws org.seasar.doma.jdbc.JdbcException if a JDBC related error occurs
  */
class BatchUpdate(
  sql: String = "",
  queryTimeOut: Int = -1,
  batchSize: Int = -1,
  ignoreVersion: Boolean = false,
  include: collection.Seq[String] = Nil,
  exclude: collection.Seq[String] = Nil,
  suppressOptimisticLockException: Boolean = false,
  sqlLog: SqlLogType = SqlLogType.FORMATTED
) extends scala.annotation.StaticAnnotation
