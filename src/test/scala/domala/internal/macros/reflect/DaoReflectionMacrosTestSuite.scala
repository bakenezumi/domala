package domala.internal.macros.reflect

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

}


