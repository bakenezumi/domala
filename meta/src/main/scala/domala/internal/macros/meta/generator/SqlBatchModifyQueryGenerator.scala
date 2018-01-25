package domala.internal.macros.meta.generator

import domala.internal.macros.meta.QueryDefDecl
import domala.internal.macros.meta.args.DaoMethodCommonBatchArgs
import domala.internal.macros.meta.util.NameConverters._

import scala.collection.immutable.Seq
import scala.meta._

object SqlBatchModifyQueryGenerator {

  def generate(
    defDecl: QueryDefDecl,
    commonArgs: DaoMethodCommonBatchArgs,
    paramName: Term.Name,
    paramType: Type.Name,
    internalType: Type.Name,
    internalMethodName: Term.Name,
    queryTemplate: (Term, Option[Term]) => Term.New,
    otherQueryArgs: Seq[Stat],
    command: Term.Apply,
    populatable: Term
  ): Defn.Def = {
    val params = defDecl.paramss.flatten

    val checkNullParameter = params.map(p => {
      q"""
      if (${Term.Name(p.name.syntax)} == null) {
        throw new org.seasar.doma.DomaNullPointerException(${p.name.literal})
      }
      """
    })

    val (isReturnResult, _) = DaoMethodGeneratorHelper.getBatchResultType(defDecl)

    val daoParam =
      q"domala.internal.macros.DaoParam.apply(${paramName.literal}, $paramName, classOf[$paramType])"

    val entityTypeOption = q"""
    domala.internal.macros.reflect.DaoReflectionMacros.getBatchEntityDesc(classOf[${defDecl.trtName}], ${defDecl.name.literal}, classOf[${defDecl._def.decltpe}], $daoParam)
    """
    val result = if(isReturnResult) {
      q"domala.jdbc.BatchResult(__counts, __query.getEntities.asScala)"
    } else {
      q"__counts"
    }

    val daoParamType =
      q"domala.internal.macros.DaoParamClass.apply(${paramName.literal}, classOf[$internalType])"

    //noinspection ScalaUnusedSymbol
    val suppress = defDecl._def.mods.collectFirst {
      case mod"@Suppress(messages=$_(..$x))" => x.map(xx => {arg"${Term.Name("\"" + xx.syntax + "\"")}" })
//      case mod"@Suppress($x)" => arg"$x.map(_.name)"
    }.getOrElse(Nil)

    val sqlValidator =
      if (commonArgs.hasSqlAnnotation) {
        q"domala.internal.macros.reflect.DaoReflectionMacros.validateBatchParameterAndSql(classOf[${defDecl.trtName}], ${defDecl.name.literal}, false, $populatable, ${commonArgs.sql}, $daoParamType, ..$suppress)"
      } else q"()"

    val query =
      if (commonArgs.hasSqlAnnotation) queryTemplate(entityTypeOption, None)
      else queryTemplate(entityTypeOption, Some(q"domala.internal.macros.reflect.DaoReflectionMacros.getSqlFilePath(classOf[${defDecl.trtName}], ${defDecl.name.literal}, false, $populatable, false, $daoParamType)"))

    q"""
    override def ${defDecl.name} = {
      $sqlValidator
      entering(${defDecl.trtName.className}, ${defDecl.name.literal}, $paramName)
      try {
        val __query = $query
        ..$checkNullParameter
        __query.setMethod($internalMethodName)
        __query.setConfig(__config)
        __query.setElements($paramName)
        __query.setParameterName(${paramName.literal})
        __query.setCallerClassName(${defDecl.trtName.className})
        __query.setCallerMethodName(${defDecl.name.literal})
        __query.setQueryTimeout(${commonArgs.queryTimeOut})
        __query.setBatchSize(${commonArgs.batchSize})
        __query.setSqlLogType(${commonArgs.sqlLogType})
        ..$otherQueryArgs
        __query.prepare()
        val __command = $command
        val __counts = __command.execute()
        __query.complete()
        val __result = $result
        exiting(${defDecl.trtName.className}, ${defDecl.name.literal}, __result)
        __result
      } catch {
        case __e: java.lang.RuntimeException => {
          throwing(${defDecl.trtName.className}, ${defDecl.name.literal}, __e)
          throw __e
        }
      }
    }
    """
  }
}
