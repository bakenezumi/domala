package domala.tests

import org.scalatest._
import domala.Required
import domala.jdbc.Config

class StandardUseCaseTestSuite extends FunSuite with BeforeAndAfter {
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
      assert(dao.selectById(5) === None)
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
        dao.selectAll === Seq(
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

  test("select to return nullable entity") {
    Required {
      assert(
        dao.selectByIdNullable(1) ===
          Person(Some(1),
            Name("SMITH"),
            Some(10),
            Address("Tokyo", "Yaesu"),
            Some(1),
            Some(0)))
      assert(dao.selectByIdNullable(5) === null)
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
        dao.selectWithDepartmentEmbeddedById(1) ===
          Some(PersonDepartmentEmbedded(1, "SMITH", Department(1, "ACCOUNTING"))))
    }
  }

  test("stream select no param") {
    Required {
      assert(dao.selectAllStream { stream =>
        stream.length
      } == 2)
    }
  }

  test("stream select with one param") {
    Required {
      assert(dao.selectByIdStream(1) { stream =>
        stream.head.address
      } == Address("Tokyo", "Yaesu"))
      assert(dao.selectByIdStream(5) { stream =>
        if (stream.isEmpty) null else fail()
      } == null)
    }
  }

  test("Sequential Map select") {
    Required {
      assert(dao.selectAllSeqMap() == Seq(
        Map("ID" -> 1, "NAME" -> "SMITH", "AGE" -> 10, "CITY" -> "Tokyo", "STREET" -> "Yaesu", "DEPARTMENT_ID" -> 1, "VERSION" -> 0),
        Map("ID" -> 2, "NAME" -> "ALLEN", "AGE" -> 20, "CITY" -> "Kyoto", "STREET" -> "Karasuma", "DEPARTMENT_ID" -> 2, "VERSION" -> 0)
      ))
    }
  }

  test("Single Map select") {
    Required {
      assert(dao.selectByIdMap(1) ==
        Map("ID" -> 1, "NAME" -> "SMITH", "AGE" -> 10, "CITY" -> "Tokyo", "STREET" -> "Yaesu", "DEPARTMENT_ID" -> 1, "VERSION" -> 0))
      assert(dao.selectByIdMap(5) == null)
    }
  }

  test("Option Map select") {
    Required {
      assert(dao.selectByIdOptionMap(1) ==
        Some(Map("ID" -> 1, "NAME" -> "SMITH", "AGE" -> 10, "CITY" -> "Tokyo", "STREET" -> "Yaesu", "DEPARTMENT_ID" -> 1, "VERSION" -> 0)))
      assert(dao.selectByIdOptionMap(5) == None)
    }
  }

  test("stream map select") {
    Required {
      assert(dao.selectAllStreamMap { stream =>
        stream.length
      } == 2)
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
