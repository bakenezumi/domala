package domala.tests.entity

import domala._
import domala.jdbc.Config
import domala.tests.TestConfig
import org.scalatest.{BeforeAndAfter, FunSuite}
import domala.jdbc.Result

class ValueTypeTestSuite  extends FunSuite with BeforeAndAfter {
  implicit val config: Config = TestConfig

  val dao: ValueTypeTestDao = ValueTypeTestDao.impl

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
      assert(dao.selectBasic(0) === ValueTypeBasic(0, boolean = false, 0: Byte, 0: Short, 0, 0, 0.0f, 0.0))
    }
  }

  test("insert basic value type") {
    Required {
      dao.insertBasic(ValueTypeBasic(1, boolean = true, 1: Byte, 1: Short, 1, 1, 1.0f, 1.0))
      assert(dao.selectBasic(1) === ValueTypeBasic(1, boolean = true, 1: Byte, 1: Short, 1, 1, 1.0f, 1.0))
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

  test("select basic value holder") {
    Required {
      assert(dao.selectHolder(0) === ValueTypeHolder(0, BooleanHolder(false), ByteHolder(0: Byte), ShortHolder(0: Short), IntHolder(0), LongHolder(0), FloatHolder(0.0f), DoubleHolder(0.0)))
    }
  }

  test("insert basic value holder") {
    Required {
      val entity = ValueTypeHolder(1, BooleanHolder(true), ByteHolder(1: Byte), ShortHolder(1: Short), IntHolder(1), LongHolder(1), FloatHolder(1.0f), DoubleHolder(1.0))
      dao.insertHolder(entity)
      assert(dao.selectHolder(1) === entity)
    }
  }

  test("select basic value holder option") {
    Required {
      assert(dao.selectHolderOption(0) === ValueTypeHolderOption(0, Some(BooleanHolder(false)), Some(ByteHolder(0: Byte)), Some(ShortHolder(0: Short)), Some(IntHolder(0)), Some(LongHolder(0)), Some(FloatHolder(0.0f)), Some(DoubleHolder(0.0))))
    }
  }

  test("insert basic value holder option") {
    Required {
      val entity = ValueTypeHolderOption(1, Some(BooleanHolder(true)), Some(ByteHolder(1: Byte)), Some(ShortHolder(1: Short)), Some(IntHolder(1)), Some(LongHolder(1)), Some(FloatHolder(1.0f)), Some(DoubleHolder(1.0)))
      dao.insertHolderOption(entity)
      assert(dao.selectHolderOption(1) === entity)
    }
  }

  test("insert basic value holder none") {
    Required {
      val entity = ValueTypeHolderOption(1, None, None, None, None, None, None, None)
      dao.insertHolderOption(entity)
      assert(dao.selectHolderOption(1) === entity)
    }
  }}

@Entity
@Table(name = "value_types")
case class ValueTypeBasic(
  id: Int,
  boolean: Boolean,
  byte: Byte,
  short: Short,
  int: Int,
  long: Long,
  float: Float,
  double: Double
)

@Entity
@Table(name = "value_types")
case class ValueTypeOption(
 id: Int,
 boolean: Option[Boolean],
 byte: Option[Byte],
 short: Option[Short],
 int: Option[Int],
 long: Option[Long],
 float: Option[Float],
 double: Option[Double]
)

@Holder
case class BooleanHolder(value: Boolean)
@Holder
case class ByteHolder(value: Byte)
@Holder
case class ShortHolder(value: Short)
@Holder
case class IntHolder(value: Int)
@Holder
case class LongHolder(value: Long)
@Holder
case class FloatHolder(value: Float)
@Holder
case class DoubleHolder(value: Double)

@Entity
@Table(name = "value_types")
case class ValueTypeHolder(
  id: Int,
  boolean: BooleanHolder,
  byte: ByteHolder,
  short: ShortHolder,
  int: IntHolder,
  long: LongHolder,
  float: FloatHolder,
  double: DoubleHolder
)

@Entity
@Table(name = "value_types")
case class ValueTypeHolderOption(
  id: Int,
  boolean: Option[BooleanHolder],
  byte: Option[ByteHolder],
  short: Option[ShortHolder],
  int: Option[IntHolder],
  long: Option[LongHolder],
  float: Option[FloatHolder],
  double: Option[DoubleHolder]
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

  @Select(sql=
    """
select * from value_types where id = /* id */0
"""
  )
  def selectHolder(id: Int): ValueTypeHolder

  @Insert
  def insertHolder(entity: ValueTypeHolder): Result[ValueTypeHolder]

  @Select(sql=
    """
select * from value_types where id = /* id */0
"""
  )
  def selectHolderOption(id: Int): ValueTypeHolderOption

  @Insert
  def insertHolderOption(entity: ValueTypeHolderOption): Result[ValueTypeHolderOption]
}

