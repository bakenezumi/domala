package domala.internal.macros.meta.generator

import domala.Script
import domala.internal.macros.meta.args.DaoMethodCommonArgs
import domala.internal.macros.meta.util.MetaHelper
import domala.internal.macros.meta.util.NameConverters._
import domala.message.Message

import scala.collection.immutable.Seq
import scala.meta._

object ScriptGenerator extends DaoMethodGenerator {

  override def annotationClass: Class[Script] = classOf[Script]

  case class ScriptArgs(
    common: DaoMethodCommonArgs,
    blockDelimiter: Term.Arg,
    haltOnError: Term.Arg
  )

  object ScriptArgs {
    def of(args: Seq[Term.Arg], traitName: String, methodName: String): ScriptArgs = {
      val blockDelimiter =
        args.collectFirst { case arg"blockDelimiter = $x" => x }.getOrElse(q""" "" """)
      val haltOnError =
        args.collectFirst { case arg"haltOnError = $x" => x }.getOrElse(q"false")
      ScriptArgs(
        DaoMethodCommonArgs.of(args, traitName, methodName),
        blockDelimiter,
        haltOnError)
    }
  }

  override def generate(trtName: Type.Name, _def: Decl.Def, internalMethodName: Term.Name, args: Seq[Term.Arg]): Defn.Def = {
    val Decl.Def(_, name, _, paramss, tpe) = _def
    val scriptArgs = ScriptArgs.of(args, trtName.syntax, _def.syntax)

    tpe match {
      case t"Unit" | t"scala.Unit" => ()
      case _ =>  MetaHelper.abort(
        Message.DOMALA4172, trtName.syntax, name.syntax)
    }
    if(paramss.flatten.nonEmpty) {
      MetaHelper.abort(
        Message.DOMALA4173, trtName.syntax, name.syntax)
    }

    val (query, setScriptFilePath) =
      if (scriptArgs.common.hasSqlAnnotation) (
        q"new domala.jdbc.query.SqlScriptQuery(${scriptArgs.common.sql})",
        q"()")
      else (
        q"new org.seasar.doma.jdbc.query.SqlFileScriptQuery",
        q"__query.setScriptFilePath(domala.internal.macros.reflect.DaoReflectionMacros.getSqlFilePath(classOf[$trtName], ${_def.name.literal}, true, false, true))")

    q"""
    override def $name = {
      entering(${trtName.className}, ${name.literal})
      try {
        val __query = $query
        __query.setMethod($internalMethodName)
        __query.setConfig(__config)
        $setScriptFilePath
        __query.setCallerClassName(${trtName.className})
        __query.setCallerMethodName(${name.literal})
        __query.setBlockDelimiter(${scriptArgs.blockDelimiter})
        __query.setHaltOnError(${scriptArgs.haltOnError})
        __query.setSqlLogType(${scriptArgs.common.sqlLogType})
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
