package domala.tests.entity

import domala._
import domala.jdbc.Config
import domala.tests.TestConfig
import org.scalatest.{BeforeAndAfter, FunSuite}
import domala.jdbc.Result

class BytesTestSuite  extends FunSuite with BeforeAndAfter {
  implicit val config: Config = TestConfig

  val dao: BytesDao = BytesDao.impl

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

  test("select bytes") {
    Required {
      assert(dao.select(0) === Bytes(0, null, None))
    }
  }

  test("insert bytes") {
    Required {
      val entity = Bytes(1, "abc".map(_.toByte).toArray, Some("def".map(_.toByte).toArray))
      dao.insert(entity)
      val selected = dao.select(1)
      assert(new String(selected.basic) === "abc")
      assert(new String(selected.option.get) === "def")
    }
  }
}

@Entity
case class Bytes(
  id : Int,
  basic : Array[Byte],
  option : Option[Array[Byte]],
)

@Dao(config = TestConfig)
trait BytesDao {

  @Script(sql =
    """
create table bytes(
  id int not null identity primary key,
  basic binary,
  option binary
);

insert into bytes (id, basic, option) values(0, null, null);
    """)
  def create()

  @Script(sql =
    """
drop table bytes;
    """)
  def drop()

  @Select(sql=
    """
select * from bytes where id = /* id */0
    """
  )
  def select(id: Int): Bytes

  @Insert
  def insert(entity: Bytes): Result[Bytes]
}

