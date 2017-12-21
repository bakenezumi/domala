package domala.tests

import domala.Required
import domala.jdbc.Config
import org.scalatest._

class SqlFileTestSuite extends FunSuite with BeforeAndAfter {
  implicit val config: Config = new H2TestConfigTemplate("sql-file"){}

  val ddlDao: PersonDao = PersonDao.impl
  val dao: PersonSqlFileDao = PersonSqlFileDao.impl

  before {
    Required {
      ddlDao.create()
    }
  }

  after {
    Required {
      ddlDao.drop()
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

  test("join select to embedded entity") {
    Required {
      assert(
        dao.selectWithDepartmentEmbeddedById(1) ==
          Some(PersonDepartmentEmbedded(1, "SMITH", Department(ID(2), "SALES"))))
    }
  }

  test("select iterator no param") {
    Required {
      assert(dao.selectAllIterator { it =>
        it.size
      } == 2)
    }
  }

  test("Iterable parameter") {
    Required {
      assert(dao.inSelect(List(1, 3, 5)) == Seq(
        Person(
          Some(ID(1)),
          Some(Name("SMITH")),
          Some(10),
          Address("Tokyo", "Yaesu"),
          Some(2),
          Some(0))
      ))
    }
  }

  test("Nil parameter") {
    Required {
      assert(dao.inSelect(Nil) == Nil)
    }
  }

  test("literal") {
    Required {
      assert(
        dao.literalSelect(1) == Some(
          Person(
            Some(ID(1)),
            Some(Name("SMITH")),
            Some(10),
            Address("Tokyo", "Yaesu"),
            Some(2),
            Some(0))))
    }
  }

  test("embedded") {
    Required {
      assert(
        dao.embeddedSelect("order by id desc") == Seq(
          Person(
            Some(ID(2)),
            Some(Name("ALLEN")),
            Some(20),
            Address("Kyoto", "Karasuma"),
            Some(1),
            Some(0)),
          Person(
            Some(ID(1)),
            Some(Name("SMITH")),
            Some(10),
            Address("Tokyo", "Yaesu"),
            Some(2),
            Some(0)),
        ))
    }
  }

  test("if expression") {
    Required{
      assert(
        dao.ifSelect(Some(2)) == Seq(
          Person(
            Some(ID(2)),
            Some(Name("ALLEN")),
            Some(20),
            Address("Kyoto", "Karasuma"),
            Some(1),
            Some(0))
        ))
      assert(
        dao.ifSelect(None) == Seq(
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

  test("else if expression") {
    Required{
      assert(
        dao.elseSelect(Some(2), Some(2)) == Seq(
          Person(Some(ID(2)),
            Some(Name("ALLEN")),
            Some(20),
            Address("Kyoto", "Karasuma"),
            Some(1),
            Some(0))
        ))
      assert(
        dao.elseSelect(None, Some(2)) == Seq(
          Person(Some(ID(1)),
            Some(Name("SMITH")),
            Some(10),
            Address("Tokyo", "Yaesu"),
            Some(2),
            Some(0))
        ))
      assert(
        dao.elseSelect(None, None) == Nil)
    }
  }

  test("for expression") {
    Required {
      assert(dao.forSelect(List("BB", "AL")) == Seq(
        Person(Some(ID(2)),
          Some(Name("ALLEN")),
          Some(20),
          Address("Kyoto", "Karasuma"),
          Some(1),
          Some(0))
      ))
    }
  }

  test("expand expression") {
    Required {
      val all = dao.ifSelect(None)
      assert(dao.expandSelect == all)
      assert(dao.expandAliasSelect == all)
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

}
