package domala.tests.expr

import domala._
import domala.tests.{ID, Name}
import org.scalatest.{AsyncFunSuite, BeforeAndAfter}

import scala.concurrent.Future

class AsyncFunctionTestSuite extends AsyncFunSuite with BeforeAndAfter{
  implicit val config: jdbc.Config = AsyncFunctionTestConfig

  val dao: FunctionDao = FunctionDao.impl

  test("future transaction") {
    Future(Required {
      dao.create()
      val emps = (1 to 10).map(i => Emp(ID(i), Name("hoge"),  Jpy(i*10), Some(ID(1)))).toList
      dao.insert(emps)
      dao.selectSalaryByDepartmentId(ID(1), _.sum)
    }).map(sum => assert(sum == 550))
  }

}
