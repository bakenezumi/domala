package domala.tests.entity

import domala._
import domala.jdbc.{Config, Result}
import domala.tests.TestConfig
import org.scalatest.{BeforeAndAfter, FunSuite}

class ColumnTestSuite extends FunSuite with BeforeAndAfter {
  implicit val config: Config = TestConfig

  val dao: ColumnTestDao = ColumnTestDao.impl

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

  test("insert & update") {
    Required {
      dao.insert(ColumnTest(Some(1), foo = Some("aaa"), bar = Some("bbb"), baz = Some("ccc")))
      assert(dao.selectAll == Seq(
        ColumnTest(Some(1), Some("aaa"), None, Some("ccc"))
      ))
      dao.update(ColumnTest(Some(1), foo = Some("ddd"), bar = Some("eee"), baz = Some("fff")))
      assert(dao.selectAll == Seq(
        ColumnTest(Some(1), Some("ddd"), Some("eee"), Some("ccc"))
      ))
    }
  }

}

@Entity
case class ColumnTest (
  @domala.Id
  id: Option[Int],
  @Column(name = "fooo", quote = true)
  foo: Option[String],
  @Column(insertable = false)
  bar: Option[String],
  @Column(updatable = false)
  baz: Option[String]
)

@Dao
trait ColumnTestDao {

  @Script(sql = """
create table column_test(
  id long not null identity primary key,
  "fooo" varchar(20),
  bar  varchar(20),
  baz  varchar(20)
);
    """)
  def create()

  @Script(sql = """
drop table column_test;
    """)
  def drop()

  @Select(sql="""
select * from column_test
  """)
  def selectAll: Seq[ColumnTest]

  @Insert
  def insert(entity: ColumnTest): Result[ColumnTest]

  @Update
  def update(entity: ColumnTest): Result[ColumnTest]

}
