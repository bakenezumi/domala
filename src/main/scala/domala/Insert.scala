package domala

import org.seasar.doma.jdbc.SqlLogType

import scala.meta._

class Insert(
  sql: String = "",
  queryTimeOut: Int = -1,
  excludeNull: Boolean = false,
  include: collection.Seq[String] = Nil,
  exclude: collection.Seq[String] = Nil,
  sqlLog: SqlLogType = SqlLogType.FORMATTED
) extends scala.annotation.StaticAnnotation

package internal { package macros {

  import scala.collection.immutable.Seq

  object InsertGenerator {
    def generate(trtName: Type.Name, _def: Decl.Def, internalMethodName: Term.Name, args: Seq[Term.Arg]): Defn.Def = {
      val commonSetting = DaoMethodMacroHelper.readCommonSetting(args)
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
      val defDecl = QueryDefDecl.of(trtName, _def)
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
}}