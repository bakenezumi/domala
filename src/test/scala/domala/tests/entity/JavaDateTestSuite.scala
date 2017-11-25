package domala.tests.entity

import java.time.{LocalDate, LocalDateTime, LocalTime}

import domala._
import domala.jdbc.Config
import domala.tests.TestConfig
import org.scalatest.{BeforeAndAfter, FunSuite}
import domala.jdbc.Result

class JavaDateTestSuite  extends FunSuite with BeforeAndAfter {
  implicit val config: Config = TestConfig

  val dao: JavaDateDao = JavaDateDao.impl

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

  test("select Java Date holder") {
    Required {
      assert(dao.selectHolder(0) === JavaDateHolderEntity(0, null, None, null, None, null, None))
    }
  }

  test("insert Java Date holder") {
    Required {
      val entity = JavaDateHolderEntity(
        1,
        LocalDateHolder(LocalDate.of(2017, 9, 5)), Some(LocalDateHolder(LocalDate.of(2020, 12, 31))),
        LocalTimeHolder(LocalTime.of(12, 59, 59)), Some(LocalTimeHolder(LocalTime.of(13, 0, 0))),
        LocalDateTimeHolder(LocalDateTime.of(2017, 9, 5, 12, 59, 59, 999999999)), Some(LocalDateTimeHolder(LocalDateTime.of(2020, 12, 31, 13, 0, 0, 1)))
      )
      dao.insertHolder(entity)
      assert(dao.selectHolder(1) == entity)
    }
  }

  test("insert Java Date AnyVal") {
    Required {
      val entity = JavaDateValEntity(
        1,
        LocalDateVal(LocalDate.of(2017, 9, 5)), Some(LocalDateVal(LocalDate.of(2020, 12, 31))),
        LocalTimeVal(LocalTime.of(12, 59, 59)), Some(LocalTimeVal(LocalTime.of(13, 0, 0))),
        LocalDateTimeVal(LocalDateTime.of(2017, 9, 5, 12, 59, 59, 999999999)), Some(LocalDateTimeVal(LocalDateTime.of(2020, 12, 31, 13, 0, 0, 1)))
      )
      dao.insertVal(entity)
      assert(dao.selectVal(1) == entity)

    }
  }

}

@Entity
@Table(name = "java_date")
case class JavaDateEntity(
   id : Int,
   basicDate : LocalDate,
   optionDate : Option[LocalDate],
   basicTime : LocalTime,
   optionTime : Option[LocalTime],
   basicDateTime : LocalDateTime,
   optionDateTime : Option[LocalDateTime]
)

@Holder
case class LocalDateHolder(value: LocalDate)
@Holder
case class LocalTimeHolder(value: LocalTime)
@Holder
case class LocalDateTimeHolder(value: LocalDateTime)
@Entity
@Table(name = "java_date")
case class JavaDateHolderEntity(
  id : Int,
  basicDate : LocalDateHolder,
  optionDate : Option[LocalDateHolder],
  basicTime : LocalTimeHolder,
  optionTime : Option[LocalTimeHolder],
  basicDateTime : LocalDateTimeHolder,
  optionDateTime : Option[LocalDateTimeHolder]
)

case class LocalDateVal(value: LocalDate) extends AnyVal
case class LocalTimeVal(value: LocalTime) extends AnyVal
case class LocalDateTimeVal(value: LocalDateTime) extends AnyVal
@Entity
@Table(name = "java_date")
case class JavaDateValEntity(
  id : Int,
  basicDate : LocalDateVal,
  optionDate : Option[LocalDateVal],
  basicTime : LocalTimeVal,
  optionTime : Option[LocalTimeVal],
  basicDateTime : LocalDateTimeVal,
  optionDateTime : Option[LocalDateTimeVal]
)

@Dao(config = TestConfig)
trait JavaDateDao {

  @Script(sql =
    """
create table java_date(
  id int not null identity primary key,
  basic_date date,
  option_date date,
  basic_time time,
  option_time time,
  basic_date_time timestamp,
  option_date_time timestamp
);

insert into java_date (id, basic_date, option_date, basic_time, option_time, basic_date_time, option_date_time) values(0, null, null, null, null, null, null);
    """)
  def create()

  @Script(sql =
    """
drop table java_date;
    """)
  def drop()

  @Select("select * from java_date where id = /* id */0")
  def select(id: Int): JavaDateEntity

  @Insert
  def insert(entity: JavaDateEntity): Result[JavaDateEntity]

  @Select("select * from java_date where id = /* id */0")
  def selectHolder(id: Int): JavaDateHolderEntity

  @Insert
  def insertHolder(entity: JavaDateHolderEntity): Result[JavaDateHolderEntity]

  @Select("select * from java_date where id = /* id */0")
  def selectVal(id: Int): JavaDateValEntity

  @Insert
  def insertVal(entity: JavaDateValEntity): Result[JavaDateValEntity]

}

