package domala.Integration.select

import java.time.{LocalDate, LocalDateTime, LocalTime}

import domala._
import domala.Integration.TestConfig
import org.scalactic.TolerantNumerics
import org.scalatest.{BeforeAndAfter, FunSuite}

class SingleResultTestSuite  extends FunSuite with BeforeAndAfter {
  implicit val config: Config = TestConfig

  val dao: SingleResultTestDao = SingleResultTestDao

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

  test("select Boolean") {
    Required {
      assert(dao.selectBoolean(0) === false)
      assert(dao.selectBoolean(1) === true)
      assert(dao.selectBooleanOption(0) === None)
      assert(dao.selectBooleanOption(1) === Some(true))
      assert(dao.selectBooleanOption(5) === None)
      assert(dao.selectBooleanSeq(0) === Seq(false))
      assert(dao.selectBooleanSeq(1) === Seq(true))
      assert(dao.selectBooleanSeq(5) === Nil)
    }
  }

  test("select Byte") {
    Required {
      assert(dao.selectByte(0) === 0)
      assert(dao.selectByte(1) === 1)
      assert(dao.selectByteOption(0) === None)
      assert(dao.selectByteOption(1) === Some(1))
      assert(dao.selectByteOption(5) === None)
      assert(dao.selectByteSeq(0) === Seq(0))
      assert(dao.selectByteSeq(1) === Seq(1))
      assert(dao.selectByteSeq(5) === Nil)
    }
  }

  test("select Short") {
    Required {
      assert(dao.selectShort(0) === 0)
      assert(dao.selectShort(1) === 1)
      assert(dao.selectShortOption(0) === None)
      assert(dao.selectShortOption(1) === Some(1))
    }
  }

  test("select Int") {
    Required {
      assert(dao.selectInt(0) === 0)
      assert(dao.selectInt(1) === 1)
      assert(dao.selectIntOption(0) === None)
      assert(dao.selectIntOption(1) === Some(1))
    }
  }

  test("select Long") {
    Required {
      assert(dao.selectLong(0) === 0)
      assert(dao.selectLong(1) === 1)
      assert(dao.selectLongOption(0) === None)
      assert(dao.selectLongOption(1) === Some(1))
    }
  }

  test("select Float") {
    val epsilon = 1e-4f
    implicit val doubleEq = TolerantNumerics.tolerantDoubleEquality(epsilon)
    Required {
      assert(dao.selectFloat(0) === 0f)
      assert(dao.selectFloat(1) === 1.1f)
      assert(dao.selectFloatOption(0) === None)
      assert(dao.selectFloatOption(1) === Some(1.1f))
    }
  }

  test("select Double") {
    val epsilon = 1e-4f
    implicit val doubleEq = TolerantNumerics.tolerantDoubleEquality(epsilon)
    Required {
      assert(dao.selectDouble(0) === 0d)
      assert(dao.selectDouble(1) === 1.1)
      assert(dao.selectDoubleOption(0) === None)
      assert(dao.selectDoubleOption(1) === Some(1.1))
      assert(dao.selectDoubleOption(5) === None)
      assert(dao.selectDoubleSeq(0) === Seq(0d))
      assert(dao.selectDoubleSeq(1) === Seq(1.1d))
      assert(dao.selectDoubleSeq(5) === Nil)
    }
  }

  test("select String") {
    Required {
      assert(dao.selectString(0) === null)
      assert(dao.selectString(1) === "abc")
      assert(dao.selectStringOption(0) === None)
      assert(dao.selectStringOption(1) === Some("abc"))
    }
  }

  test("select BigDecimal") {
    Required {
      assert(dao.selectBigDecimal(0) === null)
      assert(dao.selectBigDecimal(1) === BigDecimal("1234567890.123456789"))
      assert(dao.selectBigDecimalOption(0) === None)
      assert(dao.selectBigDecimalOption(1) === Some(BigDecimal("1234567890.123456789")))
    }
  }

  test("select BigInt") {
    Required {
      assert(dao.selectBigInt(0) === null)
      assert(dao.selectBigInt(1) === BigInt("12345678901234567890"))
      assert(dao.selectBigIntOption(0) === None)
      assert(dao.selectBigIntOption(1) === Some(BigInt("12345678901234567890")))
    }
  }

  test("select LocalDate") {
    Required {
      assert(dao.selectLocalDate(0) === null)
      assert(dao.selectLocalDate(1) === LocalDate.of(2017, 12, 31))
      assert(dao.selectLocalDateOption(0) === None)
      assert(dao.selectLocalDateOption(1) === Some(LocalDate.of(2017, 12, 31)))
    }
  }

  test("select LocalTime") {
    Required {
      assert(dao.selectLocalTime(0) === null)
      assert(dao.selectLocalTime(1) === LocalTime.of(11, 59, 59))
      assert(dao.selectLocalTimeOption(0) === None)
      assert(dao.selectLocalTimeOption(1) === Some(LocalTime.of(11, 59, 59)))
    }
  }

  test("select LocalDateTime") {
    Required {
      assert(dao.selectLocalDateTime(0) === null)
      assert(dao.selectLocalDateTime(1) === LocalDateTime.of(2017, 12, 31, 11, 59, 59, 999999999))
      assert(dao.selectLocalDateTimeOption(0) === None)
      assert(dao.selectLocalDateTimeOption(1) === Some(LocalDateTime.of(2017, 12, 31, 11, 59, 59, 999999999)))
    }
  }
}

@Dao(config = TestConfig)
trait SingleResultTestDao {

