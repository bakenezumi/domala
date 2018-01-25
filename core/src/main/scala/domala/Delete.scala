package domala

import domala.jdbc.SqlLogType

/** Indicates a delete.
  *
  * The annotated method must be a member of a [[domala.Dao Dao]] annotated interface.
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
  * @param sql a execution SQL. If not specified, SQL is auto generating.
  * @param sqlFile Whether the annotated method is mapped to an SQL file.
  * @param queryTimeOut The query timeout in seconds.
  *  If not specified, [[domala.jdbc.Config Config#getQueryTimeout]] is used.
  * @param ignoreVersion Whether a version property is ignored.
  *  If `true`, a column that mapped to the version property is excluded
  *  from SQL DELETE statements. defaults to `false`.
  * @param suppressOptimisticLockException Whether
  *  [[org.seasar.doma.jdbc.OptimisticLockException OptimisticLockException]] is suppressed.
  *  Only if `sql` is not specified, this element value is used.
  *  defaults to `false`.
  * @param sqlLog The output format of SQL logs.
  * @throws org.seasar.doma.DomaNullPointerException if any of the method parameters are
  * `null`
  * @throws org.seasar.doma.jdbc.OptimisticLockException if optimistic locking is enabled and an
  * update count is 0 for each entity
  * @throws org.seasar.doma.jdbc.JdbcException if a JDBC related error occurs
  */
class Delete(
  sql: String = "",
  sqlFile: Boolean = false,
  queryTimeOut: Int = -1,
  ignoreVersion: Boolean = false,
  suppressOptimisticLockException: Boolean = false,
  sqlLog: SqlLogType = SqlLogType.FORMATTED
) extends scala.annotation.StaticAnnotation
