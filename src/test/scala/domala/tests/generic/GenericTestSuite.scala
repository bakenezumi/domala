package domala.tests.generic

import domala._
import domala.jdbc.{Config, Result}
import domala.tests.TestConfig
import org.scalatest.{BeforeAndAfter, FunSuite}

class GenericTestSuite extends FunSuite with BeforeAndAfter {
  implicit val config: Config = TestConfig
  val dao1: GenericDao[Table1] = Dao1.impl
  val dao2: GenericDao[Table2] = Dao2.impl
  val daoList: List[GenericDao[_]] = List(dao1, dao2)

  before {
    Required {
      daoList.foreach(_.create())
    }
  }

  after {
    Required {
      daoList.foreach(_.drop())
    }
  }

  test("generic dao test") {
    val entity1_1 = Table1(1, "value1")
    val entity1_2 = Table1(2, "value2")
    val entity2_1 = Table2(1, "value1", 1.0)
    val entity2_2 = Table2(2, "value2", 2.0)

    val entities = List(
      entity1_1,
      entity1_2,
      entity2_1,
      entity2_2
    )
    Required {
      entities foreach {
        case e: Table1 => dao1.insert(e)
        case e: Table2 => dao2.insert(e)
        case _ => fail()
      }
      assert(daoList.flatMap(_.selectAll) == entities)
      assert(daoList.flatMap(_.selectById(2)) == List(entity1_2, entity2_2))
    }
  }

}

@Entity
case class Table1(
  id: Int,
  value: String
)

@Entity
case class Table2(
  id: Int,
  value1: String,
  value2: Double
)

trait GenericDao[E] {
  def create(): Unit
  def drop(): Unit
  def selectAll: Seq[E]
  def selectById(id: Int): Option[E]
  def insert(entity: E): Result[E]
}

@Dao(config = TestConfig)
trait Dao1 extends GenericDao[Table1] {
  @Script(sql="""
create table table1(
    id int not null identity primary key,
    value varchar(20)
);
  """)
  def create(): Unit

  @Script(sql="""
drop table table1
  """)
  def drop(): Unit

  @Select("""
select * from table1
  """)
  def selectAll: Seq[Table1]
  @Select("""
select * from table1 where id = /*id*/0
  """)
  def selectById(id: Int): Option[Table1]

  @Insert
  def insert(entity: Table1): Result[Table1]
}


@Dao(config = TestConfig)
trait Dao2 extends GenericDao[Table2] {
  @Script(sql="""
create table table2(
    id int not null identity primary key,
    value1 varchar(20),
    value2 double
);
  """)
  def create(): Unit

  @Script(sql="""
drop table table2
  """)
  def drop(): Unit

  @Select("""
select * from table2
  """)
  def selectAll: Seq[Table2]
  @Select("""
select * from table2 where id = /*id*/0
  """)
  def selectById(id: Int): Option[Table2]

  @Insert
  def insert(entity: Table2): Result[Table2]
}
