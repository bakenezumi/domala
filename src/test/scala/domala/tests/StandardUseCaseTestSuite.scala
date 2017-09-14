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

  test("select stream no param") {
    Required {
      assert(dao.selectAllStream { stream =>
        stream.length
      } == 2)
    }
  }

  test("select stream with one param") {
    Required {
      assert(dao.selectByIdStream(1) { stream =>
        stream.head.address
      } == Address("Tokyo", "Yaesu"))
      assert(dao.selectByIdStream(5) { stream =>
        if (stream.isEmpty) null else fail()
      } == null)
    }
  }

  test("select Sequential Map") {
    Required {
      assert(dao.selectAllSeqMap() == Seq(
        Map("ID" -> 1, "NAME" -> "SMITH", "AGE" -> 10, "CITY" -> "Tokyo", "STREET" -> "Yaesu", "DEPARTMENT_ID" -> 1, "VERSION" -> 0),
        Map("ID" -> 2, "NAME" -> "ALLEN", "AGE" -> 20, "CITY" -> "Kyoto", "STREET" -> "Karasuma", "DEPARTMENT_ID" -> 2, "VERSION" -> 0)
      ))
    }
  }

  test("select Single Map") {
    Required {
      assert(dao.selectByIdMap(1) ==
        Map("ID" -> 1, "NAME" -> "SMITH", "AGE" -> 10, "CITY" -> "Tokyo", "STREET" -> "Yaesu", "DEPARTMENT_ID" -> 1, "VERSION" -> 0))
      assert(dao.selectByIdMap(5) == null)
    }
  }

  test("select Option Map") {
    Required {
      assert(dao.selectByIdOptionMap(1) ==
        Some(Map("ID" -> 1, "NAME" -> "SMITH", "AGE" -> 10, "CITY" -> "Tokyo", "STREET" -> "Yaesu", "DEPARTMENT_ID" -> 1, "VERSION" -> 0)))
      assert(dao.selectByIdOptionMap(5) == None)
    }
  }

  test("select option domain") {
    Required {
      assert(dao.selectNameById(1) == Some(Name("SMITH")))
      assert(dao.selectNameById(99) == None)
    }
  }

  test("select nullable domain") {
    Required {
      assert(dao.selectNameByIdNullable(1) == Name("SMITH"))
      assert(dao.selectNameByIdNullable(99) == null)
    }
  }

  test("select domain list") {
    Required {
      assert(dao.selectNames == Seq(Name("SMITH"), Name("ALLEN")))
    }
  }

  test("select map stream") {
    Required {
      assert(dao.selectAllStreamMap { stream =>
        stream.length
      } == 2)
    }
  }

  test("select domain stream") {
    Required {
      assert(dao.selectNameStream{ stream =>
        assert(stream.toList == List(Name("SMITH"), Name("ALLEN")))
        stream.length
      } == 2)
    }
  }

  test("select by builder") {
    Required {
      assert(dao.selectByIDBuilder(1) === "SMITH")
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
            Name("SMITH"), // @Column(updatable = false)
            Some(5),
            Address("bbb", "ccc"),
            Some(2),
            Some(1)))) // @Version
    }
  }

  test("delete by entity") {
    Required {
      dao.selectById(1).foreach(dao.delete)
      assert(dao.selectCount() === 1)
    }
  }

  test("batch insert") {
    Required {
      dao.batchInsert(List(
        Person(
          name = Name("aaa"),
          age = Some(5),
          address = Address("bbb", "ccc"),
          departmentId = Some(1)),
        Person(
          name = Name("ddd"),
          age = Some(10),
          address = Address("eee", "fff"),
          departmentId = Some(2))))
      assert(dao.selectCount === 4)
      assert(
        dao.selectById(3) === Some(
          Person(
            Some(3),
            Name("aaa"),
            Some(5),
            Address("bbb", "ccc"),
            Some(1),
            Some(1))))
      assert(
        dao.selectById(4) === Some(
          Person(
            Some(4),
            Name("ddd"),
            Some(10),
            Address("eee", "fff"),
            Some(2),
            Some(1))))
    }
  }

  test("batch update") {
    Required {
      dao.batchUpdate(dao.selectAll().map(entity => entity.copy(age = entity.age.map(_ + 1))))
      assert(
        dao.selectAll === Seq(
          Person(
            Some(1),
            Name("SMITH"),
            Some(11),
            Address("Tokyo", "Yaesu"),
            Some(1),
            Some(1)),
          Person(
            Some(2),
            Name("ALLEN"),
            Some(21),
            Address("Kyoto", "Karasuma"),
            Some(2),
            Some(1)),
        )
      )
    }
  }
}
