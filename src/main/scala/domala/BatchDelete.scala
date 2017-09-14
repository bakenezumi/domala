package domala

import org.seasar.doma.jdbc.SqlLogType

import scala.meta._

class BatchDelete(
  sql: String = "",
  queryTimeOut: Int = -1,
  batchSize: Int = -1,
  ignoreVersion: Boolean = false,
  suppressOptimisticLockException: Boolean = false,
  sqlLog: SqlLogType = SqlLogType.FORMATTED
) extends scala.annotation.StaticAnnotation

package internal { package macros {

  import scala.collection.immutable.Seq

  object BatchDeleteGenerator {
    def generate(trtName: Type.Name, _def: Decl.Def, internalMethodName: Term.Name, args: Seq[Term.Arg]): Defn.Def = {
      val commonSetting = DaoMethodMacroHelper.readCommonBatchSetting(args)
      val ignoreVersion = args.collectFirst { case arg"ignoreVersion = $x" => x }.getOrElse(q"false")
      val suppressOptimisticLockException = args.collectFirst { case arg"suppressOptimisticLockException = $x" => x }.getOrElse(q"false")
      val defDecl = QueryDefDecl.of(trtName, _def)
      val (paramName, paramTpe) = AutoBatchModifyQueryGenerator.extractParameter(defDecl)
      val query = q"getQueryImplementors.createAutoBatchDeleteQuery($internalMethodName, ${Term.Name(paramTpe.syntax)})"
      val command = q"getCommandImplementors.createBatchDeleteCommand($internalMethodName, __query)"
      val otherQuerySettings = Seq[Stat](
        q"__query.setVersionIgnored($ignoreVersion)",
        q"__query.setOptimisticLockExceptionSuppressed($suppressOptimisticLockException)"
      )
      AutoBatchModifyQueryGenerator.generate(defDecl, commonSetting, paramName, paramTpe, internalMethodName, query, otherQuerySettings, command)
    }
  }
}}