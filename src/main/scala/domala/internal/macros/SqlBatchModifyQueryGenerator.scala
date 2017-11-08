package domala.internal.macros

import scala.collection.immutable.Seq
import scala.meta._

object SqlBatchModifyQueryGenerator {

  def generate(
    defDecl: QueryDefDecl,
    commonSetting: DaoMethodCommonBatchSetting,
    paramName: Term.Name,
    paramType: Type.Name,
    internalType: Type.Name,
    internalMethodName: Term.Name,
    query: Term => Term.New,
    otherQuerySettings: Seq[Stat],
    command: Term.Apply,
    populatable: Term
  ): Defn.Def = {
    val params = defDecl.paramss.flatten

    val checkNullParameter = params.map(p => {
      q"""
      if (${Term.Name(p.name.syntax)} == null) {
        throw new org.seasar.doma.DomaNullPointerException(${p.name.syntax})
      }
      """
    })

    val (isReturnResult, _) = DaoMacroHelper.getBatchResultType(defDecl)

    val daoParam =
      q"domala.internal.macros.DaoParam.apply(${paramName.syntax}, $paramName, classOf[$paramType])"

    val entityTypeOption = q"""
    domala.internal.macros.reflect.DaoReflectionMacros.getBatchEntityType(${defDecl.trtName.syntax}, ${defDecl.name.syntax}, classOf[$internalType], $daoParam)
    """
    val result = if(isReturnResult) {
      q"new domala.jdbc.BatchResult(__counts, __query.getEntities.asScala)"
    } else {
      q"__counts"
    }

    val daoParamType =
      q"domala.internal.macros.DaoParamClass.apply(${paramName.syntax}, classOf[$internalType])"

    //noinspection ScalaUnusedSymbol
    val suppress = defDecl._def.mods.collectFirst {
      case mod"@Suppress(messages=$_(..$x))" => x.map(xx =>  arg"${xx.toString()}")
//      case mod"@Suppress($x)" => arg"$x.map(_.name)"
    }.getOrElse(Nil)

    q"""
    override def ${defDecl.name} = {
      domala.internal.macros.reflect.DaoReflectionMacros.validateBatchParameterAndSql(${defDecl.trtName.syntax}, ${defDecl.name.syntax}, false, $populatable, ${commonSetting.sql}, $daoParamType, ..$suppress)
      entering(${defDecl.trtName.syntax}, ${defDecl.name.syntax}, $paramName)
      try {
        val __query = ${query(entityTypeOption)}
        ..$checkNullParameter
        __query.setMethod($internalMethodName)
        __query.setConfig(___config)
        __query.setElements($paramName)
        __query.setParameterName(${paramName.syntax})
        __query.setCallerClassName(${defDecl.trtName.syntax})
        __query.setCallerMethodName(${defDecl.name.syntax})
        __query.setQueryTimeout(${commonSetting.queryTimeOut})
        __query.setBatchSize(${commonSetting.batchSize})
        __query.setSqlLogType(${commonSetting.sqlLogType})
        ..$otherQuerySettings
        __query.prepare()
        val __command = $command
        val __counts = __command.execute()
        __query.complete()
        val __result = $result
        exiting(${defDecl.trtName.syntax}, ${defDecl.name.syntax}, __result)
        __result
      } catch {
        case __e: java.lang.RuntimeException => {
          throwing(${defDecl.trtName.syntax}, ${defDecl.name.syntax}, __e)
          throw __e
        }
      }
    }
    """
  }
}
