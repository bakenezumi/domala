package domala.tests.entity

import java.time.LocalDateTime

import domala._
import domala.jdbc.{Config, Result}
import domala.tests.TestConfig
import org.scalatest.{BeforeAndAfter, FunSuite}

class EmbeddableTestSuite extends FunSuite with BeforeAndAfter {
  implicit val config: Config = TestConfig

  val dao: EmbeddableDao = EmbeddableDao.impl

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

  test("insert & update Embedded") {
    Required {
      val newEntity = new Embedded(None, e1 = E1(10), e2 = E2(1.23, MyDate(LocalDateTime.of(2017, 12, 31, 23, 59, 59, 999999999))))
      dao.insert(newEntity)
      val selected1 = dao.selectById(1)
      assert(selected1.contains(Embedded(Some(1), E1(10), E2(1.23, MyDate(LocalDateTime.of(2017, 12, 31, 23, 59, 59, 999999999))))))
      selected1.map(e => e.copy(e2 = e.e2.copy(date = MyDate(e.e2.date.value.plusNanos(1))))).foreach(dao.update)
      val selected2 = dao.selectById(1)
      assert(selected2.contains(Embedded(Some(1), E1(10), E2(1.23, MyDate(LocalDateTime.of(2018, 1, 1, 0, 0, 0, 0))))))
    }
  }
}

@Entity
case class Embedded(
  @domala.Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  id : Option[Int] = None,
  e1: E1,
  e2: E2
)
object Embedded {
  def test = 1
}

@Embeddable
case class E1(int: Int)

@Embeddable
case class E2(double: Double, date: MyDate)

@Holder
case class MyDate(value: LocalDateTime)

@Dao(config = TestConfig)
trait EmbeddableDao {
  @Script(sql =
    """
create table embedded(
  id int not null identity primary key,
  int int,
  double double,
  date timestamp
);
    """)
  def create()

  @Script(sql =
    """
drop table embedded
    """)
  def drop()

  @Select(sql="""
select * from embedded
where id = /* id */0
  """)
  def selectById(id: Int): Option[Embedded]

  @Insert
  def insert(entity: Embedded): Result[Embedded]

  @Update
  def update(entity: Embedded): Result[Embedded]
}
