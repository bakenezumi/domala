package domala.internal.macros.reflect

import domala.internal.macros.DaoParam
import domala.jdbc.Result
import domala.jdbc.query.EntityAndEntityType
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.seasar.doma.internal.jdbc.command._

class DaoReflectionMacrosTestSuite extends FunSuite with BeforeAndAfter {
  
  test("getSingleResultHandler for Entity") {
    assert(DaoReflectionMacros.getSingleResultHandler(classOf[DummyEntity], "DaoRefrectionMacrosTestSuite", "get Handler for Entity").isInstanceOf[EntitySingleResultHandler[_]])
  }

  test("getSingleResultHandler for Domain") {
    assert(DaoReflectionMacros.getSingleResultHandler(classOf[DummyDomain], "DaoRefrectionMacrosTestSuite", "get Handler for Domain").isInstanceOf[DomainSingleResultHandler[_, _]])
  }

  test("getSingleResultHandler for Other") {
    //コンパイルエラー
    //DaoRefrectionMacros.getSingleResultHandler(classOf[String], "DaoRefrectionMacrosTestSuite", "type error")
  }

  test("getOptionalSingleResultHandler for Entity") {
    assert(DaoReflectionMacros.getOptionalSingleResultHandler(classOf[DummyEntity], "DaoRefrectionMacrosTestSuite", "get Handler for Entity").isInstanceOf[OptionalEntitySingleResultHandler[_]])
  }

  test("getOptinalSingleResultHandler for Domain") {
    assert(DaoReflectionMacros.getOptionalSingleResultHandler(classOf[DummyDomain], "DaoRefrectionMacrosTestSuite", "get Handler for Domain").isInstanceOf[OptionalDomainSingleResultHandler[_, _]])
  }

  test("getOptinalSingleResultHandler for Other") {
    //コンパイルエラー
    //DaoRefrectionMacros.getOptionalSingleResultHandler(classOf[String], "DaoRefrectionMacrosTestSuite", "type error")
  }

  test("getResultListHandler for Entity") {
    assert(DaoReflectionMacros.getResultListHandler(classOf[DummyEntity], "DaoRefrectionMacrosTestSuite", "get Handler for Entity").isInstanceOf[EntityResultListHandler[_]])
  }

  test("getResultListHandler for Domain") {
    assert(DaoReflectionMacros.getResultListHandler(classOf[DummyDomain], "DaoRefrectionMacrosTestSuite", "get Handler for Domain").isInstanceOf[DomainResultListHandler[_, _]])
  }

  test("getResultListHandler for Other") {
    //コンパイルエラー
    //DaoRefrectionMacros.getResultListHandler(classOf[String], "DaoRefrectionMacrosTestSuite", "getResultListHandler")
  }

  test("getStreamHandler for Entity") {
    assert(DaoReflectionMacros.getStreamHandler(classOf[DummyEntity], (p: Stream[DummyEntity]) => p.toString, "DaoRefrectionMacrosTestSuite", "get Handler for Entity").isInstanceOf[EntityStreamHandler[_, _]])
  }

  test("getStreamHandler for Domain") {
    assert(DaoReflectionMacros.getStreamHandler(classOf[DummyDomain], (p: Stream[DummyDomain]) => p.toString, "DaoRefrectionMacrosTestSuite", "get Handler for Entity").isInstanceOf[DomainStreamHandler[_, _, _]])
  }

  test("getStreamHandler for Other") {
    //コンパイルエラー
    //assert(DaoRefrectionMacros.getStreamHandler(classOf[String], (p: Stream[String]) => p.toString, "DaoRefrectionMacrosTestSuite", "get Handler for Entity").isInstanceOf[DomainStreamHandler[_, _, _]])
  }

  test("getEntityAndEntityType has entity") {
    val entity1 = DummyEntity(1, null, "aa", 2)
    val entity2 = DummyEntity(2, null, "bb", 3)
    val ret = DaoReflectionMacros.getEntityAndEntityType("Test1", "method1", classOf[Int], DaoParam("aaa", 1, classOf[Int]), DaoParam("bbb", entity1, classOf[DummyEntity]), DaoParam("ccc", entity2, classOf[DummyEntity]))
    assert(ret.contains(EntityAndEntityType("bbb", entity1, DummyEntity)))
  }

  test("getEntityAndEntityType has entity and return Result") {
    val entity1 = DummyEntity(1, null, "aa", 2)
    val entity2 = DummyEntity(2, null, "bb", 3)
    val ret = DaoReflectionMacros.getEntityAndEntityType("Test1", "method1", classOf[Result[DummyEntity]], DaoParam("aaa", 1, classOf[Int]), DaoParam("bbb", entity1, classOf[DummyEntity]), DaoParam("ccc", entity2, classOf[DummyEntity]))
    assert(ret.contains(EntityAndEntityType("bbb", entity1, DummyEntity)))
  }


  test("getEntityAndEntityType has entity and return Other") {
    //コンパイルエラー
//    val entity1 = DummyEntity(1, null, "aa", 2)
//    val entity2 = DummyEntity(2, null, "bb", 3)
//    val ret = DaoReflectionMacros.getEntityAndEntityType("Test1", "method1", classOf[Long], DaoParam("aaa", 1, classOf[Int]), DaoParam("bbb", entity1, classOf[DummyEntity]), DaoParam("ccc", entity2, classOf[DummyEntity]))
//    assert(ret.contains(EntityAndEntityType("bbb", entity1, DummyEntity)))
  }

  test("getEntityAndEntityType no entity") {
    val ret = DaoReflectionMacros.getEntityAndEntityType("Test1", "method1", classOf[Int], DaoParam("aaa", 1, classOf[Int]), DaoParam("bbb", "aaa", classOf[String]))
    assert(ret.isEmpty)
  }

  test("getEntityAndEntityType no entity and retrun Other") {
    //コンパイルエラー
//    val ret = DaoReflectionMacros.getEntityAndEntityType("Test1", "method1", classOf[String], DaoParam("aaa", 1, classOf[Int]), DaoParam("bbb", "aaa", classOf[String]))
//    assert(ret.isEmpty)
  }


}


