package domala

import domala.internal.macros.{AutoModifyQueryGenerator, DaoMethodMacroHelper, QueryDefDecl}
import org.seasar.doma.jdbc.SqlLogType

import scala.collection.immutable.Seq
import scala.meta._

class Delete(
  sql: String = "",
  queryTimeOut: Int = -1,
  ignoreVersion: Boolean = false,
  suppressOptimisticLockException: Boolean = false,
  sqlLog: SqlLogType = SqlLogType.FORMATTED
) extends scala.annotation.StaticAnnotation

object DeleteGenerator {
  def generate(trtName: Type.Name, _def: Decl.Def, internalMethodName: Term.Name, args: Seq[Term.Arg]): Defn.Def = {
    val commonSetting = DaoMethodMacroHelper.readCommonSetting(args)
    val ignoreVersion =  args.collectFirst{case arg"ignoreVersion = $x" => x}.getOrElse(q"false")
    val suppressOptimisticLockException =  args.collectFirst{case arg"suppressOptimisticLockException = $x" => x}.getOrElse(q"false")
    val defDecl = QueryDefDecl.of(trtName, _def)
    val (paramName, paramTpe) = AutoModifyQueryGenerator.extractParameter(defDecl)
    val query = q"getQueryImplementors.createAutoDeleteQuery($internalMethodName, ${Term.Name(paramTpe.value)})"
    val command = q"getCommandImplementors.createDeleteCommand($internalMethodName, __query)"
    val otherQuerySettings = Seq[Stat](
      q"__query.setVersionIgnored($ignoreVersion)",
      q"__query.setOptimisticLockExceptionSuppressed($suppressOptimisticLockException)"
    )
    AutoModifyQueryGenerator.generate(defDecl, commonSetting, paramName, paramTpe, internalMethodName, query, otherQuerySettings, command)

  }
}
