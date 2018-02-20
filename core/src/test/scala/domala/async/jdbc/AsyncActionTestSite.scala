package domala.async.jdbc

import javax.sql.DataSource

import domala.async.Async
import org.scalatest.AsyncFunSuite
import org.seasar.doma.jdbc.dialect.Dialect

import scala.concurrent.ExecutionContext

class AsyncActionTestSite extends AsyncFunSuite {
  implicit val config: AsyncConfig = AsyncActionTestConfig

  test("return x >>= f == f x") {
    Async.transactionally {
      val f = (x: Int) => AsyncAction(x * 2)
      val x = 3
      val bound = AsyncAction(x) flatMap f
      val fx = f(x)
      for {
        x <- bound
        y <- fx
      } yield {
        assert(x == y)
        assert(x == 6)
      }
    }
  }

  test("return x >>= f == f x transactionally") {
    Async.transactionally {
      val f = (x: Int) => AsyncAction(x * 2)
      val x = 3
      val bound = AsyncAction(x) flatMap f
      val fx = f(x)
      for {
        x <- bound
        y <- fx
      } yield {
        assert(x == y)
        assert(x == 6)
      }
    }
  }

  test("m >>= return == m") {
    Async {
      val m = AsyncAction(5)
      val ret = m flatMap (x => AsyncAction(x))
      for {
        x <- m
        y <- ret
      } yield {
        assert(x == y)
        assert(x == 5)
      }
    }
  }

  test("m >>= return == m transactionally") {
    Async.transactionally {
      val m = AsyncAction(5)
      val ret = m flatMap (x => AsyncAction(x))
      for {
        x <- m
        y <- ret
      } yield {
        assert(x == y)
        assert(x == 5)
      }
    }
  }

  test("""(m >>= f) >>= g == m >>= (\x -> f x >>= g)""") {
    Async {
      val m = AsyncAction(5)
      val f = (x: Int) => AsyncAction(x * 2)
      val g = (x: Int) => AsyncAction(x + 3)

      val left = (m flatMap f) flatMap g
      val right = m flatMap (x => f(x) flatMap g)

      for {
        x <- left
        y <- right
      } yield {
        assert(x == y)
        assert(x == 13)
      }
    }
  }

  test("""(m >>= f) >>= g == m >>= (\x -> f x >>= g) transactionally""") {
    Async.transactionally {
      val m = AsyncAction(5)
      val f = (x: Int) => AsyncAction(x * 2)
      val g = (x: Int) => AsyncAction(x + 3)

      val left = (m flatMap f) flatMap g
      val right = m flatMap (x => f(x) flatMap g)

      for {
        x <- left
        y <- right
      } yield {
        assert(x == y)
        assert(x == 13)
      }
    }
  }

}

object AsyncActionTestConfig extends AsyncConfig {
  override val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.global

  override def atomicOperation[R](thunk: => R): R = {
    println(s"${Thread.currentThread.getName} start")
    val ret = thunk
    println(s"${Thread.currentThread.getName} end => $ret")
    ret
  }

  override def getDataSource: DataSource = throw new NotImplementedError

  override def getDialect: Dialect = throw new NotImplementedError
}