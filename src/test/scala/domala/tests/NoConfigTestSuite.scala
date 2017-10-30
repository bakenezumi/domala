package domala.tests

import domala._
import org.scalatest.{BeforeAndAfter, FunSuite}

class NoConfigTestSuite extends FunSuite with BeforeAndAfter {

  test("implicit Config") {
    implicit val config: jdbc.Config = TestConfig
    val dao: NoConfigDao = NoConfigDao.impl
    Required {
      dao.create()
    }
  }
}
@Dao
trait NoConfigDao {
  @Script(sql = """
create table no_config(
    id int not null identity primary key,
    value varchar(20)
);
  """)
  def create(): Unit

  @Script(sql = """
drop table no_config;
  """)
  def drop(): Unit

  @Select(sql = """
select *
from person
where id = /*id*/0
  """)
  def selectById(id: Int): Option[Person]
}


