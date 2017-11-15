package domala.internal.macros

import domala.internal.macros.helper.LiteralConverters._
import domala.internal.macros.helper.{DaoMacroHelper, MacrosHelper}
import domala.message.Message

import scala.collection.immutable.Seq
import scala.meta._

object AutoBatchModifyQueryGenerator {
  def extractParameter(defDecl: QueryDefDecl): (Term.Name, Type.Name, Type.Name) = {
    if (defDecl.paramss.flatten.length != 1)
      MacrosHelper.abort(Message.DOMALA4002,
              defDecl.trtName.value, defDecl.name.value)
    defDecl.paramss.flatten.head match {
      case param"$paramName: ${paramTpe}" =>
        paramTpe match {
          case Some(t"$tpe[$internalTpe]") =>
            (Term.Name(paramName.value), Type.Name(t"$tpe[$internalTpe]".syntax), Type.Name(internalTpe.syntax))
          case _ =>
            MacrosHelper.abort(Message.DOMALA4042,
                defDecl.trtName.value, defDecl.name.value)
        }
    }
  }

  def generate(defDecl: QueryDefDecl,
               commonSetting: DaoMethodCommonBatchSetting,
               paramName: Term.Name,
               paramType: Type.Name,
               internalType: Type.Name,
               internalMethodName: Term.Name,
               query: Term.Apply,
               otherQuerySettings: Seq[Stat],
               command: Term.Apply): Defn.Def = {

    val (isReturnBatchResult, entityType) =
      DaoMacroHelper.getBatchResultType(defDecl)
    val result = if (isReturnBatchResult) {
      q"domala.jdbc.BatchResult[$entityType](__count, __query.getEntities.asScala)"
    } else {
      q"__count"
    }

    q"""
    override def ${defDecl.name} = {
      domala.internal.macros.reflect.DaoReflectionMacros.validateAutoBatchModifyParam(${defDecl.trtName.literal}, ${defDecl.name.literal}, classOf[$paramType], classOf[$internalType])
      entering(${defDecl.trtName.literal}, ${defDecl.name.literal}, $paramName)
      try {
        if ($paramName == null) {
          throw new org.seasar.doma.DomaNullPointerException(${paramName.literal})
        }
        val __query = $query
        __query.setMethod($internalMethodName)
        __query.setConfig(__config)
        __query.setEntities($paramName.asJava)
        __query.setCallerClassName(${defDecl.trtName.literal})
        __query.setCallerMethodName(${defDecl.name.literal})
        __query.setBatchSize(${commonSetting.batchSize})
        __query.setQueryTimeout(${commonSetting.queryTimeOut})
        __query.setSqlLogType(${commonSetting.sqlLogType})
        ..$otherQuerySettings
        __query.prepare()
        val __command = $command
        val __count = __command.execute()
        __query.complete()
        val __result = $result
        exiting(${defDecl.trtName.literal}, ${defDecl.name.literal}, __result)
        __result
      } catch {
        case __e: java.lang.RuntimeException => {
          throwing(${defDecl.trtName.literal}, ${defDecl.name.literal}, __e)
          throw __e
        }
      }
    }
    """
  }
}
