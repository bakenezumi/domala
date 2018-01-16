package domala.tests.entity

import java.sql.{Date, Time, Timestamp}

import domala._
import domala.jdbc.Config
import domala.tests.TestConfig
import org.scalatest.{BeforeAndAfter, FunSuite}
import domala.jdbc.Result

class SqlDateTestSuite  extends FunSuite with BeforeAndAfter {
  implicit val config: Config = TestConfig

  val dao: SqlDateDao = SqlDateDao.impl

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

  test("select JDBC type") {
    Required {
      assert(dao.select(0) === SqlDateEntity(0, null, None, null, None, null, None))
    }
  }

  test("insert JDBC type") {
    Required {
      val entity = SqlDateEntity(
        1,
        Date.valueOf("2017-9-5"), Some(Date.valueOf("2020-12-31")),
        Time.valueOf("12:59:59"), Some(Time.valueOf("13:0:0")),
        Timestamp.valueOf("2017-9-5 12:59:59.999999999"), Some(Timestamp.valueOf("2020-12-31 13:0:0.000000001"))
      )
      dao.insert(entity)
      assert(dao.select(1) === entity)
    }
  }
}

@Entity
@Table(name = "sql_date")
case class SqlDateEntity(
   id : Int,
   basicDate : Date,
   optionDate : Option[Date],
   basicTime : Time,
   optionTime : Option[Time],
   basicDateTime : Timestamp,
   optionDateTime : Option[Timestamp]
)

@Dao(config = TestConfig)
trait SqlDateDao {

  @Script(sql =
    """
create table sql_date(
  id int not null identity primary key,
  basic_date date,
  option_date date,
  basic_time time,
  option_time time,
  basic_date_time timestamp,
  option_date_time timestamp
);

insert into sql_date (id, basic_date, option_date, basic_time, option_time, basic_date_time, option_date_time) values(0, null, null, null, null, null, null);
    """)
  def create()

  @Script(sql =
    """
drop table sql_date;
    """)
  def drop()

  @Select(sql=
    """
select * from sql_date where id = /* id */0
"""
  )
  def select(id: Int): SqlDateEntity

  @Insert
  def insert(entity: SqlDateEntity): Result[SqlDateEntity]
}
