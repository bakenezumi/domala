package domala.async.jdbc

import domala.Required
import domala.async.Async
import domala.async.models.AsyncPersonDao
import domala.jdbc.dialect.H2Dialect
import domala.jdbc.models._
import domala.jdbc.tx.LocalTransactionDataSource
import domala.jdbc.{BatchResult, Naming, Result}
import org.scalatest.{AsyncFunSuite, BeforeAndAfter}

import scala.concurrent.ExecutionContextExecutor

class AsyncEntityManagerTestSuite extends AsyncFunSuite with BeforeAndAfter {

  implicit val config: AsyncConfig = AsyncEntityManagerTestConfig
  val dao: AsyncPersonDao = AsyncPersonDao.impl

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
    val newEntity = Person(ID.notAssigned, Some(Name("foo")), Some(30), Address("baz", "bar"), None, 0)
    Async {
      for {
        Result(cnt, inserted) <- AsyncEntityManager.insert(newEntity)
        selected <- dao.findAll(_.toList)
      } yield {
        assert(inserted.id != newEntity.id)
        assert(inserted.copy(ID.notAssigned) == newEntity)
        assert(cnt == 1)
        assert(selected == initialPersons ++ Seq(inserted))
      }
    }
  }

  test("update") {
    Async {
      for {
        Some(newEntity) <- dao.findById(ID(2), _.toStream.headOption.map(e => e.copy(name = Some(Name("foo")), address = e.address.copy(city = "baz"))))
        Result(cnt, updated) <- AsyncEntityManager.update(newEntity)
        selected <- dao.findAll(_.toList)
      } yield {
        assert(cnt == 1)
        assert(updated == newEntity.copy(version = newEntity.version + 1))
        assert(selected == initialPersons.head +: Seq(updated))
      }
    }
  }

  test("delete") {
    Async {
      for {
        Some(target) <- dao.findById(ID(2), _.toStream.headOption)
        Result(cnt, deleted) <- AsyncEntityManager.delete(target)
        selected <- dao.findAll(_.toList)
      } yield {
        assert(cnt == 1)
        assert(deleted == target)
        assert(selected == initialPersons.init)
      }
    }
  }

  test("batch insert") {
    Async {
      val newEntities =
        Seq(
          Person(ID.notAssigned, Some(Name("foo")), Some(30), Address("baz", "bar"), None, 0),
          Person(ID.notAssigned, Some(Name("hoge")), Some(40), Address("fuga", "piyo"), None, 0)
        )
      for {
        BatchResult(cnt, inserted) <- AsyncEntityManager.batchInsert(newEntities)
        selected <- dao.findAll(_.toList)
      } yield {
        assert(inserted.map(_.copy(id = ID.notAssigned)) == newEntities)
        assert(cnt sameElements Array(1, 1))
        assert(selected == initialPersons ++ inserted)
      }
    }
  }

  test("batch update") {
    Async {
      for {
        newEntities <- dao.findAll(_.toList.map(e => e.copy(name = Some(Name("foo")), address = e.address.copy(city = "baz"))))
        BatchResult(cnt, updated) <- AsyncEntityManager.batchUpdate(newEntities)
        selected <- dao.findAll(_.toList)
      } yield {
        assert(cnt sameElements Array(1, 1))
        assert(updated == newEntities.map(e => e.copy(version = e.version + 1)))
        assert(selected == updated)
      }
    }
  }

  test("batch delete") {
    Async {
      for {
        target <- dao.findAll(_.toList)
        BatchResult(cnt, deleted) <- AsyncEntityManager.batchDelete(target)
        selected <- dao.findAll(_.toList)
      } yield {
        assert(cnt sameElements Array(1, 1))
        assert(deleted == target)
        assert(selected.isEmpty)
      }
    }
  }

  test("insert transactionally") {
    val newEntity = Person(ID.notAssigned, Some(Name("foo")), Some(30), Address("baz", "bar"), None, 0)
    Async.transactionally {
      for {
        Result(cnt, inserted) <- AsyncEntityManager.insert(newEntity)
        selected <- dao.findAll(_.toList)
      } yield {
        assert(inserted.id != newEntity.id)
        assert(inserted.copy(ID.notAssigned) == newEntity)
        assert(cnt == 1)
        assert(selected == initialPersons ++ Seq(inserted))
      }
    }
  }

  test("update transactionally") {
    Async.transactionally {
      for {
        Some(newEntity) <- dao.findById(ID(2), _.toStream.headOption.map(e => e.copy(name = Some(Name("foo")), address = e.address.copy(city = "baz"))))
        Result(cnt, updated) <- AsyncEntityManager.update(newEntity)
        selected <- dao.findAll(_.toList)
      } yield {
        assert(cnt == 1)
        assert(updated == newEntity.copy(version = newEntity.version + 1))
        assert(selected == initialPersons.head +: Seq(updated))
      }
    }
  }

  test("delete transactionally") {
    Async.transactionally {
      for {
        Some(target) <- dao.findById(ID(2), _.toStream.headOption)
        Result(cnt, deleted) <- AsyncEntityManager.delete(target)
        selected <- dao.findAll(_.toList)
      } yield {
        assert(cnt == 1)
        assert(deleted == target)
        assert(selected == initialPersons.init)
      }
    }
  }

  test("batch insert transactionally") {
    Async.transactionally {
      val newEntities =
        Seq(
          Person(ID.notAssigned, Some(Name("foo")), Some(30), Address("baz", "bar"), None, 0),
          Person(ID.notAssigned, Some(Name("hoge")), Some(40), Address("fuga", "piyo"), None, 0)
        )
      for {
        BatchResult(cnt, inserted) <- AsyncEntityManager.batchInsert(newEntities)
        selected <- dao.findAll(_.toList)
      } yield {
        assert(inserted.map(_.copy(id = ID.notAssigned)) == newEntities)
        assert(cnt sameElements Array(1, 1))
        assert(selected == initialPersons ++ inserted)
      }
    }
  }

  test("batch update transactionally") {
    Async.transactionally {
      for {
        newEntities <- dao.findAll(_.toList.map(e => e.copy(name = Some(Name("foo")), address = e.address.copy(city = "baz"))))
        BatchResult(cnt, updated) <- AsyncEntityManager.batchUpdate(newEntities)
        selected <- dao.findAll(_.toList)
      } yield {
        assert(cnt sameElements Array(1, 1))
        assert(updated == newEntities.map(e => e.copy(version = e.version + 1)))
        assert(selected == updated)
      }
    }
  }

  test("batch delete transactionally") {
    Async.transactionally {
      for {
        target <- dao.findAll(_.toList)
        BatchResult(cnt, deleted) <- AsyncEntityManager.batchDelete(target)
        selected <- dao.findAll(_.toList)
      } yield {
        assert(cnt sameElements Array(1, 1))
        assert(deleted == target)
        assert(selected.isEmpty)
      }
    }
  }

}

object AsyncEntityManagerTestConfig extends AsyncLocalTransactionConfig(
  dataSource =  new LocalTransactionDataSource(
    "jdbc:h2:mem:async-entity-manager;DB_CLOSE_DELAY=-1", "sa", null),
  dialect = new H2Dialect,
  naming = Naming.SNAKE_LOWER_CASE
) {

  Class.forName("org.h2.Driver")

  override val executionContext: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global

}
