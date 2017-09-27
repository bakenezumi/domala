package domala.internal.macros

import scala.collection.immutable.Seq
import scala.meta._

object AutoBatchModifyQueryGenerator {
  def extractParameter(defDecl: QueryDefDecl): (Term.Name, Type.Name) = {
    if (defDecl.paramss.flatten.length != 1)
      abort(defDecl._def.pos,
        org.seasar.doma.message.Message.DOMA4002
          .getMessage(defDecl.trtName.value, defDecl.name.value))
    defDecl.paramss.flatten.head match {
      case param"$paramName: ${Some(paramTpe)}" => paramTpe match {
        // TODO: _ <: Seqでないとコンパイルエラーにすべき
        case t"Seq[$internalTpe]" => (Term.Name(paramName.value), Type.Name(internalTpe.toString))
      }
    }
  }

  def generate(
    defDecl: QueryDefDecl,
    commonSetting: DaoMethodCommonBatchSetting,
    paramName: Term.Name,
    paramType: Type.Name,
    internalMethodName: Term.Name,
    query: Term.Apply,
    otherQuerySettings: Seq[Stat],
    command: Term.Apply): Defn.Def = {

    val (isReturnBatchResult, entityType) = DaoMacroHelper.getBatchResultType(defDecl)
    val result = if(isReturnBatchResult) {
      q"new domala.jdbc.BatchResult[$entityType](__count, __query.getEntities.asScala)"
    } else {
      q"__count"
    }

    q"""
    override def ${defDecl.name} = {
      entering(${defDecl.trtName.syntax}, ${defDecl.name.syntax}, $paramName)
      try {
        if ($paramName == null) {
          throw new org.seasar.doma.DomaNullPointerException(${paramName.syntax})
        }
        val __query = $query
        __query.setMethod($internalMethodName)
        __query.setConfig(__config)
        __query.setEntities($paramName.asJava)
        __query.setCallerClassName(${defDecl.trtName.syntax})
        __query.setCallerMethodName(${defDecl.name.syntax})
        __query.setBatchSize(${commonSetting.batchSize})
        __query.setQueryTimeout(${commonSetting.queryTimeout})
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
