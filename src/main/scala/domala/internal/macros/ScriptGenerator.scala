package domala.internal.macros

import scala.collection.immutable.Seq
import scala.meta._

object ScriptGenerator extends DaoMethodGenerator {
  override def annotationName: String = "@Script"
  override def generate(trtName: Type.Name, _def: Decl.Def, internalMethodName: Term.Name, args: Seq[Term.Arg]): Defn.Def = {
    val Decl.Def(mods, name, tparams, paramss, tpe) = _def
    val commonSetting = DaoMacroHelper.readCommonSetting(
      args,
      trtName.syntax,
      _def.name.syntax)

    q"""
    override def $name = {
      entering(${trtName.syntax}, ${name.syntax})
      try {
        val __query = new domala.jdbc.query.SqlAnnotationScriptQuery(${commonSetting.sql})
        __query.setMethod($internalMethodName)
        __query.setConfig(__config)
        __query.setCallerClassName(${trtName.syntax})
        __query.setCallerMethodName(${name.syntax})
        __query.setBlockDelimiter("")
        __query.setHaltOnError(true)
        __query.setSqlLogType(org.seasar.doma.jdbc.SqlLogType.FORMATTED)
        __query.prepare()
        val __command = new domala.jdbc.command.ScriptCommand(__query)
        __command.execute()
        __query.complete()
        exiting(${trtName.syntax}, ${name.syntax}, null)
      } catch {
        case __e: java.lang.RuntimeException => {
          throwing(${trtName.syntax}, ${name.syntax}, __e)
          throw __e
        }
      }
    }
  """
  }
}
