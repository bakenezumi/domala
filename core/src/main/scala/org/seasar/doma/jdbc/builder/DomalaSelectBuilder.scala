package org.seasar.doma.jdbc.builder

import java.util.function.{Function, Supplier}
import java.util.stream

import domala.internal.jdbc.scalar.Scalars
import domala.internal.{OptionConverters, WrapIterator}
import domala.jdbc.{Config, SelectOptions}
import domala.jdbc.entity.EntityDesc
import domala.jdbc.query.SqlSelectQuery
import org.seasar.doma.internal.jdbc.command._
import org.seasar.doma.internal.jdbc.scalar.{Scalar, ScalarException}
import org.seasar.doma.jdbc.command.{ResultSetHandler, SelectCommand}
import org.seasar.doma.jdbc.{ClassHelper, Sql, SqlLogType}
import org.seasar.doma.message.Message
import org.seasar.doma.{DomaIllegalArgumentException, DomaNullPointerException, FetchType, MapKeyNamingType}

import scala.collection.JavaConverters._
import scala.language.experimental.macros
import scala.reflect._

// Domaのパッケージプライベートクラスを利用しているためここに配置
// org.seasar.doma.jdbc.builder.SelectBuilderを元にしており、
// 下記修正をしている
// - 独自拡張したSqlSelectQueryを利用する
// - getXXにてOption, Seq, IteratorなどScalaの標準コレクションを返す
class DomalaSelectBuilder(
    val config: Config,
    val helper: BuildingHelper = new BuildingHelper(),
    query: SqlSelectQuery = new SqlSelectQuery,
    paramIndex: ParamIndex = new ParamIndex()) {
  implicit val classHelper: ClassHelper = config.getClassHelper

  query.setConfig(config)
  query.setCallerClassName(getClass.getName)
  query.setFetchType(FetchType.LAZY)
  query.setSqlLogType(SqlLogType.FORMATTED)

  def sql(sql: String): DomalaSelectBuilder = {
    if (sql == null) throw new DomaNullPointerException("sql")
    helper.appendSqlWithLineSeparator(sql)
    new SubsequentSelectBuilder(config, helper, query, paramIndex)
  }

  def removeLast(): DomalaSelectBuilder = {
    helper.removeLast()
    new SubsequentSelectBuilder(config, helper, query, paramIndex)
  }

  def param[P](paramClass: Class[P], param: P): DomalaSelectBuilder = {
    if (paramClass == null) throw new DomaNullPointerException("paramClass")
    appendParam(paramClass, param, false)
  }

  def params[E](elementClass: Class[E], params: Iterable[E]): DomalaSelectBuilder = {
    if (elementClass == null) throw new DomaNullPointerException("elementClass")
    if (params == null) throw new DomaNullPointerException("params")
    appendParams(elementClass, params, false)
  }

  def literal[P](paramClass: Class[P], param: P): DomalaSelectBuilder = {
    if (paramClass == null) throw new DomaNullPointerException("paramClass")
    appendParam(paramClass, param, true)
  }

  def literals[E](elementClass: Class[E], params: Seq[E]): DomalaSelectBuilder = {
    if (elementClass == null) throw new DomaNullPointerException("elementClass")
    if (params == null) throw new DomaNullPointerException("params")
    appendParams(elementClass, params, true)
  }

  private def appendParam[P](paramClass: Class[P],
                             param: P,
                             literal: Boolean) = {
    helper.appendParam(new Param(paramClass, param, paramIndex, literal))
    paramIndex.increment()
    new SubsequentSelectBuilder(config, helper, query, paramIndex)
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

  def getEntitySingleResult[RESULT](implicit entityDesc: EntityDesc[RESULT]): RESULT = {
    if (query.getMethodName == null)
      query.setCallerMethodName("getEntitySingleResult")
    query.setEntityType(entityDesc)
    val handler = new EntitySingleResultHandler[RESULT](entityDesc)
    execute(handler)
  }

  def getOptionEntitySingleResult[RESULT](implicit entityDesc: EntityDesc[RESULT]): Option[RESULT] = {
    if (query.getMethodName == null)
      query.setCallerMethodName("getOptionalEntitySingleResult")
    query.setEntityType(entityDesc)
    val handler = new OptionalEntitySingleResultHandler[RESULT](entityDesc)
    OptionConverters.asScala(execute(handler))
  }

  @SuppressWarnings(Array("unchecked", "rawtypes"))
  def getScalarSingleResult[RESULT](implicit cTag: ClassTag[RESULT]): RESULT = {
    if (query.getMethodName == null)
      query.setCallerMethodName("getScalarSingleResult")
    val supplier = createScalarSupplier("resultClass", cTag.runtimeClass, false).asInstanceOf[Supplier[Scalar[Any, RESULT]]]
    val handler = new ScalarSingleResultHandler(supplier)
    execute(handler)
  }

  @SuppressWarnings(Array("unchecked", "rawtypes"))
  def getOptionScalarSingleResult[RESULT](implicit cTag: ClassTag[RESULT]): Option[RESULT] = {
    if (query.getMethodName == null)
      query.setCallerMethodName("getOptionScalarSingleResult")
    val supplier = createScalarSupplier("resultClass", cTag.runtimeClass, true).asInstanceOf[Supplier[Scalar[Any, RESULT]]]
    val handler = new ScalarSingleResultHandler(supplier)
    execute(handler).asInstanceOf[Option[RESULT]]
  }

  def getMapSingleResult(mapKeyNamingType: MapKeyNamingType = MapKeyNamingType.NONE): Map[String, AnyRef] = {
    if (mapKeyNamingType == null)
      throw new DomaNullPointerException("mapKeyNamingType")
    if (query.getMethodName == null)
      query.setCallerMethodName("getMapSingleResult")
    val handler = new MapSingleResultHandler(mapKeyNamingType)
    Option(execute(handler)).map(_.asScala.toMap).orNull
  }

  def getOptionMapSingleResult(
      mapKeyNamingType: MapKeyNamingType = MapKeyNamingType.NONE): Option[Map[String, AnyRef]] = {
    if (mapKeyNamingType == null)
      throw new DomaNullPointerException("mapKeyNamingType")
    if (query.getMethodName == null)
      query.setCallerMethodName("getOptionMapSingleResult")
    val handler = new OptionalMapSingleResultHandler(mapKeyNamingType)
    OptionConverters.asScala(execute(handler)).map(_.asScala.toMap)
  }

  def getEntityResultSeq[ELEMENT](implicit entityDesc: EntityDesc[ELEMENT]): Seq[ELEMENT] = {
    if (query.getMethodName == null)
      query.setCallerMethodName("getEntityResultSeq")
    query.setEntityType(entityDesc)
    val handler = new EntityResultListHandler[ELEMENT](entityDesc)
    execute(handler).asScala
  }

  @SuppressWarnings(Array("unchecked", "rawtypes"))
  def getScalarResultSeq[ELEMENT]( implicit cTag: ClassTag[ELEMENT]): Seq[ELEMENT] = {
    if (query.getMethodName == null)
      query.setCallerMethodName("getScalarResultSeq")
    val supplier = createScalarSupplier("elementClass", cTag.runtimeClass, false).asInstanceOf[Supplier[Scalar[Any, Any]]]
    val handler = new ScalarResultListHandler(supplier)
    execute(handler).asScala.asInstanceOf[Seq[ELEMENT]]
  }

  @SuppressWarnings(Array("unchecked", "rawtypes"))
  def getOptionalScalarResultSeq[ELEMENT](cTag: ClassTag[ELEMENT]): Seq[Option[ELEMENT]] = {
    if (query.getMethodName == null)
      query.setCallerMethodName("getOptionalScalarResultSeq")
    val supplier = createScalarSupplier("elementClass", cTag.runtimeClass, true).asInstanceOf[Supplier[Scalar[Any, Any]]]
    val handler = new ScalarResultListHandler(supplier)
    execute(handler).asScala.map(x => Option(x.asInstanceOf[ELEMENT]))
  }

  def getMapResultSeq(mapKeyNamingType: MapKeyNamingType = MapKeyNamingType.NONE): Seq[Map[String, AnyRef]] = {
    if (mapKeyNamingType == null)
      throw new DomaNullPointerException("mapKeyNamingType")
    if (query.getMethodName == null)
      query.setCallerMethodName("getMapResultSeq")
    val handler = new MapResultListHandler(mapKeyNamingType)
    execute(handler).asScala.map(_.asScala.toMap)
  }

  def iteratorEntity[TARGET, RESULT](
      mapper: Iterator[TARGET] => RESULT)(implicit entityDesc: EntityDesc[TARGET]): RESULT = {
    if (mapper == null) throw new DomaNullPointerException("mapper")
    iteratorEntityInternal(mapper)
  }

  protected def iteratorEntityInternal[TARGET, RESULT](
      mapper: Iterator[TARGET] => RESULT)(implicit entityDesc: EntityDesc[TARGET]): RESULT = {
    if (query.getMethodName == null) query.setCallerMethodName("iteratorEntity")

    query.setEntityType(entityDesc)
    val handler = new EntityStreamHandler(entityDesc, (p: java.util.stream.Stream[TARGET]) => mapper(WrapIterator.of(p)))
    execute(handler)
  }

  def iteratorScalar[RESULT, TARGET](
      mapper: Iterator[TARGET] => RESULT)(implicit cTag: ClassTag[TARGET]): RESULT = {
    if (mapper == null) throw new DomaNullPointerException("mapper")
    iteratorScalarInternal(cTag, mapper)
  }

  @SuppressWarnings(Array("unchecked", "rawtypes"))
  protected def iteratorScalarInternal[
      RESULT,
      TARGET](cTag: ClassTag[TARGET],
              mapper: Iterator[TARGET] => RESULT): RESULT = {
    if (query.getMethodName == null) query.setCallerMethodName("iteratorScalar")
    val supplier = createScalarSupplier("targetClass", cTag.runtimeClass, false).asInstanceOf[Supplier[Scalar[Any, TARGET]]]
    val handler = new ScalarStreamHandler(supplier, (p: java.util.stream.Stream[TARGET]) => mapper(WrapIterator.of(p)))
    execute(handler)
  }

  def iteratorOptionalScalar[RESULT, TARGET](
      mapper: Iterator[Option[TARGET]] => RESULT)(implicit cTag: ClassTag[TARGET]): RESULT = {
    if (mapper == null) throw new DomaNullPointerException("mapper")
    iteratorOptionalScalarInternal(cTag, mapper)
  }

  @SuppressWarnings(Array("unchecked", "rawtypes"))
  protected def iteratorOptionalScalarInternal[RESULT, TARGET](cTag: ClassTag[TARGET],
              mapper: Iterator[Option[TARGET]] => RESULT): RESULT = {
    if (query.getMethodName == null)
      query.setCallerMethodName("iteratorOptionalScalar")
    val supplier: Supplier[Scalar[TARGET, TARGET]] = createScalarSupplier("targetClass", cTag.runtimeClass, true).asInstanceOf[Supplier[Scalar[TARGET, TARGET]]]
    val bridgeMapper: Function[stream.Stream[TARGET], RESULT] = (p: java.util.stream.Stream[TARGET]) => mapper(WrapIterator.of(p).map(x => Option(x)))
    val handler = new ScalarStreamHandler[TARGET, TARGET, RESULT](supplier, bridgeMapper)
    execute(handler)
  }

  def iteratorMap[RESULT](
    mapper: Iterator[Map[String, AnyRef]] => RESULT,
    mapKeyNamingType: MapKeyNamingType = MapKeyNamingType.NONE): RESULT = {
    if (mapKeyNamingType == null)
      throw new DomaNullPointerException("mapKeyNamingType")
    if (mapper == null) throw new DomaNullPointerException("mapper")
    iteratorMapInternal(mapper, mapKeyNamingType)
  }

  protected def iteratorMapInternal[RESULT](
    mapper: Iterator[Map[String, AnyRef]] => RESULT,
    mapKeyNamingType: MapKeyNamingType = MapKeyNamingType.NONE
    ): RESULT = {
    if (query.getMethodName == null) query.setCallerMethodName("iteratorMap")
    val handler = new MapStreamHandler[RESULT](mapKeyNamingType, (p: java.util.stream.Stream[java.util.Map[String, Object]]) => mapper(WrapIterator.of(p).map(_.asScala.toMap)))
    execute(handler)
  }

  def execute[RESULT](resultSetHandler: ResultSetHandler[RESULT]): RESULT = {
    prepare()
    val command = new SelectCommand[RESULT](query, resultSetHandler)
    val result = command.execute
    query.complete()
    result
  }

  private def prepare(): Unit = {
    query.clearParameters()
    helper.getParams.forEach{ p =>
      query.addParameter(p.name, p.paramClass, p.param)
    }
    query.setSqlNode(helper.getSqlNode)
    query.prepare()
  }

  def ensureResult(ensureResult: Boolean): Unit = {
    query.setResultEnsured(ensureResult)
  }

  def ensureResultMapping(ensureResultMapping: Boolean): Unit = {
    query.setResultMappingEnsured(ensureResultMapping)
  }

  def fetch(fetchType: FetchType): Unit = {
    query.setFetchType(fetchType)
  }

  def fetchSize(fetchSize: Int): Unit = {
    query.setFetchSize(fetchSize)
  }

  def maxRows(maxRows: Int): Unit = {
    query.setMaxRows(maxRows)
  }

  def queryTimeout(queryTimeout: Int): Unit = {
    query.setQueryTimeout(queryTimeout)
  }

  def sqlLogType(sqlLogType: SqlLogType): Unit = {
    if (sqlLogType == null) throw new DomaNullPointerException("sqlLogType")
    query.setSqlLogType(sqlLogType)
  }

  def callerClassName(className: String): Unit = {
    if (className == null) throw new DomaNullPointerException("className")
    query.setCallerClassName(className)
  }

  def callerMethodName(methodName: String): Unit = {
    if (methodName == null) throw new DomaNullPointerException("methodName")
    query.setCallerMethodName(methodName)
  }

  def options(options: SelectOptions): Unit = {
    if (options == null) throw new DomaNullPointerException("options")
    query.setOptions(options)
  }

  def getSql: Sql[_] = {
    if (query.getMethodName == null) query.setCallerMethodName("getSql")
    prepare()
    query.getSql
  }

 def createScalarSupplier(parameterName: String,
                                 clazz: Class[_],
                                 optional: Boolean): Supplier[Scalar[_, _]] =
  try Scalars.wrap(null, clazz, optional, config.getClassHelper)
  catch {
    case e: ScalarException =>
      throw new DomaIllegalArgumentException(
        parameterName,
        Message.DOMA2204.getMessage(clazz, e))
  }

}

private class SubsequentSelectBuilder (
  config: Config,
  builder: BuildingHelper,
  query: SqlSelectQuery,
  paramIndex: ParamIndex)
  extends DomalaSelectBuilder(config, builder, query, paramIndex) {

  override def sql(fragment: String): DomalaSelectBuilder = {
    helper.appendSql(fragment)
    this
  }
}

object DomalaSelectBuilder {
  def newInstance(config: Config): DomalaSelectBuilder = {
    if (config == null) throw new DomaNullPointerException("config")
    new DomalaSelectBuilder(config)
  }

}
