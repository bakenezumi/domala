package domala.internal.macros

import scala.collection.immutable.Seq
import scala.meta._

object BatchInsertGenerator extends DaoMethodGenerator {
  override def anotationName: String = "@BatchInsert"
  override def generate(trtName: Type.Name,
               _def: Decl.Def,
               internalMethodName: Term.Name,
               args: Seq[Term.Arg]): Defn.Def = {
    val commonSetting = DaoMacroHelper.readCommonBatchSetting(
      args,
      trtName.syntax,
      _def.name.syntax)
    val include =
      args.collectFirst { case arg"include = $x" => Some(x) }.getOrElse(None)
    val exclude =
      args.collectFirst { case arg"exclude = $x" => Some(x) }.getOrElse(None)
    val includedPropertyNames = include match {
      case Some(x: Term.Apply) => x.args
      case _                   => Nil
    }
    val excludePropertyNames = exclude match {
      case Some(x: Term.Apply) => x.args
      case _                   => Nil
    }
    val defDecl = QueryDefDecl.of(trtName, _def)
    val (paramName, paramTpe, internalTpe) =
      AutoBatchModifyQueryGenerator.extractParameter(defDecl)

    val query =
      q"getQueryImplementors.createAutoBatchInsertQuery($internalMethodName, ${Term
        .Name(internalTpe.syntax)})"
    val command =
      q"getCommandImplementors.createBatchInsertCommand($internalMethodName, __query)"
    val otherQuerySettings = Seq[Stat](
      q"__query.setIncludedPropertyNames(..$includedPropertyNames)",
      q"__query.setExcludedPropertyNames(..$excludePropertyNames)"
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
