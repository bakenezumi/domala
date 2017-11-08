package domala.internal.macros

import domala.message.Message
import scala.meta._
import scala.collection.immutable.Seq

object AutoModifyQueryGenerator {
  def extractParameter(defDecl: QueryDefDecl): (Term.Name, Type.Name) = {
    if (defDecl.paramss.flatten.length != 1)
      MacrosHelper.abort(Message.DOMALA4002,
              defDecl.trtName.value, defDecl.name.value)
    defDecl.paramss.flatten.head match {
      case param"$paramName: ${Some(paramTpe)}" =>
        (Term.Name(paramName.value), Type.Name(paramTpe.toString))
    }
  }

  def generate(
    defDecl: QueryDefDecl,
    commonSetting: DaoMethodCommonSetting,
    paramName: Term.Name,
    paramType: Type.Name,
    internalMethodName: Term.Name,
    query: Term.Apply,
    otherQuerySettings: Seq[Stat],
    command: Term.Apply): Defn.Def = {

    val (isReturnResult, entityType) = DaoMacroHelper.getResultType(defDecl)
    val result = if (isReturnResult) {
      q"new domala.jdbc.Result[$entityType](__count, __query.getEntity)"
    } else {
      q"__count"
    }

    q"""
    override def ${defDecl.name} = {
      domala.internal.macros.reflect.DaoReflectionMacros.validateAutoModifyParam(${defDecl.trtName.syntax}, ${defDecl.name.syntax}, classOf[$paramType])
      entering(${defDecl.trtName.syntax}, ${defDecl.name.syntax}, $paramName)
      try {
        if ($paramName == null) {
          throw new org.seasar.doma.DomaNullPointerException(${paramName.syntax})
        }
        val __query = $query
        __query.setMethod($internalMethodName)
        __query.setConfig(___config)
        __query.setEntity($paramName)
        __query.setCallerClassName(${defDecl.trtName.syntax})
        __query.setCallerMethodName(${defDecl.name.syntax})
        __query.setQueryTimeout(${commonSetting.queryTimeOut})
        __query.setSqlLogType(${commonSetting.sqlLogType})
        ..$otherQuerySettings
        __query.prepare()
        val __command = $command
        val __count = __command.execute()
        __query.complete()
        val __result = $result
        exiting(${defDecl.trtName.syntax}, ${defDecl.name.syntax}, __result)
        __result
      } catch {
        case __e: java.lang.RuntimeException => {
          throwing(${defDecl.trtName.syntax}, ${defDecl.name.syntax}, __e)
          throw __e
        }
      }
    }
    """
  }
}
