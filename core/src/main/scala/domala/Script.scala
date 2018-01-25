package domala

import domala.jdbc.SqlLogType

/** Indicates a script.
  *
  * The annotated method must be a member of a [[domala.Dao Dao]] annotated trait.
  *
  * {{{
  * @Dao
  * trait EmployeeDao {
  *
  *   @Script("""
  *   create table employee ...
  *   """)
  *   def createTables(): Unit
  * }
  * }}}
  *
  * @param sql a execution SQL.
  * @param blockDelimiter The SQL block delimiter.
  *  The SQL block delimiter is a mark that indicates the end of definition of
  *  such as stored procedures, stored functions and triggers.
  *  If not specified, the return value of
  *  [[org.seasar.doma.jdbc.dialect.Dialect Dialect#getScriptBlockDelimiter]] is used.
  * @throws org.seasar.doma.jdbc.ScriptException if an exception is thrown
  *  while executing a script
  * @throws org.seasar.doma.jdbc.JdbcException if a JDBC related error occurs
  */
class Script(
  sql: String = "",
  blockDelimiter: String = "",
  haltOnError: Boolean = true,
  sqlLog: SqlLogType = SqlLogType.FORMATTED
) extends scala.annotation.StaticAnnotation
