package domala.async

import javax.sql.DataSource

import domala.Required
import domala.async.jdbc.{AsyncConfig, AsyncLocalTransactionConfig}
import domala.async.models.AsyncPersonDao
import domala.jdbc.dialect.{Dialect, H2Dialect}
import domala.jdbc.models.{Address, ID, Name, Person}
import domala.jdbc.{Naming, Result}
import org.scalatest.{AsyncFunSuite, BeforeAndAfter}
import org.seasar.doma.jdbc.tx.LocalTransactionDataSource
import org.seasar.doma.jdbc.{SimpleDataSource, SqlExecutionException}

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

class AsyncTestSuite extends AsyncFunSuite with BeforeAndAfter {
  implicit val config: AsyncConfig = AsyncTestConfig
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

  test("select only") {
    Async{
      for(result <- dao.findAll(_.toList))
        yield assert(result == initialPersons)
    }
  }

  test("insert only") {
    val newEntity = Person(ID.notAssigned, Some(Name("foo")), Some(30), Address("baz", "bar"), None, 0)
    Async{
      for(Result(cnt, result) <- dao.add(newEntity))
        yield {
          assert(cnt == 1)
          assert(result == newEntity.copy(id = ID(3)))
        }
    }
  }

  test("insert map select") {
    val newEntity = Person(ID.notAssigned, Some(Name("foo")), Some(30), Address("baz", "bar"), None, 0)
    Async{
      for {
        Result(cnt, result) <- dao.add(newEntity)
        selected <- dao.findAll(_.toList)
      }
        yield {
          assert(cnt == 1)
          assert(result == newEntity.copy(id = ID(3)))
          assert(selected == initialPersons ++ Seq(result))
        }
    }
  }

  test("insert flatMap insert map select") {
    val newEntity = Person(ID.notAssigned, Some(Name("foo")), Some(30), Address("baz", "bar"), None, 0)
    Async {
      for {
        Result(cnt, result) <- dao.add(newEntity)
        Result(cnt2, result2) <- dao.add(result.copy(id = ID.notAssigned, name = None))
        selected <- dao.findAll(_.toList)
      } yield {
        assert(cnt == 1)
        assert(result == newEntity.copy(id = ID(3)))
        assert(cnt2 == 1)
        assert(result2 == newEntity.copy(id = ID(4), name = None))
        assert(selected == initialPersons ++ Seq(result, result2))
      }
    }
  }

  test("insert andThen insert map select") {
    val newEntity = Person(ID.notAssigned, Some(Name("foo")), Some(30), Address("baz", "bar"), None, 0)
    Async {
      for {
        Result(cnt, result) <- dao.add(newEntity) andThen {
          case Success(Result(_, result2)) =>
            dao.add(result2.copy(id = ID.notAssigned, age = Some(50)))
        }
        Result(cnt2, result2) <- dao.add(result.copy(id = ID.notAssigned, name = None))
        selected <- dao.findAll(_.toList)
      } yield {
        assert(cnt == 1)
        assert(result == newEntity.copy(id = ID(3)))
        assert(cnt2 == 1)
        assert(result2 == newEntity.copy(id = ID(5), name = None))
        assert(selected == initialPersons ++ Seq(result, result.copy(id = ID(4), age = Some(50)), result2))
      }
    }
  }

  test("no transactional") {
    val newEntity = Person(ID.notAssigned, Some(Name("foo")), Some(30), Address("baz", "bar"), None, 0)
    Async {
      for {
        Result(_, result) <- dao.add(newEntity) // => commit
        _ <- dao.add(result.copy(id = ID.notAssigned, name = Some(Name("long " * 10)))) // Too Long Error  => rollback
        selected <- dao.findAll(_.toList)
      } yield {
        selected
      }
    }.transformWith {
      case Failure(_) =>
        Async {
          for {
            selected <- dao.findAll(_.toList)
          } yield {
            assert(selected == initialPersons ++ Seq(newEntity.copy(id = ID(3))))
          }
        }
      case _ => fail()
    }
  }

  test("transactional") {
    val newEntity = Person(ID.notAssigned, Some(Name("foo")), Some(30), Address("baz", "bar"), None, 0)
    Async.transactionally {
      for {
        Result(_, result) <- dao.add(newEntity) // => rollback
        _ <- dao.add(result.copy(id = ID.notAssigned, name = Some(Name("long " * 10)))) // Too Long Error => rollback
        selected <- dao.findAll(_.toList)
      } yield {
        selected
      }
    }.transformWith {
      case Failure(_) =>
        Async {
          for {
            selected <- dao.findAll(_.toList)
          } yield {
            assert(selected == initialPersons)
          }
        }
      case _ => fail()
    }
  }


