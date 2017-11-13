package domala.tests.expr

import domala._
import domala.jdbc.SelectOptions
import domala.tests.{ID, Name}
import org.scalatest.{AsyncFunSuite, BeforeAndAfter}

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

class AsyncFunctionTestSuite extends AsyncFunSuite with BeforeAndAfter{

  override def executionContext: ExecutionContext = global

  test("future transaction") {
    implicit val config: jdbc.Config = AsyncFunctionTestConfig1
    val dao: FunctionDao = FunctionDao.impl

    Required {
      dao.create()
      val employees = (1 to 100).map(i => Emp(ID(i), Name("hoge"),  Jpy(i * 10), Some(ID(1)))).toList
      dao.insert(employees)
    }

    Future(Required {
      val option = SelectOptions.get
      dao.selectAll(_.map(_.salary).sum)(option)
    }).map(sum => assert(sum == Jpy(50500)))
  }

  test("parallel future") {
    implicit val config: jdbc.Config = AsyncFunctionTestConfig2
    val dao: FunctionDao = FunctionDao.impl

    Required {
      dao.create()
      val employees = (1 to 100).map(i => Emp(ID(i), Name("hoge"), Jpy(i * 10), Some(ID(1)))).toList
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

    ret.map(sum => assert(sum == Jpy(50500)))
  }

  test("future iterator") {
    implicit val config: jdbc.Config = AsyncFunctionTestConfig3
    val dao: FunctionDao = FunctionDao.impl
    Required {
      dao.create()
      val employees = (1 to 100).map(i => Emp(ID(i), Name("hoge"),  Jpy(i * 10), Some(ID(1)))).toList
      dao.insert(employees)
    }

    def slice[A, T <: Traversable[A]](f: SelectOptions => T)(offset: Int, limit: Int): Future[T] = Future(Required({
      val option = SelectOptions.get.offset(offset).limit(limit)
      f(option)
    }))

    def partition[A, T <: Traversable[A]](f: SelectOptions => T)(limit: Int): Future[Iterator[T]] = {
      def next(offset: Int): Future[Iterator[T]] =
        slice[A, T](f)(offset, limit).map { x =>
          if (x.isEmpty)
            Iterator()
          else
            Iterator(x) ++ Await.result(next(limit + offset), Duration.Inf)
        }
      next(0)
    }
    val selectFunction = dao.selectAll(_.map(_.salary).toList) _
    val partitioningFunction: Future[Iterator[List[Jpy]]] = partition[Jpy, List[Jpy]](selectFunction)(33) // needs type parameter
    partitioningFunction.map{ x =>
      assert(x.map{_.sum}.toList == Seq(Jpy(5610), Jpy(16500), Jpy(27390), Jpy(1000)))
    }
  }

}
