package domala

import org.seasar.doma.jdbc.SqlLogType

/** Indicates a script.
  *
  * The annotated method must be a member of a [[domala.Dao Dao]] annotated trait.
  *
  * {{{
  *@literal @Dao
  * trait EmployeeDao {
  *
  *  @literal @Script("""
  *   create table employee ...
  *   """)
  *   def createTables(): Unit
  * }
  * }}}
  *
  * The method may throw following exceptions:
  * @throws org.seasar.doma.jdbc.ScriptException if an exception is thrown while executing a
  *         script
  * @throws org.seasar.doma.jdbc.JdbcException if a JDBC related error occurs
  */
class Script(
  sql: String,
  blockDelimiter: String = "",
  haltOnError: Boolean = true,
  sqlLog: SqlLogType = SqlLogType.FORMATTED
) extends scala.annotation.StaticAnnotation
