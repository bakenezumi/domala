package domala.tests.expr

import domala._
import domala.tests._
import org.scalatest.{BeforeAndAfter, FunSuite}

class ExpressionTestSuite extends FunSuite with BeforeAndAfter {
  implicit val config: jdbc.Config = ExprTestConfig
  val personDao: PersonDao = PersonDao.impl(ExprTestConfig)
  val dao: ExpressionDao = ExpressionDao.impl

  before {
    Required {
      personDao.create()
    }
  }

  after {
    Required {
      personDao.drop()
    }
  }

  test("Iterable parameter") {
    Required {
      assert(dao.inSelect(List(1, 3, 5)) == Seq(
        Person(Some(1),
          Name("SMITH"),
          Some(10),
          Address("Tokyo", "Yaesu"),
          Some(2),
          Some(0))
      ))
    }
  }

  test("literal") {
    Required {
      assert(
        dao.literalSelect(1).contains(
        Person(Some(1),
          Name("SMITH"),
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
          Person(Some(2),
            Name("ALLEN"),
            Some(20),
            Address("Kyoto", "Karasuma"),
            Some(1),
            Some(0)),
          Person(Some(1),
            Name("SMITH"),
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
          Person(Some(2),
            Name("ALLEN"),
            Some(20),
            Address("Kyoto", "Karasuma"),
            Some(1),
            Some(0))
        ))
      assert(
        dao.ifSelect(None) == Seq(
          Person(Some(1),
            Name("SMITH"),
            Some(10),
            Address("Tokyo", "Yaesu"),
            Some(2),
            Some(0)),
          Person(Some(2),
            Name("ALLEN"),
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
          Person(Some(2),
            Name("ALLEN"),
            Some(20),
            Address("Kyoto", "Karasuma"),
            Some(1),
            Some(0))
        ))
      assert(
        dao.elseSelect(None, Some(2)) == Seq(
          Person(Some(1),
            Name("SMITH"),
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
        Person(Some(2),
          Name("ALLEN"),
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

  test("populate expression") {
    Required {
      val entity =
        Person(
          Some(3),
          Name("AAA"),
          Some(3),
          Address("BBB", "CCC"),
          Some(1),
          Some(1))
      dao.populateUpdate(entity, 2)
    }
  }

  test("populate expression int result") {
    Required {
      val entity =
        Person(
          Some(3),
          Name("AAA"),
          Some(3),
          Address("BBB", "CCC"),
          Some(1),
          Some(1))
      dao.populateUpdate2(entity, 2)
    }
  }
}

@Dao(config = ExprTestConfig)
trait ExpressionDao {

  @Select("""
select * from person
where
id in /*ids*/(0)
  """)
  def inSelect(ids: List[Int]): Seq[Person]

  @Select("""
select * from person
where
id = /*^ id*/0
  """)
  def literalSelect(id: Int): Option[Person]

  @Select("""
select * from person
/*# orderBy */
  """)
  def embeddedSelect(orderBy: String): Seq[Person]

  @Select("""
select * from person
where
/*%if id.isDefined() */
id = /*id.get()*/0
/*%end*/
  """)
  def ifSelect(id: Option[Int]): Seq[Person]

  @Select("""
select * from person
where
/*%if id.isDefined() */
  id = /*id.get()*/0
/*%elseif departmentId.isDefined() */
  and
  department_id = /*departmentId.get()*/0
/*%else */
  and
  department_id is null
/*%end*/
  """)
  def elseSelect(id: Option[Int], departmentId: Option[Int]): Seq[Person]

  @Select("""
select * from person
where
/*%for name : names */
name like /* name + "%" */'hoge%'
  /*%if name_has_next */
/*# "or" */
  /*%end */
/*%end*/
  """)
  def forSelect(names: List[String]): Seq[Person]

  @Select("""
select /*%expand */* from person
  """)
  def expandSelect: Seq[Person]

  @Select("""
select /*%expand "p"*/* from person p
  """)
  def expandAliasSelect: Seq[Person]

  @Update("""
update person set /*%populate*/id = id where id = /* id */0
  """)
  def populateUpdate(entity: Person, id: Int): jdbc.Result[Person]

  // TODO: Entityのパラメータ判定変更
    @Update("""
  update person set /*%populate*/id = id where id = /* id */0
    """)
    def populateUpdate2(entity: Person, id: Int): Int

}
