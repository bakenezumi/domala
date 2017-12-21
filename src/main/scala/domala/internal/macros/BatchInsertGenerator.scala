package domala.internal.macros

import domala.internal.macros.args.DaoMethodCommonBatchArgs
import org.seasar.doma.BatchInsert

import scala.collection.immutable.Seq
import scala.meta._

object BatchInsertGenerator extends DaoMethodGenerator {
  override def annotationClass: Class[BatchInsert] = classOf[BatchInsert]
  override def generate(trtName: Type.Name,
               _def: Decl.Def,
               internalMethodName: Term.Name,
               args: Seq[Term.Arg]): Defn.Def = {
    val defDecl = QueryDefDecl.of(trtName, _def)
    val commonArgs = DaoMethodCommonBatchArgs.read(
      args,
      trtName.syntax,
      _def.name.syntax)
    if (commonArgs.hasSqlAnnotation || commonArgs.sqlFile) {
      val (paramName, paramTpe, internalTpe) = AutoBatchModifyQueryGenerator.extractParameter(defDecl)
      val query: (Term, Option[Term]) => Term.New =
        if(commonArgs.hasSqlAnnotation) (entityType, _) => q"new domala.jdbc.query.SqlAnnotationBatchInsertQuery(classOf[$internalTpe], ${commonArgs.sql})($entityType)"
        else (entityType, path) => q"new domala.jdbc.query.SqlFileBatchInsertQuery(classOf[$internalTpe], ${path.get})($entityType)"
      val otherQueryArgs = Seq[Stat]()
      val command = q"getCommandImplementors.createBatchInsertCommand($internalMethodName, __query)"
      SqlBatchModifyQueryGenerator.generate(defDecl, commonArgs, paramName, paramTpe, internalTpe, internalMethodName, query, otherQueryArgs, command, q"false")
    } else {
      val include =
        args.collectFirst { case arg"include = $x" => Some(x) }.flatten
      val exclude =
        args.collectFirst { case arg"exclude = $x" => Some(x) }.flatten
      val includedPropertyNames = include match {
        case Some(x: Term.Apply) => x.args
        case _ => Nil
      }
      val excludedPropertyNames = exclude match {
        case Some(x: Term.Apply) => x.args
        case _ => Nil
      }
      val (paramName, paramTpe, internalTpe) =
        AutoBatchModifyQueryGenerator.extractParameter(defDecl)

      val query =
        q"getQueryImplementors.createAutoBatchInsertQuery($internalMethodName, ${
          Term
            .Name(internalTpe.syntax)
        })"
      val command =
        q"getCommandImplementors.createBatchInsertCommand($internalMethodName, __query)"
      val otherQueryArgs = Seq[Stat](
        q"__query.setIncludedPropertyNames(..$includedPropertyNames)",
        q"__query.setExcludedPropertyNames(..$excludedPropertyNames)"
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
