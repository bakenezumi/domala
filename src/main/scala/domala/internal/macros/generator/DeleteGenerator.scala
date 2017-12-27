package domala.internal.macros.generator

import domala.Delete
import domala.internal.macros.args.DaoMethodCommonArgs
import domala.internal.macros.QueryDefDecl

import scala.collection.immutable.Seq
import scala.meta._

object DeleteGenerator extends DaoMethodGenerator {
  override def annotationClass: Class[Delete] = classOf[Delete]
  override def generate(
    trtName: Type.Name,
    _def: Decl.Def,
    internalMethodName: Term.Name,
    args: Seq[Term.Arg]): Defn.Def = {
    val defDecl = QueryDefDecl.of(trtName, _def)
    val commonArgs =
      DaoMethodCommonArgs.read(args, trtName.syntax, _def.name.syntax)
    val ignoreVersion = args
      .collectFirst { case arg"ignoreVersion = $x" => x }
      .getOrElse(q"false")
    val suppressOptimisticLockException = args
      .collectFirst { case arg"suppressOptimisticLockException = $x" => x }
      .getOrElse(q"false")

    if (commonArgs.hasSqlAnnotation || commonArgs.sqlFile) {
      val query: (Term, Option[Term]) => Term.New =
        if(commonArgs.hasSqlAnnotation) (entityAndEntityType, _) =>
          q"new domala.jdbc.query.SqlAnnotationDeleteQuery(${commonArgs.sql}, $ignoreVersion, $suppressOptimisticLockException)($entityAndEntityType)"
        else (entityAndEntityType, path) =>
          q"new domala.jdbc.query.SqlFileDeleteQuery(${path.get}, $ignoreVersion, $suppressOptimisticLockException)($entityAndEntityType)"
      val otherQueryArgs = Seq[Stat]()
      val command =
        q"getCommandImplementors.createDeleteCommand($internalMethodName, __query)"
      SqlModifyQueryGenerator.generate(
        defDecl,
        commonArgs,
        internalMethodName,
        query,
        otherQueryArgs,
        command,
        q"false")
    } else {
      val (paramName, paramTpe) =
        AutoModifyQueryGenerator.extractParameter(defDecl)
      val query =
        q"getQueryImplementors.createAutoDeleteQuery($internalMethodName, ${Term
          .Name(paramTpe.syntax)})"
      val command =
        q"getCommandImplementors.createDeleteCommand($internalMethodName, __query)"
      val otherQueryArgs = Seq[Stat](
        q"__query.setVersionIgnored($ignoreVersion)",
        q"__query.setOptimisticLockExceptionSuppressed($suppressOptimisticLockException)"
      )
      AutoModifyQueryGenerator.generate(
        defDecl,
        commonArgs,
        paramName,
        paramTpe,
        internalMethodName,
        query,
        otherQueryArgs,
        command)
    }
  }
}
