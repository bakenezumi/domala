package domala.tests

import domala._
import domala.jdbc.{BatchResult, Result, SelectOptions}

trait PersonDao {
  def create(): Unit

  def drop(): Unit

  def selectById(id: Int): Option[Person]

  def selectCount(): Int

  def selectAll(): Seq[Person]

  def selectByIdNullable(id: Int): Person

  def selectWithDepartmentById(id: Int): Option[PersonDepartment]

  def selectWithDepartmentEmbeddedById(id: Int): Option[PersonDepartmentEmbedded]

  def selectAllStream(f: Stream[Person] => Int): Int

  def selectAllIterator(f: Iterator[Person] => Int): Int

  def selectByIdStream(id: Int)(f: Stream[Person] => Option[Address]): Option[Address]

  def selectByIdIterator(id: Int)(f: Iterator[Person] => Option[Address]): Option[Address]

  def selectAllSeqMap(): Seq[Map[String, Any]]

  def selectByIdMap(id: Int): Map[String, Any]

  def selectByIdOptionMap(id: Int): Option[Map[String, Any]]

  def selectAllStreamMap(f: Stream[Map[String, Any]] => Int): Int

  def selectAllIteratorMap(f: Iterator[Map[String, Any]] => Int): Int

  def selectNameById(id: Int): Option[Name]

  def selectNameByIdNullable(id: Int): Name

  def selectNames: Seq[Name]

  def selectNameStream(f: Stream[Name] => Int): Int

  def selectNameIterator(f: Iterator[Name] => Int): Int

  def selectByIDBuilder(id: Int): String = {
    import org.seasar.doma.jdbc.builder.SelectBuilder
    val builder = SelectBuilder.newInstance(TestConfig)
    builder.sql("select")
    builder.sql("name")
    builder.sql("from person")
    builder.sql("where")
    builder.sql("id =").param(classOf[Int], id)
    builder.getScalarSingleResult(classOf[String])
  }

  def insert(person: Person): Result[Person]

  def update(person: Person): Result[Person]

  def delete(person: Person): Int

  def batchInsert(persons: Seq[Person]): BatchResult[Person]

  def batchUpdate(persons: Seq[Person]): BatchResult[Person]

  def batchDelete(persons: Seq[Person]): Array[Int]

  def insertSql(entity: Person, entity2: Person, version: Int): Result[Person]

  def updateSql(entity: Person, entity2: Person, version: Int): Result[Person]

  def deleteSql(entity: Person, version: Int): Int

  def selectAllByOption(options: SelectOptions): Seq[Person]

  def batchInsertSql(persons: Seq[Person]): BatchResult[Person]

  def batchUpdateSql(persons: Seq[Person]): BatchResult[Person]

  def batchDeleteSql(persons: Seq[Person]): Array[Int]
}

object PersonDao {
  def impl(implicit config: domala.jdbc.Config): PersonDao = new Internal(config, Option(config).getOrElse(throw new org.seasar.doma.DomaNullPointerException("config")).getDataSource)

  def impl(connection: java.sql.Connection)(implicit config: domala.jdbc.Config): PersonDao = new Internal(config, connection)

  def impl(dataSource: javax.sql.DataSource)(implicit config: domala.jdbc.Config): PersonDao = new Internal(config, dataSource)

  private[this] val __method0 = domala.internal.jdbc.dao.DaoUtil.getDeclaredMethod(classOf[PersonDao], "create")
  private[this] val __method1 = domala.internal.jdbc.dao.DaoUtil.getDeclaredMethod(classOf[PersonDao], "drop")
  private[this] val __method2 = domala.internal.jdbc.dao.DaoUtil.getDeclaredMethod(classOf[PersonDao], "selectById", classOf[Int])
  private[this] val __method3 = domala.internal.jdbc.dao.DaoUtil.getDeclaredMethod(classOf[PersonDao], "selectCount")
  private[this] val __method4 = domala.internal.jdbc.dao.DaoUtil.getDeclaredMethod(classOf[PersonDao], "selectAll")
  private[this] val __method5 = domala.internal.jdbc.dao.DaoUtil.getDeclaredMethod(classOf[PersonDao], "selectByIdNullable", classOf[Int])
  private[this] val __method6 = domala.internal.jdbc.dao.DaoUtil.getDeclaredMethod(classOf[PersonDao], "selectWithDepartmentById", classOf[Int])
  private[this] val __method7 = domala.internal.jdbc.dao.DaoUtil.getDeclaredMethod(classOf[PersonDao], "selectWithDepartmentEmbeddedById", classOf[Int])
  private[this] val __method8 = domala.internal.jdbc.dao.DaoUtil.getDeclaredMethod(classOf[PersonDao], "selectAllStream", classOf[Stream[Person] => _])
  private[this] val __method9 = domala.internal.jdbc.dao.DaoUtil.getDeclaredMethod(classOf[PersonDao], "selectAllIterator", classOf[Iterator[Person] => _])
  private[this] val __method10 = domala.internal.jdbc.dao.DaoUtil.getDeclaredMethod(classOf[PersonDao], "selectByIdStream", classOf[Int], classOf[Stream[Person] => _])
  private[this] val __method11 = domala.internal.jdbc.dao.DaoUtil.getDeclaredMethod(classOf[PersonDao], "selectByIdIterator", classOf[Int], classOf[Iterator[Person] => _])
  private[this] val __method12 = domala.internal.jdbc.dao.DaoUtil.getDeclaredMethod(classOf[PersonDao], "selectAllSeqMap")
  private[this] val __method13 = domala.internal.jdbc.dao.DaoUtil.getDeclaredMethod(classOf[PersonDao], "selectByIdMap", classOf[Int])
  private[this] val __method14 = domala.internal.jdbc.dao.DaoUtil.getDeclaredMethod(classOf[PersonDao], "selectByIdOptionMap", classOf[Int])
  private[this] val __method15 = domala.internal.jdbc.dao.DaoUtil.getDeclaredMethod(classOf[PersonDao], "selectAllStreamMap", classOf[Stream[Map[String, Any]] => _])
  private[this] val __method16 = domala.internal.jdbc.dao.DaoUtil.getDeclaredMethod(classOf[PersonDao], "selectAllIteratorMap", classOf[Iterator[Map[String, Any]] => _])
  private[this] val __method17 = domala.internal.jdbc.dao.DaoUtil.getDeclaredMethod(classOf[PersonDao], "selectNameById", classOf[Int])
  private[this] val __method18 = domala.internal.jdbc.dao.DaoUtil.getDeclaredMethod(classOf[PersonDao], "selectNameByIdNullable", classOf[Int])
  private[this] val __method19 = domala.internal.jdbc.dao.DaoUtil.getDeclaredMethod(classOf[PersonDao], "selectNames")
  private[this] val __method20 = domala.internal.jdbc.dao.DaoUtil.getDeclaredMethod(classOf[PersonDao], "selectNameStream", classOf[Stream[Name] => _])
  private[this] val __method21 = domala.internal.jdbc.dao.DaoUtil.getDeclaredMethod(classOf[PersonDao], "selectNameIterator", classOf[Iterator[Name] => _])
  private[this] val __method22 = domala.internal.jdbc.dao.DaoUtil.getDeclaredMethod(classOf[PersonDao], "insert", classOf[Person])
  private[this] val __method23 = domala.internal.jdbc.dao.DaoUtil.getDeclaredMethod(classOf[PersonDao], "update", classOf[Person])
  private[this] val __method24 = domala.internal.jdbc.dao.DaoUtil.getDeclaredMethod(classOf[PersonDao], "delete", classOf[Person])
  private[this] val __method25 = domala.internal.jdbc.dao.DaoUtil.getDeclaredMethod(classOf[PersonDao], "batchInsert", classOf[Seq[Person]])
  private[this] val __method26 = domala.internal.jdbc.dao.DaoUtil.getDeclaredMethod(classOf[PersonDao], "batchUpdate", classOf[Seq[Person]])
  private[this] val __method27 = domala.internal.jdbc.dao.DaoUtil.getDeclaredMethod(classOf[PersonDao], "batchDelete", classOf[Seq[Person]])
  private[this] val __method28 = domala.internal.jdbc.dao.DaoUtil.getDeclaredMethod(classOf[PersonDao], "insertSql", classOf[Person], classOf[Person], classOf[Int])
  private[this] val __method29 = domala.internal.jdbc.dao.DaoUtil.getDeclaredMethod(classOf[PersonDao], "updateSql", classOf[Person], classOf[Person], classOf[Int])
  private[this] val __method30 = domala.internal.jdbc.dao.DaoUtil.getDeclaredMethod(classOf[PersonDao], "deleteSql", classOf[Person], classOf[Int])
  private[this] val __method31 = domala.internal.jdbc.dao.DaoUtil.getDeclaredMethod(classOf[PersonDao], "selectAllByOption", classOf[SelectOptions])
  private[this] val __method32 = domala.internal.jdbc.dao.DaoUtil.getDeclaredMethod(classOf[PersonDao], "batchInsertSql", classOf[Seq[Person]])
  private[this] val __method33 = domala.internal.jdbc.dao.DaoUtil.getDeclaredMethod(classOf[PersonDao], "batchUpdateSql", classOf[Seq[Person]])
  private[this] val __method34 = domala.internal.jdbc.dao.DaoUtil.getDeclaredMethod(classOf[PersonDao], "batchDeleteSql", classOf[Seq[Person]])

