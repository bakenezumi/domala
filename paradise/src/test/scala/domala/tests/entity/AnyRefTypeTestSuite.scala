package domala.tests.entity

import domala._
import domala.jdbc.Config
import domala.tests.TestConfig
import org.scalatest.{BeforeAndAfter, FunSuite}
import domala.jdbc.Result

class AnyRefTypeTestSuite  extends FunSuite with BeforeAndAfter {
  implicit val config: Config = TestConfig

  val dao: AnyRefTypeDao =AnyRefTypeDao.impl

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

  test("select AnyRefType") {
    Required {
      assert(dao.select(0) === AnyRefType(0, null, None, null, None))
    }
  }

  test("insert AnyRefType") {
    Required {
      val entity = AnyRefType(1, "abc".map(_.toByte).toArray, Some("def".map(_.toByte).toArray), "ghi", Some(new java.math.BigDecimal(123)))
      dao.insert(entity)
      val selected = dao.select(1)
      assert(selected.any === "abc".getBytes())
      assert(selected.anyOption.get === "def".getBytes())
      assert(selected.obj === "ghi")
      assert(selected.objOption.get === new java.math.BigDecimal(123))
    }
  }
}

@Entity
case class AnyRefType(
  id : Int,
  any : AnyRef,
  anyOption : Option[AnyRef],
  obj : Object,
  objOption : Option[Object])

@Dao(config = TestConfig)
trait AnyRefTypeDao {

  @Script(sql =
    """
create table any_ref_type(
  id int not null identity primary key,
  any binary,
  any_option binary,
  obj varchar(20),
  obj_option decimal
);

insert into any_ref_type (id, any, any_option, obj, obj_option) values(0, null, null, null, null);
    """)
  def create()

  @Script(sql =
    """
drop table any_ref_type;
    """)
  def drop()

  @Select(sql=
    """
select * from any_ref_type where id = /* id */0
    """
  )
  def select(id: Int): AnyRefType

  @Insert
  def insert(entity: AnyRefType): Result[AnyRefType]
}

