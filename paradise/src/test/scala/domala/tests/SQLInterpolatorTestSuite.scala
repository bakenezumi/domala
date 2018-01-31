package domala.tests

import domala._
import domala.jdbc.Config
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.seasar.doma.MapKeyNamingType
import org.seasar.doma.jdbc.JdbcException

class SQLInterpolatorTestSuite extends FunSuite with BeforeAndAfter {
  implicit val config: Config = SQLInterpolatorTestConfig
  val dao: PersonDao = PersonDao.impl
  val singleResultStatement = select"select * from person where id = 1"
  val listResultStatement = select"select * from person"

  val person1 = Person(Some(ID(1)),
    Some(Name("SMITH")),
    Some(10),
    Address("Tokyo", "Yaesu"),
    Some(2),
    Some(0))
  val person2 = Person(
    Some(ID(2)),
    Some(Name("ALLEN")),
    Some(20),
    Address("Kyoto", "Karasuma"),
    Some(1),
    Some(0))


  before {
    Required(dao.create())
  }

  after {
    Required(dao.drop())
  }


  test("basic parameter multi column single row select") {
    val statement = (id: Int) => select"select * from person where id = $id"
    Required {
      assert(statement(1).getSingle[Person] == person1)
      assert(statement(2).getOption[Person] == Some(person2))
      assert(statement(99).getSingle[Person] == null)
      assert(statement(99).getOption[Person] == None)
      assert(statement(1).getMapSingle == Map("ID" -> 1, "NAME" -> "SMITH", "CITY" -> "Tokyo", "DEPARTMENT_ID" -> 2, "AGE" -> 10, "VERSION" -> 0, "STREET" -> "Yaesu"))
      assert(statement(2).getOptionMapSingle == Some(Map("ID" -> 2, "NAME" -> "ALLEN", "CITY" -> "Kyoto", "DEPARTMENT_ID" -> 1, "AGE" -> 20, "VERSION" -> 0, "STREET" -> "Karasuma")))
      assert(statement(99).getMapSingle == null)
      assert(statement(99).getOptionMapSingle == None)
    }
  }

  test("holder parameter single column single row select") {
    val statement = (id: ID[Person]) => select"select name from person where id = $id"
    Required {
      assert(statement(ID(1)).getSingle[Name] == Name("SMITH"))
      assert(statement(ID(2)).getOption[Name] == Some(Name("ALLEN")))
      assert(statement(ID(99)).getSingle[Name] == Name(null))
      assert(statement(ID(99)).getOption[Name] == None)
      assert(statement(ID(1)).getSingle[String] == "SMITH")
      assert(statement(ID(2)).getOption[String] == Some("ALLEN"))
    }
  }

  test("value class parameter single column single row select") {
    val statement = (id: IDVal[Person]) => select"select name from person where id = $id"
    Required {
      assert(statement(IDVal(1)).getSingle[NameVal] == NameVal("SMITH"))
      assert(statement(IDVal(2)).getOption[NameVal] == Some(NameVal("ALLEN")))
      assert(statement(IDVal(99)).getSingle[NameVal] == NameVal(null))
      assert(statement(IDVal(99)).getOption[NameVal] == None)
      assert(statement(IDVal(1)).getSingle[String] == "SMITH")
      assert(statement(IDVal(2)).getOption[String] == Some("ALLEN"))
    }
  }

  test("multi column multi row select") {
    val statement = (id: Int) => select"select * from person where id > $id"
    Required {
      assert(statement(0).getList[Person] == List(person1, person2))
      assert(statement(99).getList[Person] == Nil)
      assert(statement(0).getMapSeq == Seq(
        Map("ID" -> 1, "NAME" -> "SMITH", "CITY" -> "Tokyo", "DEPARTMENT_ID" -> 2, "AGE" -> 10, "VERSION" -> 0, "STREET" -> "Yaesu"),
        Map("ID" -> 2, "NAME" -> "ALLEN", "CITY" -> "Kyoto", "DEPARTMENT_ID" -> 1, "AGE" -> 20, "VERSION" -> 0, "STREET" -> "Karasuma")
      ))
      assert(statement(99).getMapSeq == Nil)
    }
  }

