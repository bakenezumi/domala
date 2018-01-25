package domala.jdbc.interpolation

import domala.jdbc.Config
import domala.jdbc.command.ScriptCommand

/** The object used for executing SQL script.
  *
  * @param scripts  SQL scripts
  */
class ScriptStatement(scripts: String, config: Config) {
  def execute(): Unit = {
    val query = new domala.jdbc.query.SqlScriptQuery(scripts)
    query.setConfig(config)
    query.setCallerClassName("ScriptStatement")
    query.setCallerMethodName("execute")
    query.setBlockDelimiter(Option(config.getDialect.getScriptBlockDelimiter).getOrElse(""))
    query.setHaltOnError(false)
    query.setSqlLogType(org.seasar.doma.jdbc.SqlLogType.FORMATTED)
    query.prepare()
    val command = new ScriptCommand(query)
    command.execute()
    query.complete()
  }
}

object ScriptStatement {
  def apply(scripts: String, config: Config): ScriptStatement = new ScriptStatement(scripts, config)
}
