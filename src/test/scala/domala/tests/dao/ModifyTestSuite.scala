package domala.tests.dao

import domala._
import domala.jdbc.{BatchResult, Config, Result}
import domala.tests.{ID, Person, PersonDao}
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.seasar.doma.jdbc.Naming
import org.seasar.doma.jdbc.dialect.H2Dialect
import org.seasar.doma.jdbc.tx.LocalTransactionDataSource

class ModifyTestSuite extends FunSuite with BeforeAndAfter {
  implicit val config: Config = ModifyTestConfig
  val personDao: PersonDao = PersonDao.impl
  val dao: ModifyTestDao = ModifyTestDao.impl

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

  test("sql insert") {
    Required {
      dao.sqlInsert(ID(3))
    }
  }

  test("batch sql insert") {
    Required {
      val counts =dao.batchSqlInsert(Seq("3","4"))
      assert(counts sameElements Array(1, 1))
    }
  }
}

@Dao
trait ModifyTestDao {
  @Insert("""
insert into person (id, city, street, department_id, version) values (
  /*id*/0,
  '',
  '',
  1,
  1
)
  """)
  def sqlInsert(id: ID[Person]): Int

  @BatchInsert("""
insert into person (id, city, street, department_id, version) values (
  /*id*/0,
  '',
  '',
  1,
  1
)
  """)
  def batchSqlInsert(id: Seq[String]): Array[Int]
}
