package org.seasar.doma.jdbc.builder

import domala.jdbc.query.SqlUpdateQuery
import org.seasar.doma.DomaNullPointerException
import org.seasar.doma.jdbc._
import org.seasar.doma.jdbc.command.UpdateCommand

// Domaのパッケージプライベートクラスを利用しているためここに配置
// org.seasar.doma.jdbc.builder.UpdateBuilderを元にしており、
// 独自拡張したSqlUpdateQueryを利用するように修正
class DomalaUpdateBuilder(
  val config: Config,
  val helper: BuildingHelper = new BuildingHelper(),
  query: SqlUpdateQuery = new SqlUpdateQuery(),
  paramIndex: ParamIndex = new ParamIndex()) {

  query.setConfig(config)
  query.setCallerClassName(getClass.getName)
  query.setSqlLogType(SqlLogType.FORMATTED)

  def sql(sql: String): DomalaUpdateBuilder = {
    if (sql == null) throw new DomaNullPointerException("sql")
    helper.appendSqlWithLineSeparator(sql)
    new SubsequentUpdateBuilder(config, helper, query, paramIndex)
  }

  def removeLast(): DomalaUpdateBuilder = {
    helper.removeLast()
    new SubsequentUpdateBuilder(config, helper, query, paramIndex)
  }

  def param[P](paramClass: Class[P], param: P): DomalaUpdateBuilder = {
    if (paramClass == null) throw new DomaNullPointerException("paramClass")
    appendParam(paramClass, param, literal = false)
  }

  def params[E](elementClass: Class[E], params: Iterable[E]): DomalaUpdateBuilder = {
    if (elementClass == null) throw new DomaNullPointerException("elementClass")
    if (params == null) throw new DomaNullPointerException("params")
    appendParams(elementClass, params, false)
  }

  def literal[P](paramClass: Class[P], param: P): DomalaUpdateBuilder = {
    if (paramClass == null) throw new DomaNullPointerException("paramClass")
    appendParam(paramClass, param, literal = true)
  }

  private def appendParam[P](paramClass: Class[P],
                             param: P,
                             literal: Boolean): DomalaUpdateBuilder = {
    helper.appendParam(new Param(paramClass, param, paramIndex, literal))
    paramIndex.increment()
    new SubsequentUpdateBuilder(config, helper, query, paramIndex)
  }

  private def appendParams[E](elementClass: Class[E],
    params: Iterable[E],
    literal: Boolean) = {
    var builder = this
    var index = 0
    for (param <- params) {
      builder = builder.appendParam(elementClass, param, literal).sql(", ")
      index += 1
    }
    if (index == 0) builder = builder.sql("null")
    else builder = builder.removeLast()
    builder
  }

  def execute(): Int = {
    if (query.getMethodName == null) query.setCallerMethodName("execute")
    prepare()
    val command = new UpdateCommand(query)
    val result = command.execute
    query.complete()
    result
  }

  private def prepare(): Unit = {
    query.clearParameters()
    helper.getParams.forEach { p =>
      query.addParameter(p.name, p.paramClass, p.param)
    }
    query.setSqlNode(helper.getSqlNode)
    query.prepare()
  }

  def queryTimeout(queryTimeout: Int): Unit = {
    query.setQueryTimeout(queryTimeout)
  }

  def callerClassName(className: String): Unit = {
    if (className == null) throw new DomaNullPointerException("className")
    query.setCallerClassName(className)
  }

  def sqlLogType(sqlLogType: SqlLogType): Unit = {
    if (sqlLogType == null) throw new DomaNullPointerException("sqlLogType")
    query.setSqlLogType(sqlLogType)
  }

  def callerMethodName(methodName: String): Unit = {
    if (methodName == null) throw new DomaNullPointerException("methodName")
    query.setCallerMethodName(methodName)
  }

  def getSql: Sql[_ <: SqlParameter] = {
    if (query.getMethodName == null) query.setCallerMethodName("getSql")
    prepare()
    query.getSql
  }

}

private class SubsequentUpdateBuilder(
  config: Config,
  builder: BuildingHelper,
  query: SqlUpdateQuery,
  parameterIndex: ParamIndex)
    extends DomalaUpdateBuilder(config, builder, query, parameterIndex) {
  override def sql(sql: String): DomalaUpdateBuilder = {
    if (sql == null) throw new DomaNullPointerException("sql")
    helper.appendSql(sql)
    this
  }
}

object DomalaUpdateBuilder {
  def newInstance(config: Config): DomalaUpdateBuilder = {
    if (config == null) throw new DomaNullPointerException("config")
    new DomalaUpdateBuilder(config)
  }
}
