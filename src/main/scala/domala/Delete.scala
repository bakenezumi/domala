package domala

import org.seasar.doma.jdbc.SqlLogType
import scala.meta._

class Delete(
  sql: String = "",
  queryTimeOut: Int = -1,
  ignoreVersion: Boolean = false,
  suppressOptimisticLockException: Boolean = false,
  sqlLog: SqlLogType = SqlLogType.FORMATTED
) extends scala.annotation.StaticAnnotation

package internal { package macros {

  import scala.collection.immutable.Seq

  object DeleteGenerator {
    def generate(trtName: Type.Name, _def: Decl.Def, internalMethodName: Term.Name, args: Seq[Term.Arg]): Defn.Def = {
      val defDecl = QueryDefDecl.of(trtName, _def)
      val commonSetting = DaoMacroHelper.readCommonSetting(args)
      val ignoreVersion = args.collectFirst { case arg"ignoreVersion = $x" => x }.getOrElse(q"false")
      val suppressOptimisticLockException = args.collectFirst { case arg"suppressOptimisticLockException = $x" => x }.getOrElse(q"false")
      val (paramName, paramTpe) = AutoModifyQueryGenerator.extractParameter(defDecl)
      val query = q"getQueryImplementors.createAutoDeleteQuery($internalMethodName, ${Term.Name(paramTpe.syntax)})"
      val command = q"getCommandImplementors.createDeleteCommand($internalMethodName, __query)"
      val otherQuerySettings = Seq[Stat](
        q"__query.setVersionIgnored($ignoreVersion)",
        q"__query.setOptimisticLockExceptionSuppressed($suppressOptimisticLockException)"
      )
      AutoModifyQueryGenerator.generate(defDecl, commonSetting, paramName, paramTpe, internalMethodName, query, otherQuerySettings, command)

    }
  }
}}
