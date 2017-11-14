package domala.tests.expr

import domala._
import domala.jdbc.SelectOptions
import domala.tests.{ID, Name}
import org.scalatest.{AsyncFunSuite, BeforeAndAfter}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class AsyncFunctionTestSuite extends AsyncFunSuite with BeforeAndAfter{

  override def executionContext: ExecutionContext = global

  test("future transaction") {
    implicit val config: jdbc.Config = AsyncFunctionTestConfig1
    val dao: FunctionDao = FunctionDao.impl

    Required {
      dao.create()
      val employees = (1 to 100).map(i => Emp(ID(i), Name("hoge"),  Jpy(i), Some(ID(1)))).toList
      dao.insert(employees)
    }

    Future(Required {
      val option = SelectOptions.get
      dao.selectAll(_.map(_.salary).sum)(option)
    }).map(sum => assert(sum == Jpy(5050)))
  }

  test("parallel future") {
    implicit val config: jdbc.Config = AsyncFunctionTestConfig2
    val dao: FunctionDao = FunctionDao.impl

    Required {
      dao.create()
      val employees = (1 to 100).map(i => Emp(ID(i), Name("hoge"), Jpy(i), Some(ID(1)))).toList
      dao.insert(employees)
    }

    def f(offset: Int, limit: Int) = Future(Required {
      val option = SelectOptions.get.offset(offset).limit(limit)
      dao.selectAll(_.map(_.salary).toList)(option)
    })

    import Numeric.Implicits._
    val f1 = f(0, 50)
    val f2 = f(50, 100)
    val ret = for {
      sum1 <- f1
      sum2 <- f2
    } yield sum1.sum + sum2.sum

    ret.map(sum => assert(sum == Jpy(5050)))
  }

  test("partitioning iterator") {
    implicit val config: jdbc.Config = AsyncFunctionTestConfig3
    val dao: FunctionDao = FunctionDao.impl
    Required {
      dao.create()
      val employees = (1 to 100).map(i => Emp(ID(i), Name("hoge"),  Jpy(i), Some(ID(1)))).toList
      dao.insert(employees)
    }

    // one partition is one transaction
    def slice[A, T <: Traversable[A]](f: SelectOptions => T)(offset: Int, limit: Int, option: SelectOptions = SelectOptions.get): T =
      Required {
        f(option.clone.offset(offset).limit(limit))
      }

    def partition[A, T <: Traversable[A]](f: SelectOptions => T)(limit: Int, option: SelectOptions = SelectOptions.get): Iterator[T] = {
      def next(offset: Int): Iterator[T] = {
        val list = slice[A, T](f)(offset, limit, option)
          if (list.isEmpty)
            Iterator()
          else
            Iterator(list) ++ next(limit + offset) // Iterator's `++` operation is lazy evaluation
        }
      next(0)
    }
    val selectFunction = dao.selectAll(_.map(_.salary).toList) _
    val partitioningFunction = partition[Jpy, List[Jpy]](selectFunction)(33) // needs type parameter
    Future(partitioningFunction).map{ x =>
      assert(x.map{_.sum}.toList == Seq(Jpy(561), Jpy(1650), Jpy(2739), Jpy(100))) // (1 to 33).sum, (34 to 66).sum, (67 to 99).sum, 100,
    }
  }

  test("partitioning iterator in one transaction") {
    implicit val config: jdbc.Config = AsyncFunctionTestConfig4

    val createDao: FunctionDao = FunctionDao.impl
    Required {
      createDao.create()
      val employees = (1 to 100).map(i => Emp(ID(i), Name("hoge"),  Jpy(i), Some(ID(1)))).toList
      createDao.insert(employees)
    }

    // original connection use
    println("connection open!")
    import java.sql.DriverManager
    val connection = DriverManager.getConnection("jdbc:h2:mem:asyncfnctest4;DB_CLOSE_DELAY=-1", "sa", "")
    connection.setAutoCommit(false)
    val dao: FunctionDao = FunctionDao.impl(connection)

    // no transaction
    def slice[A, T <: Traversable[A]](f: SelectOptions => T)(offset: Int, limit: Int, option: SelectOptions = SelectOptions.get): T =
      f(option.clone.offset(offset).limit(limit))

    def partition[A, T <: Traversable[A]](f: SelectOptions => T)(limit: Int, option: SelectOptions = SelectOptions.get): Iterator[T] = {
      def next(offset: Int): Iterator[T] = {
        val list = slice[A, T](f)(offset, limit, option)
        if (list.isEmpty)
          Iterator()
        else
          Iterator(list) ++ next(limit + offset) // Iterator's `++` operation is lazy evaluation
      }
      next(0)
    }
    val selectFunction = dao.selectAll(_.map(_.salary).toList) _
    val partitioningFunction = partition[Jpy, List[Jpy]](selectFunction)(33) // needs type parameter

    val assertion = Future(partitioningFunction).map{ x =>
      assert(x.map{_.sum}.toList == Seq(Jpy(561), Jpy(1650), Jpy(2739), Jpy(100))) // (1 to 33).sum, (34 to 66).sum, (67 to 99).sum, 100,
    }

    assertion.onComplete(_ => {
      connection.close()
      println("connection close!")
    })
    assertion
  }

}
