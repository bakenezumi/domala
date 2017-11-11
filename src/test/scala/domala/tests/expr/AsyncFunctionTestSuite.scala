package domala.tests.expr

import domala._
import domala.jdbc.SelectOptions
import domala.tests.{ID, Name}
import org.scalatest.{AsyncFunSuite, BeforeAndAfter}
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global

class AsyncFunctionTestSuite extends AsyncFunSuite with BeforeAndAfter{

  override def executionContext: ExecutionContext = global

  test("future transaction") {
    implicit val config: jdbc.Config = AsyncFunctionTestConfig1
    val dao: FunctionDao = FunctionDao.impl

    Required {
      dao.create()
      val employees = (1 to 1000).map(i => Emp(ID(i), Name("hoge"),  Jpy(i * 10), Some(ID(1)))).toList
      dao.insert(employees)
    }

    Future(Required {
      val option = SelectOptions.get
      dao.selectAll(option, _.map(_.salary).sum)
    }).map(sum => assert(sum == Jpy(5005000)))
  }

  test("parallel future") {
    implicit val config: jdbc.Config = AsyncFunctionTestConfig2
    val dao: FunctionDao = FunctionDao.impl

    Required {
      dao.create()
      val employees = (1 to 1000).map(i => Emp(ID(i), Name("hoge"),  Jpy(i * 10), Some(ID(1)))).toList
      dao.insert(employees)
    }

    def f(offset: Int, limit: Int) = Future(Required {
      val option = SelectOptions.get.offset(offset).limit(limit)
      dao.selectAll(option, _.map(_.salary).toList)
    })

    import Numeric.Implicits._
    val f1 = f(0, 500)
    val f2 = f(500, 1000)
    val ret = for {
      sum1 <- f1
      sum2 <- f2
    } yield sum1.sum + sum2.sum

    ret.map(sum => assert(sum == Jpy(5005000)))
   }

}