  test("recover") {
    val newEntity = Person(ID.notAssigned, Some(Name("foo")), Some(30), Address("baz", "bar"), None, 0)
    Async {
      for {
        Result(_, result) <- dao.add(newEntity)
        _ <- dao.add(result.copy(id = ID.notAssigned, name = Some(Name("long " * 10)))) recover {
          case e: SqlExecutionException => dao.add(result.copy(id = ID.notAssigned, name = Some(Name(e.getClass.getSimpleName))))
        }
        selected <- dao.findAll(_.toList)
      } yield {
        assert(selected == initialPersons ++ Seq(result, result.copy(id = ID(5), name = Some(Name(classOf[SqlExecutionException].getSimpleName)))))
      }
    }
  }

  test("recover transactionally") {
    val newEntity = Person(ID.notAssigned, Some(Name("foo")), Some(30), Address("baz", "bar"), None, 0)
    Async.transactionally {
      for {
        Result(_, result) <- dao.add(newEntity)
        _ <- dao.add(result.copy(id = ID.notAssigned, name = Some(Name("long " * 10)))) recover {
          case e: SqlExecutionException => dao.add(result.copy(id = ID.notAssigned, name = Some(Name(e.getClass.getSimpleName))))
        }
        selected <- dao.findAll(_.toList)
      } yield {
        assert(selected == initialPersons ++ Seq(result, result.copy(id = ID(5), name = Some(Name(classOf[SqlExecutionException].getSimpleName)))))
      }
    }
  }

  test("filter success") {
    Async {
      for(result <- dao.findAll(_.toList) if result.nonEmpty)
        yield assert(result == initialPersons)
    }
  }

  test("filter failure") {
    Async {
      for(result <- dao.findAll(_.toList) if result.isEmpty)
        yield assert(result == initialPersons)
    }.transformWith {
      case Failure(_: NoSuchElementException) =>
        succeed
      case _ => fail()
    }
  }

  test("parallel action") {
    Async {
      val action1 = dao.findAll(_.toList)
      val action2 = dao.findAll(_.toList)
      for {
        selected1 <- action1
        selected2 <- action2
      } yield assert(selected1 == selected2)
    }
  }

  test("parallel future") {
    val future1 = Async { dao.findAll(_.toList) }
    val future2 = Async.transactionally { dao.findAll(_.toList) }

    for {
      selected1 <- future1
      selected2 <- future2
    } yield assert(selected1 == selected2)
  }

  test("parallel transactional action") {
    Async.transactionally {
      val action1 = dao.findAll(_.toList)
      val action2 = dao.findAll(_.toList)
      for {
        selected1 <- action1
        selected2 <- action2
      } yield {
        assert(selected1 == initialPersons)
        assert(selected2 == initialPersons)
      }
    }
  }

  val readOnlyConfig: AsyncConfig = AsyncTestReadOnlyConfig
  val readOnlyDao: AsyncPersonDao = AsyncPersonDao.readOnlyImpl(readOnlyConfig)

  test("parallel read only action") {
    Async {
      val action1 = readOnlyDao.findAll(_.toList)
      val action2 = readOnlyDao.findAll(_.toList)
      for {
        selected1 <- action1
        selected2 <- action2
      } yield {
        assert(selected1 == initialPersons)
        assert(selected2 == initialPersons)
      }
    }(readOnlyConfig)
  }

}

object AsyncTestConfig extends AsyncLocalTransactionConfig(
  dataSource =  new LocalTransactionDataSource(
    "jdbc:h2:mem:async-test;DB_CLOSE_DELAY=-1", "sa", null),
  dialect = new H2Dialect,
  naming = Naming.SNAKE_LOWER_CASE
) {

  Class.forName("org.h2.Driver")

  override val executionContext: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global

}

object AsyncTestReadOnlyConfig extends AsyncConfig {

  Class.forName("org.h2.Driver")

  override val executionContext: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global

  override def atomicOperation[R](thunk: => R): R = thunk

  override def getDataSource: DataSource = new SimpleDataSource() {
    setUrl("jdbc:h2:mem:async-test;DB_CLOSE_DELAY=-1")
    setUser("sa")
  }
  override def getDialect: Dialect = new H2Dialect
  override def getNaming: Naming = Naming.SNAKE_LOWER_CASE

}