package domala.internal.macros.meta.generator

import domala.internal.macros.MacrosAbortException
import domala.message.Message
import org.scalatest.FunSuite

import scala.meta._

class DaoGeneratorTestSuite extends FunSuite {

  test("normal dao") {
    val trt = q"""
trait PersonDao {
  @Select(sql = "select * from person where id = /*id*/0")
  def selectById(id: Int): Option[Person]

  @Insert
  def insert(person: Person): Result[Person]

  @Update
  def update(person: Person): Result[Person]

  @Delete
  def delete(person: Person): Int

  @BatchInsert
  def batchInsert(persons: Seq[Person]): BatchResult[Person]

  @BatchUpdate
  def batchUpdate(persons: Seq[Person]): BatchResult[Person]

  @BatchDelete
  def batchDelete(persons: Seq[Person]): Array[Int]
}
    """

    val expect = q"""
{
  trait PersonDao {
    def selectById(id: Int): Option[Person]
    @Insert def insert(person: Person): Result[Person]
    @Update def update(person: Person): Result[Person]
    @Delete def delete(person: Person): Int
    @BatchInsert def batchInsert(persons: Seq[Person]): BatchResult[Person]
    @BatchUpdate def batchUpdate(persons: Seq[Person]): BatchResult[Person]
    @BatchDelete def batchDelete(persons: Seq[Person]): Array[Int]
  }
  object PersonDao {
    def impl(implicit config: domala.jdbc.Config): PersonDao = new Internal(config, Option(config).getOrElse(throw new org.seasar.doma.DomaNullPointerException("config")).getDataSource)
    def impl(connection: java.sql.Connection)(implicit config: domala.jdbc.Config): PersonDao = new Internal(config, connection)
    def impl(dataSource: javax.sql.DataSource)(implicit config: domala.jdbc.Config): PersonDao = new Internal(config, dataSource)
    private[this] val __method0 = domala.internal.jdbc.dao.DaoUtil.getDeclaredMethod(classOf[PersonDao], "selectById", classOf[Int])
    private[this] val __method1 = domala.internal.jdbc.dao.DaoUtil.getDeclaredMethod(classOf[PersonDao], "insert", classOf[Person])
    private[this] val __method2 = domala.internal.jdbc.dao.DaoUtil.getDeclaredMethod(classOf[PersonDao], "update", classOf[Person])
    private[this] val __method3 = domala.internal.jdbc.dao.DaoUtil.getDeclaredMethod(classOf[PersonDao], "delete", classOf[Person])
    private[this] val __method4 = domala.internal.jdbc.dao.DaoUtil.getDeclaredMethod(classOf[PersonDao], "batchInsert", classOf[Seq[Person]])
    private[this] val __method5 = domala.internal.jdbc.dao.DaoUtil.getDeclaredMethod(classOf[PersonDao], "batchUpdate", classOf[Seq[Person]])
    private[this] val __method6 = domala.internal.jdbc.dao.DaoUtil.getDeclaredMethod(classOf[PersonDao], "batchDelete", classOf[Seq[Person]])
    class Internal(___config: domala.jdbc.Config, dataSource: javax.sql.DataSource) extends org.seasar.doma.internal.jdbc.dao.AbstractDao(___config, dataSource) with PersonDao {
      def this(config: domala.jdbc.Config, connection: java.sql.Connection) = this(config, org.seasar.doma.internal.jdbc.dao.DomalaAbstractDaoHelper.toDataSource(connection))
      import scala.collection.JavaConverters._
      implicit val __sqlNodeRepository: domala.jdbc.SqlNodeRepository = ___config.getSqlNodeRepository
      override def selectById(id: Int): Option[Person] = {
        domala.internal.macros.reflect.DaoReflectionMacros.validateParameterAndSql(classOf[PersonDao], "selectById", true, false, "select * from person where id = /*id*/0", domala.internal.macros.DaoParamClass("id", classOf[Int]))
        entering(classOf[PersonDao].getName, "selectById", id.asInstanceOf[Object])
        try {
          val __query = new domala.jdbc.query.SqlAnnotationSelectQuery("select * from person where id = /*id*/0")
          __query.setMethod(__method0)
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
          val __result: Option[Person] = getCommandImplementors.createSelectCommand(__method0, __query, domala.internal.macros.reflect.DaoReflectionMacros.getOptionSingleResultHandler[PersonDao, Person](classOf[PersonDao], "selectById")).execute()
          __query.complete()
          exiting(classOf[PersonDao].getName, "selectById", __result)
          __result
        } catch {
          case __e: java.lang.RuntimeException =>
            throwing(classOf[PersonDao].getName, "selectById", __e)
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
          val __query = getQueryImplementors.createAutoInsertQuery(__method1, __desc)
          __query.setMethod(__method1)
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
          val __command = getCommandImplementors.createInsertCommand(__method1, __query)
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
          val __query = getQueryImplementors.createAutoUpdateQuery(__method2, __desc)
          __query.setMethod(__method2)
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
          val __command = getCommandImplementors.createUpdateCommand(__method2, __query)
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
          val __query = getQueryImplementors.createAutoDeleteQuery(__method3, __desc)
          __query.setMethod(__method3)
          __query.setConfig(__config)
          __query.setEntity(person)
          __query.setCallerClassName(classOf[PersonDao].getName)
          __query.setCallerMethodName("delete")
          __query.setQueryTimeout(-1)
          __query.setSqlLogType(org.seasar.doma.jdbc.SqlLogType.FORMATTED)
          __query.setVersionIgnored(false)
          __query.setOptimisticLockExceptionSuppressed(false)
          __query.prepare()
          val __command = getCommandImplementors.createDeleteCommand(__method3, __query)
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
          val __query = getQueryImplementors.createAutoBatchInsertQuery(__method4, __desc)
          __query.setMethod(__method4)
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
          val __command = getCommandImplementors.createBatchInsertCommand(__method4, __query)
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
          val __query = getQueryImplementors.createAutoBatchUpdateQuery(__method5, __desc)
          __query.setMethod(__method5)
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
          val __command = getCommandImplementors.createBatchUpdateCommand(__method5, __query)
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
          val __query = getQueryImplementors.createAutoBatchDeleteQuery(__method6, __desc)
          __query.setMethod(__method6)
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
          val __command = getCommandImplementors.createBatchDeleteCommand(__method6, __query)
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
    }
  }
}
    """
    val ret = DaoGenerator.generate(trt, null, None)
    assert(ret.syntax == expect.syntax)
  }

