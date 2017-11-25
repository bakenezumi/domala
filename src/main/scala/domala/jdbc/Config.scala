package domala.jdbc

import java.lang.reflect.Method

import domala.jdbc.command.ScriptCommand
import domala.jdbc.query.SqlScriptQuery
import org.seasar.doma.jdbc.query.ScriptQuery

/** A runtime configuration for DAOs.
  *
  * The implementation must be thread safe.
  *
  */
trait Config extends org.seasar.doma.jdbc.Config {
  getSqlFileRepository.clearCache()
  getSqlNodeRepository.clearCache()
  override def getCommandImplementors: org.seasar.doma.jdbc.CommandImplementors = CommandImplementors
  def getSqlNodeRepository: SqlNodeRepository = GreedyCacheSqlNodeRepository

}

/**
  * A factory for the [[org.seasar.doma.jdbc.command.Command]] implementation classes.
  */
object CommandImplementors extends org.seasar.doma.jdbc.CommandImplementors {
  override def createScriptCommand(method: Method, query: ScriptQuery): org.seasar.doma.jdbc.command.ScriptCommand = query match {
    case q: SqlScriptQuery => new ScriptCommand(q)
    case _ => super.createScriptCommand(method, query)
  }
}
