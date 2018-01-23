package domala.internal.macros.meta.generator

import domala.BatchDelete
import domala.internal.macros.meta.QueryDefDecl
import domala.internal.macros.meta.args.DaoMethodCommonBatchArgs

import scala.collection.immutable.Seq
import scala.meta._

object BatchDeleteGenerator extends DaoMethodGenerator {
  override def annotationClass: Class[BatchDelete] = classOf[BatchDelete]
  override def generate(
    trtName: Type.Name,
    _def: Decl.Def,
    internalMethodName: Term.Name,
    args: Seq[Term.Arg]): Defn.Def = {
    val defDecl = QueryDefDecl.of(trtName, _def)
    val commonArgs = DaoMethodCommonBatchArgs.of(
      args,
      trtName.syntax,
      _def.name.syntax)
    val ignoreVersion = args
      .collectFirst { case arg"ignoreVersion = $x" => x }
      .getOrElse(q"false")
    val suppressOptimisticLockException = args
      .collectFirst { case arg"suppressOptimisticLockException = $x" => x }
      .getOrElse(q"false")
    if (commonArgs.hasSqlAnnotation || commonArgs.sqlFile) {
      val (paramName, paramTpe, internalTpe) = AutoBatchModifyQueryGenerator.extractParameter(defDecl)
      val query: (Term, Option[Term]) => Term.New =
        if (commonArgs.hasSqlAnnotation) (entityDesc, _) => q"new domala.jdbc.query.SqlAnnotationBatchDeleteQuery(classOf[$internalTpe], ${commonArgs.sql}, $ignoreVersion, $suppressOptimisticLockException)($entityDesc)"
        else  (entityDesc, path) => q"new domala.jdbc.query.SqlFileBatchDeleteQuery(classOf[$internalTpe], ${path.get}, $ignoreVersion, $suppressOptimisticLockException)($entityDesc)"
      val otherQueryArgs = Seq[Stat]()
      val command = q"getCommandImplementors.createBatchDeleteCommand($internalMethodName, __query)"
      SqlBatchModifyQueryGenerator.generate(defDecl, commonArgs, paramName, paramTpe, internalTpe, internalMethodName, query, otherQueryArgs, command, q"false")
    } else {
      val (paramName, paramTpe, internalTpe) =
        AutoBatchModifyQueryGenerator.extractParameter(defDecl)
      val query =
        q"getQueryImplementors.createAutoBatchDeleteQuery($internalMethodName, __desc)"
      val command =
        q"getCommandImplementors.createBatchDeleteCommand($internalMethodName, __query)"
      val otherQueryArgs = Seq[Stat](
        q"__query.setVersionIgnored($ignoreVersion)",
        q"__query.setOptimisticLockExceptionSuppressed($suppressOptimisticLockException)"
      )
      AutoBatchModifyQueryGenerator.generate(
        defDecl,
        commonArgs,
        paramName,
        paramTpe,
        internalTpe,
        internalMethodName,
        query,
        otherQueryArgs,
        command)
    }
  }
}
