package domala.tests.entity

import java.sql.{Blob, Clob}
import javax.sql.rowset.serial.{SerialBlob, SerialClob}

import domala._
import domala.jdbc.Config
import domala.tests.TestConfig
import org.scalatest.{BeforeAndAfter, FunSuite}
import domala.jdbc.Result

class LobTestSuite  extends FunSuite with BeforeAndAfter {
  implicit val config: Config = TestConfig

  val dao: LobDao = LobDao.impl

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

  test("select Lob") {
    Required {
      assert(dao.select(0) === Lob(0, null, None, null, None))
    }
  }

  test("insert Lob") {
    Required {
      val entity = Lob(1, new SerialBlob("abc".map(_.toByte).toArray), Some(new SerialBlob("def".map(_.toByte).toArray)),
        new SerialClob("ghi".toArray), Some(new SerialClob("jkl".toArray)))
      dao.insert(entity)
      val selected = dao.select(1)
      assert(new String(selected.basicBlob.getBytes(0, 4)) === "abc")
      assert(new String(selected.optionBlob.get.getBytes(0, 4)) === "def")
      assert(selected.basicClob.getSubString(1, 4) === "ghi")
      assert(selected.optionClob.get.getSubString(1, 4) === "jkl")
    }
  }
}

@Entity
case class Lob(
  id : Int,
  basicBlob : Blob,
  optionBlob : Option[Blob],
  basicClob : Clob,
  optionClob : Option[Clob])

@Dao(config = TestConfig)
trait LobDao {

  @Script(sql =
    """
create table lob(
  id int not null identity primary key,
  basic_blob blob,
  option_blob blob,
  basic_clob clob,
  option_clob clob,
);

insert into lob (id, basic_blob, option_blob, basic_clob, option_clob) values(0, null, null, null, null);
    """)
  def create()

  @Script(sql =
    """
drop table lob;
    """)
  def drop()

  @Select(sql=
    """
select * from lob where id = /* id */0
    """
  )
  def select(id: Int): Lob

  @Insert
  def insert(entity: Lob): Result[Lob]
}