  test("illegal parameter name") {
    val trt = q"""
trait IllegalParameterNameDao {
  @Select("select * from test where name = /* __name */")
  def select(__name: String): Emp
}
"""
    val caught = intercept[MacrosAbortException] {
      DaoGenerator.generate(trt, null, None)
    }
    assert(caught.message == Message.DOMALA4025)
  }

  test("wildcard type parameter") {
    val trt = q"""
trait WildcardTypeParamDao {
  @Select("select * from test where height >= /* height */")
  def select(height: Height[_]): Emp
}
"""
    val caught = intercept[MacrosAbortException] {
      DaoGenerator.generate(trt, null, None)
    }
    assert(caught.message == Message.DOMALA4209)
  }

  test("annotation not found") {
    val trt = q"""
trait AnnotationNotFoundDao {

  def aaa(): Unit
}
"""
    val caught = intercept[MacrosAbortException] {
      DaoGenerator.generate(trt, null, None)
    }
    assert(caught.message == Message.DOMALA4005)
  }

  test("annotation conflicted") {
    val trt = q"""
trait AnnotationConflictedDao {
   @Update
   @Delete
  def delete(): Int
}
"""
    val caught = intercept[MacrosAbortException] {
      DaoGenerator.generate(trt, null, None)
    }
    assert(caught.message == Message.DOMALA4087)
  }

  test("illegal parameter") {
    val trt = q"""
trait IllegalParameterDao {
   @Insert
  def insert(dept: Dept): Result[Emp]
}
"""
    val caught = intercept[MacrosAbortException] {
      DaoGenerator.generate(trt, null, None)
    }
    assert(caught.message == Message.DOMALA4222)
  }

  test("illegal batch parameter") {
    val trt = q"""
trait IllegalBatchParameterDao {
   @BatchInsert
  def insert(dept: Seq[Dept]): BatchResult[Emp]
}
"""
    val caught = intercept[MacrosAbortException] {
      DaoGenerator.generate(trt, null, None)
    }
    assert(caught.message == Message.DOMALA4223)
  }

  test("no test literal") {
    val trt = q"""
trait NoTestLiteralDao {
   @Select("select * from Emp where id = /*id*/")
  def selectById(id: Int): Emp
}
"""
    val caught = intercept[MacrosAbortException] {
      DaoGenerator.generate(trt, null, None)
    }
    assert(caught.message == Message.DOMALA4069)
  }

  test("companion merge") {
    val trt = q"""
trait PersonDao {
  @Insert
  def insert(person: Person): Result[Person]
}
"""
    val companion = q"""
object PersonDao {
  def foo: Unit = println("bar")
}
"""

    val expect = q"""
{
  trait PersonDao {
    @Insert def insert(person: Person): Result[Person]
  }
  object PersonDao {
    def impl(implicit config: domala.jdbc.Config): PersonDao = new Internal(config, Option(config).getOrElse(throw new org.seasar.doma.DomaNullPointerException("config")).getDataSource)
    def impl(connection: java.sql.Connection)(implicit config: domala.jdbc.Config): PersonDao = new Internal(config, connection)
    def impl(dataSource: javax.sql.DataSource)(implicit config: domala.jdbc.Config): PersonDao = new Internal(config, dataSource)
    private[this] val __method0 = domala.internal.jdbc.dao.DaoUtil.getDeclaredMethod(classOf[PersonDao], "insert", classOf[Person])
    class Internal(___config: domala.jdbc.Config, dataSource: javax.sql.DataSource) extends org.seasar.doma.internal.jdbc.dao.AbstractDao(___config, dataSource) with PersonDao {
      def this(config: domala.jdbc.Config, connection: java.sql.Connection) = this(config, org.seasar.doma.internal.jdbc.dao.DomalaAbstractDaoHelper.toDataSource(connection))
      import scala.collection.JavaConverters._
      implicit val __sqlNodeRepository: domala.jdbc.SqlNodeRepository = ___config.getSqlNodeRepository
      override def insert(person: Person): Result[Person] = {
        val __desc = domala.internal.macros.reflect.DaoReflectionMacros.validateAutoModifyParam(classOf[PersonDao], "insert", classOf[Person])
        entering(classOf[PersonDao].getName, "insert", person)
        try {
          if (person == null) {
            throw new org.seasar.doma.DomaNullPointerException("person")
          }
          val __query = getQueryImplementors.createAutoInsertQuery(__method0, __desc)
          __query.setMethod(__method0)
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
          val __command = getCommandImplementors.createInsertCommand(__method0, __query)
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
    }
    def foo: Unit = println("bar")
  }
}
    """

    val ret = DaoGenerator.generate(trt, null, Some(companion))
    assert(ret.syntax == expect.syntax)
  }


}
