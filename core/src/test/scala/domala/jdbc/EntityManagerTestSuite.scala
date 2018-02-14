package domala.jdbc

import domala.Required
import domala.jdbc.dialect.H2Dialect
import domala.jdbc.models._
import domala.jdbc.tx.LocalTransactionDataSource
import org.scalatest.{BeforeAndAfter, FunSuite}

class EntityManagerTestSuite  extends FunSuite with BeforeAndAfter {
  implicit val config: Config = EntityManagerTestConfig
  val dao: PersonDao = PersonDao.impl

  val initialPersons =
    Seq(
      Person(ID(1),Some(Name("SMITH")),Some(10),Address("Tokyo","Yaesu"),Some(2),0),
      Person(ID(2),Some(Name("ALLEN")),Some(20),Address("Kyoto","Karasuma"),Some(1),0))

  before {
    Required {
      dao.create()
    }
  }

  after {
    Required {
      dao.drop()
    }
  }

  test("insert") {
    Required {
      val newEntity = Person(ID.notAssigned, Some(Name("foo")), Some(30), Address("baz", "bar"), None, 0)
      val Result(cnt, inserted) = EntityManager.insert(newEntity)
      assert(inserted.id != newEntity.id)
      assert(inserted.copy(ID.notAssigned) == newEntity)
      assert(cnt == 1)
      assert(dao.findAll == initialPersons ++ Seq(inserted))
    }
  }

  test("update") {
    Required {
      val Some(newEntity) = dao.findById(ID(2)).map(e => e.copy(name = Some(Name("foo")), address = e.address.copy(city = "baz")))
      val Result(cnt, updated) = EntityManager.update(newEntity)
      assert(cnt == 1)
      assert(updated == newEntity.copy(version = newEntity.version + 1))
      assert(dao.findAll == initialPersons.head +: Seq(updated))
    }
  }

  test("delete") {
    Required {
      val Some(target) = dao.findById(ID(2))
      val Result(cnt, deleted) = EntityManager.delete(target)
      assert(cnt == 1)
      assert(deleted == target)
      assert(dao.findAll == initialPersons.init)
    }
  }

  test("batch insert") {
    Required {
      val newEntities =
        Seq(
          Person(ID.notAssigned, Some(Name("foo")), Some(30), Address("baz", "bar"), None, 0),
          Person(ID.notAssigned, Some(Name("hoge")), Some(40), Address("fuga", "piyo"), None, 0)
        )
      val BatchResult(cnt, inserted) = EntityManager.batchInsert(newEntities)
      assert(inserted.map(_.copy(id = ID.notAssigned)) == newEntities)
      assert(cnt sameElements Array(1, 1))
      assert(dao.findAll == initialPersons ++ inserted)
    }
  }

  test("batch update") {
    Required {
      val newEntities = dao.findAll.map(e => e.copy(name = Some(Name("foo")), address = e.address.copy(city = "baz")))
      val BatchResult(cnt, updated) = EntityManager.batchUpdate(newEntities)
      assert(cnt sameElements Array(1, 1))
      assert(updated == newEntities.map(e => e.copy(version = e.version + 1)))
      assert(dao.findAll == updated)
    }
  }

  test("batch delete") {
    Required {
      val target = dao.findAll
      val BatchResult(cnt, deleted) = EntityManager.batchDelete(target)
      assert(cnt sameElements Array(1, 1))
      assert(deleted == target)
      assert(dao.findAll.isEmpty)
    }
  }

}

object EntityManagerTestConfig extends LocalTransactionConfig(
  dataSource =  new LocalTransactionDataSource(
    "jdbc:h2:mem:entity-manager;DB_CLOSE_DELAY=-1", "sa", null),
  dialect = new H2Dialect,
  naming = Naming.SNAKE_LOWER_CASE
) {
  Class.forName("org.h2.Driver")
}
