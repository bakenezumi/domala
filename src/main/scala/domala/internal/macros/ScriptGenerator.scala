package domala.internal.macros

import domala.Script
import domala.internal.macros.helper.LiteralConverters._
import domala.internal.macros.helper.{DaoMacroHelper, MacrosHelper}
import domala.message.Message

import scala.collection.immutable.Seq
import scala.meta._

case class ScriptSetting(
  blockDelimiter: Term.Arg,
  haltOnError: Term.Arg
)

object ScriptGenerator extends DaoMethodGenerator {

  override def annotationClass: Class[Script] = classOf[Script]

  def readSelectSetting(args: Seq[Term.Arg]): ScriptSetting = {
    val blockDelimiter =
      args.collectFirst { case arg"blockDelimiter = $x" => x }.getOrElse(q""" "" """)
    val haltOnError =
      args.collectFirst { case arg"haltOnError = $x" => x }.getOrElse(q"false")
    ScriptSetting(
      blockDelimiter,
      haltOnError)
  }
  override def generate(trtName: Type.Name, _def: Decl.Def, internalMethodName: Term.Name, args: Seq[Term.Arg]): Defn.Def = {
    val Decl.Def(_, name, _, paramss, tpe) = _def
    val commonSetting = DaoMacroHelper.readCommonSetting(
      args,
      trtName.syntax,
      _def.name.syntax)
    val scriptSetting = readSelectSetting(args)

    tpe match {
      case t"Unit" | t"scala.Unit" => ()
      case _ =>  MacrosHelper.abort(
        Message.DOMALA4172, trtName.syntax, name.syntax)
    }
    if(paramss.flatten.nonEmpty) {
      MacrosHelper.abort(
        Message.DOMALA4173, trtName.syntax, name.syntax)
    }

    q"""
    override def $name = {
      entering(${trtName.literal}, ${name.literal})
      try {
        val __query = new domala.jdbc.query.SqlScriptQuery(${commonSetting.sql})
        __query.setMethod($internalMethodName)
        __query.setConfig(__config)
        __query.setCallerClassName(${trtName.literal})
        __query.setCallerMethodName(${name.literal})
        __query.setBlockDelimiter(${scriptSetting.blockDelimiter})
        __query.setHaltOnError(${scriptSetting.haltOnError})
        __query.setSqlLogType(${commonSetting.sqlLogType})
        __query.prepare()
        val __command = getCommandImplementors.createScriptCommand($internalMethodName, __query)
        __command.execute()
        __query.complete()
        exiting(${trtName.literal}, ${name.literal}, null)
      } catch {
        case __e: java.lang.RuntimeException => {
          throwing(${trtName.literal}, ${name.literal}, __e)
          throw __e
        }
      }
    }
  """
  }
}
