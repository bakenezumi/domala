package domala.internal.macros

import domala.Script
import domala.internal.macros.args.DaoMethodCommonArgs
import domala.internal.macros.util.LiteralConverters._
import domala.internal.macros.util.MacrosHelper
import domala.message.Message

import scala.collection.immutable.Seq
import scala.meta._

object ScriptGenerator extends DaoMethodGenerator {

  override def annotationClass: Class[Script] = classOf[Script]

  case class ScriptArgs(
    blockDelimiter: Term.Arg,
    haltOnError: Term.Arg
  )

  object ScriptArgs {
    def read(args: Seq[Term.Arg]): ScriptArgs = {
      val blockDelimiter =
        args.collectFirst { case arg"blockDelimiter = $x" => x }.getOrElse(q""" "" """)
      val haltOnError =
        args.collectFirst { case arg"haltOnError = $x" => x }.getOrElse(q"false")
      ScriptArgs(
        blockDelimiter,
        haltOnError)
    }
  }

  override def generate(trtName: Type.Name, _def: Decl.Def, internalMethodName: Term.Name, args: Seq[Term.Arg]): Defn.Def = {
    val Decl.Def(_, name, _, paramss, tpe) = _def
    val commonArgs = DaoMethodCommonArgs.read(
      args,
      trtName.syntax,
      _def.name.syntax)
    val scriptArgs = ScriptArgs.read(args)

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
      entering(${trtName.className}, ${name.literal})
      try {
        val __query = new domala.jdbc.query.SqlScriptQuery(${commonArgs.sql})
        __query.setMethod($internalMethodName)
        __query.setConfig(__config)
        __query.setCallerClassName(${trtName.className})
        __query.setCallerMethodName(${name.literal})
        __query.setBlockDelimiter(${scriptArgs.blockDelimiter})
        __query.setHaltOnError(${scriptArgs.haltOnError})
        __query.setSqlLogType(${commonArgs.sqlLogType})
        __query.prepare()
        val __command = getCommandImplementors.createScriptCommand($internalMethodName, __query)
        __command.execute()
        __query.complete()
        exiting(${trtName.className}, ${name.literal}, null)
      } catch {
        case __e: java.lang.RuntimeException => {
          throwing(${trtName.className}, ${name.literal}, __e)
          throw __e
        }
      }
    }
  """
  }

}
