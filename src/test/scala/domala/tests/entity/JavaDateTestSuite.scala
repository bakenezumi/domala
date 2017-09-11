package domala.tests.entity

import java.time.{LocalDate, LocalDateTime, LocalTime}

import domala._
import domala.jdbc.Config
import domala.tests.TestConfig
import org.scalatest.{BeforeAndAfter, FunSuite}
import domala.jdbc.Result

class JavaDateTestSuite  extends FunSuite with BeforeAndAfter {
  implicit val config: Config = TestConfig

  val dao: JavaDateDao = JavaDateDao

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

  test("select Java Date") {
    Required {
      assert(dao.select(0) === JavaDateEntity(0, null, None, null, None, null, None))
    }
  }

  test("insert Java Date") {
    Required {
      val entity = JavaDateEntity(
        1,
        LocalDate.of(2017, 9, 5), Some(LocalDate.of(2020, 12, 31)),
        LocalTime.of(12, 59, 59), Some(LocalTime.of(13, 0, 0)),
        LocalDateTime.of(2017, 9, 5, 12, 59, 59, 999999999), Some(LocalDateTime.of(2020, 12, 31, 13, 0, 0, 1))
      )
      dao.insert(entity)
      assert(dao.select(1) === entity)
    }
  }
}

@Entity(name = "javadate")
case class JavaDateEntity(
   id : Int,
   basicDate : LocalDate,
   optionDate : Option[LocalDate],
   basicTime : LocalTime,
   optionTime : Option[LocalTime],
   basicDateTime : LocalDateTime,
   optionDateTime : Option[LocalDateTime]
)


@Dao(config = TestConfig)
trait JavaDateDao {

  @Script(sql =
    """
create table javadate(
  id int not null identity primary key,
  basic_date date,
  option_date date,
  basic_time time,
  option_time time,
  basic_date_time timestamp,
  option_date_time timestamp
);

insert into javadate (id, basic_date, option_date, basic_time, option_time, basic_date_time, option_date_time) values(0, null, null, null, null, null, null);
    """)
  def create()

  @Script(sql =
    """
drop table javadate;
    """)
  def drop()

  @Select(sql=
    """
select * from javadate where id = /* id */0
"""
  )
  def select(id: Int): JavaDateEntity

  @Insert
  def insert(entity: JavaDateEntity): Result[JavaDateEntity]
}

