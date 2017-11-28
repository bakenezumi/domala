package example

import domala.Required
import domala.jdbc.Config
import org.scalatest._

class PersonDaoSuite extends FunSuite {
  implicit val config: Config = ExampleConfig

  val dao: PersonDao = PersonDao.impl

  test("select by ID(1)") {
     Required {
      dao.create()
      assert (dao.selectById(1) contains
         Person(
           ID(1),
           Name("SMITH"),
           Some(10),
           Address("Tokyo", "Yaesu"),
           Some(ID(1)),
           0
         )
      )
    }
  }
}