  class Internal(___config: domala.jdbc.Config, dataSource: javax.sql.DataSource) extends org.seasar.doma.internal.jdbc.dao.AbstractDao(___config, dataSource) with
    PersonDao {
    def this(config: domala.jdbc.Config, connection: java.sql.Connection) = this(config, org.seasar.doma.internal.jdbc.dao.DomalaAbstractDaoHelper.toDataSource(connection))

    import scala.collection.JavaConverters._

    implicit val __sqlNodeRepository: domala.jdbc.SqlNodeRepository = ___config.getSqlNodeRepository

    override def create(): Unit = {
      entering(classOf[PersonDao].getName, "create")
      try {
        val __query = new domala.jdbc.query.SqlScriptQuery("\ncreate table department(\n    id int not null identity primary key,\n    name varchar(20),\n    version int not null\n);\n\ncreate table person(\n    id int not null identity primary key,\n    name varchar(20),\n    age int,\n    city varchar(20) not null,\n    street varchar(20) not null,\n    department_id int not null,\n    version int not null,\n    constraint fk_department_id foreign key(department_id) references department(id)\n);\n\ninsert into department (id, name, version) values(1, 'ACCOUNTING', 0);\ninsert into department (id, name, version) values(2, 'SALES', 0);\n\ninsert into person (id, name, age, city, street, department_id, version) values(1, 'SMITH', 10, 'Tokyo', 'Yaesu', 2, 0);\ninsert into person (id, name, age, city, street, department_id, version) values(2, 'ALLEN', 20, 'Kyoto', 'Karasuma', 1, 0);\n  ")
        __query.setMethod(__method0)
        __query.setConfig(__config)
        ()
        __query.setCallerClassName(classOf[PersonDao].getName)
        __query.setCallerMethodName("create")
        __query.setBlockDelimiter("")
        __query.setHaltOnError(false)
        __query.setSqlLogType(org.seasar.doma.jdbc.SqlLogType.FORMATTED)
        __query.prepare()
        val __command = getCommandImplementors.createScriptCommand(__method0, __query)
        __command.execute()
        __query.complete()
        exiting(classOf[PersonDao].getName, "create", null)
      } catch {
        case __e: java.lang.RuntimeException =>
          throwing(classOf[PersonDao].getName, "create", __e)
          throw __e
      }
    }

    override def drop(): Unit = {
      entering(classOf[PersonDao].getName, "drop")
      try {
        val __query = new domala.jdbc.query.SqlScriptQuery("\ndrop table person;\ndrop table department;\n  ")
        __query.setMethod(__method1)
        __query.setConfig(__config)
        ()
        __query.setCallerClassName(classOf[PersonDao].getName)
        __query.setCallerMethodName("drop")
        __query.setBlockDelimiter("")
        __query.setHaltOnError(false)
        __query.setSqlLogType(org.seasar.doma.jdbc.SqlLogType.FORMATTED)
        __query.prepare()
        val __command = getCommandImplementors.createScriptCommand(__method1, __query)
        __command.execute()
        __query.complete()
        exiting(classOf[PersonDao].getName, "drop", null)
      } catch {
        case __e: java.lang.RuntimeException =>
          throwing(classOf[PersonDao].getName, "drop", __e)
          throw __e
      }
    }

    override def selectById(id: Int): Option[Person] = {
      domala.internal.macros.reflect.DaoReflectionMacros.validateParameterAndSql(classOf[PersonDao], "selectById", true, false, "\nselect *\nfrom person\nwhere id = /*id*/0\n  ", domala.internal.macros.DaoParamClass("id", classOf[Int]))
      entering(classOf[PersonDao].getName, "selectById", id.asInstanceOf[Object])
      try {
        val __query = new domala.jdbc.query.SqlAnnotationSelectQuery("\nselect *\nfrom person\nwhere id = /*id*/0\n  ")
        __query.setMethod(__method2)
        __query.setConfig(__config)
        domala.internal.macros.reflect.DaoReflectionMacros.setEntityType[Person](__query)
        __query.addParameter("id", classOf[Int], id)
        __query.setCallerClassName(classOf[PersonDao].getName)
        __query.setCallerMethodName("selectById")
        __query.setResultEnsured(false)
        __query.setResultMappingEnsured(false)
        __query.setFetchType(org.seasar.doma.FetchType.LAZY)
        __query.setQueryTimeout(-1)
        __query.setMaxRows(-1)
        __query.setFetchSize(-1)
        __query.setSqlLogType(org.seasar.doma.jdbc.SqlLogType.FORMATTED)
        __query.prepare()
        val __result: Option[Person] = getCommandImplementors.createSelectCommand(__method2, __query, domala.internal.macros.reflect.DaoReflectionMacros.getOptionSingleResultHandler[PersonDao, Person](classOf[PersonDao], "selectById")).execute()
        __query.complete()
        exiting(classOf[PersonDao].getName, "selectById", __result)
        __result
      } catch {
        case __e: java.lang.RuntimeException =>
          throwing(classOf[PersonDao].getName, "selectById", __e)
          throw __e
      }
    }

    override def selectCount(): Int = {
      domala.internal.macros.reflect.DaoReflectionMacros.validateParameterAndSql(classOf[PersonDao], "selectCount", true, false, "\nselect count(*)\nfrom person\n  ")
      entering(classOf[PersonDao].getName, "selectCount")
      try {
        val __query = new domala.jdbc.query.SqlAnnotationSelectQuery("\nselect count(*)\nfrom person\n  ")
        __query.setMethod(__method3)
        __query.setConfig(__config)
        __query.setCallerClassName(classOf[PersonDao].getName)
        __query.setCallerMethodName("selectCount")
        __query.setResultEnsured(false)
        __query.setResultMappingEnsured(false)
        __query.setFetchType(org.seasar.doma.FetchType.LAZY)
        __query.setQueryTimeout(-1)
        __query.setMaxRows(-1)
        __query.setFetchSize(-1)
        __query.setSqlLogType(org.seasar.doma.jdbc.SqlLogType.FORMATTED)
        __query.prepare()
        val __result: Int = getCommandImplementors.createSelectCommand(__method3, __query, new org.seasar.doma.internal.jdbc.command.BasicSingleResultHandler(() => new org.seasar.doma.wrapper.IntegerWrapper(): org.seasar.doma.wrapper.Wrapper[Integer], false)).execute()
        __query.complete()
        exiting(classOf[PersonDao].getName, "selectCount", __result)
        __result
      } catch {
        case __e: java.lang.RuntimeException =>
          throwing(classOf[PersonDao].getName, "selectCount", __e)
          throw __e
      }
    }

    override def selectAll(): Seq[Person] = {
      domala.internal.macros.reflect.DaoReflectionMacros.validateParameterAndSql(classOf[PersonDao], "selectAll", true, false, "\nselect *\nfrom person\norder by id\n  ")
      entering(classOf[PersonDao].getName, "selectAll")
      try {
        val __query = new domala.jdbc.query.SqlAnnotationSelectQuery("\nselect *\nfrom person\norder by id\n  ")
        __query.setMethod(__method4)
        __query.setConfig(__config)
        domala.internal.macros.reflect.DaoReflectionMacros.setEntityType[Person](__query)
        __query.setCallerClassName(classOf[PersonDao].getName)
        __query.setCallerMethodName("selectAll")
        __query.setResultEnsured(false)
        __query.setResultMappingEnsured(false)
        __query.setFetchType(org.seasar.doma.FetchType.LAZY)
        __query.setQueryTimeout(-1)
        __query.setMaxRows(-1)
        __query.setFetchSize(-1)
        __query.setSqlLogType(org.seasar.doma.jdbc.SqlLogType.FORMATTED)
        __query.prepare()
        val __result: Seq[Person] = getCommandImplementors.createSelectCommand(__method4, __query, domala.internal.macros.reflect.DaoReflectionMacros.getResultListHandler[PersonDao, Person](classOf[PersonDao], "selectAll")).execute().asScala
        __query.complete()
        exiting(classOf[PersonDao].getName, "selectAll", __result)
        __result
      } catch {
        case __e: java.lang.RuntimeException =>
          throwing(classOf[PersonDao].getName, "selectAll", __e)
          throw __e
      }
    }

    override def selectByIdNullable(id: Int): Person = {
      domala.internal.macros.reflect.DaoReflectionMacros.validateParameterAndSql(classOf[PersonDao], "selectByIdNullable", true, false, "\nselect *\nfrom person\nwhere id = /*id*/0\n  ", domala.internal.macros.DaoParamClass("id", classOf[Int]))
      entering(classOf[PersonDao].getName, "selectByIdNullable", id.asInstanceOf[Object])
      try {
        val __query = new domala.jdbc.query.SqlAnnotationSelectQuery("\nselect *\nfrom person\nwhere id = /*id*/0\n  ")
        __query.setMethod(__method5)
        __query.setConfig(__config)
        domala.internal.macros.reflect.DaoReflectionMacros.setEntityType[Person](__query)
        __query.addParameter("id", classOf[Int], id)
        __query.setCallerClassName(classOf[PersonDao].getName)
        __query.setCallerMethodName("selectByIdNullable")
        __query.setResultEnsured(false)
        __query.setResultMappingEnsured(false)
        __query.setFetchType(org.seasar.doma.FetchType.LAZY)
        __query.setQueryTimeout(-1)
        __query.setMaxRows(-1)
        __query.setFetchSize(-1)
        __query.setSqlLogType(org.seasar.doma.jdbc.SqlLogType.FORMATTED)
        __query.prepare()
        val __result: Person = domala.internal.macros.reflect.DaoReflectionMacros.getOtherResult[PersonDao, Person](classOf[PersonDao], "selectByIdNullable", getCommandImplementors, __query, __method5)
        __query.complete()
        exiting(classOf[PersonDao].getName, "selectByIdNullable", __result)
        __result
      } catch {
        case __e: java.lang.RuntimeException =>
          throwing(classOf[PersonDao].getName, "selectByIdNullable", __e)
          throw __e
      }
    }

    override def selectWithDepartmentById(id: Int): Option[PersonDepartment] = {
      domala.internal.macros.reflect.DaoReflectionMacros.validateParameterAndSql(classOf[PersonDao], "selectWithDepartmentById", true, false, "\nselect\n    p.id,\n    p.name,\n    d.id as department_id,\n    d.name as department_name\nfrom\n    person p\n    inner join\n    department d\n    on (p.department_id = d.id)\nwhere\n    p.id = /*id*/0\n  ", domala.internal.macros.DaoParamClass("id", classOf[Int]))
      entering(classOf[PersonDao].getName, "selectWithDepartmentById", id.asInstanceOf[Object])
      try {
        val __query = new domala.jdbc.query.SqlAnnotationSelectQuery("\nselect\n    p.id,\n    p.name,\n    d.id as department_id,\n    d.name as department_name\nfrom\n    person p\n    inner join\n    department d\n    on (p.department_id = d.id)\nwhere\n    p.id = /*id*/0\n  ")
        __query.setMethod(__method6)
        __query.setConfig(__config)
        domala.internal.macros.reflect.DaoReflectionMacros.setEntityType[PersonDepartment](__query)
        __query.addParameter("id", classOf[Int], id)
        __query.setCallerClassName(classOf[PersonDao].getName)
        __query.setCallerMethodName("selectWithDepartmentById")
        __query.setResultEnsured(false)
        __query.setResultMappingEnsured(false)
        __query.setFetchType(org.seasar.doma.FetchType.LAZY)
        __query.setQueryTimeout(-1)
        __query.setMaxRows(-1)
        __query.setFetchSize(-1)
        __query.setSqlLogType(org.seasar.doma.jdbc.SqlLogType.FORMATTED)
        __query.prepare()
        val __result: Option[PersonDepartment] = getCommandImplementors.createSelectCommand(__method6, __query, domala.internal.macros.reflect.DaoReflectionMacros.getOptionSingleResultHandler[PersonDao, PersonDepartment](classOf[PersonDao], "selectWithDepartmentById")).execute()
        __query.complete()
        exiting(classOf[PersonDao].getName, "selectWithDepartmentById", __result)
        __result
      } catch {
        case __e: java.lang.RuntimeException =>
          throwing(classOf[PersonDao].getName, "selectWithDepartmentById", __e)
          throw __e
      }
    }

    override def selectWithDepartmentEmbeddedById(id: Int): Option[PersonDepartmentEmbedded] = {
      domala.internal.macros.reflect.DaoReflectionMacros.validateParameterAndSql(classOf[PersonDao], "selectWithDepartmentEmbeddedById", true, false, "\nselect\n    p.id,\n    p.name,\n    d.id as department_id,\n    d.name as department_name\nfrom\n    person p\n    inner join\n    department d\n    on (p.department_id = d.id)\nwhere\n    p.id = /*id*/0\n  ", domala.internal.macros.DaoParamClass("id", classOf[Int]))
      entering(classOf[PersonDao].getName, "selectWithDepartmentEmbeddedById", id.asInstanceOf[Object])
      try {
        val __query = new domala.jdbc.query.SqlAnnotationSelectQuery("\nselect\n    p.id,\n    p.name,\n    d.id as department_id,\n    d.name as department_name\nfrom\n    person p\n    inner join\n    department d\n    on (p.department_id = d.id)\nwhere\n    p.id = /*id*/0\n  ")
        __query.setMethod(__method7)
        __query.setConfig(__config)
        domala.internal.macros.reflect.DaoReflectionMacros.setEntityType[PersonDepartmentEmbedded](__query)
        __query.addParameter("id", classOf[Int], id)
        __query.setCallerClassName(classOf[PersonDao].getName)
        __query.setCallerMethodName("selectWithDepartmentEmbeddedById")
        __query.setResultEnsured(false)
        __query.setResultMappingEnsured(false)
        __query.setFetchType(org.seasar.doma.FetchType.LAZY)
        __query.setQueryTimeout(-1)
        __query.setMaxRows(-1)
        __query.setFetchSize(-1)
        __query.setSqlLogType(org.seasar.doma.jdbc.SqlLogType.FORMATTED)
        __query.prepare()
        val __result: Option[PersonDepartmentEmbedded] = getCommandImplementors.createSelectCommand(__method7, __query, domala.internal.macros.reflect.DaoReflectionMacros.getOptionSingleResultHandler[PersonDao, PersonDepartmentEmbedded](classOf[PersonDao], "selectWithDepartmentEmbeddedById")).execute()
        __query.complete()
        exiting(classOf[PersonDao].getName, "selectWithDepartmentEmbeddedById", __result)
        __result
      } catch {
        case __e: java.lang.RuntimeException =>
          throwing(classOf[PersonDao].getName, "selectWithDepartmentEmbeddedById", __e)
          throw __e
      }
    }

    override def selectAllStream(f: Stream[Person] => Int): Int = {
      domala.internal.macros.reflect.DaoReflectionMacros.validateParameterAndSql(classOf[PersonDao], "selectAllStream", true, false, "\nselect *\nfrom person\n  ")
      entering(classOf[PersonDao].getName, "selectAllStream", f.asInstanceOf[Object])
      try {
        val __query = new domala.jdbc.query.SqlAnnotationSelectQuery("\nselect *\nfrom person\n  ")
        if (f == null) throw new org.seasar.doma.DomaNullPointerException("f")
        __query.setMethod(__method8)
        __query.setConfig(__config)
        domala.internal.macros.reflect.DaoReflectionMacros.setEntityType[Person](__query)
        __query.addParameter("f", classOf[java.util.function.Function[Stream[Person], _]], f)
        __query.setCallerClassName(classOf[PersonDao].getName)
        __query.setCallerMethodName("selectAllStream")
        __query.setResultEnsured(false)
        __query.setResultMappingEnsured(false)
        __query.setFetchType(org.seasar.doma.FetchType.LAZY)
        __query.setQueryTimeout(-1)
        __query.setMaxRows(-1)
        __query.setFetchSize(-1)
        __query.setSqlLogType(org.seasar.doma.jdbc.SqlLogType.FORMATTED)
        __query.setResultStream(true)
        __query.prepare()
        val __result: Int = getCommandImplementors.createSelectCommand(__method8, __query, domala.internal.macros.reflect.DaoReflectionMacros.getStreamHandler(f, classOf[PersonDao], "selectAllStream")).execute()
        __query.complete()
        exiting(classOf[PersonDao].getName, "selectAllStream", __result)
        __result
      } catch {
        case __e: java.lang.RuntimeException =>
          throwing(classOf[PersonDao].getName, "selectAllStream", __e)
          throw __e
      }
    }

    override def selectAllIterator(f: Iterator[Person] => Int): Int = {
      domala.internal.macros.reflect.DaoReflectionMacros.validateParameterAndSql(classOf[PersonDao], "selectAllIterator", true, false, "\nselect *\nfrom person\n  ")
      entering(classOf[PersonDao].getName, "selectAllIterator", f.asInstanceOf[Object])
      try {
        val __query = new domala.jdbc.query.SqlAnnotationSelectQuery("\nselect *\nfrom person\n  ")
        if (f == null) throw new org.seasar.doma.DomaNullPointerException("f")
        __query.setMethod(__method9)
        __query.setConfig(__config)
        domala.internal.macros.reflect.DaoReflectionMacros.setEntityType[Person](__query)
        __query.addParameter("f", classOf[java.util.function.Function[Iterator[Person], _]], f)
        __query.setCallerClassName(classOf[PersonDao].getName)
        __query.setCallerMethodName("selectAllIterator")
        __query.setResultEnsured(false)
        __query.setResultMappingEnsured(false)
        __query.setFetchType(org.seasar.doma.FetchType.LAZY)
        __query.setQueryTimeout(-1)
        __query.setMaxRows(-1)
        __query.setFetchSize(-1)
        __query.setSqlLogType(org.seasar.doma.jdbc.SqlLogType.FORMATTED)
        __query.setResultStream(true)
        __query.prepare()
        val __result: Int = getCommandImplementors.createSelectCommand(__method9, __query, domala.internal.macros.reflect.DaoReflectionMacros.getIteratorHandler(f, classOf[PersonDao], "selectAllIterator")).execute()
        __query.complete()
        exiting(classOf[PersonDao].getName, "selectAllIterator", __result)
        __result
      } catch {
        case __e: java.lang.RuntimeException =>
          throwing(classOf[PersonDao].getName, "selectAllIterator", __e)
          throw __e
      }
    }

    override def selectByIdStream(id: Int)(f: Stream[Person] => Option[Address]): Option[Address] = {
      domala.internal.macros.reflect.DaoReflectionMacros.validateParameterAndSql(classOf[PersonDao], "selectByIdStream", true, false, "\nselect *\nfrom person\nwhere\n    id = /*id*/0\n  ", domala.internal.macros.DaoParamClass("id", classOf[Int]))
      entering(classOf[PersonDao].getName, "selectByIdStream", id.asInstanceOf[Object], f.asInstanceOf[Object])
      try {
        val __query = new domala.jdbc.query.SqlAnnotationSelectQuery("\nselect *\nfrom person\nwhere\n    id = /*id*/0\n  ")
        if (f == null) throw new org.seasar.doma.DomaNullPointerException("f")
        __query.setMethod(__method10)
        __query.setConfig(__config)
        domala.internal.macros.reflect.DaoReflectionMacros.setEntityType[Person](__query)
        __query.addParameter("id", classOf[Int], id)
        __query.addParameter("f", classOf[java.util.function.Function[Stream[Person], _]], f)
        __query.setCallerClassName(classOf[PersonDao].getName)
        __query.setCallerMethodName("selectByIdStream")
        __query.setResultEnsured(false)
        __query.setResultMappingEnsured(false)
        __query.setFetchType(org.seasar.doma.FetchType.LAZY)
        __query.setQueryTimeout(-1)
        __query.setMaxRows(-1)
        __query.setFetchSize(-1)
        __query.setSqlLogType(org.seasar.doma.jdbc.SqlLogType.FORMATTED)
        __query.setResultStream(true)
        __query.prepare()
        val __result: Option[Address] = getCommandImplementors.createSelectCommand(__method10, __query, domala.internal.macros.reflect.DaoReflectionMacros.getStreamHandler(f, classOf[PersonDao], "selectByIdStream")).execute()
        __query.complete()
        exiting(classOf[PersonDao].getName, "selectByIdStream", __result)
        __result
      } catch {
        case __e: java.lang.RuntimeException =>
          throwing(classOf[PersonDao].getName, "selectByIdStream", __e)
          throw __e
      }
    }

    override def selectByIdIterator(id: Int)(f: Iterator[Person] => Option[Address]): Option[Address] = {
      domala.internal.macros.reflect.DaoReflectionMacros.validateParameterAndSql(classOf[PersonDao], "selectByIdIterator", true, false, "\nselect *\nfrom person\nwhere\n    id = /*id*/0\n  ", domala.internal.macros.DaoParamClass("id", classOf[Int]))
      entering(classOf[PersonDao].getName, "selectByIdIterator", id.asInstanceOf[Object], f.asInstanceOf[Object])
      try {
        val __query = new domala.jdbc.query.SqlAnnotationSelectQuery("\nselect *\nfrom person\nwhere\n    id = /*id*/0\n  ")
        if (f == null) throw new org.seasar.doma.DomaNullPointerException("f")
        __query.setMethod(__method11)
        __query.setConfig(__config)
        domala.internal.macros.reflect.DaoReflectionMacros.setEntityType[Person](__query)
        __query.addParameter("id", classOf[Int], id)
        __query.addParameter("f", classOf[java.util.function.Function[Iterator[Person], _]], f)
        __query.setCallerClassName(classOf[PersonDao].getName)
        __query.setCallerMethodName("selectByIdIterator")
        __query.setResultEnsured(false)
        __query.setResultMappingEnsured(false)
        __query.setFetchType(org.seasar.doma.FetchType.LAZY)
        __query.setQueryTimeout(-1)
        __query.setMaxRows(-1)
        __query.setFetchSize(-1)
        __query.setSqlLogType(org.seasar.doma.jdbc.SqlLogType.FORMATTED)
        __query.setResultStream(true)
        __query.prepare()
        val __result: Option[Address] = getCommandImplementors.createSelectCommand(__method11, __query, domala.internal.macros.reflect.DaoReflectionMacros.getIteratorHandler(f, classOf[PersonDao], "selectByIdIterator")).execute()
        __query.complete()
        exiting(classOf[PersonDao].getName, "selectByIdIterator", __result)
        __result
      } catch {
        case __e: java.lang.RuntimeException =>
          throwing(classOf[PersonDao].getName, "selectByIdIterator", __e)
          throw __e
      }
    }

    override def selectAllSeqMap(): Seq[Map[String, Any]] = {
      domala.internal.macros.reflect.DaoReflectionMacros.validateParameterAndSql(classOf[PersonDao], "selectAllSeqMap", true, false, "\nselect *\nfrom person\norder by id\n  ")
      entering(classOf[PersonDao].getName, "selectAllSeqMap")
      try {
        val __query = new domala.jdbc.query.SqlAnnotationSelectQuery("\nselect *\nfrom person\norder by id\n  ")
        __query.setMethod(__method12)
        __query.setConfig(__config)
        __query.setCallerClassName(classOf[PersonDao].getName)
        __query.setCallerMethodName("selectAllSeqMap")
        __query.setResultEnsured(false)
        __query.setResultMappingEnsured(false)
        __query.setFetchType(org.seasar.doma.FetchType.LAZY)
        __query.setQueryTimeout(-1)
        __query.setMaxRows(-1)
        __query.setFetchSize(-1)
        __query.setSqlLogType(org.seasar.doma.jdbc.SqlLogType.FORMATTED)
        __query.prepare()
        val __result: Seq[Map[String, Any]] = getCommandImplementors.createSelectCommand(__method12, __query, new org.seasar.doma.internal.jdbc.command.MapResultListHandler(org.seasar.doma.MapKeyNamingType.NONE)).execute().asScala.map(_.asScala.toMap)
        __query.complete()
        exiting(classOf[PersonDao].getName, "selectAllSeqMap", __result)
        __result
      } catch {
        case __e: java.lang.RuntimeException =>
          throwing(classOf[PersonDao].getName, "selectAllSeqMap", __e)
          throw __e
      }
    }

    override def selectByIdMap(id: Int): Map[String, Any] = {
      domala.internal.macros.reflect.DaoReflectionMacros.validateParameterAndSql(classOf[PersonDao], "selectByIdMap", true, false, "\nselect *\nfrom person\nwhere\n    id = /*id*/0\n  ", domala.internal.macros.DaoParamClass("id", classOf[Int]))
      entering(classOf[PersonDao].getName, "selectByIdMap", id.asInstanceOf[Object])
      try {
        val __query = new domala.jdbc.query.SqlAnnotationSelectQuery("\nselect *\nfrom person\nwhere\n    id = /*id*/0\n  ")
        __query.setMethod(__method13)
        __query.setConfig(__config)
        __query.addParameter("id", classOf[Int], id)
        __query.setCallerClassName(classOf[PersonDao].getName)
        __query.setCallerMethodName("selectByIdMap")
        __query.setResultEnsured(false)
        __query.setResultMappingEnsured(false)
        __query.setFetchType(org.seasar.doma.FetchType.LAZY)
        __query.setQueryTimeout(-1)
        __query.setMaxRows(-1)
        __query.setFetchSize(-1)
        __query.setSqlLogType(org.seasar.doma.jdbc.SqlLogType.FORMATTED)
        __query.prepare()
        val __result: Map[String, Any] = Option(getCommandImplementors.createSelectCommand(__method13, __query, new org.seasar.doma.internal.jdbc.command.MapSingleResultHandler(org.seasar.doma.MapKeyNamingType.NONE)).execute()).map(_.asScala.toMap).getOrElse(Map.empty)
        __query.complete()
        exiting(classOf[PersonDao].getName, "selectByIdMap", __result)
        __result
      } catch {
        case __e: java.lang.RuntimeException =>
          throwing(classOf[PersonDao].getName, "selectByIdMap", __e)
          throw __e
      }
    }

    override def selectByIdOptionMap(id: Int): Option[Map[String, Any]] = {
      domala.internal.macros.reflect.DaoReflectionMacros.validateParameterAndSql(classOf[PersonDao], "selectByIdOptionMap", true, false, "\nselect *\nfrom person\nwhere\n    id = /*id*/0\n  ", domala.internal.macros.DaoParamClass("id", classOf[Int]))
      entering(classOf[PersonDao].getName, "selectByIdOptionMap", id.asInstanceOf[Object])
      try {
        val __query = new domala.jdbc.query.SqlAnnotationSelectQuery("\nselect *\nfrom person\nwhere\n    id = /*id*/0\n  ")
        __query.setMethod(__method14)
        __query.setConfig(__config)
        __query.addParameter("id", classOf[Int], id)
        __query.setCallerClassName(classOf[PersonDao].getName)
        __query.setCallerMethodName("selectByIdOptionMap")
        __query.setResultEnsured(false)
        __query.setResultMappingEnsured(false)
        __query.setFetchType(org.seasar.doma.FetchType.LAZY)
        __query.setQueryTimeout(-1)
        __query.setMaxRows(-1)
        __query.setFetchSize(-1)
        __query.setSqlLogType(org.seasar.doma.jdbc.SqlLogType.FORMATTED)
        __query.prepare()
        val __result: Option[Map[String, Any]] = getCommandImplementors.createSelectCommand(__method14, __query, new domala.internal.jdbc.command.OptionMapSingleResultHandler(org.seasar.doma.MapKeyNamingType.NONE)).execute().map(x => x.asScala.toMap)
        __query.complete()
        exiting(classOf[PersonDao].getName, "selectByIdOptionMap", __result)
        __result
      } catch {
        case __e: java.lang.RuntimeException =>
          throwing(classOf[PersonDao].getName, "selectByIdOptionMap", __e)
          throw __e
      }
    }

    override def selectAllStreamMap(f: Stream[Map[String, Any]] => Int): Int = {
      domala.internal.macros.reflect.DaoReflectionMacros.validateParameterAndSql(classOf[PersonDao], "selectAllStreamMap", true, false, "\nselect *\nfrom person\norder by id\n  ")
      entering(classOf[PersonDao].getName, "selectAllStreamMap", f.asInstanceOf[Object])
      try {
        val __query = new domala.jdbc.query.SqlAnnotationSelectQuery("\nselect *\nfrom person\norder by id\n  ")
        if (f == null) throw new org.seasar.doma.DomaNullPointerException("f")
        __query.setMethod(__method15)
        __query.setConfig(__config)
        __query.addParameter("f", classOf[java.util.function.Function[Stream[Map[String, Any]], _]], f)
        __query.setCallerClassName(classOf[PersonDao].getName)
        __query.setCallerMethodName("selectAllStreamMap")
        __query.setResultEnsured(false)
        __query.setResultMappingEnsured(false)
        __query.setFetchType(org.seasar.doma.FetchType.LAZY)
        __query.setQueryTimeout(-1)
        __query.setMaxRows(-1)
        __query.setFetchSize(-1)
        __query.setSqlLogType(org.seasar.doma.jdbc.SqlLogType.FORMATTED)
        __query.setResultStream(true)
        __query.prepare()
        val __result: Int = getCommandImplementors.createSelectCommand(__method15, __query, new org.seasar.doma.internal.jdbc.command.MapStreamHandler[Int](org.seasar.doma.MapKeyNamingType.NONE, p => f(domala.internal.WrapStream.of(p).map(_.asScala.toMap)))).execute()
        __query.complete()
        exiting(classOf[PersonDao].getName, "selectAllStreamMap", __result)
        __result
      } catch {
        case __e: java.lang.RuntimeException =>
          throwing(classOf[PersonDao].getName, "selectAllStreamMap", __e)
          throw __e
      }
    }

    override def selectAllIteratorMap(f: Iterator[Map[String, Any]] => Int): Int = {
      domala.internal.macros.reflect.DaoReflectionMacros.validateParameterAndSql(classOf[PersonDao], "selectAllIteratorMap", true, false, "\nselect *\nfrom person\norder by id\n  ")
      entering(classOf[PersonDao].getName, "selectAllIteratorMap", f.asInstanceOf[Object])
      try {
        val __query = new domala.jdbc.query.SqlAnnotationSelectQuery("\nselect *\nfrom person\norder by id\n  ")
        if (f == null) throw new org.seasar.doma.DomaNullPointerException("f")
        __query.setMethod(__method16)
        __query.setConfig(__config)
        __query.addParameter("f", classOf[java.util.function.Function[Iterator[Map[String, Any]], _]], f)
        __query.setCallerClassName(classOf[PersonDao].getName)
        __query.setCallerMethodName("selectAllIteratorMap")
        __query.setResultEnsured(false)
        __query.setResultMappingEnsured(false)
        __query.setFetchType(org.seasar.doma.FetchType.LAZY)
        __query.setQueryTimeout(-1)
        __query.setMaxRows(-1)
        __query.setFetchSize(-1)
        __query.setSqlLogType(org.seasar.doma.jdbc.SqlLogType.FORMATTED)
        __query.setResultStream(true)
        __query.prepare()
        val __result: Int = getCommandImplementors.createSelectCommand(__method16, __query, new org.seasar.doma.internal.jdbc.command.MapStreamHandler[Int](org.seasar.doma.MapKeyNamingType.NONE, p => f(domala.internal.WrapIterator.of(p).map(_.asScala.toMap)))).execute()
        __query.complete()
        exiting(classOf[PersonDao].getName, "selectAllIteratorMap", __result)
        __result
      } catch {
        case __e: java.lang.RuntimeException =>
          throwing(classOf[PersonDao].getName, "selectAllIteratorMap", __e)
          throw __e
      }
    }

    override def selectNameById(id: Int): Option[Name] = {
      domala.internal.macros.reflect.DaoReflectionMacros.validateParameterAndSql(classOf[PersonDao], "selectNameById", true, false, "\nselect name\nfrom person\nwhere\n    id = /*id*/0\n  ", domala.internal.macros.DaoParamClass("id", classOf[Int]))
      entering(classOf[PersonDao].getName, "selectNameById", id.asInstanceOf[Object])
      try {
        val __query = new domala.jdbc.query.SqlAnnotationSelectQuery("\nselect name\nfrom person\nwhere\n    id = /*id*/0\n  ")
        __query.setMethod(__method17)
        __query.setConfig(__config)
        domala.internal.macros.reflect.DaoReflectionMacros.setEntityType[Name](__query)
        __query.addParameter("id", classOf[Int], id)
        __query.setCallerClassName(classOf[PersonDao].getName)
        __query.setCallerMethodName("selectNameById")
        __query.setResultEnsured(false)
        __query.setResultMappingEnsured(false)
        __query.setFetchType(org.seasar.doma.FetchType.LAZY)
        __query.setQueryTimeout(-1)
        __query.setMaxRows(-1)
        __query.setFetchSize(-1)
        __query.setSqlLogType(org.seasar.doma.jdbc.SqlLogType.FORMATTED)
        __query.prepare()
        val __result: Option[Name] = getCommandImplementors.createSelectCommand(__method17, __query, domala.internal.macros.reflect.DaoReflectionMacros.getOptionSingleResultHandler[PersonDao, Name](classOf[PersonDao], "selectNameById")).execute()
        __query.complete()
        exiting(classOf[PersonDao].getName, "selectNameById", __result)
        __result
      } catch {
        case __e: java.lang.RuntimeException =>
          throwing(classOf[PersonDao].getName, "selectNameById", __e)
          throw __e
      }
    }

    override def selectNameByIdNullable(id: Int): Name = {
      domala.internal.macros.reflect.DaoReflectionMacros.validateParameterAndSql(classOf[PersonDao], "selectNameByIdNullable", true, false, "\nselect name\nfrom person\nwhere\n    id = /*id*/0\n  ", domala.internal.macros.DaoParamClass("id", classOf[Int]))
      entering(classOf[PersonDao].getName, "selectNameByIdNullable", id.asInstanceOf[Object])
      try {
        val __query = new domala.jdbc.query.SqlAnnotationSelectQuery("\nselect name\nfrom person\nwhere\n    id = /*id*/0\n  ")
        __query.setMethod(__method18)
        __query.setConfig(__config)
        domala.internal.macros.reflect.DaoReflectionMacros.setEntityType[Name](__query)
        __query.addParameter("id", classOf[Int], id)
        __query.setCallerClassName(classOf[PersonDao].getName)
        __query.setCallerMethodName("selectNameByIdNullable")
        __query.setResultEnsured(false)
        __query.setResultMappingEnsured(false)
        __query.setFetchType(org.seasar.doma.FetchType.LAZY)
        __query.setQueryTimeout(-1)
        __query.setMaxRows(-1)
        __query.setFetchSize(-1)
        __query.setSqlLogType(org.seasar.doma.jdbc.SqlLogType.FORMATTED)
        __query.prepare()
        val __result: Name = domala.internal.macros.reflect.DaoReflectionMacros.getOtherResult[PersonDao, Name](classOf[PersonDao], "selectNameByIdNullable", getCommandImplementors, __query, __method18)
        __query.complete()
        exiting(classOf[PersonDao].getName, "selectNameByIdNullable", __result)
        __result
      } catch {
        case __e: java.lang.RuntimeException =>
          throwing(classOf[PersonDao].getName, "selectNameByIdNullable", __e)
          throw __e
      }
    }

    override def selectNames: Seq[Name] = {
      domala.internal.macros.reflect.DaoReflectionMacros.validateParameterAndSql(classOf[PersonDao], "selectNames", true, false, "\nselect name\nfrom person\norder by id\n  ")
      entering(classOf[PersonDao].getName, "selectNames")
      try {
        val __query = new domala.jdbc.query.SqlAnnotationSelectQuery("\nselect name\nfrom person\norder by id\n  ")
        __query.setMethod(__method19)
        __query.setConfig(__config)
        domala.internal.macros.reflect.DaoReflectionMacros.setEntityType[Name](__query)
        __query.setCallerClassName(classOf[PersonDao].getName)
        __query.setCallerMethodName("selectNames")
        __query.setResultEnsured(false)
        __query.setResultMappingEnsured(false)
        __query.setFetchType(org.seasar.doma.FetchType.LAZY)
        __query.setQueryTimeout(-1)
        __query.setMaxRows(-1)
        __query.setFetchSize(-1)
        __query.setSqlLogType(org.seasar.doma.jdbc.SqlLogType.FORMATTED)
        __query.prepare()
        val __result: Seq[Name] = getCommandImplementors.createSelectCommand(__method19, __query, domala.internal.macros.reflect.DaoReflectionMacros.getResultListHandler[PersonDao, Name](classOf[PersonDao], "selectNames")).execute().asScala
        __query.complete()
        exiting(classOf[PersonDao].getName, "selectNames", __result)
        __result
      } catch {
        case __e: java.lang.RuntimeException =>
          throwing(classOf[PersonDao].getName, "selectNames", __e)
          throw __e
      }
    }

    override def selectNameStream(f: Stream[Name] => Int): Int = {
      domala.internal.macros.reflect.DaoReflectionMacros.validateParameterAndSql(classOf[PersonDao], "selectNameStream", true, false, "\nselect name\nfrom person\norder by id\n  ")
      entering(classOf[PersonDao].getName, "selectNameStream", f.asInstanceOf[Object])
      try {
        val __query = new domala.jdbc.query.SqlAnnotationSelectQuery("\nselect name\nfrom person\norder by id\n  ")
        if (f == null) throw new org.seasar.doma.DomaNullPointerException("f")
        __query.setMethod(__method20)
        __query.setConfig(__config)
        domala.internal.macros.reflect.DaoReflectionMacros.setEntityType[Name](__query)
        __query.addParameter("f", classOf[java.util.function.Function[Stream[Name], _]], f)
        __query.setCallerClassName(classOf[PersonDao].getName)
        __query.setCallerMethodName("selectNameStream")
        __query.setResultEnsured(false)
        __query.setResultMappingEnsured(false)
        __query.setFetchType(org.seasar.doma.FetchType.LAZY)
        __query.setQueryTimeout(-1)
        __query.setMaxRows(-1)
        __query.setFetchSize(-1)
        __query.setSqlLogType(org.seasar.doma.jdbc.SqlLogType.FORMATTED)
        __query.setResultStream(true)
        __query.prepare()
        val __result: Int = getCommandImplementors.createSelectCommand(__method20, __query, domala.internal.macros.reflect.DaoReflectionMacros.getStreamHandler(f, classOf[PersonDao], "selectNameStream")).execute()
        __query.complete()
        exiting(classOf[PersonDao].getName, "selectNameStream", __result)
        __result
      } catch {
        case __e: java.lang.RuntimeException =>
          throwing(classOf[PersonDao].getName, "selectNameStream", __e)
          throw __e
      }
    }

    override def selectNameIterator(f: Iterator[Name] => Int): Int = {
      domala.internal.macros.reflect.DaoReflectionMacros.validateParameterAndSql(classOf[PersonDao], "selectNameIterator", true, false, "\nselect name\nfrom person\norder by id\n  ")
      entering(classOf[PersonDao].getName, "selectNameIterator", f.asInstanceOf[Object])
      try {
        val __query = new domala.jdbc.query.SqlAnnotationSelectQuery("\nselect name\nfrom person\norder by id\n  ")
        if (f == null) throw new org.seasar.doma.DomaNullPointerException("f")
        __query.setMethod(__method21)
        __query.setConfig(__config)
        domala.internal.macros.reflect.DaoReflectionMacros.setEntityType[Name](__query)
        __query.addParameter("f", classOf[java.util.function.Function[Iterator[Name], _]], f)
        __query.setCallerClassName(classOf[PersonDao].getName)
        __query.setCallerMethodName("selectNameIterator")
        __query.setResultEnsured(false)
        __query.setResultMappingEnsured(false)
        __query.setFetchType(org.seasar.doma.FetchType.LAZY)
        __query.setQueryTimeout(-1)
        __query.setMaxRows(-1)
        __query.setFetchSize(-1)
        __query.setSqlLogType(org.seasar.doma.jdbc.SqlLogType.FORMATTED)
        __query.setResultStream(true)
        __query.prepare()
        val __result: Int = getCommandImplementors.createSelectCommand(__method21, __query, domala.internal.macros.reflect.DaoReflectionMacros.getIteratorHandler(f, classOf[PersonDao], "selectNameIterator")).execute()
        __query.complete()
        exiting(classOf[PersonDao].getName, "selectNameIterator", __result)
        __result
      } catch {
        case __e: java.lang.RuntimeException =>
          throwing(classOf[PersonDao].getName, "selectNameIterator", __e)
          throw __e
      }
    }

    override def insert(person: Person): Result[Person] = {
      val __desc = domala.internal.macros.reflect.DaoReflectionMacros.validateAutoModifyParam(classOf[PersonDao], "insert", classOf[Person])
      entering(classOf[PersonDao].getName, "insert", person)
      try {
        if (person == null) {
          throw new org.seasar.doma.DomaNullPointerException("person")
        }
        val __query = getQueryImplementors.createAutoInsertQuery(__method22, __desc)
        __query.setMethod(__method22)
        __query.setConfig(__config)
        __query.setEntity(person)
        __query.setCallerClassName(classOf[PersonDao].getName)
        __query.setCallerMethodName("insert")
        __query.setQueryTimeout(-1)
        __query.setSqlLogType(org.seasar.doma.jdbc.SqlLogType.FORMATTED)
        __query.setNullExcluded(false)
        __query.setIncludedPropertyNames()
        __query.setExcludedPropertyNames()
        __query.prepare()
        val __command = getCommandImplementors.createInsertCommand(__method22, __query)
        val __count = __command.execute()
        __query.complete()
        val __result = domala.jdbc.Result[Person](__count, __query.getEntity)
        exiting(classOf[PersonDao].getName, "insert", __result)
        __result
      } catch {
        case __e: java.lang.RuntimeException =>
          throwing(classOf[PersonDao].getName, "insert", __e)
          throw __e
      }
    }

    override def update(person: Person): Result[Person] = {
      val __desc = domala.internal.macros.reflect.DaoReflectionMacros.validateAutoModifyParam(classOf[PersonDao], "update", classOf[Person])
      entering(classOf[PersonDao].getName, "update", person)
      try {
        if (person == null) {
          throw new org.seasar.doma.DomaNullPointerException("person")
        }
        val __query = getQueryImplementors.createAutoUpdateQuery(__method23, __desc)
        __query.setMethod(__method23)
        __query.setConfig(__config)
        __query.setEntity(person)
        __query.setCallerClassName(classOf[PersonDao].getName)
        __query.setCallerMethodName("update")
        __query.setQueryTimeout(-1)
        __query.setSqlLogType(org.seasar.doma.jdbc.SqlLogType.FORMATTED)
        __query.setNullExcluded(false)
        __query.setVersionIgnored(false)
        __query.setIncludedPropertyNames()
        __query.setExcludedPropertyNames()
        __query.setUnchangedPropertyIncluded(false)
        __query.setOptimisticLockExceptionSuppressed(false)
        __query.prepare()
        val __command = getCommandImplementors.createUpdateCommand(__method23, __query)
        val __count = __command.execute()
        __query.complete()
        val __result = domala.jdbc.Result[Person](__count, __query.getEntity)
        exiting(classOf[PersonDao].getName, "update", __result)
        __result
      } catch {
        case __e: java.lang.RuntimeException =>
          throwing(classOf[PersonDao].getName, "update", __e)
          throw __e
      }
    }

    override def delete(person: Person): Int = {
      val __desc = domala.internal.macros.reflect.DaoReflectionMacros.validateAutoModifyParam(classOf[PersonDao], "delete", classOf[Person])
      entering(classOf[PersonDao].getName, "delete", person)
      try {
        if (person == null) {
          throw new org.seasar.doma.DomaNullPointerException("person")
        }
        val __query = getQueryImplementors.createAutoDeleteQuery(__method24, __desc)
        __query.setMethod(__method24)
        __query.setConfig(__config)
        __query.setEntity(person)
        __query.setCallerClassName(classOf[PersonDao].getName)
        __query.setCallerMethodName("delete")
        __query.setQueryTimeout(-1)
        __query.setSqlLogType(org.seasar.doma.jdbc.SqlLogType.FORMATTED)
        __query.setVersionIgnored(false)
        __query.setOptimisticLockExceptionSuppressed(false)
        __query.prepare()
        val __command = getCommandImplementors.createDeleteCommand(__method24, __query)
        val __count = __command.execute()
        __query.complete()
        val __result = __count
        exiting(classOf[PersonDao].getName, "delete", __result)
        __result
      } catch {
        case __e: java.lang.RuntimeException =>
          throwing(classOf[PersonDao].getName, "delete", __e)
          throw __e
      }
    }

    override def batchInsert(persons: Seq[Person]): BatchResult[Person] = {
      val __desc = domala.internal.macros.reflect.DaoReflectionMacros.validateAutoBatchModifyParam(classOf[PersonDao], "batchInsert", classOf[Seq[Person]], classOf[Person])
      entering(classOf[PersonDao].getName, "batchInsert", persons)
      try {
        if (persons == null) {
          throw new org.seasar.doma.DomaNullPointerException("persons")
        }
        val __query = getQueryImplementors.createAutoBatchInsertQuery(__method25, __desc)
        __query.setMethod(__method25)
        __query.setConfig(__config)
        __query.setEntities(persons.asJava)
        __query.setCallerClassName(classOf[PersonDao].getName)
        __query.setCallerMethodName("batchInsert")
        __query.setBatchSize(-1)
        __query.setQueryTimeout(-1)
        __query.setSqlLogType(org.seasar.doma.jdbc.SqlLogType.FORMATTED)
        __query.setIncludedPropertyNames()
        __query.setExcludedPropertyNames()
        __query.prepare()
        val __command = getCommandImplementors.createBatchInsertCommand(__method25, __query)
        val __count = __command.execute()
        __query.complete()
        val __result = domala.jdbc.BatchResult[Person](__count, __query.getEntities.asScala)
        exiting(classOf[PersonDao].getName, "batchInsert", __result)
        __result
      } catch {
        case __e: java.lang.RuntimeException =>
          throwing(classOf[PersonDao].getName, "batchInsert", __e)
          throw __e
      }
    }

    override def batchUpdate(persons: Seq[Person]): BatchResult[Person] = {
      val __desc = domala.internal.macros.reflect.DaoReflectionMacros.validateAutoBatchModifyParam(classOf[PersonDao], "batchUpdate", classOf[Seq[Person]], classOf[Person])
      entering(classOf[PersonDao].getName, "batchUpdate", persons)
      try {
        if (persons == null) {
          throw new org.seasar.doma.DomaNullPointerException("persons")
        }
        val __query = getQueryImplementors.createAutoBatchUpdateQuery(__method26, __desc)
        __query.setMethod(__method26)
        __query.setConfig(__config)
        __query.setEntities(persons.asJava)
        __query.setCallerClassName(classOf[PersonDao].getName)
        __query.setCallerMethodName("batchUpdate")
        __query.setBatchSize(-1)
        __query.setQueryTimeout(-1)
        __query.setSqlLogType(org.seasar.doma.jdbc.SqlLogType.FORMATTED)
        __query.setVersionIgnored(false)
        __query.setIncludedPropertyNames()
        __query.setExcludedPropertyNames()
        __query.setOptimisticLockExceptionSuppressed(false)
        __query.prepare()
        val __command = getCommandImplementors.createBatchUpdateCommand(__method26, __query)
        val __count = __command.execute()
        __query.complete()
        val __result = domala.jdbc.BatchResult[Person](__count, __query.getEntities.asScala)
        exiting(classOf[PersonDao].getName, "batchUpdate", __result)
        __result
      } catch {
        case __e: java.lang.RuntimeException =>
          throwing(classOf[PersonDao].getName, "batchUpdate", __e)
          throw __e
      }
    }

    override def batchDelete(persons: Seq[Person]): Array[Int] = {
      val __desc = domala.internal.macros.reflect.DaoReflectionMacros.validateAutoBatchModifyParam(classOf[PersonDao], "batchDelete", classOf[Seq[Person]], classOf[Person])
      entering(classOf[PersonDao].getName, "batchDelete", persons)
      try {
        if (persons == null) {
          throw new org.seasar.doma.DomaNullPointerException("persons")
        }
        val __query = getQueryImplementors.createAutoBatchDeleteQuery(__method27, __desc)
        __query.setMethod(__method27)
        __query.setConfig(__config)
        __query.setEntities(persons.asJava)
        __query.setCallerClassName(classOf[PersonDao].getName)
        __query.setCallerMethodName("batchDelete")
        __query.setBatchSize(-1)
        __query.setQueryTimeout(-1)
        __query.setSqlLogType(org.seasar.doma.jdbc.SqlLogType.FORMATTED)
        __query.setVersionIgnored(false)
        __query.setOptimisticLockExceptionSuppressed(false)
        __query.prepare()
        val __command = getCommandImplementors.createBatchDeleteCommand(__method27, __query)
        val __count = __command.execute()
        __query.complete()
        val __result = __count
        exiting(classOf[PersonDao].getName, "batchDelete", __result)
        __result
      } catch {
        case __e: java.lang.RuntimeException =>
          throwing(classOf[PersonDao].getName, "batchDelete", __e)
          throw __e
      }
    }

    override def insertSql(entity: Person, entity2: Person, version: Int): Result[Person] = {
      domala.internal.macros.reflect.DaoReflectionMacros.validateParameterAndSql(classOf[PersonDao], "insertSql", false, false, "\ninsert into person(id, name, age, city, street, department_id, version)\nvalues(\n  /* entity.id */0,\n  /* entity.name */'hoge',\n  /* entity.age */0,\n  /* entity2.address.city */'hoge',\n  /* entity2.address.street */'hoge',\n  /* 2 */0,\n  /* version */0)\n  ", domala.internal.macros.DaoParamClass.apply("entity", classOf[Person]), domala.internal.macros.DaoParamClass.apply("entity2", classOf[Person]), domala.internal.macros.DaoParamClass.apply("version", classOf[Int]))
      entering(classOf[PersonDao].getName, "insertSql", entity.asInstanceOf[Object], entity2.asInstanceOf[Object], version.asInstanceOf[Object])
      try {
        val __query = new domala.jdbc.query.SqlAnnotationInsertQuery("\ninsert into person(id, name, age, city, street, department_id, version)\nvalues(\n  /* entity.id */0,\n  /* entity.name */'hoge',\n  /* entity.age */0,\n  /* entity2.address.city */'hoge',\n  /* entity2.address.street */'hoge',\n  /* 2 */0,\n  /* version */0)\n  ")(domala.internal.macros.reflect.DaoReflectionMacros.getEntityAndEntityDesc(classOf[PersonDao], "insertSql", classOf[Result[Person]], domala.internal.macros.DaoParam.apply("entity", entity, classOf[Person]), domala.internal.macros.DaoParam.apply("entity2", entity2, classOf[Person]), domala.internal.macros.DaoParam.apply("version", version, classOf[Int])))
        if (entity == null) {
          throw new org.seasar.doma.DomaNullPointerException("entity")
        }
        if (entity2 == null) {
          throw new org.seasar.doma.DomaNullPointerException("entity2")
        }
        ()
        __query.setMethod(__method28)
        __query.setConfig(__config)
        __query.addParameter("entity", classOf[Person], entity)
        __query.addParameter("entity2", classOf[Person], entity2)
        __query.addParameter("version", classOf[Int], version)
        __query.setCallerClassName(classOf[PersonDao].getName)
        __query.setCallerMethodName("insertSql")
        __query.setQueryTimeout(-1)
        __query.setSqlLogType(org.seasar.doma.jdbc.SqlLogType.FORMATTED)
        __query.prepare()
        val __command = getCommandImplementors.createInsertCommand(__method28, __query)
        val __count = __command.execute()
        __query.complete()
        val __result = domala.jdbc.Result(__count, __query.getEntity.asInstanceOf[Person])
        exiting(classOf[PersonDao].getName, "insertSql", __result)
        __result
      } catch {
        case __e: java.lang.RuntimeException =>
          throwing(classOf[PersonDao].getName, "insertSql", __e)
          throw __e
      }
    }

    override def updateSql(entity: Person, entity2: Person, version: Int): Result[Person] = {
      domala.internal.macros.reflect.DaoReflectionMacros.validateParameterAndSql(classOf[PersonDao], "updateSql", false, true, "\nupdate person set\n  name = /* entity.name */'hoge',\n  age = /* entity.age */0,\n  city = /* entity2.address.city */'hoge',\n  street = /* entity2.address.street */'hoge',\n  department_id = /* 2 */0,\n  version = version + 1\nwhere\n  id = /* entity.id */0 and\n  version = /* version */0\n  ", domala.internal.macros.DaoParamClass.apply("entity", classOf[Person]), domala.internal.macros.DaoParamClass.apply("entity2", classOf[Person]), domala.internal.macros.DaoParamClass.apply("version", classOf[Int]))
      entering(classOf[PersonDao].getName, "updateSql", entity.asInstanceOf[Object], entity2.asInstanceOf[Object], version.asInstanceOf[Object])
      try {
        val __query = new domala.jdbc.query.SqlAnnotationUpdateQuery("\nupdate person set\n  name = /* entity.name */'hoge',\n  age = /* entity.age */0,\n  city = /* entity2.address.city */'hoge',\n  street = /* entity2.address.street */'hoge',\n  department_id = /* 2 */0,\n  version = version + 1\nwhere\n  id = /* entity.id */0 and\n  version = /* version */0\n  ", false, false, false, Seq().toArray, Seq().toArray)(domala.internal.macros.reflect.DaoReflectionMacros.getEntityAndEntityDesc(classOf[PersonDao], "updateSql", classOf[Result[Person]], domala.internal.macros.DaoParam.apply("entity", entity, classOf[Person]), domala.internal.macros.DaoParam.apply("entity2", entity2, classOf[Person]), domala.internal.macros.DaoParam.apply("version", version, classOf[Int])))
        if (entity == null) {
          throw new org.seasar.doma.DomaNullPointerException("entity")
        }
        if (entity2 == null) {
          throw new org.seasar.doma.DomaNullPointerException("entity2")
        }
        ()
        __query.setMethod(__method29)
        __query.setConfig(__config)
        __query.addParameter("entity", classOf[Person], entity)
        __query.addParameter("entity2", classOf[Person], entity2)
        __query.addParameter("version", classOf[Int], version)
        __query.setCallerClassName(classOf[PersonDao].getName)
        __query.setCallerMethodName("updateSql")
        __query.setQueryTimeout(-1)
        __query.setSqlLogType(org.seasar.doma.jdbc.SqlLogType.FORMATTED)
        __query.prepare()
        val __command = getCommandImplementors.createUpdateCommand(__method29, __query)
        val __count = __command.execute()
        __query.complete()
        val __result = domala.jdbc.Result(__count, __query.getEntity.asInstanceOf[Person])
        exiting(classOf[PersonDao].getName, "updateSql", __result)
        __result
      } catch {
        case __e: java.lang.RuntimeException =>
          throwing(classOf[PersonDao].getName, "updateSql", __e)
          throw __e
      }
    }

    override def deleteSql(entity: Person, version: Int): Int = {
      domala.internal.macros.reflect.DaoReflectionMacros.validateParameterAndSql(classOf[PersonDao], "deleteSql", false, false, "\ndelete from person\nwhere\n  id = /* entity.id */0 and\n  version = /* version */0\n  ", domala.internal.macros.DaoParamClass.apply("entity", classOf[Person]), domala.internal.macros.DaoParamClass.apply("version", classOf[Int]))
      entering(classOf[PersonDao].getName, "deleteSql", entity.asInstanceOf[Object], version.asInstanceOf[Object])
      try {
        val __query = new domala.jdbc.query.SqlAnnotationDeleteQuery("\ndelete from person\nwhere\n  id = /* entity.id */0 and\n  version = /* version */0\n  ", false, false)(domala.internal.macros.reflect.DaoReflectionMacros.getEntityAndEntityDesc(classOf[PersonDao], "deleteSql", classOf[Int], domala.internal.macros.DaoParam.apply("entity", entity, classOf[Person]), domala.internal.macros.DaoParam.apply("version", version, classOf[Int])))
        if (entity == null) {
          throw new org.seasar.doma.DomaNullPointerException("entity")
        }
        ()
        __query.setMethod(__method30)
        __query.setConfig(__config)
        __query.addParameter("entity", classOf[Person], entity)
        __query.addParameter("version", classOf[Int], version)
        __query.setCallerClassName(classOf[PersonDao].getName)
        __query.setCallerMethodName("deleteSql")
        __query.setQueryTimeout(-1)
        __query.setSqlLogType(org.seasar.doma.jdbc.SqlLogType.FORMATTED)
        __query.prepare()
        val __command = getCommandImplementors.createDeleteCommand(__method30, __query)
        val __count = __command.execute()
        __query.complete()
        val __result = __count
        exiting(classOf[PersonDao].getName, "deleteSql", __result)
        __result
      } catch {
        case __e: java.lang.RuntimeException =>
          throwing(classOf[PersonDao].getName, "deleteSql", __e)
          throw __e
      }
    }

    override def selectAllByOption(options: SelectOptions): Seq[Person] = {
      domala.internal.macros.reflect.DaoReflectionMacros.validateParameterAndSql(classOf[PersonDao], "selectAllByOption", true, false, "\nselect *\nfrom person\norder by id\n  ")
      entering(classOf[PersonDao].getName, "selectAllByOption", options.asInstanceOf[Object])
      try {
        val __query = new domala.jdbc.query.SqlAnnotationSelectQuery("\nselect *\nfrom person\norder by id\n  ")
        __query.setMethod(__method31)
        __query.setConfig(__config)
        __query.setOptions(options)
        domala.internal.macros.reflect.DaoReflectionMacros.setEntityType[Person](__query)
        __query.addParameter("options", classOf[SelectOptions], options)
        __query.setCallerClassName(classOf[PersonDao].getName)
        __query.setCallerMethodName("selectAllByOption")
        __query.setResultEnsured(false)
        __query.setResultMappingEnsured(false)
        __query.setFetchType(org.seasar.doma.FetchType.LAZY)
        __query.setQueryTimeout(-1)
        __query.setMaxRows(-1)
        __query.setFetchSize(-1)
        __query.setSqlLogType(org.seasar.doma.jdbc.SqlLogType.FORMATTED)
        __query.prepare()
        val __result: Seq[Person] = getCommandImplementors.createSelectCommand(__method31, __query, domala.internal.macros.reflect.DaoReflectionMacros.getResultListHandler[PersonDao, Person](classOf[PersonDao], "selectAllByOption")).execute().asScala
        __query.complete()
        exiting(classOf[PersonDao].getName, "selectAllByOption", __result)
        __result
      } catch {
        case __e: java.lang.RuntimeException =>
          throwing(classOf[PersonDao].getName, "selectAllByOption", __e)
          throw __e
      }
    }

    override def batchInsertSql(persons: Seq[Person]): BatchResult[Person] = {
      domala.internal.macros.reflect.DaoReflectionMacros.validateBatchParameterAndSql(classOf[PersonDao], "batchInsertSql", false, false, "\ninsert into person(id, name, age, city, street, department_id, version)\nvalues(\n  /* persons.id */0,\n  /* persons.name */'hoge',\n  /* persons.age */0,\n  /* persons.address.city */'hoge',\n  /* persons.address.street */'hoge',\n  /* 2 */0,\n  /* persons.version */0)\n  ", domala.internal.macros.DaoParamClass.apply("persons", classOf[Person]))
      entering(classOf[PersonDao].getName, "batchInsertSql", persons)
      try {
        val __query = new domala.jdbc.query.SqlAnnotationBatchInsertQuery(classOf[Person], "\ninsert into person(id, name, age, city, street, department_id, version)\nvalues(\n  /* persons.id */0,\n  /* persons.name */'hoge',\n  /* persons.age */0,\n  /* persons.address.city */'hoge',\n  /* persons.address.street */'hoge',\n  /* 2 */0,\n  /* persons.version */0)\n  ")(domala.internal.macros.reflect.DaoReflectionMacros.getBatchEntityDesc(classOf[PersonDao], "batchInsertSql", classOf[BatchResult[Person]], domala.internal.macros.DaoParam.apply("persons", persons, classOf[Seq[Person]])))
        if (persons == null) {
          throw new org.seasar.doma.DomaNullPointerException("persons")
        }
        __query.setMethod(__method32)
        __query.setConfig(__config)
        __query.setElements(persons)
        __query.setParameterName("persons")
        __query.setCallerClassName(classOf[PersonDao].getName)
        __query.setCallerMethodName("batchInsertSql")
        __query.setQueryTimeout(-1)
        __query.setBatchSize(100)
        __query.setSqlLogType(org.seasar.doma.jdbc.SqlLogType.FORMATTED)
        __query.prepare()
        val __command = getCommandImplementors.createBatchInsertCommand(__method32, __query)
        val __counts = __command.execute()
        __query.complete()
        val __result = domala.jdbc.BatchResult(__counts, __query.getEntities.asScala)
        exiting(classOf[PersonDao].getName, "batchInsertSql", __result)
        __result
      } catch {
        case __e: java.lang.RuntimeException =>
          throwing(classOf[PersonDao].getName, "batchInsertSql", __e)
          throw __e
      }
    }

    override def batchUpdateSql(persons: Seq[Person]): BatchResult[Person] = {
      domala.internal.macros.reflect.DaoReflectionMacros.validateBatchParameterAndSql(classOf[PersonDao], "batchUpdateSql", false, true, "\nupdate person set\n  name = /* persons.name */'hoge',\n  age = /* persons.age */0,\n  city = /* persons.address.city */'hoge',\n  street = /* persons.address.street */'hoge',\n  department_id = /* 2 */0,\n  version = version + 1\nwhere\n  id = /* persons.id */0 and\n  version = /* persons.version */0\n  ", domala.internal.macros.DaoParamClass.apply("persons", classOf[Person]))
      entering(classOf[PersonDao].getName, "batchUpdateSql", persons)
      try {
        val __query = new domala.jdbc.query.SqlAnnotationBatchUpdateQuery(classOf[Person], "\nupdate person set\n  name = /* persons.name */'hoge',\n  age = /* persons.age */0,\n  city = /* persons.address.city */'hoge',\n  street = /* persons.address.street */'hoge',\n  department_id = /* 2 */0,\n  version = version + 1\nwhere\n  id = /* persons.id */0 and\n  version = /* persons.version */0\n  ", false, false)(domala.internal.macros.reflect.DaoReflectionMacros.getBatchEntityDesc(classOf[PersonDao], "batchUpdateSql", classOf[BatchResult[Person]], domala.internal.macros.DaoParam.apply("persons", persons, classOf[Seq[Person]])))
        if (persons == null) {
          throw new org.seasar.doma.DomaNullPointerException("persons")
        }
        __query.setMethod(__method33)
        __query.setConfig(__config)
        __query.setElements(persons)
        __query.setParameterName("persons")
        __query.setCallerClassName(classOf[PersonDao].getName)
        __query.setCallerMethodName("batchUpdateSql")
        __query.setQueryTimeout(-1)
        __query.setBatchSize(100)
        __query.setSqlLogType(org.seasar.doma.jdbc.SqlLogType.FORMATTED)
        __query.prepare()
        val __command = getCommandImplementors.createBatchUpdateCommand(__method33, __query)
        val __counts = __command.execute()
        __query.complete()
        val __result = domala.jdbc.BatchResult(__counts, __query.getEntities.asScala)
        exiting(classOf[PersonDao].getName, "batchUpdateSql", __result)
        __result
      } catch {
        case __e: java.lang.RuntimeException =>
          throwing(classOf[PersonDao].getName, "batchUpdateSql", __e)
          throw __e
      }
    }

    override def batchDeleteSql(persons: Seq[Person]): Array[Int] = {
      domala.internal.macros.reflect.DaoReflectionMacros.validateBatchParameterAndSql(classOf[PersonDao], "batchDeleteSql", false, false, "\ndelete from person\nwhere\n  id = /* persons.id */0 and\n  name = /* persons.name */'hoge' and\n  version = /* persons.version */0\n  ", domala.internal.macros.DaoParamClass.apply("persons", classOf[Person]))
      entering(classOf[PersonDao].getName, "batchDeleteSql", persons)
      try {
        val __query = new domala.jdbc.query.SqlAnnotationBatchDeleteQuery(classOf[Person], "\ndelete from person\nwhere\n  id = /* persons.id */0 and\n  name = /* persons.name */'hoge' and\n  version = /* persons.version */0\n  ", false, false)(domala.internal.macros.reflect.DaoReflectionMacros.getBatchEntityDesc(classOf[PersonDao], "batchDeleteSql", classOf[Array[Int]], domala.internal.macros.DaoParam.apply("persons", persons, classOf[Seq[Person]])))
        if (persons == null) {
          throw new org.seasar.doma.DomaNullPointerException("persons")
        }
        __query.setMethod(__method34)
        __query.setConfig(__config)
        __query.setElements(persons)
        __query.setParameterName("persons")
        __query.setCallerClassName(classOf[PersonDao].getName)
        __query.setCallerMethodName("batchDeleteSql")
        __query.setQueryTimeout(-1)
        __query.setBatchSize(100)
        __query.setSqlLogType(org.seasar.doma.jdbc.SqlLogType.FORMATTED)
        __query.prepare()
        val __command = getCommandImplementors.createBatchDeleteCommand(__method34, __query)
        val __counts = __command.execute()
        __query.complete()
        val __result = __counts
        exiting(classOf[PersonDao].getName, "batchDeleteSql", __result)
        __result
      } catch {
        case __e: java.lang.RuntimeException =>
          throwing(classOf[PersonDao].getName, "batchDeleteSql", __e)
          throw __e
      }
    }
  }

}
