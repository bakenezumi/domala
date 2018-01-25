package domala.internal.macros.reflect

import domala.internal.jdbc.command._
import domala.internal.macros.DaoParam
import domala.jdbc.Result
import domala.jdbc.query.EntityAndEntityDesc
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.seasar.doma.internal.jdbc.command._

class DaoReflectionMacrosTestSuite extends FunSuite with BeforeAndAfter {

  test("getOptionSingleResultHandler for Entity") {
    assert(DaoReflectionMacros.getOptionSingleResultHandler[DaoReflectionMacrosTestSuite, DummyEntity](classOf[DaoReflectionMacrosTestSuite], "get Handler for Entity").isInstanceOf[OptionEntitySingleResultHandler[_]])
  }

  test("getOptionSingleResultHandler for Domain") {
    assert(DaoReflectionMacros.getOptionSingleResultHandler[DaoReflectionMacrosTestSuite, DummyHolder](classOf[DaoReflectionMacrosTestSuite], "get Handler for Domain").isInstanceOf[OptionHolderSingleResultHandler[_, _]])
  }

  test("getOptionSingleResultHandler for Other") {
    //コンパイルエラー
    //DaoReflectionMacros.getOptionSingleResultHandler[DaoReflectionMacrosTestSuite, String](classOf[DaoReflectionMacrosTestSuite], "type error")
  }

  test("getResultListHandler for Entity") {
    assert(DaoReflectionMacros.getResultListHandler[DaoReflectionMacrosTestSuite, DummyEntity](classOf[DaoReflectionMacrosTestSuite], "get Handler for Entity").isInstanceOf[EntityResultListHandler[_]])
  }

  test("getResultListHandler for Domain") {
    assert(DaoReflectionMacros.getResultListHandler[DaoReflectionMacrosTestSuite, DummyHolder](classOf[DaoReflectionMacrosTestSuite], "get Handler for Domain").isInstanceOf[DomainResultListHandler[_, _]])
  }

  test("getResultListHandler for Other") {
    //コンパイルエラー
    //DaoReflectionMacros.getResultListHandler[DaoReflectionMacrosTestSuite, String](classOf[DaoReflectionMacrosTestSuite], "getResultListHandler")
  }

  test("getStreamHandler for Entity") {
    assert(DaoReflectionMacros.getStreamHandler((p: Stream[DummyEntity]) => p.toString, classOf[DaoReflectionMacrosTestSuite], "get Handler for Entity").isInstanceOf[EntityStreamHandler[_, _]])
  }

  test("getStreamHandler for Domain") {
    assert(DaoReflectionMacros.getStreamHandler((p: Stream[DummyHolder]) => p.toString, classOf[DaoReflectionMacrosTestSuite], "get Handler for Entity").isInstanceOf[DomainStreamHandler[_, _, _]])
  }

  test("getStreamHandler for Other") {
    //コンパイルエラー
    //assert(DaoReflectionMacros.getStreamHandler((p: Stream[String]) => p.toString, classOf[DaoReflectionMacrosTestSuite], "get Handler for Entity").isInstanceOf[DomainStreamHandler[_, _, _]])
  }

  test("getEntityAndEntityDesc has entity") {
    val entity1 = DummyEntity(1, DummyHolder("aa"), "aa", 2)
    val entity2 = DummyEntity(2, DummyHolder("bb"), "bb", 3)
    val ret = DaoReflectionMacros.getEntityAndEntityDesc(classOf[DummyEntity], "method1", classOf[Int], DaoParam("aaa", 1, classOf[Int]), DaoParam("bbb", entity1, classOf[DummyEntity]), DaoParam("ccc", entity2, classOf[DummyEntity]))
    assert(ret == Some(EntityAndEntityDesc("bbb", entity1, DummyEntity.entityDesc)))
  }

  test("getEntityAndEntityDesc has entity and return Result") {
    val entity1 = DummyEntity(1, DummyHolder("aa"), "aa", 2)
    val entity2 = DummyEntity(2, DummyHolder("bb"), "bb", 3)
    val ret = DaoReflectionMacros.getEntityAndEntityDesc(classOf[DummyEntity], "method1", classOf[Result[DummyEntity]], DaoParam("aaa", 1, classOf[Int]), DaoParam("bbb", entity1, classOf[DummyEntity]), DaoParam("ccc", entity2, classOf[DummyEntity]))
    assert(ret == Some(EntityAndEntityDesc("bbb", entity1, DummyEntity.entityDesc)))
  }


  test("getEntityAndEntityDesc has entity and return Other") {
    //コンパイルエラー
//    val entity1 = DummyEntity(1, null, "aa", 2)
//    val entity2 = DummyEntity(2, null, "bb", 3)
//    val ret = DaoReflectionMacros.getEntityAndEntityDesc(classOf[DummyEntity], "method1", classOf[Long], DaoParam("aaa", 1, classOf[Int]), DaoParam("bbb", entity1, classOf[DummyEntity]), DaoParam("ccc", entity2, classOf[DummyEntity]))
//    assert(ret == Some(EntityAndEntityDesc("bbb", entity1, DummyEntity.entityDesc)))
  }

  test("getEntityAndEntityDesc no entity") {
    val ret = DaoReflectionMacros.getEntityAndEntityDesc(classOf[Int], "method1", classOf[Int], DaoParam("aaa", 1, classOf[Int]), DaoParam("bbb", "aaa", classOf[String]))
    assert(ret.isEmpty)
  }

  test("getEntityAndEntityDesc no entity and return Other") {
    //コンパイルエラー
//    val ret = DaoReflectionMacros.getEntityAndEntityDesc(classOf[DummyEntity], "method1", classOf[String], DaoParam("aaa", 1, classOf[Int]), DaoParam("bbb", "aaa", classOf[String]))
//    assert(ret.isEmpty)
  }


}