  test("single column multi row select") {
    val statement = (id: ID[Person]) => select"select name from person where id > $id"
    Required {
      assert(statement(ID(0)).getList[Name] == List(Name("SMITH"), Name("ALLEN")))
      assert(statement(ID(99)).getList[Name] == Nil)
      assert(statement(ID(0)).getList[String] == List("SMITH", "ALLEN"))
      assert(statement(ID(99)).getList[String] == Nil)
    }
  }

  test("iterator entity select") {
    val statement = (ids: List[ID[Person]]) => select"select * from person where id in ($ids)"
    Required {
      assert(statement(List(ID(1), ID(2))).apply((it: Iterator[Person]) => it.flatMap(_.id).sum) == ID(3))
      // `apply` is can be omitted
      assert(statement(List(ID(2))) { (it: Iterator[Person]) => it.flatMap(_.age).sum } == 20)
      assert(statement(Nil) { (it: Iterator[Person]) => it.flatMap(_.age).sum } == 0)
    }
  }

  test("iterator holder select") {
    val statement = (ids: List[ID[Person]]) => select"select Name from person where id in ($ids)"
    Required {
      assert(statement(List(ID(1), ID(2))) { (it: Iterator[Name]) => it.toList } == List(Name("SMITH"), Name("ALLEN")))
      assert(statement(Nil) { (it: Iterator[Name]) => it.toList } == Nil)
    }
  }

  test("return a not support type select") {
    val statement = select"select * from person"

    // compile error
//    val caught1 = intercept[DomaException] {
//      statement.getSingle[Calendar]
//    }
//    assert(caught1.getMessageResource == Message.DOMALA4008)

    // compile error
//    val caught2 = intercept[DomaException] {
//      statement.getOption[PersonDao]
//    }
//    assert(caught2.getMessageResource == Message.DOMALA4008)

    // compile error
//    val caught3 = intercept[DomaException] {
//      statement.getList[Calendar]
//    }
//    assert(caught3.getMessageResource == Message.DOMALA4008)


    // compile error
//    val caught4 = intercept[DomaException] {
//      statement { (_: Iterator[PersonDao]) => 1}
//    }
//    assert(caught4.getMessageResource == Message.DOMALA4008)

  }

  private[this] val createScript =
    script"""
      create table emp(
        id int serial primary key,
        name varchar(20),
        age int default 1
      )
    """

  private[this] val dropScript = script"drop table emp"

  test("script execute") {
    Required {
      createScript.execute()
      assert(select"select * from emp".getOptionMapSingle == None)
      dropScript.execute()
      val caught = intercept[JdbcException] {
        select"select * from emp".getOptionMapSingle
      }
      assert(caught.getMessageResource == org.seasar.doma.message.Message.DOMA2016)
    }
  }

  test("insert execute") {
    Required {
      createScript.execute()
      Seq("foo", "baz", "bar").foreach { name =>
        update"insert into emp(name) values($name)".execute()
      }
      assert(select"select count(*) from emp".getSingle[Int] == 3)
      dropScript.execute()
    }
  }

  test("update execute") {
    Required {
      createScript.execute()
      Seq("foo", "baz", "bar").foreach { name =>
        update"insert into emp(name) values($name)".execute()
      }
      assert(select"select age from emp".getList[Int].sum == 3)
      select"select id from emp".apply { (ids: Iterator[Int]) =>
        ids.foreach{ id =>
          update"update emp set age = $id where id = $id".execute()
        }
      }
      assert(select"select age from emp".getList[Int].sum == 6)
      dropScript.execute()
    }
  }

  test("delete execute") {
    Required {
      createScript.execute()
      Seq("foo", "baz", "bar").foreach { name =>
        update"insert into emp(name) values($name)".execute()
      }
      select"select id from emp".apply { (ids: Iterator[Int]) =>
        ids.filter(_ %2 != 0).foreach{ id =>
          update"delete from emp where id = $id".execute()
        }
      }
      assert(select"select * from emp".getMapList(MapKeyNamingType.LOWER_CASE) == List(Map("id" -> 2, "name" -> "baz", "age" -> 1)))
      dropScript.execute()
    }
  }

}

object SQLInterpolatorTestConfig extends H2TestConfigTemplate("interpolator")
