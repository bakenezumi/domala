package domala.internal.macros.reflect

import domala.internal.macros.DaoParam
import domala.jdbc.Result
import domala.jdbc.query.EntityAndEntityType
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.seasar.doma.internal.jdbc.command._

class DaoReflectionMacrosTestSuite extends FunSuite with BeforeAndAfter {

  test("getOptionalSingleResultHandler for Entity") {
    assert(DaoReflectionMacros.getOptionalSingleResultHandler[DummyEntity]("DaoReflectionMacrosTestSuite", "get Handler for Entity").isInstanceOf[OptionalEntitySingleResultHandler[_]])
  }

  test("getOptionalSingleResultHandler for Domain") {
    assert(DaoReflectionMacros.getOptionalSingleResultHandler[DummyDomain]("DaoReflectionMacrosTestSuite", "get Handler for Domain").isInstanceOf[OptionalDomainSingleResultHandler[_, _]])
  }

  test("getOptionalSingleResultHandler for Other") {
    //コンパイルエラー
    //DaoReflectionMacros.getOptionalSingleResultHandler[String]( "DaoReflectionMacrosTestSuite", "type error")
  }

  test("getResultListHandler for Entity") {
    assert(DaoReflectionMacros.getResultListHandler[DummyEntity]("DaoReflectionMacrosTestSuite", "get Handler for Entity").isInstanceOf[EntityResultListHandler[_]])
  }

  test("getResultListHandler for Domain") {
    assert(DaoReflectionMacros.getResultListHandler[DummyDomain]("DaoReflectionMacrosTestSuite", "get Handler for Domain").isInstanceOf[DomainResultListHandler[_, _]])
  }

  test("getResultListHandler for Other") {
    //コンパイルエラー
    //DaoReflectionMacros.getResultListHandler[String]("DaoReflectionMacrosTestSuite", "getResultListHandler")
  }

  test("getStreamHandler for Entity") {
    assert(DaoReflectionMacros.getStreamHandler((p: Stream[DummyEntity]) => p.toString, "DaoReflectionMacrosTestSuite", "get Handler for Entity").isInstanceOf[EntityStreamHandler[_, _]])
  }

  test("getStreamHandler for Domain") {
    assert(DaoReflectionMacros.getStreamHandler((p: Stream[DummyDomain]) => p.toString, "DaoReflectionMacrosTestSuite", "get Handler for Entity").isInstanceOf[DomainStreamHandler[_, _, _]])
  }

  test("getStreamHandler for Other") {
    //コンパイルエラー
    //assert(DaoReflectionMacros.getStreamHandler((p: Stream[String]) => p.toString, "DaoReflectionMacrosTestSuite", "get Handler for Entity").isInstanceOf[DomainStreamHandler[_, _, _]])
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

  test("getEntityAndEntityType no entity and return Other") {
    //コンパイルエラー
//    val ret = DaoReflectionMacros.getEntityAndEntityType("Test1", "method1", classOf[String], DaoParam("aaa", 1, classOf[Int]), DaoParam("bbb", "aaa", classOf[String]))
//    assert(ret.isEmpty)
  }


}


