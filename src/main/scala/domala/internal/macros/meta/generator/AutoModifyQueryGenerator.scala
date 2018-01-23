package domala.internal.macros.meta.generator

import domala.internal.macros.meta.QueryDefDecl
import domala.internal.macros.meta.args.DaoMethodCommonArgs
import domala.internal.macros.meta.util.MetaHelper
import domala.internal.macros.meta.util.NameConverters._
import domala.message.Message

import scala.collection.immutable.Seq
import scala.meta._

object AutoModifyQueryGenerator {
  def extractParameter(defDecl: QueryDefDecl): (Term.Name, Type.Name) = {
    if (defDecl.paramss.flatten.length != 1)
      MetaHelper.abort(Message.DOMALA4002,
              defDecl.trtName.value, defDecl.name.value)
    defDecl.paramss.flatten.head match {
      case param"$paramName: ${Some(paramTpe)}" =>
        (Term.Name(paramName.value), Type.Name(paramTpe.toString))
    }
  }

  def generate(
    defDecl: QueryDefDecl,
    commonArgs: DaoMethodCommonArgs,
    paramName: Term.Name,
    paramType: Type.Name,
    internalMethodName: Term.Name,
    query: Term.Apply,
    otherQueryArgs: Seq[Stat],
    command: Term.Apply): Defn.Def = {

    val (isReturnResult, entityType) = DaoMethodGeneratorHelper.getResultType(defDecl)
    val result = if (isReturnResult) {
      q"domala.jdbc.Result[$entityType](__count, __query.getEntity)"
    } else {
      q"__count"
    }

    q"""
    override def ${defDecl.name} = {
      val __desc = domala.internal.macros.reflect.DaoReflectionMacros.validateAutoModifyParam(classOf[${defDecl.trtName}], ${defDecl.name.literal}, classOf[$paramType])
      entering(${defDecl.trtName.className}, ${defDecl.name.literal}, $paramName)
      try {
        if ($paramName == null) {
          throw new org.seasar.doma.DomaNullPointerException(${paramName.literal})
        }
        val __query = $query
        __query.setMethod($internalMethodName)
        __query.setConfig(__config)
        __query.setEntity($paramName)
        __query.setCallerClassName(${defDecl.trtName.className})
        __query.setCallerMethodName(${defDecl.name.literal})
        __query.setQueryTimeout(${commonArgs.queryTimeOut})
        __query.setSqlLogType(${commonArgs.sqlLogType})
        ..$otherQueryArgs
        __query.prepare()
        val __command = $command
        val __count = __command.execute()
        __query.complete()
        val __result = $result
        exiting(${defDecl.trtName.className}, ${defDecl.name.literal}, __result)
        __result
      } catch {
        case __e: java.lang.RuntimeException => {
          throwing(${defDecl.trtName.className}, ${defDecl.name.literal}, __e)
          throw __e
        }
      }
    }
    """
  }
}
