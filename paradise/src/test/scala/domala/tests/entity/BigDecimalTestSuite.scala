package domala.tests.entity

import domala._
import domala.jdbc.Config
import domala.tests.TestConfig
import org.scalatest.{BeforeAndAfter, FunSuite}
import domala.jdbc.Result

class BigDecimalTestSuite  extends FunSuite with BeforeAndAfter {
  implicit val config: Config = TestConfig

  val dao: BigDecimalDao = BigDecimalDao.impl

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

  test("select BigDecimal") {
    Required {
      assert(dao.select(0) == BigDecimalEntity(0, null, None))
    }
  }

  test("insert BigDecimal") {
    Required {
      val entity = BigDecimalEntity(1, BigDecimal("1234567890.1234567890123"), Some(BigDecimal("1234567890.12345678901234")))
      dao.insert(entity)
      assert(dao.select(1) == entity)
    }
  }


  test("select java.math.BigDecimal") {
    Required {
      assert(dao.selectJava(0) == JavaBigDecimalEntity(0, null, None))
    }
  }

  test("insert java.math.BigDecimal") {
    Required {
      val entity = JavaBigDecimalEntity(1, new java.math.BigDecimal("1234567890.1234567890123"), Some(new java.math.BigDecimal("1234567890.12345678901234")))
      dao.insertJava(entity)
      assert(dao.selectJava(1) == entity)
    }
  }

  test("select BigDecimal holder") {
    Required {
      assert(dao.selectHolder(0) == BigDecimalHolderEntity(0, null, None))
    }
  }

  test("insert BigDecimal holder") {
    Required {
      val entity = BigDecimalHolderEntity(1, BigDecimalHolder(BigDecimal("1234567890.1234567890123")),
        Some(BigDecimalHolder(BigDecimal("1234567890.12345678901234"))))
      dao.insertHolder(entity)
      assert(dao.selectHolder(1) === entity)
    }
  }

  test("select BigDecimal AnyVal") {
    Required {
      assert(dao.selectVal(0) == BigDecimalValEntity(0, null.asInstanceOf[BigDecimalVal], None))
    }
  }

  test("insert BigDecimal AnyVal") {
    Required {
      val entity = BigDecimalValEntity(1, BigDecimalVal(BigDecimal("1234567890.1234567890123")),
        Some(BigDecimalVal(BigDecimal("1234567890.12345678901234"))))
      dao.insertVal(entity)
      assert(dao.selectVal(1) == entity)
    }
  }

}

@Entity
@Table(name = "big_decimal")
case class BigDecimalEntity(
  id : Int,
  basic : BigDecimal,
  option : Option[BigDecimal],
)

@Entity
@Table(name = "big_decimal")
case class JavaBigDecimalEntity(
  id : Int,
  basic : java.math.BigDecimal,
  option : Option[java.math.BigDecimal],
)

@Holder
case class BigDecimalHolder(value: BigDecimal)

@Entity
@Table(name = "big_decimal")
case class BigDecimalHolderEntity(
  id : Int,
  basic : BigDecimalHolder,
  option : Option[BigDecimalHolder],
)

case class BigDecimalVal(value: BigDecimal) extends AnyVal

@Entity
@Table(name = "big_decimal")
case class BigDecimalValEntity(
  id : Int,
  basic : BigDecimalVal,
  option : Option[BigDecimalVal],
)

@Dao(config = TestConfig)
trait BigDecimalDao {

  @Script(sql =
    """
create table big_decimal(
  id int not null identity primary key,
  basic decimal,
  option decimal
);

insert into big_decimal (id, basic, option) values(0, null, null);
    """)
  def create()

  @Script(sql =
    """
drop table big_decimal;
    """)
  def drop()

  @Select("select * from big_decimal where id = /* id */0")
  def select(id: Int): BigDecimalEntity

  @Insert
  def insert(entity: BigDecimalEntity): Result[BigDecimalEntity]


  @Select("select * from big_decimal where id = /* id */0")
  def selectJava(id: Int): JavaBigDecimalEntity

  @Insert
  def insertJava(entity: JavaBigDecimalEntity): Result[JavaBigDecimalEntity]


  @Select("select * from big_decimal where id = /* id */0")
  def selectHolder(id: Int): BigDecimalHolderEntity

  @Insert
  def insertHolder(entity: BigDecimalHolderEntity): Result[BigDecimalHolderEntity]


  @Select("select * from big_decimal where id = /* id */0")
  def selectVal(id: Int): BigDecimalValEntity

  @Insert
  def insertVal(entity: BigDecimalValEntity): Result[BigDecimalValEntity]

}

