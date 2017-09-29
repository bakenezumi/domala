package domala.internal.macros

import scala.meta._

object ScriptGenerator {
  def generate(trtName: Type.Name, _def: Decl.Def, internalMethodName: Term.Name, sql: Term.Arg): Defn.Def = {
    val Decl.Def(mods, name, tparams, paramss, tpe) = _def
    val trtNameStr = trtName.syntax
    val nameStr = name.syntax

    q"""
    override def $name = {
      entering($trtNameStr, $nameStr)
      try {
        val __query = new domala.jdbc.query.SqlAnnotationScriptQuery($sql)
        __query.setMethod($internalMethodName)
        __query.setConfig(__config)
        __query.setCallerClassName($trtNameStr)
        __query.setCallerMethodName($nameStr)
        __query.setBlockDelimiter("")
        __query.setHaltOnError(true)
        __query.setSqlLogType(org.seasar.doma.jdbc.SqlLogType.FORMATTED)
        __query.prepare()
        val __command = new domala.jdbc.command.ScriptCommand(__query)
        __command.execute()
        __query.complete()
        exiting($trtNameStr, $nameStr, null)
      } catch {
        case __e: java.lang.RuntimeException => {
          throwing($trtNameStr, $nameStr, __e)
          throw __e
        }
      }
    }
  """
  }
}
