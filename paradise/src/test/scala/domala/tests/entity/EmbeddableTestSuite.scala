package domala.tests.entity

import java.time.LocalDateTime
import java.util.Calendar

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
      val newEntity = new Embedded(
        None,
        E1(10),
        E2(1.23, MyDate(LocalDateTime.of(2017, 12, 31, 23, 59, 59, 999999999))),
        E3(Some("123"), Some(456), Some(MyDate(LocalDateTime.of(2018, 12, 31, 23, 59, 59, 999999999))))
      )
      dao.insert(newEntity)
      val selected1 = dao.selectById(1)
      assert(
        selected1 ==
          Some(Embedded(
            Some(1),
            E1(10),
            E2(1.23, MyDate(LocalDateTime.of(2017, 12, 31, 23, 59, 59, 999999999))),
            E3(Some("123"), Some(456), Some(MyDate(LocalDateTime.of(2018, 12, 31, 23, 59, 59, 999999999))))
          ))
      )
      selected1.map(e => e.copy(
        e2 = e.e2.copy(date = MyDate(e.e2.date.value.plusNanos(1))),
        e3 = E3(None, None, None)
      )).foreach(dao.update)
      val selected2 = dao.selectById(1)
      assert(selected2 ==
        Some(Embedded(
          Some(1),
          E1(10),
          E2(1.23, MyDate(LocalDateTime.of(2018, 1, 1, 0, 0, 0, 0))),
          E3(None, None, None))))
    }
  }
}

@Entity
case class Embedded(
  @domala.Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  id : Option[Int] = None,
  e1: E1,
  e2: E2,
  e3: E3
)
object Embedded {
  def test = 1
}

case class E1(int: Int)

case class E2(double: Double, date: MyDate)

case class E3(option1: Option[String], option2: Option[Int] , option3: Option[MyDate])

case class MyDate(value: LocalDateTime) extends AnyVal

@Dao(config = TestConfig)
trait EmbeddableDao {
  @Script(sql =
    """
create table embedded(
  id int not null identity primary key,
  int int,
  double double,
  date timestamp,
  option1 varchar(20),
  option2 int,
  option3 timestamp,
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