  @Script(sql =
    """
create table single_result(
  id int not null identity primary key,
  boolean boolean,
  byte tinyint,
  short smallint,
  int int,
  long bigint,
  float real,
  double double,
  string varchar(10),
  bigdecimal decimal,
  bigint decimal,
  localdate date,
  localtime time,
  localdatetime timestamp
);

insert into single_result (id, boolean, byte, short, int, long, float, double,
string, bigdecimal, bigint, localdate, localtime, localdatetime)
values(0, null, null, null, null, null, null, null,
  null, null, null, null, null, null);
insert into single_result (id, boolean, byte, short, int, long, float, double,
  string, bigdecimal, bigint, localdate, localtime, localdatetime)
values(1, true, 1, 1, 1, 1, 1.1, 1.1,
  'abc', 1234567890.123456789, 12345678901234567890, '2017-12-31', '11:59:59', '2017-12-31 11:59:59.999999999');
    """)
  def create()

  @Script(sql ="drop table single_result")
  def drop()

  @Select(sql ="select boolean from single_result where id = /* id */0")
  def selectBoolean(id: Int): Boolean

  @Select(sql ="select byte from single_result where id = /* id */0")
  def selectByte(id: Int): Byte

  @Select(sql ="select short from single_result where id = /* id */0")
  def selectShort(id: Int): Short

  @Select(sql ="select int from single_result where id = /* id */0")
  def selectInt(id: Int): Int

  @Select(sql ="select long from single_result where id = /* id */0")
  def selectLong(id: Int): Long

  @Select(sql ="select float from single_result where id = /* id */0")
  def selectFloat(id: Int): Float

  @Select(sql ="select double from single_result where id = /* id */0")
  def selectDouble(id: Int): Double

  @Select(sql ="select string from single_result where id = /* id */0")
  def selectString(id: Int): String

  @Select(sql ="select bigdecimal from single_result where id = /* id */0")
  def selectBigDecimal(id: Int): BigDecimal

  @Select(sql ="select bigint from single_result where id = /* id */0")
  def selectBigInt(id: Int): BigInt

  @Select(sql ="select localdate from single_result where id = /* id */0")
  def selectLocalDate(id: Int): LocalDate

  @Select(sql ="select localtime from single_result where id = /* id */0")
  def selectLocalTime(id: Int): LocalTime

  @Select(sql ="select localdatetime from single_result where id = /* id */0")
  def selectLocalDateTime(id: Int): LocalDateTime

  @Select(sql ="select boolean from single_result where id = /* id */0")
  def selectBooleanOption(id: Int): Option[Boolean]

  @Select(sql ="select byte from single_result where id = /* id */0")
  def selectByteOption(id: Int): Option[Byte]

  @Select(sql ="select short from single_result where id = /* id */0")
  def selectShortOption(id: Int): Option[Short]

  @Select(sql ="select int from single_result where id = /* id */0")
  def selectIntOption(id: Int): Option[Int]

  @Select(sql ="select long from single_result where id = /* id */0")
  def selectLongOption(id: Int): Option[Long]

  @Select(sql ="select float from single_result where id = /* id */0")
  def selectFloatOption(id: Int): Option[Float]

  @Select(sql ="select double from single_result where id = /* id */0")
  def selectDoubleOption(id: Int): Option[Double]

  @Select(sql ="select string from single_result where id = /* id */0")
  def selectStringOption(id: Int): Option[String]

  @Select(sql ="select bigdecimal from single_result where id = /* id */0")
  def selectBigDecimalOption(id: Int): Option[BigDecimal]

  @Select(sql ="select bigint from single_result where id = /* id */0")
  def selectBigIntOption(id: Int): Option[BigInt]

  @Select(sql ="select localdate from single_result where id = /* id */0")
  def selectLocalDateOption(id: Int): Option[LocalDate]

  @Select(sql ="select localtime from single_result where id = /* id */0")
  def selectLocalTimeOption(id: Int): Option[LocalTime]

  @Select(sql ="select localdatetime from single_result where id = /* id */0")
  def selectLocalDateTimeOption(id: Int): Option[LocalDateTime]

  @Select(sql ="select boolean from single_result where id = /* id */0")
  def selectBooleanSeq(id: Int): Seq[Boolean]

  @Select(sql ="select byte from single_result where id = /* id */0")
  def selectByteSeq(id: Int): Seq[Byte]

  @Select(sql ="select short from single_result where id = /* id */0")
  def selectShortSeq(id: Int): Seq[Short]

  @Select(sql ="select int from single_result where id = /* id */0")
  def selectIntSeq(id: Int): Seq[Int]

  @Select(sql ="select long from single_result where id = /* id */0")
  def selectLongSeq(id: Int): Seq[Long]

  @Select(sql ="select float from single_result where id = /* id */0")
  def selectFloatSeq(id: Int): Seq[Float]

  @Select(sql ="select double from single_result where id = /* id */0")
  def selectDoubleSeq(id: Int): Seq[Double]

  @Select(sql ="select string from single_result where id = /* id */0")
  def selectStringSeq(id: Int): Seq[String]

  @Select(sql ="select bigdecimal from single_result where id = /* id */0")
  def selectBigDecimalSeq(id: Int): Seq[BigDecimal]

  @Select(sql ="select bigint from single_result where id = /* id */0")
  def selectBigIntSeq(id: Int): Seq[BigInt]

  @Select(sql ="select localdate from single_result where id = /* id */0")
  def selectLocalDateSeq(id: Int): Seq[LocalDate]

  @Select(sql ="select localtime from single_result where id = /* id */0")
  def selectLocalTimeSeq(id: Int): Seq[LocalTime]

  @Select(sql ="select localdatetime from single_result where id = /* id */0")
  def selectLocalDateTimeSeq(id: Int): Seq[LocalDateTime]
}
