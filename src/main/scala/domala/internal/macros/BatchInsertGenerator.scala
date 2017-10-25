package domala.internal.macros

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
    val commonSetting = DaoMacroHelper.readCommonBatchSetting(
      args,
      trtName.syntax,
      _def.name.syntax)
    if (commonSetting.hasSql) {
      val (paramName, paramTpe, internalTpe) = AutoBatchModifyQueryGenerator.extractParameter(defDecl)
      val query: Term => Term.New = (entityType) => q"new domala.jdbc.query.SqlAnnotationBatchInsertQuery(classOf[$internalTpe], ${commonSetting.sql})($entityType)"
      val otherQuerySettings = Seq[Stat]()
      val command = q"getCommandImplementors.createBatchInsertCommand($internalMethodName, __query)"
      SqlBatchModifyQueryGenerator.generate(defDecl, commonSetting, paramName, paramTpe, internalTpe, internalMethodName, query, otherQuerySettings, command, q"false")
    } else {
      val include =
        args.collectFirst { case arg"include = $x" => Some(x) }.getOrElse(None)
      val exclude =
        args.collectFirst { case arg"exclude = $x" => Some(x) }.getOrElse(None)
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
      val otherQuerySettings = Seq[Stat](
        q"__query.setIncludedPropertyNames(..$includedPropertyNames)",
        q"__query.setExcludedPropertyNames(..$excludedPropertyNames)"
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
}
