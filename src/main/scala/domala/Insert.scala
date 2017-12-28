package domala

import domala.jdbc.SqlLogType

/** Indicates a insert.
  *
  * The annotated method must be a member of a [[domala.Dao Dao]] annotated trait.
  *
  * {{{
  * @Entity
  * case class Employee(
  *   ...
  * )
  *
  * @Dao
  * trait EmployeeDao {
  *  @Insert
  *   def insert(employee: Employee): Result[Employee]
  * }
  * }}}
  *
  * @param sql a execution SQL. If not specified, SQL is auto generating.
  * @param sqlFile Whether the annotated method is mapped to an SQL file.
  * @param queryTimeOut The query timeout in seconds.
  *  If not specified, [[domala.jdbc.Config Config#getQueryTimeout]] is used.
  * @param include The properties whose mapped columns are included in
  *  SQL INSERT statements.
  *  Only if `sql` is not specified, this value is used.
  * @param exclude The properties whose mapped columns are excluded
  *  from SQL INSERT statements.
  *  Only if `sql` is not specified, this value is used.
  * @param sqlLog The output format of SQL logs.
  * @throws org.seasar.doma.DomaNullPointerException if any of the method parameters are
  * `null`
  * @throws org.seasar.doma.jdbc.UniqueConstraintException if an unique constraint is violated
  * @throws org.seasar.doma.jdbc.JdbcException if a JDBC related error occurs
  */
class Insert(
  sql: String = "",
  sqlFile: Boolean = false,
  queryTimeOut: Int = -1,
  excludeNull: Boolean = false,
  include: collection.Seq[String] = Nil,
  exclude: collection.Seq[String] = Nil,
  sqlLog: SqlLogType = SqlLogType.FORMATTED
) extends scala.annotation.StaticAnnotation
