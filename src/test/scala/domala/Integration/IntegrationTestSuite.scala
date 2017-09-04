package domala.Integration

import org.scalatest._
import domala.Config
import domala.Required

import scala.collection.mutable

class IntegrationTestSuite extends FunSuite with BeforeAndAfter {
  implicit val config: Config = TestConfig

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
          Some(
            PersonDepartment(ID(1),
                             Name("SMITH"),
                             Some(ID(1)),
                             Some(Name("ACCOUNTING")))))
    }
  }

  test("join select to enbedded entity") {
    Required {
      assert(
        dao.selectWithDepartmentById2(1) ===
          Some(PersonDepartment2(1, "SMITH", Department(1, "ACCOUNTING"))))
    }
  }

  test("insert from entity") {
    Required {
      dao.insert(
        Person(
          name = Name("aaa"),
          age = Some(5),
          address = Address("bbb", "ccc"),
          departmentId = Some(1)))
      assert(dao.selectCount === 3)
      assert(
        dao.selectById(3) === Some(
          Person(
            Some(3),
            Name("aaa"),
            Some(5),
            Address("bbb", "ccc"),
            Some(1),
            Some(1))))
    }
  }

  test("update by entity") {
    Required {
      dao.update(
        Person(
          id = Some(1),
          name = Name("aaa"),
          age = Some(5),
          address = Address("bbb", "ccc"),
          departmentId = Some(2),
          version = Some(0)))
      assert(
        dao.selectById(1) === Some(
          Person(Some(1),
            Name("aaa"),
            Some(5),
            Address("bbb", "ccc"),
            Some(2),
            Some(1))))
    }
  }

  test("delete by entity") {
    Required {
      dao.selectById(1).foreach(dao.delete)
      assert(dao.selectCount() === 1)
    }
  }
}
