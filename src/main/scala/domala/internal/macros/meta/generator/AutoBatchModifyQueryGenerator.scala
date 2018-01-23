package domala.internal.macros.meta.generator

import domala.internal.macros.meta.QueryDefDecl
import domala.internal.macros.meta.args.DaoMethodCommonBatchArgs
import domala.internal.macros.meta.util.MetaHelper
import domala.internal.macros.meta.util.NameConverters._
import domala.message.Message

import scala.collection.immutable.Seq
import scala.meta._

object AutoBatchModifyQueryGenerator {
  def extractParameter(defDecl: QueryDefDecl): (Term.Name, Type.Name, Type.Name) = {
    if (defDecl.paramss.flatten.length != 1)
      MetaHelper.abort(Message.DOMALA4002,
              defDecl.trtName.value, defDecl.name.value)
    defDecl.paramss.flatten.head match {
      case param"$paramName: ${paramTpe}" =>
        paramTpe match {
          case Some(t"$tpe[$internalTpe]") =>
            (Term.Name(paramName.value), Type.Name(t"$tpe[$internalTpe]".syntax), Type.Name(internalTpe.syntax))
          case _ =>
            MetaHelper.abort(Message.DOMALA4042,
                defDecl.trtName.value, defDecl.name.value)
        }
    }
  }

  def generate(defDecl: QueryDefDecl,
               commonArgs: DaoMethodCommonBatchArgs,
               paramName: Term.Name,
               paramType: Type.Name,
               internalType: Type.Name,
               internalMethodName: Term.Name,
               query: Term.Apply,
               otherQueryArgs: Seq[Stat],
               command: Term.Apply): Defn.Def = {

    val (isReturnBatchResult, entityType) =
      DaoMethodGeneratorHelper.getBatchResultType(defDecl)
    val result = if (isReturnBatchResult) {
      q"domala.jdbc.BatchResult[$entityType](__count, __query.getEntities.asScala)"
    } else {
      q"__count"
    }

    q"""
    override def ${defDecl.name} = {
      val __desc = domala.internal.macros.reflect.DaoReflectionMacros.validateAutoBatchModifyParam(classOf[${defDecl.trtName}], ${defDecl.name.literal}, classOf[$paramType], classOf[$internalType])
      entering(${defDecl.trtName.className}, ${defDecl.name.literal}, $paramName)
      try {
        if ($paramName == null) {
          throw new org.seasar.doma.DomaNullPointerException(${paramName.literal})
        }
        val __query = $query
        __query.setMethod($internalMethodName)
        __query.setConfig(__config)
        __query.setEntities($paramName.asJava)
        __query.setCallerClassName(${defDecl.trtName.className})
        __query.setCallerMethodName(${defDecl.name.literal})
        __query.setBatchSize(${commonArgs.batchSize})
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
