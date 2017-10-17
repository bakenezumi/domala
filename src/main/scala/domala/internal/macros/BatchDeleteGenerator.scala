package domala.internal.macros

import scala.collection.immutable.Seq
import scala.meta._

object BatchDeleteGenerator {
  def generate(
    trtName: Type.Name,
    _def: Decl.Def,
    internalMethodName: Term.Name,
    args: Seq[Term.Arg]): Defn.Def = {
    val commonSetting = DaoMacroHelper.readCommonBatchSetting(
      args,
      trtName.syntax,
      _def.name.syntax)
    val ignoreVersion = args
      .collectFirst { case arg"ignoreVersion = $x" => x }
      .getOrElse(q"false")
    val suppressOptimisticLockException = args
      .collectFirst { case arg"suppressOptimisticLockException = $x" => x }
      .getOrElse(q"false")
    val defDecl = QueryDefDecl.of(trtName, _def)
    val (paramName, paramTpe, internalTpe) =
      AutoBatchModifyQueryGenerator.extractParameter(defDecl)
    val query =
      q"getQueryImplementors.createAutoBatchDeleteQuery($internalMethodName, ${Term
        .Name(internalTpe.syntax)})"
    val command =
      q"getCommandImplementors.createBatchDeleteCommand($internalMethodName, __query)"
    val otherQuerySettings = Seq[Stat](
      q"__query.setVersionIgnored($ignoreVersion)",
      q"__query.setOptimisticLockExceptionSuppressed($suppressOptimisticLockException)"
    )
    AutoBatchModifyQueryGenerator.generate(
      defDecl,
      commonSetting,
      paramName,
      paramTpe,
      internalTpe,
      internalMethodName,
      query,
      otherQuerySettings,
      command)
  }
}
