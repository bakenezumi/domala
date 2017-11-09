package domala.internal.macros

import domala.internal.macros.helper.DaoMacroHelper
import org.seasar.doma.BatchUpdate

import scala.collection.immutable.Seq
import scala.meta._

object BatchUpdateGenerator extends DaoMethodGenerator {
  override def annotationClass: Class[BatchUpdate] = classOf[BatchUpdate]
  override def generate(
    trtName: Type.Name,
    _def: Decl.Def,
    internalMethodName: Term.Name,
    args: Seq[Term.Arg]): Defn.Def = {
    val defDecl = QueryDefDecl.of(trtName, _def)
    val commonSetting = DaoMacroHelper.readCommonBatchSetting(
      args,
      trtName.syntax,
      _def.name.syntax)
    val ignoreVersion = args
      .collectFirst { case arg"ignoreVersion = $x" => x }
      .getOrElse(q"false")
    val suppressOptimisticLockException = args
      .collectFirst { case arg"suppressOptimisticLockException = $x" => x }
      .getOrElse(q"false")
    if (commonSetting.hasSql) {
      val (paramName, paramTpe, internalTpe) = AutoBatchModifyQueryGenerator.extractParameter(defDecl)
      val query: Term => Term.New = (entityType) => q"new domala.jdbc.query.SqlAnnotationBatchUpdateQuery(classOf[$internalTpe], ${commonSetting.sql}, $ignoreVersion, $suppressOptimisticLockException)($entityType)"
      val otherQuerySettings = Seq[Stat]()
      val command = q"getCommandImplementors.createBatchUpdateCommand($internalMethodName, __query)"
      SqlBatchModifyQueryGenerator.generate(defDecl, commonSetting, paramName, paramTpe, internalTpe, internalMethodName, query, otherQuerySettings, command, q"true")
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
        q"getQueryImplementors.createAutoBatchUpdateQuery($internalMethodName, ${
          Term
            .Name(internalTpe.syntax)
        })"
      val command =
        q"getCommandImplementors.createBatchUpdateCommand($internalMethodName, __query)"
      val otherQuerySettings = Seq[Stat](
        q"__query.setVersionIgnored($ignoreVersion)",
        q"__query.setIncludedPropertyNames(..$includedPropertyNames)",
        q"__query.setExcludedPropertyNames(..$excludedPropertyNames)",
        q"__query.setOptimisticLockExceptionSuppressed($suppressOptimisticLockException)"
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
