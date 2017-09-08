package domala.tests.entity

import domala._
import domala.jdbc.Config
import domala.tests.TestConfig
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.seasar.doma.jdbc.Result

class ValueTypeTestSuite  extends FunSuite with BeforeAndAfter {
  implicit val config: Config = TestConfig

  val dao: ValueTypeTestDao = ValueTypeTestDao

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

  test("select basic value type") {
    Required {
      assert(dao.selectBasic(0) === ValueTypeBasic(0, false, 0: Byte, 0: Short, 0, 0, 0.0f, 0.0))
    }
  }

  test("insert basic value type") {
    Required {
      dao.insertBasic(ValueTypeBasic(1, true, 1: Byte, 1: Short, 1, 1, 1.0f, 1.0))
      assert(dao.selectBasic(1) === ValueTypeBasic(1, true, 1: Byte, 1: Short, 1, 1, 1.0f, 1.0))
    }
  }

  test("select basic value type option") {
    Required {
      assert(dao.selectOption(0) === ValueTypeOption(0, Some(false), Some(0: Byte), Some(0: Short), Some(0), Some(0), Some(0.0f), Some(0.0)))
    }
  }

  test("insert basic value type option") {
    Required {
      val entity = ValueTypeOption(1, Some(true), Some(1: Byte), Some(1: Short), Some(1), Some(1), Some(1.0f), Some(1.0))
      dao.insertOption(entity)
      assert(dao.selectOption(1) === entity)
    }
  }

  test("insert basic value type none") {
    Required {
      val entity = ValueTypeOption(1, None, None, None, None, None, None, None)
      dao.insertOption(entity)
      assert(dao.selectOption(1) === entity)
    }
  }
}

@Entity(name = "value_types")
case class ValueTypeBasic(
  id : Int,
  boolean : Boolean,
  byte : Byte,
  short : Short,
  int : Int,
  long : Long,
  float : Float,
  double : Double
)

@Entity(name = "value_types")
case class ValueTypeOption(
 id : Int,
 boolean : Option[Boolean],
 byte : Option[Byte],
 short : Option[Short],
 int : Option[Int],
 long : Option[Long],
 float : Option[Float],
 double : Option[Double]
)


@Dao(config = TestConfig)
trait ValueTypeTestDao {

  @Script(sql =
    """
create table value_types(
  id int not null identity primary key,
  boolean boolean,
  byte tinyint,
  short smallint,
  int int,
  long bigint,
  float real,
  double double
);

insert into value_types (id, boolean, byte, short, int, long, float, double) values(0, false, 0, 0, 0, 0, 0, 0);
    """)
  def create()

  @Script(sql =
    """
drop table value_types;
    """)
  def drop()

  @Select(sql=
"""
select * from value_types where id = /* id */0
"""
  )
  def selectBasic(id: Int): ValueTypeBasic

  @Insert
  def insertBasic(entity: ValueTypeBasic): Result[ValueTypeBasic]

  @Select(sql=
"""
select * from value_types where id = /* id */0
"""
  )
  def selectOption(id: Int): ValueTypeOption

  @Insert
  def insertOption(entity: ValueTypeOption): Result[ValueTypeOption]
}

