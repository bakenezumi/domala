package sample

import domala.Required
import org.scalatest._

class PersonDaoSuite extends FunSuite {

  implicit val config = SampleConfig
  val dao: PersonDao = PersonDao

  test("select by ID(1)") {
     Required {
      dao.create()
      assert (dao.selectById(1) === Some(
         Person(
           Some(1),
           Name("SMITH"),
           Some(10),
           Address("Tokyo", "Yaesu"),
           Some(1),
           Some(0)
         )
        )
      )
    }
  }
}


