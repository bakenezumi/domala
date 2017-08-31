package domala.Integration

import org.scalatest._
import domala.Required

import scala.collection.mutable

class IntegrationTestSuite extends FunSuite with BeforeAndAfter {
  implicit val config = TestConfig

  val dao: PersonDao = PersonDao

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

  test("select by 1 basic parameter to return optional entity") {
    Required {
      assert(
        dao.selectById(1) === Some(
          Person(Some(1),
                 Name("SMITH"),
                 Some(10),
                 Address("Tokyo", "Yaesu"),
                 Some(1),
                 Some(0))))
    }
  }

  test("select to return Int") {
    Required {
      assert(dao.selectCount === 2)
    }
  }

  test("select to return Seq") {
    Required {
      assert(
        dao.selectAll === mutable.Buffer(
          Person(Some(1),
                 Name("SMITH"),
                 Some(10),
                 Address("Tokyo", "Yaesu"),
                 Some(1),
                 Some(0)),
          Person(Some(2),
                 Name("ALLEN"),
                 Some(20),
                 Address("Kyoto", "Karasuma"),
                 Some(2),
                 Some(0))
        ))
    }
  }

  test("select to return entity") {
    Required {
      assert(
        dao.selectById2(1) ===
          Person(Some(1),
            Name("SMITH"),
            Some(10),
            Address("Tokyo", "Yaesu"),
            Some(1),
            Some(0)))
    }
  }

  test("join select") {
    Required {
      assert(
        dao.selectWithDepartmentById(1) ===
          Some(PersonDepartment(1,Name("SMITH"),Some(1),Some("ACCOUNTING"))))
    }
  }

  test("insert from entity") {
    Required {
      dao.insert(
        Person(name = Name("aaa"),
               age = Some(5),
               address = Address("bbb", "ccc"),
               departmentId = Some(1)))
      assert(dao.selectCount === 3)
      assert(
        dao.selectById(3) === Some(
          Person(Some(3),
                 Name("aaa"),
                 Some(5),
                 Address("bbb", "ccc"),
                 Some(1),
                 Some(1))))
    }
  }

}
