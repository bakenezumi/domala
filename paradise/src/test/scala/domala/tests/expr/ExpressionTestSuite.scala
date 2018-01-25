package domala.tests.expr

import domala._
import domala.jdbc.BatchResult
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

  test("populate expression") {
    Required {
      val entity =
        Person(
          Some(ID(3)),
          Some(Name("AAA")),
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
          Some(ID(3)),
          Some(Name("AAA")),
          Some(3),
          Address("BBB", "CCC"),
          Some(1),
          Some(1))
      dao.populateUpdate2(entity, 2)
    }
  }

  test("function parameter") {
    Required {
      assert(
        dao.functionSelect("2") == Some(
          Person(Some(ID(1)),
            Some(Name("SMITH")),
            Some(10),
            Address("Tokyo", "Yaesu"),
            Some(2),
            Some(0))))
    }
  }

  test("embedded function parameter") {
    Required {
      assert(
        dao.embeddedFunctionSelect(Name("S")) == Seq(
          Person(Some(ID(1)),
            Some(Name("SMITH")),
            Some(10),
            Address("Tokyo", "Yaesu"),
            Some(2),
            Some(0))))
    }
  }

  test("entity parameter") {
    val entity1 = Some(Person(Some(ID(1)), null, None, null, None, None))
    val entity2 = Some(Person(None, Some(Name("ALLEN")), None, null, None, None))
    val entity3 = Some(Person(None, null, None, Address(null, "Karasuma"), None, None))

    Required {
      assert(
        dao.entityParameterSelect(entity1) == Some(
          Person(
            Some(ID(1)),
            Some(Name("SMITH")),
            Some(10),
            Address("Tokyo", "Yaesu"),
            Some(2),
            Some(0))))
      assert(
        dao.entityParameterSelect(entity2) == Some(
          Person(
            Some(ID(2)),
            Some(Name("ALLEN")),
            Some(20),
            Address("Kyoto", "Karasuma"),
            Some(1),
            Some(0))))
      assert(
        dao.entityParameterSelect(entity3) == Some(
          Person(Some(ID(2)),
            Some(Name("ALLEN")),
            Some(20),
            Address("Kyoto", "Karasuma"),
            Some(1),
            Some(0))))
    }
  }

  test("populate batch update") {
    Required {
      val entities = Seq(
        Person(
          Some(ID(1)),
          Some(Name("AAA")),
          Some(3),
          Address("BBB", "CCC"),
          Some(1),
          Some(0)),
        Person(
          Some(ID(2)),
          Some(Name("DDD")),
          Some(4),
          Address("EEE", "FFF"),
          Some(2),
          Some(0))
      )
      dao.populateBatchUpdate(entities)
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
/*%if id != null */
id = /*id*/0
/*%end*/
  """)
  def ifSelect(id: Option[Int]): Seq[Person]

  @Select("""
select * from person
where
/*%if id != null*/
  id = /*id*/0
/*%elseif departmentId != null */
  and
  department_id = /*departmentId */0
/*%else */
  and
  department_id is null
/*%end*/
  """)
  def elseSelect(id: Option[Int], departmentId: Option[Int]): Seq[Person]

  @Select("""
select * from person
where
/*%for name: names */
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

  @Update("""
update person set /*%populate*/id = id where id = /* id */0
  """)
  def populateUpdate2(entity: Person, id: Int): Int

  @Select("""
select * from person
where
id = /* f(id) - 1 */0
  """)
  def functionSelect(id: String, f: String => Int = s => s.toInt) : Option[Person]

  @Select("""
select * from person
where
name like /* @prefix(name.value) */'a%' escape '$'
  """)
  def embeddedFunctionSelect(name: Name) : Seq[Person]

  @Select("""
select * from person
where
/*%if entity.id != null */
  id = /*entity.id*/0
/*%end*/
/*%if entity.name != null */
  and
  name = /*entity.name*/0
/*%end*/
/*%if entity.age != null */
  and
  age = /*entity.age*/0
/*%end*/
/*%if entity.address != null && entity.address.city != null */
  and
  address = /*entity.address.city*/0
/*%end*/
/*%if entity.address != null && entity.address.street != null */
  and
  street = /*entity.address.street*/0
/*%end*/
/*%if entity.departmentId != null */
  and
  department_id = /*entity.departmentId*/0
/*%end*/
/*%if entity.version != null */
  and
  version = /*entity.version*/0
/*%end*/
  """)
  def entityParameterSelect(entity: Option[Person]) : Option[Person]

  @BatchUpdate("""
update person set /*%populate*/id = id where id = /* entity.id */0
  """)
  def populateBatchUpdate(entity: Seq[Person]): BatchResult[Person]
}
