package domala.internal.macros

import domala.Delete

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
    val commonSetting =
      DaoMacroHelper.readCommonSetting(args, trtName.syntax, _def.name.syntax)
    val ignoreVersion = args
      .collectFirst { case arg"ignoreVersion = $x" => x }
      .getOrElse(q"false")
    val suppressOptimisticLockException = args
      .collectFirst { case arg"suppressOptimisticLockException = $x" => x }
      .getOrElse(q"false")

    if (commonSetting.hasSql) {
      val query: Term => Term.New = (entityAndEntityType) =>
        q"new domala.jdbc.query.SqlAnnotationDeleteQuery(${commonSetting.sql}, $ignoreVersion, $suppressOptimisticLockException)($entityAndEntityType)"
      val otherQuerySettings = Seq[Stat]()
      val command =
        q"getCommandImplementors.createDeleteCommand($internalMethodName, __query)"
      SqlModifyQueryGenerator.generate(
        defDecl,
        commonSetting,
        internalMethodName,
        query,
        otherQuerySettings,
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
      val otherQuerySettings = Seq[Stat](
        q"__query.setVersionIgnored($ignoreVersion)",
        q"__query.setOptimisticLockExceptionSuppressed($suppressOptimisticLockException)"
      )
      AutoModifyQueryGenerator.generate(
        defDecl,
        commonSetting,
        paramName,
        paramTpe,
        internalMethodName,
        query,
        otherQuerySettings,
        command)
    }
  }
}
