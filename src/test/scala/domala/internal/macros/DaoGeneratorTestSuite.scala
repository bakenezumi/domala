package domala.internal.macros

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
    def insert(person: Person): Result[Person]
    def update(person: Person): Result[Person]
    def delete(person: Person): Int
    def batchInsert(persons: Seq[Person]): BatchResult[Person]
    def batchUpdate(persons: Seq[Person]): BatchResult[Person]
    def batchDelete(persons: Seq[Person]): Array[Int]
  }
  object PersonDao {
    ()
    def impl(implicit config: domala.jdbc.Config): PersonDao = new Internal(config)
    class Internal(___config: domala.jdbc.Config) extends org.seasar.doma.internal.jdbc.dao.AbstractDao(___config) with PersonDao {
      import scala.collection.JavaConverters._
      implicit val __sqlNodeRepository: domala.jdbc.SqlNodeRepository = ___config.getSqlNodeRepository
      private[this] val __method0 = org.seasar.doma.internal.jdbc.dao.AbstractDao.getDeclaredMethod(classOf[PersonDao], "selectById", classOf[Int])
      override def selectById(id: Int): Option[Person] = {
        domala.internal.macros.reflect.DaoReflectionMacros.validateParameterAndSql("PersonDao", "selectById", true, false, "select * from person where id = /*id*/0", domala.internal.macros.DaoParamClass.apply("id", classOf[Int]))
        entering("PersonDao", "selectById", id.asInstanceOf[Object])
        try {
          val __query = new domala.jdbc.query.SqlAnnotationSelectQuery("select * from person where id = /*id*/0")
          __query.setMethod(__method0)
          __query.setConfig(___config)
          domala.internal.macros.reflect.DaoReflectionMacros.setEntityType[Person](__query)
          __query.addParameter("id", classOf[Int], id)
          __query.setCallerClassName("PersonDao")
          __query.setCallerMethodName("selectById")
          __query.setResultEnsured(false)
          __query.setResultMappingEnsured(false)
          __query.setFetchType(org.seasar.doma.FetchType.LAZY)
          __query.setQueryTimeout(-1)
          __query.setMaxRows(-1)
          __query.setFetchSize(-1)
          __query.setSqlLogType(org.seasar.doma.jdbc.SqlLogType.FORMATTED)
          __query.prepare()
          import domala.internal.OptionConverters._
          val __result: Option[Person] = getCommandImplementors.createSelectCommand(__method0, __query, domala.internal.macros.reflect.DaoReflectionMacros.getOptionalSingleResultHandler[Person]("PersonDao", "selectById")).execute().asScala
          __query.complete()
          exiting("PersonDao", "selectById", __result)
          __result
        } catch {
          case __e: java.lang.RuntimeException =>
            throwing("PersonDao", "selectById", __e)
            throw __e
        }
      }
      private[this] val __method1 = org.seasar.doma.internal.jdbc.dao.AbstractDao.getDeclaredMethod(classOf[PersonDao], "insert", classOf[Person])
      override def insert(person: Person): Result[Person] = {
        domala.internal.macros.reflect.DaoReflectionMacros.validateAutoModifyParam("PersonDao", "insert", classOf[Person])
        entering("PersonDao", "insert", person)
        try {
          if (person == null) {
            throw new org.seasar.doma.DomaNullPointerException("person")
          }
          val __query = getQueryImplementors.createAutoInsertQuery(__method1, Person)
          __query.setMethod(__method1)
          __query.setConfig(___config)
          __query.setEntity(person)
          __query.setCallerClassName("PersonDao")
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
          val __result = new domala.jdbc.Result[Person](__count, __query.getEntity)
          exiting("PersonDao", "insert", __result)
          __result
        } catch {
          case __e: java.lang.RuntimeException =>
            throwing("PersonDao", "insert", __e)
            throw __e
        }
      }
      private[this] val __method2 = org.seasar.doma.internal.jdbc.dao.AbstractDao.getDeclaredMethod(classOf[PersonDao], "update", classOf[Person])
      override def update(person: Person): Result[Person] = {
        domala.internal.macros.reflect.DaoReflectionMacros.validateAutoModifyParam("PersonDao", "update", classOf[Person])
        entering("PersonDao", "update", person)
        try {
          if (person == null) {
            throw new org.seasar.doma.DomaNullPointerException("person")
          }
          val __query = getQueryImplementors.createAutoUpdateQuery(__method2, Person)
          __query.setMethod(__method2)
          __query.setConfig(___config)
          __query.setEntity(person)
          __query.setCallerClassName("PersonDao")
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
          val __result = new domala.jdbc.Result[Person](__count, __query.getEntity)
          exiting("PersonDao", "update", __result)
          __result
        } catch {
          case __e: java.lang.RuntimeException =>
            throwing("PersonDao", "update", __e)
            throw __e
        }
      }
      private[this] val __method3 = org.seasar.doma.internal.jdbc.dao.AbstractDao.getDeclaredMethod(classOf[PersonDao], "delete", classOf[Person])
      override def delete(person: Person): Int = {
        domala.internal.macros.reflect.DaoReflectionMacros.validateAutoModifyParam("PersonDao", "delete", classOf[Person])
        entering("PersonDao", "delete", person)
        try {
          if (person == null) {
            throw new org.seasar.doma.DomaNullPointerException("person")
          }
          val __query = getQueryImplementors.createAutoDeleteQuery(__method3, Person)
          __query.setMethod(__method3)
          __query.setConfig(___config)
          __query.setEntity(person)
          __query.setCallerClassName("PersonDao")
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
          exiting("PersonDao", "delete", __result)
          __result
        } catch {
          case __e: java.lang.RuntimeException =>
            throwing("PersonDao", "delete", __e)
            throw __e
        }
      }
      private[this] val __method4 = org.seasar.doma.internal.jdbc.dao.AbstractDao.getDeclaredMethod(classOf[PersonDao], "batchInsert", classOf[Seq[_]])
      override def batchInsert(persons: Seq[Person]): BatchResult[Person] = {
        domala.internal.macros.reflect.DaoReflectionMacros.validateAutoBatchModifyParam("PersonDao", "batchInsert", classOf[Seq[Person]], classOf[Person])
        entering("PersonDao", "batchInsert", persons)
        try {
          if (persons == null) {
            throw new org.seasar.doma.DomaNullPointerException("persons")
          }
          val __query = getQueryImplementors.createAutoBatchInsertQuery(__method4, Person)
          __query.setMethod(__method4)
          __query.setConfig(___config)
          __query.setEntities(persons.asJava)
          __query.setCallerClassName("PersonDao")
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
          val __result = new domala.jdbc.BatchResult[Person](__count, __query.getEntities.asScala)
          exiting("PersonDao", "batchInsert", __result)
          __result
        } catch {
          case __e: java.lang.RuntimeException =>
            throwing("PersonDao", "batchInsert", __e)
            throw __e
        }
      }
      private[this] val __method5 = org.seasar.doma.internal.jdbc.dao.AbstractDao.getDeclaredMethod(classOf[PersonDao], "batchUpdate", classOf[Seq[_]])
      override def batchUpdate(persons: Seq[Person]): BatchResult[Person] = {
        domala.internal.macros.reflect.DaoReflectionMacros.validateAutoBatchModifyParam("PersonDao", "batchUpdate", classOf[Seq[Person]], classOf[Person])
        entering("PersonDao", "batchUpdate", persons)
        try {
          if (persons == null) {
            throw new org.seasar.doma.DomaNullPointerException("persons")
          }
          val __query = getQueryImplementors.createAutoBatchUpdateQuery(__method5, Person)
          __query.setMethod(__method5)
          __query.setConfig(___config)
          __query.setEntities(persons.asJava)
          __query.setCallerClassName("PersonDao")
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
          val __result = new domala.jdbc.BatchResult[Person](__count, __query.getEntities.asScala)
          exiting("PersonDao", "batchUpdate", __result)
          __result
        } catch {
          case __e: java.lang.RuntimeException =>
            throwing("PersonDao", "batchUpdate", __e)
            throw __e
        }
      }
      private[this] val __method6 = org.seasar.doma.internal.jdbc.dao.AbstractDao.getDeclaredMethod(classOf[PersonDao], "batchDelete", classOf[Seq[_]])
      override def batchDelete(persons: Seq[Person]): Array[Int] = {
        domala.internal.macros.reflect.DaoReflectionMacros.validateAutoBatchModifyParam("PersonDao", "batchDelete", classOf[Seq[Person]], classOf[Person])
        entering("PersonDao", "batchDelete", persons)
        try {
          if (persons == null) {
            throw new org.seasar.doma.DomaNullPointerException("persons")
          }
          val __query = getQueryImplementors.createAutoBatchDeleteQuery(__method6, Person)
          __query.setMethod(__method6)
          __query.setConfig(___config)
          __query.setEntities(persons.asJava)
          __query.setCallerClassName("PersonDao")
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
          exiting("PersonDao", "batchDelete", __result)
          __result
        } catch {
          case __e: java.lang.RuntimeException =>
            throwing("PersonDao", "batchDelete", __e)
            throw __e
        }
      }
    }
  }
}
    """
    val ret = DaoGenerator.generate(trt, null)
    assert(ret.syntax == expect.syntax)
  }

  test("illegal parameter name") {
    val trt = q"""
trait IllegalParameterNameDao {
  @Select("select * from test where name = /* __name */")
  def select(__name: String): Emp
}
"""
    val caught = intercept[MacrosException] {
      DaoGenerator.generate(trt, null)
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
    val caught = intercept[MacrosException] {
      DaoGenerator.generate(trt, null)
    }
    assert(caught.message == Message.DOMALA4209)
  }

  test("annotation not found") {
    val trt = q"""
trait AnnotationNotFoundDao {

  def aaa(): Unit
}
"""
    val caught = intercept[MacrosException] {
      DaoGenerator.generate(trt, null)
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
    val caught = intercept[MacrosException] {
      DaoGenerator.generate(trt, null)
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
    val caught = intercept[MacrosException] {
      DaoGenerator.generate(trt, null)
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
    val caught = intercept[MacrosException] {
      DaoGenerator.generate(trt, null)
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
    val caught = intercept[MacrosException] {
      DaoGenerator.generate(trt, null)
    }
    assert(caught.message == Message.DOMALA4069)
  }

}
