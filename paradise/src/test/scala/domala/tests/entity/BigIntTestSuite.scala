package domala.tests.entity

import java.math.BigInteger

import domala._
import domala.jdbc.Config
import domala.tests.TestConfig
import org.scalatest.{BeforeAndAfter, FunSuite}
import domala.jdbc.Result

class BigIntTestSuite  extends FunSuite with BeforeAndAfter {
  implicit val config: Config = TestConfig

  val dao: BigIntDao = BigIntDao.impl

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

  test("select BigInt") {
    Required {
      assert(dao.select(0) === BigIntEntity(0, null, None))
    }
  }

  test("insert BigInt") {
    Required {
      val entity = BigIntEntity(1, BigInt("12345678901234567890123"), Some(BigInt("123456789012345678901234")))
      dao.insert(entity)
      assert(dao.select(1) === entity)
    }
  }


  test("select java.math.BigInteger") {
    Required {
      assert(dao.selectJava(0) === JavaBigIntegerEntity(0, null, None))
    }
  }

  test("insert java.math.BigInteger") {
    Required {
      val entity = JavaBigIntegerEntity(1, new BigInteger("12345678901234567890123"), Some(new BigInteger("123456789012345678901234")))
      dao.insertJava(entity)
      assert(dao.selectJava(1) === entity)
    }
  }
}

@Entity
@Table(name = "bigint")
case class BigIntEntity(
  id : Int,
  basic : BigInt,
  option : Option[BigInt],
)

@Entity
@Table(name = "bigint")
case class JavaBigIntegerEntity(
  id : Int,
  basic : java.math.BigInteger,
  option : Option[java.math.BigInteger],
)

@Dao(config = TestConfig)
trait BigIntDao {

  @Script(sql =
    """
create table bigint(
  id int not null identity primary key,
  basic decimal,
  option decimal
);

insert into bigint (id, basic, option) values(0, null, null);
    """)
  def create()

  @Script(sql =
    """
drop table bigint;
    """)
  def drop()

  @Select(sql=
    """
select * from bigint where id = /* id */0
    """
  )
  def select(id: Int): BigIntEntity

  @Insert
  def insert(entity: BigIntEntity): Result[BigIntEntity]


  @Select(sql=
    """
select * from bigint where id = /* id */0
    """
  )
  def selectJava(id: Int): JavaBigIntegerEntity

  @Insert
  def insertJava(entity: JavaBigIntegerEntity): Result[JavaBigIntegerEntity]
}

