package domala.tests.entity

import domala._
import domala.jdbc.Config
import domala.tests.TestConfig
import org.scalatest.{BeforeAndAfter, FunSuite}
import domala.jdbc.Result

class BigDecimalTestSuite  extends FunSuite with BeforeAndAfter {
  implicit val config: Config = TestConfig

  val dao: BigDecimalDao = BigDecimalDao

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
      assert(dao.select(0) === BigDecimalEntity(0, null, None))
    }
  }

  test("insert BigDecimal") {
    Required {
      val entity = BigDecimalEntity(1, BigDecimal("1234567890.1234567890123"), Some(BigDecimal("1234567890.12345678901234")))
      dao.insert(entity)
      assert(dao.select(1) === entity)
    }
  }


  test("select java.math.BigDecimal") {
    Required {
      assert(dao.selectJava(0) === JavaBigDecimalEntity(0, null, None))
    }
  }

  test("insert java.math.BigDecimal") {
    Required {
      val entity = JavaBigDecimalEntity(1, new java.math.BigDecimal("1234567890.1234567890123"), Some(new java.math.BigDecimal("1234567890.12345678901234")))
      dao.insertJava(entity)
      assert(dao.selectJava(1) === entity)
    }
  }
}

@Entity(name = "bigdecimal")
case class BigDecimalEntity(
  id : Int,
  basic : BigDecimal,
  option : Option[BigDecimal],
)

@Entity(name = "bigdecimal")
case class JavaBigDecimalEntity(
  id : Int,
  basic : java.math.BigDecimal,
  option : Option[java.math.BigDecimal],
)

@Dao(config = TestConfig)
trait BigDecimalDao {

  @Script(sql =
    """
create table bigdecimal(
  id int not null identity primary key,
  basic decimal,
  option decimal
);

insert into bigdecimal (id, basic, option) values(0, null, null);
    """)
  def create()

  @Script(sql =
    """
drop table bigdecimal;
    """)
  def drop()

  @Select(sql=
    """
select * from bigdecimal where id = /* id */0
"""
  )
  def select(id: Int): BigDecimalEntity

  @Insert
  def insert(entity: BigDecimalEntity): Result[BigDecimalEntity]


  @Select(sql=
"""
select * from bigdecimal where id = /* id */0
"""
  )
  def selectJava(id: Int): JavaBigDecimalEntity

  @Insert
  def insertJava(entity: JavaBigDecimalEntity): Result[JavaBigDecimalEntity]
}

