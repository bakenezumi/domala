package domala.internal.macros.generator

import domala.BatchDelete
import domala.internal.macros.args.DaoMethodCommonBatchArgs
import domala.internal.macros.QueryDefDecl

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
        if (commonArgs.hasSqlAnnotation) (entityType, _) => q"new domala.jdbc.query.SqlAnnotationBatchDeleteQuery(classOf[$internalTpe], ${commonArgs.sql}, $ignoreVersion, $suppressOptimisticLockException)($entityType)"
        else  (entityType, path) => q"new domala.jdbc.query.SqlFileBatchDeleteQuery(classOf[$internalTpe], ${path.get}, $ignoreVersion, $suppressOptimisticLockException)($entityType)"
      val otherQueryArgs = Seq[Stat]()
      val command = q"getCommandImplementors.createBatchDeleteCommand($internalMethodName, __query)"
      SqlBatchModifyQueryGenerator.generate(defDecl, commonArgs, paramName, paramTpe, internalTpe, internalMethodName, query, otherQueryArgs, command, q"false")
    } else {
      val (paramName, paramTpe, internalTpe) =
        AutoBatchModifyQueryGenerator.extractParameter(defDecl)
      val query =
        q"getQueryImplementors.createAutoBatchDeleteQuery($internalMethodName, ${
          Term
            .Name(internalTpe.syntax)
        }.entityDesc)"
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
