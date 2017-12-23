package domala.jdbc

import java.sql.DriverManager

import domala.Required
import domala.tests.{H2TestConfigTemplate, Person, PersonDao}
import org.scalatest.FunSuite
import org.seasar.doma.internal.jdbc.dao.DomalaAbstractDaoHelper

class DaoProviderTestSuite extends FunSuite {
  implicit val config: Config = DaoProviderTestConfig1

  test("implicit only") {
    val dao = DaoProvider.get[PersonDao]
    assert(dao.isInstanceOf[PersonDao])
    Required {
      dao.create()
    }
  }


  test("config parameter") {
    val dao = DaoProvider.get[PersonDao](DaoProviderTestConfig2)
    assert(dao.isInstanceOf[PersonDao])
    Required {
      dao.create()
    }(DaoProviderTestConfig2)
  }

  // compile error
//  test("not Dao") {
//    DaoProvider.get[Person]
//  }

  test("connection parameter") {
    val connection = DriverManager.getConnection("jdbc:h2:mem:dao-provider-conn;DB_CLOSE_DELAY=-1", "sa", "")
    val dao = DaoProvider.get[PersonDao](connection)
    assert(dao.isInstanceOf[PersonDao])
    dao.create()
  }

  // compile error
//    test("not Dao") {
//      val connection = DriverManager.getConnection("jdbc:h2:mem:dao-provider-conn;DB_CLOSE_DELAY=-1", "sa", "")
//      DaoProvider.get[Person](connection, config)
//    }

  test("data source parameter") {
    val connection = DriverManager.getConnection("jdbc:h2:mem:dao-provider-ds;DB_CLOSE_DELAY=-1", "sa", "")
    val dataSource = DomalaAbstractDaoHelper.toDataSource(connection)
    val dao = DaoProvider.get[PersonDao](dataSource)
    assert(dao.isInstanceOf[PersonDao])
    dao.create()
  }

  // compile error
//  test("not Dao") {
//    val connection = DriverManager.getConnection("jdbc:h2:mem:dao-provider-ds;DB_CLOSE_DELAY=-1", "sa", "")
//    val dataSource = DomalaAbstractDaoHelper.toDataSource(connection)
//    DaoProvider.get[Person](dataSource, config)
//  }

}

object DaoProviderTestConfig1 extends H2TestConfigTemplate("dao-provider1")
object DaoProviderTestConfig2 extends H2TestConfigTemplate("dao-provider2")