package domala.jdbc.command

import domala.jdbc.query.SqlScriptQuery
import org.seasar.doma.internal.jdbc.util.JdbcUtil
import org.seasar.doma.jdbc.AbstractSql
import org.seasar.doma.jdbc.ScriptException
import org.seasar.doma.jdbc.SqlKind
import org.seasar.doma.jdbc.SqlLogType
import org.seasar.doma.jdbc.SqlParameter

class ScriptCommand(query: SqlScriptQuery)
    extends org.seasar.doma.jdbc.command.ScriptCommand(query) {
  override def execute(): Void = {
    val config = query.getConfig
    val connection = JdbcUtil.getConnection(config.getDataSource)
    try {
      val reader = new ScriptReader(query)
      try {
        var script = reader.readSql()
        while (script != null) {
          val sql =
            new ScriptSql(script, query.getSqlLogType(), query.comment _)
          val statement = JdbcUtil.createStatement(connection)
          try {
            log(sql)
            setupOptions(statement)
            statement.execute(script)
          } catch {
            case e: Exception =>
              if (query.getHaltOnError()) {
                throw new ScriptException(e, sql, reader.getLineNumber)
              }
              if (savedScriptException == null) {
                savedScriptException =
                  new ScriptException(e, sql, reader.getLineNumber)
              }
          } finally {
            JdbcUtil.close(statement, config.getJdbcLogger)
          }
          script = reader.readSql()
        }
      } finally {
        reader.close()
      }
    } finally {
      JdbcUtil.close(connection, config.getJdbcLogger)
    }
    throwSavedScriptExceptionIfExists()
    null
  }

  protected def log(sql: ScriptSql): Unit = {
    val logger = query.getConfig.getJdbcLogger
    logger.logSql(query.getClassName, query.getMethodName, sql)
  }

  protected class ScriptSql(
    rawSql: String,
    sqlLogType: SqlLogType,
    converter: java.util.function.Function[String, String]
  ) extends AbstractSql[SqlParameter](
    SqlKind.SCRIPT,
    rawSql,
    rawSql,
    null,
    java.util.Collections.emptyList(),
    sqlLogType,
    converter) {}
}
