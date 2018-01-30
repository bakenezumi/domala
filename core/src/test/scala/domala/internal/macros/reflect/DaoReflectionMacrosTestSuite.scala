package domala.internal.macros.reflect

import domala.internal.jdbc.command._
import domala.internal.macros.DaoParam
import domala.internal.macros.reflect.mock.{MockEmbeddable, MockEntity, MockHolder}
import domala.jdbc.Result
import domala.jdbc.query.EntityAndEntityDesc
import org.scalatest.FunSuite
import org.seasar.doma.internal.jdbc.command._

class DaoReflectionMacrosTestSuite extends FunSuite {

  test("getOptionSingleResultHandler for Entity") {
    assert(DaoReflectionMacros.getOptionSingleResultHandler[DaoReflectionMacrosTestSuite, MockEntity](classOf[DaoReflectionMacrosTestSuite], "get Handler for Entity").isInstanceOf[OptionEntitySingleResultHandler[_]])
  }

  test("getOptionSingleResultHandler for Domain") {
    assert(DaoReflectionMacros.getOptionSingleResultHandler[DaoReflectionMacrosTestSuite, MockHolder](classOf[DaoReflectionMacrosTestSuite], "get Handler for Domain").isInstanceOf[OptionHolderSingleResultHandler[_, _]])
  }

  test("getOptionSingleResultHandler for Other") {
    //コンパイルエラー
    //DaoReflectionMacros.getOptionSingleResultHandler[DaoReflectionMacrosTestSuite, String](classOf[DaoReflectionMacrosTestSuite], "type error")
  }

  test("getResultListHandler for Entity") {
    assert(DaoReflectionMacros.getResultListHandler[DaoReflectionMacrosTestSuite, MockEntity](classOf[DaoReflectionMacrosTestSuite], "get Handler for Entity").isInstanceOf[EntityResultListHandler[_]])
  }

  test("getResultListHandler for Domain") {
    assert(DaoReflectionMacros.getResultListHandler[DaoReflectionMacrosTestSuite, MockHolder](classOf[DaoReflectionMacrosTestSuite], "get Handler for Domain").isInstanceOf[DomainResultListHandler[_, _]])
  }

  test("getResultListHandler for Other") {
    //コンパイルエラー
    //DaoReflectionMacros.getResultListHandler[DaoReflectionMacrosTestSuite, String](classOf[DaoReflectionMacrosTestSuite], "getResultListHandler")
  }

  test("getStreamHandler for Entity") {
    assert(DaoReflectionMacros.getStreamHandler((p: Stream[MockEntity]) => p.toString, classOf[DaoReflectionMacrosTestSuite], "get Handler for Entity").isInstanceOf[EntityStreamHandler[_, _]])
  }

  test("getStreamHandler for Domain") {
    assert(DaoReflectionMacros.getStreamHandler((p: Stream[MockHolder]) => p.toString, classOf[DaoReflectionMacrosTestSuite], "get Handler for Entity").isInstanceOf[DomainStreamHandler[_, _, _]])
  }

  test("getStreamHandler for Other") {
    //コンパイルエラー
    //assert(DaoReflectionMacros.getStreamHandler((p: Stream[String]) => p.toString, classOf[DaoReflectionMacrosTestSuite], "get Handler for Entity").isInstanceOf[DomainStreamHandler[_, _, _]])
  }

  test("getEntityAndEntityDesc has entity") {
    val entity1 = MockEntity(1, MockHolder("aa"), "bb", MockEmbeddable(2, "cc"), 2)
    val entity2 = MockEntity(2, MockHolder("bb"), "cc", MockEmbeddable(1, "dd"), 3)
    val ret = DaoReflectionMacros.getEntityAndEntityDesc(classOf[MockEntity], "method1", classOf[Int], DaoParam("aaa", 1, classOf[Int]), DaoParam("bbb", entity1, classOf[MockEntity]), DaoParam("ccc", entity2, classOf[MockEntity]))
    assert(ret == Some(EntityAndEntityDesc("bbb", entity1, MockEntity.entityDesc)))
  }

  test("getEntityAndEntityDesc has entity and return Result") {
    val entity1 = MockEntity(1, MockHolder("aa"), "aa", MockEmbeddable(2, "cc"),2)
    val entity2 = MockEntity(2, MockHolder("bb"), "bb", MockEmbeddable(1, "dd"),3)
    val ret = DaoReflectionMacros.getEntityAndEntityDesc(classOf[MockEntity], "method1", classOf[Result[MockEntity]], DaoParam("aaa", 1, classOf[Int]), DaoParam("bbb", entity1, classOf[MockEntity]), DaoParam("ccc", entity2, classOf[MockEntity]))
    assert(ret == Some(EntityAndEntityDesc("bbb", entity1, MockEntity.entityDesc)))
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


