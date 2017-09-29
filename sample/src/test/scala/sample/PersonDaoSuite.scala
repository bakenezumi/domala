package sample

import domala.Required
import domala.jdbc.Config
import org.scalatest._

class PersonDaoSuite extends FunSuite {
  implicit val config: Config = SampleConfig

  val dao: PersonDao = PersonDao.impl

  test("select by ID(1)") {
     Required {
      dao.create()
      assert (dao.selectById(1) contains
         Person(
           Some(1),
           Name("SMITH"),
           Some(10),
           Address("Tokyo", "Yaesu"),
           Some(1),
           Some(0)
         )
      )
    }
  }
}


