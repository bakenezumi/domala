package domala.tests

import org.scalatest._
import domala.Required
import domala.jdbc.{BatchResult, Config, SelectOptions}

class StandardUseCaseTestSuite extends FunSuite with BeforeAndAfter {
  implicit val config: Config = TestConfig

  val dao: PersonDao = PersonDao.impl

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
        dao.selectById(1) == Some(
          Person(Some(ID(1)),
                 Some(Name("SMITH")),
                 Some(10),
                 Address("Tokyo", "Yaesu"),
                 Some(2),
                 Some(0))))
      assert(dao.selectById(5) == None)
    }
  }

  test("select to return Int") {
    Required {
      assert(dao.selectCount == 2)
    }
  }

  test("select to return Seq") {
    Required {
      assert(
        dao.selectAll == Seq(
          Person(
            Some(ID(1)),
            Some(Name("SMITH")),
            Some(10),
            Address("Tokyo", "Yaesu"),
            Some(2),
            Some(0)),
          Person(
            Some(ID(2)),
            Some(Name("ALLEN")),
            Some(20),
            Address("Kyoto", "Karasuma"),
            Some(1),
            Some(0))
        ))
    }
  }

  test("select to return nullable entity") {
    Required {
      assert(
        dao.selectByIdNullable(1) ==
          Person(
            Some(ID(1)),
            Some(Name("SMITH")),
            Some(10),
            Address("Tokyo", "Yaesu"),
            Some(2),
            Some(0)))
      assert(dao.selectByIdNullable(5) == null)
    }
  }

  test("join select") {
    Required {
      assert(
        dao.selectWithDepartmentById(1) ==
          Some(
            PersonDepartment(
              ID(1),
              Name("SMITH"),
              Some(ID(2)),
              Some(Name("SALES")))))
    }
  }

  test("join select to embedded entity") {
    Required {
      assert(
        dao.selectWithDepartmentEmbeddedById(1) ==
          Some(PersonDepartmentEmbedded(1, "SMITH", Department(ID(2), "SALES"))))
    }
  }

  test("select stream no param") {
    Required {
      assert(dao.selectAllStream { stream =>
        stream.size
      } == 2)
    }
  }

  test("select iterator no param") {
    Required {
      assert(dao.selectAllIterator { it =>
        it.size
      } == 2)
    }
  }

  test("select stream with one param") {
    Required {
      assert(dao.selectByIdStream(1) { stream =>
        stream.headOption.map(_.address)
      } == Some(Address("Tokyo", "Yaesu")))
      assert(dao.selectByIdStream(5) { stream =>
        stream.headOption.map(_.address)
      }.isEmpty)
    }
  }

  test("select iterator with one param") {
    Required {
      assert(dao.selectByIdIterator(1) { it =>
        it.toStream.headOption.map(_.address)
      } == Some(Address("Tokyo", "Yaesu")))
      assert(dao.selectByIdIterator(5) { it =>
        it.toStream.headOption.map(_.address)
      }.isEmpty)
    }
  }

  test("select Sequential Map") {
    Required {
      assert(dao.selectAllSeqMap() == Seq(
        Map("ID" -> 1, "NAME" -> "SMITH", "AGE" -> 10, "CITY" -> "Tokyo", "STREET" -> "Yaesu", "DEPARTMENT_ID" -> 2, "VERSION" -> 0),
        Map("ID" -> 2, "NAME" -> "ALLEN", "AGE" -> 20, "CITY" -> "Kyoto", "STREET" -> "Karasuma", "DEPARTMENT_ID" -> 1, "VERSION" -> 0)
      ))
    }
  }

  test("select Single Map") {
    Required {
      assert(dao.selectByIdMap(1) ==
        Map("ID" -> 1, "NAME" -> "SMITH", "AGE" -> 10, "CITY" -> "Tokyo", "STREET" -> "Yaesu", "DEPARTMENT_ID" -> 2, "VERSION" -> 0))
      assert(dao.selectByIdMap(5) == Map.empty)
    }
  }

  test("select Option Map") {
    Required {
      assert(dao.selectByIdOptionMap(1) == Some(Map("ID" -> 1, "NAME" -> "SMITH", "AGE" -> 10, "CITY" -> "Tokyo", "STREET" -> "Yaesu", "DEPARTMENT_ID" -> 2, "VERSION" -> 0)))
      assert(dao.selectByIdOptionMap(5).isEmpty)
    }
  }

  test("select option domain") {
    Required {
      assert(dao.selectNameById(1) == Some(Name("SMITH")))
      assert(dao.selectNameById(99).isEmpty)
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
        stream.size
      } == 2)
    }
  }

  test("select map iterator") {
    Required {
      assert(dao.selectAllIteratorMap { it =>
        it.size
      } == 2)
    }
  }

  test("select domain stream") {
    Required {
      assert(dao.selectNameStream{ stream =>
        assert(stream.toList == List(Name("SMITH"), Name("ALLEN")))
        stream.size
      } == 2)
    }
  }

  test("select domain iterator") {
    Required {
      assert(dao.selectNameIterator{ it =>
        val list = it.toList
        assert(list == List(Name("SMITH"), Name("ALLEN")))
        list.size
      } == 2)
    }
  }

  test("select by builder") {
    Required {
      assert(dao.selectByIDBuilder(1) == "SMITH")
    }
  }

  test("insert from entity") {
    Required {
      dao.insert(
        Person(
          name = Some(Name("aaa")),
          age = Some(5),
          address = Address("bbb", "ccc"),
          departmentId = Some(1)))
      assert(dao.selectCount == 3)
      assert(
        dao.selectById(3) == Some(
          Person(
            Some(ID(3)),
            Some(Name("aaa")),
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
          id = Some(ID(1)),
          name = Some(Name("aaa")),
          age = Some(5),
          address = Address("bbb", "ccc"),
          departmentId = Some(2),
          version = Some(0)))
      assert(
        dao.selectById(1) == Some(
          Person(
            Some(ID(1)),
            Some(Name("SMITH")), // @Column(updatable = false)
            Some(5),
            Address("bbb", "ccc"),
            Some(2),
            Some(1)))) // @Version
    }
  }

  test("delete by entity") {
    Required {
      dao.selectById(1).foreach(dao.delete)
      assert(dao.selectCount() == 1)
    }
  }

  test("batch insert") {
    Required {
      val BatchResult(counts, entitys) = dao.batchInsert(List(
        Person(
          name = Some(Name("aaa")),
          age = Some(5),
          address = Address("bbb", "ccc"),
          departmentId = Some(1)),
        Person(
          name = Some(Name("ddd")),
          age = Some(10),
          address = Address("eee", "fff"),
          departmentId = Some(2))))
      assert(counts sameElements Array(1, 1))
      assert(entitys == Seq(
        Person(
          Some(ID(3)),
          Some(Name("aaa")),
          Some(5),
          Address("bbb", "ccc"),
          Some(1),
          Some(1)),
        Person(
          Some(ID(4)),
          Some(Name("ddd")),
          Some(10),
          Address("eee","fff"),
          Some(2),
          Some(1))))
      assert(dao.selectCount == 4)
      assert(
        dao.selectById(3) == Some(
          Person(
            Some(ID(3)),
            Some(Name("aaa")),
            Some(5),
            Address("bbb", "ccc"),
            Some(1),
            Some(1))))
      assert(
        dao.selectById(4) == Some(
          Person(
            Some(ID(4)),
            Some(Name("ddd")),
            Some(10),
            Address("eee", "fff"),
            Some(2),
            Some(1))))
    }
  }

  test("batch update") {
    Required {
      val BatchResult(counts, entitys) = dao.batchUpdate(dao.selectAll().map(entity => entity.copy(age = entity.age.map(_ + 1))))
      assert(counts sameElements Array(1, 1))
      assert(entitys ==  Seq(
        Person(
          Some(ID(1)),
          Some(Name("SMITH")),
          Some(11),
          Address("Tokyo", "Yaesu"),
          Some(2),
          Some(1)),
        Person(
          Some(ID(2)),
          Some(Name("ALLEN")),
          Some(21),
          Address("Kyoto", "Karasuma"),
          Some(1),
          Some(1))
      ))
      assert(
        dao.selectAll == Seq(
          Person(
            Some(ID(1)),
            Some(Name("SMITH")),
            Some(11),
            Address("Tokyo", "Yaesu"),
            Some(2),
            Some(1)),
          Person(
            Some(ID(2)),
            Some(Name("ALLEN")),
            Some(21),
            Address("Kyoto", "Karasuma"),
            Some(1),
            Some(1))
        )
      )
    }
  }

  test("batch delete") {
    Required {
      dao.batchDelete(dao.selectAll())
      assert(dao.selectCount() == 0)
    }
  }

  test("insert by Sql") {
    Required {
      dao.insertSql(
        Person(
          Some(ID(3)),
          Some(Name("aaa")),
          Some(5),
          Address("bbb", "ccc"),
          Some(1),
          Some(1)),
        Person(
          Some(ID(3)),
          Some(Name("ddd")),
          Some(10),
          Address("eee", "fff"),
          Some(1),
          Some(2)),
        3
      )
      assert(dao.selectCount == 3)
      assert(
        dao.selectById(3) == Some(Person(
          Some(ID(3)),
          Some(Name("aaa")),
          Some(5),
          Address("eee", "fff"),
          Some(2),
          Some(3))))
    }
  }

  test("update by Sql") {
    Required {
      dao.updateSql(
        Person(
          Some(ID(1)),
          Some(Name("aaa")),
          Some(5),
          Address("bbb", "ccc"),
          Some(1),
          Some(1)),
        Person(
          Some(ID(3)),
          Some(Name("ddd")),
          Some(10),
          Address("eee", "fff"),
          Some(1),
          Some(2)),
        0
      )
      assert(
        dao.selectById(1) == Some(Person(
          Some(ID(1)),
          Some(Name("aaa")),
          Some(5),
          Address("eee", "fff"),
          Some(2),
          Some(1))))
    }
  }

  test("delete by Sql") {
    Required {
      dao.deleteSql(
        Person(
          Some(ID(1)),
          Some(Name("aaa")),
          Some(5),
          Address("bbb", "ccc"),
          Some(1),
          Some(1)),
        0
      )
      assert(dao.selectById(1).isEmpty)
    }
  }

  test("select by options") {
    val options = SelectOptions.get
    Required {
      assert(
        dao.selectAllByOption(options) == Seq(
          Person(
            Some(ID(1)),
            Some(Name("SMITH")),
            Some(10),
            Address("Tokyo", "Yaesu"),
            Some(2),
            Some(0)),
          Person(
            Some(ID(2)),
            Some(Name("ALLEN")),
            Some(20),
            Address("Kyoto", "Karasuma"),
            Some(1),
            Some(0))
        ))
      assert(options.getCount == -1)
      options.limit(1).count()
      assert(
        dao.selectAllByOption(options) == Seq(
          Person(
            Some(ID(1)),
            Some(Name("SMITH")),
            Some(10),
            Address("Tokyo", "Yaesu"),
            Some(2),
            Some(0))
        ))
      assert(options.getCount == 2)
    }
  }

  test("batch insert by Sql") {
    Required {
      dao.batchInsertSql(Seq(
        Person(
          Some(ID(3)),
          Some(Name("aaa")),
          Some(5),
          Address("bbb", "ccc"),
          Some(1),
          Some(1)),
        Person(
          Some(ID(4)),
          Some(Name("ddd")),
          Some(10),
          Address("eee", "fff"),
          Some(1),
          Some(1)),
        Person(
          Some(ID(5)),
          Some(Name("ggg")),
          Some(15),
          Address("hhh", "iii"),
          Some(1),
          Some(1))
      ))
      assert(dao.selectCount == 5)
      assert(
        dao.selectById(3) == Some(Person(
          Some(ID(3)),
          Some(Name("aaa")),
          Some(5),
          Address("bbb", "ccc"),
          Some(2),
          Some(1))))
      assert(
        dao.selectById(4) == Some(Person(
          Some(ID(4)),
          Some(Name("ddd")),
          Some(10),
          Address("eee", "fff"),
          Some(2),
          Some(1))))
      assert(
        dao.selectById(5) == Some(Person(
          Some(ID(5)),
          Some(Name("ggg")),
          Some(15),
          Address("hhh", "iii"),
          Some(2),
          Some(1))))
    }
  }

  test("batch update by Sql") {
    Required {
      val entities = for(e <- dao.selectAll()) yield e.copy(age = e.age.map(_ + 10))
      dao.batchUpdateSql(entities)
      assert(
        dao.selectAll == Seq(
          Person(
            Some(ID(1)),
            Some(Name("SMITH")),
            Some(20),
            Address("Tokyo", "Yaesu"),
            Some(2),
            Some(1)),
          Person(
            Some(ID(2)),
            Some(Name("ALLEN")),
            Some(30),
            Address("Kyoto", "Karasuma"),
            Some(2),
            Some(1)),
        )
      )
    }
  }

  test("batch delete by Sql") {
    Required {
      dao.batchDeleteSql(dao.selectAll())
      assert(dao.selectCount() == 0)
    }
  }

}
