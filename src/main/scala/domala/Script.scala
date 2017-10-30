package domala

import org.seasar.doma.jdbc.{JdbcException, ScriptException, SqlLogType}

/** Indicates a script.
  *
  * The annotated method must be a member of a [[Dao]] annotated trait.
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
  * The method may throw following exceptions:
  * @throws ScriptException if an exception is thrown while executing a
  *         script
  * @throws JdbcException if a JDBC related error occurs
  */
class Script(
  sql: String,
  blockDelimiter: String = "",
  haltOnError: Boolean = true,
  sqlLog: SqlLogType = SqlLogType.FORMATTED
) extends scala.annotation.StaticAnnotation
