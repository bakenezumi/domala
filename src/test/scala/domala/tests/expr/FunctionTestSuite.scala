package domala.tests.expr

import domala._
import domala.tests._
import org.scalatest.{BeforeAndAfter, FunSuite}

class FunctionTestSuite extends FunSuite with BeforeAndAfter {
  implicit val config: jdbc.Config = FunctionTestConfig

  val personDao: PersonDao = PersonDao.impl
  val dao:FunctionDao = FunctionDao.impl

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
  val person1 = Person(Some(1), Some(Name("SMITH")), Some(10), Address("Tokyo", "Yaesu"), Some(2), Some(0))
  val person2 = Person(Some(2), Some(Name("ALLEN")), Some(20), Address("Kyoto", "Karasuma"), Some(1), Some(0))

  test("2 parameter") {
    Required {
      assert(
        dao.twoParameterFunctionSelect("2", "3",  (x, y) => x.toInt + y.toInt).contains(person1)
      )
    }
  }

  test("default parameter") {
    Required {
      assert(
        dao.defaultParameterFunctionSelect(1).contains(person2)
      )
    }
  }

}

@Dao
trait FunctionDao {

  @Select("""
select * from person
where
id = /* f(x, y) - 4 */0
  """)
  def twoParameterFunctionSelect(x: String, y: String, f: (String, String) => Int): Option[Person]

  @Select("""
select * from person
where
id = /* twice(x) */0
  """)
  def defaultParameterFunctionSelect(x: Integer, twice: Integer => Int = _ * 2): Option[Person]

}
