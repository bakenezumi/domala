package domala.internal.macros

import domala.internal.macros.helper.LiteralConverters._
import domala.internal.macros.helper.DaoMacroHelper

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
        throw new org.seasar.doma.DomaNullPointerException(${p.name.literal})
      }
      """
    })

    val (isReturnResult, _) = DaoMacroHelper.getBatchResultType(defDecl)

    val daoParam =
      q"domala.internal.macros.DaoParam.apply(${paramName.literal}, $paramName, classOf[$paramType])"

    val entityTypeOption = q"""
    domala.internal.macros.reflect.DaoReflectionMacros.getBatchEntityType(${defDecl.trtName.literal}, ${defDecl.name.literal}, classOf[$internalType], $daoParam)
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

    q"""
    override def ${defDecl.name} = {
      domala.internal.macros.reflect.DaoReflectionMacros.validateBatchParameterAndSql(${defDecl.trtName.literal}, ${defDecl.name.literal}, false, $populatable, ${commonSetting.sql}, $daoParamType, ..$suppress)
      entering(${defDecl.trtName.literal}, ${defDecl.name.literal}, $paramName)
      try {
        val __query = ${query(entityTypeOption)}
        ..$checkNullParameter
        __query.setMethod($internalMethodName)
        __query.setConfig(__config)
        __query.setElements($paramName)
        __query.setParameterName(${paramName.literal})
        __query.setCallerClassName(${defDecl.trtName.literal})
        __query.setCallerMethodName(${defDecl.name.literal})
        __query.setQueryTimeout(${commonSetting.queryTimeOut})
        __query.setBatchSize(${commonSetting.batchSize})
        __query.setSqlLogType(${commonSetting.sqlLogType})
        ..$otherQuerySettings
        __query.prepare()
        val __command = $command
        val __counts = __command.execute()
        __query.complete()
        val __result = $result
        exiting(${defDecl.trtName.literal}, ${defDecl.name.literal}, __result)
        __result
      } catch {
        case __e: java.lang.RuntimeException => {
          throwing(${defDecl.trtName.literal}, ${defDecl.name.literal}, __e)
          throw __e
        }
      }
    }
    """
  }
}
