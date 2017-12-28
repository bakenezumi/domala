package domala.internal.macros.generator

import domala.Insert
import domala.internal.macros.args.DaoMethodCommonArgs
import domala.internal.macros.QueryDefDecl

import scala.collection.immutable.Seq
import scala.meta._

object InsertGenerator extends DaoMethodGenerator {
  override def annotationClass: Class[Insert] = classOf[Insert]
  override def generate(trtName: Type.Name, _def: Decl.Def, internalMethodName: Term.Name, args: Seq[Term.Arg]): Defn.Def = {
    val defDecl = QueryDefDecl.of(trtName, _def)
    val commonArgs = DaoMethodCommonArgs.read(args, trtName.syntax, _def.name.syntax)
    val excludeNull = args.collectFirst { case arg"excludeNull = $x" => x }.getOrElse(q"false")
    val include = args.collectFirst { case arg"include = $x" => Some(x) }.flatten
    val exclude = args.collectFirst { case arg"exclude = $x" => Some(x) }.flatten

    if (commonArgs.hasSqlAnnotation || commonArgs.sqlFile) {
      val query: (Term, Option[Term]) => Term.New =
        if(commonArgs.hasSqlAnnotation) (entityAndEntityType, _) =>
          q"new domala.jdbc.query.SqlAnnotationInsertQuery(${commonArgs.sql})($entityAndEntityType)"
        else (entityAndEntityType, path) =>
          q"new domala.jdbc.query.SqlFileInsertQuery(${path.get})($entityAndEntityType)"

      val otherQuerySettings = Seq[Stat]()
      val command = q"getCommandImplementors.createInsertCommand($internalMethodName, __query)"
      SqlModifyQueryGenerator.generate(defDecl, commonArgs, internalMethodName, query, otherQuerySettings, command, q"false")
    } else {
      val includedPropertyNames = include match {
        case Some(x: Term.Apply) => x.args
        case _ => Nil
      }
      val excludedPropertyNames = exclude match {
        case Some(x: Term.Apply) => x.args
        case _ => Nil
      }
      val (paramName, paramTpe) = AutoModifyQueryGenerator.extractParameter(defDecl)
      val query = q"getQueryImplementors.createAutoInsertQuery($internalMethodName, ${Term.Name(paramTpe.syntax)})"
      val command = q"getCommandImplementors.createInsertCommand($internalMethodName, __query)"
      val validateEntityPropertyNames = DaoMethodGeneratorHelper.validateEntityPropertyNames(defDecl, paramTpe, includedPropertyNames, excludedPropertyNames)
      val otherQueryArgs = validateEntityPropertyNames ++ Seq[Stat](
        q"__query.setNullExcluded($excludeNull)",
        q"__query.setIncludedPropertyNames(..$includedPropertyNames)",
        q"__query.setExcludedPropertyNames(..$excludedPropertyNames)"
      )
      AutoModifyQueryGenerator.generate(defDecl, commonArgs, paramName, paramTpe, internalMethodName, query, otherQueryArgs, command)
    }
  }
}
