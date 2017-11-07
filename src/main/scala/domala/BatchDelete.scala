package domala

import org.seasar.doma.jdbc.SqlLogType

/** Indicates a batch delete.
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
  *  @literal @BatchDelete
  *   def delete(employee: Seq[Employee]): BatchResult[Employee]
  * }
  * }}}
  * @param sql a execution SQL. If not specified, SQL is auto generating.
  * @param queryTimeOut The query timeout in seconds.
  *  If not specified, [[domala.jdbc.Config Config#getQueryTimeout]] is used.
  * @param batchSize The batch size.
  *  If not specified, [[domala.jdbc.Config Config#getBatchSize]] is used.
  *  This value is used when [[java.sql.PreparedStatement PreparedStatement#executeBatch()]]
  *  is executed.
  * @param ignoreVersion Whether a version property is ignored.
  *  If `true`, a column that mapped to the version property is excluded
  *  from SQL DELETE statements. defaults to `false`.
  * @param suppressOptimisticLockException Whether
  *  [[org.seasar.doma.jdbc.OptimisticLockException OptimisticLockException]] is suppressed.
  *  Only if `sql` is not specified, this element value is used.
  *  dafaults to `false`.
  * @param sqlLog The output format of SQL logs.
  * @throws org.seasar.doma.DomaNullPointerException if any of the method parameters are
  * `null`
  * @throws org.seasar.doma.jdbc.OptimisticLockException if optimistic locking is enabled and an
  * update count is 0 for each entity
  * @throws org.seasar.doma.jdbc.JdbcException if a JDBC related error occurs
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
