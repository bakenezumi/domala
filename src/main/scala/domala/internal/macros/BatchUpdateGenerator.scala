package domala.internal.macros

import scala.collection.immutable.Seq
import scala.meta._

object BatchUpdateGenerator {
  def generate(trtName: Type.Name, _def: Decl.Def, internalMethodName: Term.Name, args: Seq[Term.Arg]): Defn.Def = {
    val commonSetting = DaoMacroHelper.readCommonBatchSetting(args, trtName.syntax, _def.name.syntax)
    val ignoreVersion = args.collectFirst { case arg"ignoreVersion = $x" => x }.getOrElse(q"false")
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
    val suppressOptimisticLockException = args.collectFirst { case arg"suppressOptimisticLockException = $x" => x }.getOrElse(q"false")
    val defDecl = QueryDefDecl.of(trtName, _def)
    val (paramName, paramTpe) = AutoBatchModifyQueryGenerator.extractParameter(defDecl)
    val query = q"getQueryImplementors.createAutoBatchUpdateQuery($internalMethodName, ${Term.Name(paramTpe.syntax)})"
    val command = q"getCommandImplementors.createBatchUpdateCommand($internalMethodName, __query)"
    val otherQuerySettings = Seq[Stat](
      q"__query.setVersionIgnored($ignoreVersion)",
      q"__query.setIncludedPropertyNames(..$includedPropertyNames)",
      q"__query.setExcludedPropertyNames(..$excludePropertyNames)",
      q"__query.setOptimisticLockExceptionSuppressed($suppressOptimisticLockException)"
    )
    AutoBatchModifyQueryGenerator.generate(defDecl, commonSetting, paramName, paramTpe, internalMethodName, query, otherQuerySettings, command)
  }
}
