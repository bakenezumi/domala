package domala.internal.macros

import domala._
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.seasar.doma.internal.jdbc.command.{DomainSingleResultHandler, EntitySingleResultHandler}

class DaoRefrectionMacrosTestSuite extends FunSuite with BeforeAndAfter {
  test("Entity getgetSingleResultHandler") {
    assert(DaoRefrectionMacros.getSingleResultHandler(classOf[DummyEntity], "MacrosTestSuite", "Entity get").isInstanceOf[EntitySingleResultHandler[_]])
  }

  test("Domain getSingleResultHandler") {
    assert(DaoRefrectionMacros.getSingleResultHandler(classOf[DummyDomain], "MacrosTestSuite", "Domain get").isInstanceOf[DomainSingleResultHandler[_, _]])
  }

  test("Error getSingleResultHandler") {
    //コンパイルエラー
    //DaoRefrectionMacros.getSingleResultHandler("abc", "MacrosTestSuite", "type error")
  }

}

@Entity
case class DummyEntity(id: String)

@Domain
case class DummyDomain(value: String)
