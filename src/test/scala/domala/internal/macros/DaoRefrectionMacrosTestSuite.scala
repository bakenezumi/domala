package domala.internal.macros

import domala._
import domala.tests.entity.AnyRefType
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.seasar.doma.DomaException
import org.seasar.doma.internal.jdbc.command.{DomainSingleResultHandler, EntitySingleResultHandler}

class DaoRefrectionMacrosTestSuite extends FunSuite with BeforeAndAfter {
  test("Entity get") {
    assert(DaoRefrectionMacros.getSingleResultHandler(DummyEntity, "MacrosTestSuite", "Entity get").isInstanceOf[EntitySingleResultHandler[_]])
  }

  test("Domain get") {
    assert(DaoRefrectionMacros.getSingleResultHandler(DummyDomain, "MacrosTestSuite", "Domain get").isInstanceOf[DomainSingleResultHandler[_, _]])
  }

}

@Entity
case class DummyEntity(id: String)

@Domain
case class DummyDomain(value: String)
