package domala.internal.macros.generator

import domala.Update
import domala.internal.macros.QueryDefDecl
import domala.internal.macros.args.DaoMethodCommonArgs

import scala.collection.immutable.Seq
import scala.meta._

object UpdateGenerator extends DaoMethodGenerator {
  override def annotationClass: Class[Update] = classOf[Update]
  override def generate(trtName: Type.Name, _def: Decl.Def, internalMethodName: Term.Name, args: Seq[Term.Arg]): Defn.Def = {
    val defDecl = QueryDefDecl.of(trtName, _def)
    val commonArgs = DaoMethodCommonArgs.read(args, trtName.syntax, _def.name.syntax)
    val excludeNull = args.collectFirst { case arg"excludeNull = $x" => x }.getOrElse(q"false")
    val ignoreVersion = args.collectFirst { case arg"ignoreVersion = $x" => x }.getOrElse(q"false")
    val include = args.collectFirst { case arg"include = $x" => Some(x) }.flatten
    val exclude = args.collectFirst { case arg"exclude = $x" => Some(x) }.flatten
    val includedPropertyNames = include match {
      case Some(x: Term.Apply) => x.args
      case _ => Nil
    }
    val excludedPropertyNames = exclude match {
      case Some(x: Term.Apply) => x.args
      case _ => Nil
    }
    val suppressOptimisticLockException = args.collectFirst { case arg"suppressOptimisticLockException = $x" => x }.getOrElse(q"false")

    if (commonArgs.hasSqlAnnotation || commonArgs.sqlFile) {
      val query: (Term, Option[Term]) => Term.New =
        if(commonArgs.hasSqlAnnotation) (entityAndEntityType, _) =>
          q"""new domala.jdbc.query.SqlAnnotationUpdateQuery(
            ${commonArgs.sql},
            $excludeNull,
            $ignoreVersion,
            $suppressOptimisticLockException,
            Seq(..$includedPropertyNames).toArray,
            Seq(..$excludedPropertyNames).toArray)($entityAndEntityType)
          """
        else (entityAndEntityType, path) =>
          q"""new domala.jdbc.query.SqlFileUpdateQuery(
            ${path.get},
            $excludeNull,
            $ignoreVersion,
            $suppressOptimisticLockException,
            Seq(..$includedPropertyNames).toArray,
            Seq(..$excludedPropertyNames).toArray)($entityAndEntityType)
          """
      val otherQueryArgs = Seq[Stat]()
      val command = q"getCommandImplementors.createUpdateCommand($internalMethodName, __query)"
      SqlModifyQueryGenerator.generate(defDecl, commonArgs, internalMethodName, query, otherQueryArgs, command, q"true")

    } else {
      val (paramName, paramTpe) = AutoModifyQueryGenerator.extractParameter(defDecl)
      val query = q"getQueryImplementors.createAutoUpdateQuery($internalMethodName, ${Term.Name(paramTpe.syntax)})"
      val command = q"getCommandImplementors.createUpdateCommand($internalMethodName, __query)"
      val validateEntityPropertyNames = DaoMethodGeneratorHelper.validateEntityPropertyNames(defDecl, paramTpe, includedPropertyNames, excludedPropertyNames)
      val otherQueryArgs = validateEntityPropertyNames ++ Seq[Stat](
        q"__query.setNullExcluded($excludeNull)",
        q"__query.setVersionIgnored($ignoreVersion)",
        q"__query.setIncludedPropertyNames(..$includedPropertyNames)",
        q"__query.setExcludedPropertyNames(..$excludedPropertyNames)",
        q"__query.setUnchangedPropertyIncluded(false)", //TODO: 未実装
        q"__query.setOptimisticLockExceptionSuppressed($suppressOptimisticLockException)"
      )
      AutoModifyQueryGenerator.generate(defDecl, commonArgs, paramName, paramTpe, internalMethodName, query, otherQueryArgs, command)
    }
  }
}
