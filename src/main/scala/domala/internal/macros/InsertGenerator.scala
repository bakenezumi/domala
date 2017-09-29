package domala.internal.macros

import scala.collection.immutable.Seq
import scala.meta._

object InsertGenerator {
  def generate(trtName: Type.Name, _def: Decl.Def, internalMethodName: Term.Name, args: Seq[Term.Arg]): Defn.Def = {
    val defDecl = QueryDefDecl.of(trtName, _def)
    val commonSetting = DaoMacroHelper.readCommonSetting(args, trtName.syntax, _def.name.syntax)
    if (commonSetting.hasSql) {
      val query: Term => Term.New = (entityAndEntityType) => q"new domala.jdbc.query.SqlAnnotationInsertQuery(${commonSetting.sql})($entityAndEntityType)"
      val otherQuerySettings = Seq[Stat]()
      val command = q"getCommandImplementors.createInsertCommand($internalMethodName, __query)"
      SqlModifyQueryGenerator.generate(defDecl, commonSetting, internalMethodName, query, otherQuerySettings, command)
    } else {
      val excludeNull = args.collectFirst { case arg"excludeNull = $x" => x }.getOrElse(q"false")
      val include = args.collectFirst { case arg"include = $x" => Some(x) }.getOrElse(None)
      val exclude = args.collectFirst { case arg"exclude = $x" => Some(x) }.getOrElse(None)
      val includedPropertyNames = include match {
        case Some(x: Term.Apply) => x.args
        case _ => Nil
      }
      val excludePropertyNames = exclude match {
        case Some(x: Term.Apply) => x.args
        case _ => Nil
      }
      val (paramName, paramTpe) = AutoModifyQueryGenerator.extractParameter(defDecl)
      val query = q"getQueryImplementors.createAutoInsertQuery($internalMethodName, ${Term.Name(paramTpe.syntax)})"
      val command = q"getCommandImplementors.createInsertCommand($internalMethodName, __query)"
      val otherQuerySettings = Seq[Stat](
        q"__query.setNullExcluded($excludeNull)",
        q"__query.setIncludedPropertyNames(..$includedPropertyNames)",
        q"__query.setExcludedPropertyNames(..$excludePropertyNames)"
      )
      AutoModifyQueryGenerator.generate(defDecl, commonSetting, paramName, paramTpe, internalMethodName, query, otherQuerySettings, command)
    }
  }
}
