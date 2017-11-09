package domala.tests.expr

import domala._
import domala.jdbc.SelectOptions
import domala.tests.{ID, Name}
import org.scalatest.{AsyncFunSuite, BeforeAndAfter}

import scala.concurrent.Future

class AsyncFunctionTestSuite extends AsyncFunSuite with BeforeAndAfter{

  implicit val config: jdbc.Config = AsyncFunctionTestConfig
  val dao: FunctionDao = FunctionDao.impl

  test("future transaction") {
    Required {
      dao.create()
      val employees = (1 to 100).map(i => Emp(ID(i), Name("hoge"),  Jpy(i * 10), Some(ID(1)))).toList
      dao.insert(employees)
    }

    Future(Required {
      val option = SelectOptions.get
      dao.selectAll(option, _.map(_.salary).sum)
    }).map(sum => assert(sum == Jpy(50500)))
  }

}
