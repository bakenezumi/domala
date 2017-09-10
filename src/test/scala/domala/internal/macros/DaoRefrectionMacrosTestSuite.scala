package domala.internal.macros

import domala._
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.seasar.doma.internal.jdbc.command._

class DaoRefrectionMacrosTestSuite extends FunSuite with BeforeAndAfter {
  
  test("getSingleResultHandler for Entity") {
    assert(DaoRefrectionMacros.getSingleResultHandler(classOf[DummyEntity], "DaoRefrectionMacrosTestSuite", "get Handler for Entity").isInstanceOf[EntitySingleResultHandler[_]])
  }

  test("getSingleResultHandler for Domain") {
    assert(DaoRefrectionMacros.getSingleResultHandler(classOf[DummyDomain], "DaoRefrectionMacrosTestSuite", "get Handler for Domain").isInstanceOf[DomainSingleResultHandler[_, _]])
  }

  test("getSingleResultHandler for Other") {
    //コンパイルエラー
    //DaoRefrectionMacros.getSingleResultHandler(classOf[String], "DaoRefrectionMacrosTestSuite", "type error")
  }

  test("getOptionalSingleResultHandler for Entity") {
    assert(DaoRefrectionMacros.getOptionalSingleResultHandler(classOf[DummyEntity], "DaoRefrectionMacrosTestSuite", "get Handler for Entity").isInstanceOf[OptionalEntitySingleResultHandler[_]])
  }

  test("getOptinalSingleResultHandler for Domain") {
    assert(DaoRefrectionMacros.getOptionalSingleResultHandler(classOf[DummyDomain], "DaoRefrectionMacrosTestSuite", "get Handler for Domain").isInstanceOf[OptionalDomainSingleResultHandler[_, _]])
  }

  test("getOptinalSingleResultHandler for Other") {
    //コンパイルエラー
    //DaoRefrectionMacros.getOptionalSingleResultHandler(classOf[String], "DaoRefrectionMacrosTestSuite", "type error")
  }

  test("getResultListHandler for Entity") {
    assert(DaoRefrectionMacros.getResultListHandler(classOf[DummyEntity], "DaoRefrectionMacrosTestSuite", "get Handler for Entity").isInstanceOf[EntityResultListHandler[_]])
  }

  test("getResultListHandler for Domain") {
    assert(DaoRefrectionMacros.getResultListHandler(classOf[DummyDomain], "DaoRefrectionMacrosTestSuite", "get Handler for Domain").isInstanceOf[DomainResultListHandler[_, _]])
  }

  test("getResultListHandler for Other") {
    //コンパイルエラー
    //DaoRefrectionMacros.getResultListHandler(classOf[String], "DaoRefrectionMacrosTestSuite", "getResultListHandler")
  }

  test("getStreamHandler for Entity") {
    assert(DaoRefrectionMacros.getStreamHandler(classOf[DummyEntity], (p: Stream[DummyEntity]) => p.toString, "DaoRefrectionMacrosTestSuite", "get Handler for Entity").isInstanceOf[EntityStreamHandler[_, _]])
  }

  test("getStreamHandler for Domain") {
    assert(DaoRefrectionMacros.getStreamHandler(classOf[DummyDomain], (p: Stream[DummyDomain]) => p.toString, "DaoRefrectionMacrosTestSuite", "get Handler for Entity").isInstanceOf[DomainStreamHandler[_, _, _]])
  }

  test("getStreamHandler for Other") {
    //コンパイルエラー
    //assert(DaoRefrectionMacros.getStreamHandler(classOf[String], (p: Stream[String]) => p.toString, "DaoRefrectionMacrosTestSuite", "get Handler for Entity").isInstanceOf[DomainStreamHandler[_, _, _]])
  }


}

@Entity
case class DummyEntity(id: String)

@Domain
case class DummyDomain(value: String)
