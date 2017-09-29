package domala.tests.select

import java.time.{LocalDate, LocalDateTime, LocalTime}

import domala._
import domala.jdbc.Config
import domala.tests.TestConfig
import org.scalactic.{Equality, TolerantNumerics}
import org.scalatest.{BeforeAndAfter, FunSuite}

class SingleResultTestSuite  extends FunSuite with BeforeAndAfter {
  implicit val config: Config = TestConfig

  val dao: SingleResultTestDao = SingleResultTestDao.impl

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
      assert(dao.selectBoolean(5) === false)
      assert(dao.selectBooleanOption(0) === None)
      assert(dao.selectBooleanOption(1) === Some(true))
      assert(dao.selectBooleanOption(5) === None)
      assert(dao.selectBooleanSeq(0) === Seq(false))
      assert(dao.selectBooleanSeq(1) === Seq(true))
      assert(dao.selectBooleanSeq(5) === Nil)
      assert(dao.selectBooleanStream(0)(_.toList) === Seq(false))
      assert(dao.selectBooleanStream(1)(_.toList) === Seq(true))
      assert(dao.selectBooleanStream(5)(_.toList) === Nil)
    }
  }

  test("select Byte") {
    Required {
      assert(dao.selectByte(0) === 0)
      assert(dao.selectByte(1) === 1)
      assert(dao.selectByte(5) === 0)
      assert(dao.selectByteOption(0) === None)
      assert(dao.selectByteOption(1) === Some(1))
      assert(dao.selectByteOption(5) === None)
      assert(dao.selectByteSeq(0) === Seq(0))
      assert(dao.selectByteSeq(1) === Seq(1))
      assert(dao.selectByteSeq(5) === Nil)
      assert(dao.selectByteStream(0)(_.toList) === Seq(0))
      assert(dao.selectByteStream(1)(_.toList) === Seq(1))
      assert(dao.selectByteStream(5)(_.toList) === Nil)
    }
  }

  test("select Short") {
    Required {
      assert(dao.selectShort(0) === 0)
      assert(dao.selectShort(1) === 1)
      assert(dao.selectShort(5) === 0)
      assert(dao.selectShortOption(0) === None)
      assert(dao.selectShortOption(1) === Some(1))
      assert(dao.selectShortOption(5) === None)
      assert(dao.selectShortSeq(0) === Seq(0))
      assert(dao.selectShortSeq(1) === Seq(1))
      assert(dao.selectShortSeq(5) === Nil)
      assert(dao.selectShortStream(0)(_.toList) === Seq(0))
      assert(dao.selectShortStream(1)(_.toList) === Seq(1))
      assert(dao.selectShortStream(5)(_.toList) === Nil)
    }
  }

  test("select Int") {
    Required {
      assert(dao.selectInt(0) === 0)
      assert(dao.selectInt(1) === 1)
      assert(dao.selectInt(5) === 0)
      assert(dao.selectIntOption(0) === None)
      assert(dao.selectIntOption(1) === Some(1))
      assert(dao.selectIntOption(5) === None)
      assert(dao.selectIntSeq(0) === Seq(0))
      assert(dao.selectIntSeq(1) === Seq(1))
      assert(dao.selectIntSeq(5) === Nil)
      assert(dao.selectIntStream(0)(_.toList) === Seq(0))
      assert(dao.selectIntStream(1)(_.toList) === Seq(1))
      assert(dao.selectIntStream(5)(_.toList) === Nil)
    }
  }

  test("select Long") {
    Required {
      assert(dao.selectLong(0) === 0)
      assert(dao.selectLong(1) === 1)
      assert(dao.selectLong(5) === 0)
      assert(dao.selectLongOption(0) === None)
      assert(dao.selectLongOption(1) === Some(1))
      assert(dao.selectLongOption(5) === None)
      assert(dao.selectLongSeq(0) === Seq(0))
      assert(dao.selectLongSeq(1) === Seq(1))
      assert(dao.selectLongSeq(5) === Nil)
      assert(dao.selectLongStream(0)(_.toList) === Seq(0))
      assert(dao.selectLongStream(1)(_.toList) === Seq(1))
      assert(dao.selectLongStream(5)(_.toList) === Nil)
    }
  }

  test("select Float") {
    implicit val doubleEq: Equality[Double] = TolerantNumerics.tolerantDoubleEquality(1e-4f)
    Required {
      assert(dao.selectFloat(0) === 0f)
      assert(dao.selectFloat(1) === 1.1f)
      assert(dao.selectFloat(5) === 0f)
      assert(dao.selectFloatOption(0) === None)
      assert(dao.selectFloatOption(1) === Some(1.1f))
      assert(dao.selectFloatOption(5) === None)
      assert(dao.selectFloatSeq(0) === Seq(0))
      assert(dao.selectFloatSeq(1) === Seq(1.1f))
      assert(dao.selectFloatSeq(5) === Nil)
      assert(dao.selectFloatStream(0)(_.toList) === Seq(0))
      assert(dao.selectFloatStream(1)(_.toList) === Seq(1.1f))
      assert(dao.selectFloatStream(5)(_.toList) === Nil)
    }
  }

  test("select Double") {
    implicit val doubleEq: Equality[Double] = TolerantNumerics.tolerantDoubleEquality(1e-4f)
    Required {
      assert(dao.selectDouble(0) === 0d)
      assert(dao.selectDouble(1) === 1.1)
      assert(dao.selectDouble(5) === 0d)
      assert(dao.selectDoubleOption(0) === None)
      assert(dao.selectDoubleOption(1) === Some(1.1))
      assert(dao.selectDoubleOption(5) === None)
      assert(dao.selectDoubleSeq(0) === Seq(0d))
      assert(dao.selectDoubleSeq(1) === Seq(1.1d))
      assert(dao.selectDoubleSeq(5) === Nil)
      assert(dao.selectDoubleStream(0)(_.toList) === Seq(0d))
      assert(dao.selectDoubleStream(1)(_.toList) === Seq(1.1d))
      assert(dao.selectDoubleStream(5)(_.toList) === Nil)
    }
  }

  test("select String") {
    Required {
      assert(dao.selectString(0) === null)
      assert(dao.selectString(1) === "abc")
      assert(dao.selectString(5) === null)
      assert(dao.selectStringOption(0) === None)
      assert(dao.selectStringOption(1) === Some("abc"))
      assert(dao.selectStringOption(5) === None)
      assert(dao.selectStringSeq(0) === Seq(null))
      assert(dao.selectStringSeq(1) === Seq("abc"))
      assert(dao.selectStringSeq(5) === Nil)
      assert(dao.selectStringStream(0)(_.toList) === Seq(null))
      assert(dao.selectStringStream(1)(_.toList) === Seq("abc"))
      assert(dao.selectStringStream(5)(_.toList) === Nil)
    }
  }

  test("select BigDecimal") {
    Required {
      assert(dao.selectBigDecimal(0) === null)
      assert(dao.selectBigDecimal(1) === BigDecimal("1234567890.123456789"))
      assert(dao.selectBigDecimal(5) === null)
      assert(dao.selectBigDecimalOption(0) === None)
      assert(dao.selectBigDecimalOption(1) === Some(BigDecimal("1234567890.123456789")))
      assert(dao.selectBigDecimalOption(5) === None)
      assert(dao.selectBigDecimalSeq(0) === Seq(null))
      assert(dao.selectBigDecimalSeq(1) === Seq(BigDecimal("1234567890.123456789")))
      assert(dao.selectBigDecimalSeq(5) === Nil)
      assert(dao.selectBigDecimalStream(0)(_.toList) === Seq(null))
      assert(dao.selectBigDecimalStream(1)(_.toList) === Seq(BigDecimal("1234567890.123456789")))
      assert(dao.selectBigDecimalStream(5)(_.toList) === Nil)
    }
  }

  test("select BigInt") {
    Required {
      assert(dao.selectBigInt(0) === null)
      assert(dao.selectBigInt(1) === BigInt("12345678901234567890"))
      assert(dao.selectBigInt(5) === null)
      assert(dao.selectBigIntOption(0) === None)
      assert(dao.selectBigIntOption(1) === Some(BigInt("12345678901234567890")))
      assert(dao.selectBigIntOption(5) === None)
      assert(dao.selectBigIntSeq(0) === Seq(null))
      assert(dao.selectBigIntSeq(1) === Seq(BigInt("12345678901234567890")))
      assert(dao.selectBigIntSeq(5) === Nil)
      assert(dao.selectBigIntStream(0)(_.toList) === Seq(null))
      assert(dao.selectBigIntStream(1)(_.toList) === Seq(BigInt("12345678901234567890")))
      assert(dao.selectBigIntStream(5)(_.toList) === Nil)
    }
  }

  test("select LocalDate") {
    Required {
      assert(dao.selectLocalDate(0) === null)
      assert(dao.selectLocalDate(1) === LocalDate.of(2017, 12, 31))
      assert(dao.selectLocalDate(5) === null)
      assert(dao.selectLocalDateOption(0) === None)
      assert(dao.selectLocalDateOption(1) === Some(LocalDate.of(2017, 12, 31)))
      assert(dao.selectLocalDateOption(5) === None)
      assert(dao.selectLocalDateSeq(0) === Seq(null))
      assert(dao.selectLocalDateSeq(1) === Seq(LocalDate.of(2017, 12, 31)))
      assert(dao.selectLocalDateSeq(5) === Nil)
      assert(dao.selectLocalDateStream(0)(_.toList) === Seq(null))
      assert(dao.selectLocalDateStream(1)(_.toList) === Seq(LocalDate.of(2017, 12, 31)))
      assert(dao.selectLocalDateStream(5)(_.toList) === Nil)
    }
  }

  test("select LocalTime") {
    Required {
      assert(dao.selectLocalTime(0) === null)
      assert(dao.selectLocalTime(1) === LocalTime.of(11, 59, 59))
      assert(dao.selectLocalTime(5) === null)
      assert(dao.selectLocalTimeOption(0) === None)
      assert(dao.selectLocalTimeOption(1) === Some(LocalTime.of(11, 59, 59)))
      assert(dao.selectLocalTimeOption(5) === None)
      assert(dao.selectLocalTimeSeq(0) === Seq(null))
      assert(dao.selectLocalTimeSeq(1) === Seq(LocalTime.of(11, 59, 59)))
      assert(dao.selectLocalTimeSeq(5) === Nil)
      assert(dao.selectLocalTimeStream(0)(_.toList) === Seq(null))
      assert(dao.selectLocalTimeStream(1)(_.toList) === Seq(LocalTime.of(11, 59, 59)))
      assert(dao.selectLocalTimeStream(5)(_.toList) === Nil)
    }
  }

  test("select LocalDateTime") {
    Required {
      assert(dao.selectLocalDateTime(0) === null)
      assert(dao.selectLocalDateTime(1) === LocalDateTime.of(2017, 12, 31, 11, 59, 59, 999999999))
      assert(dao.selectLocalDateTime(5) === null)
      assert(dao.selectLocalDateTimeOption(0) === None)
      assert(dao.selectLocalDateTimeOption(1) === Some(LocalDateTime.of(2017, 12, 31, 11, 59, 59, 999999999)))
      assert(dao.selectLocalDateTimeOption(5) === None)
      assert(dao.selectLocalDateTimeSeq(0) === Seq(null))
      assert(dao.selectLocalDateTimeSeq(1) === Seq(LocalDateTime.of(2017, 12, 31, 11, 59, 59, 999999999)))
      assert(dao.selectLocalDateTimeSeq(5) === Nil)
      assert(dao.selectLocalDateTimeStream(0)(_.toList) === Seq(null))
      assert(dao.selectLocalDateTimeStream(1)(_.toList) === Seq(LocalDateTime.of(2017, 12, 31, 11, 59, 59, 999999999)))
      assert(dao.selectLocalDateTimeStream(5)(_.toList) === Nil)
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

  @Select(sql ="select boolean from single_result where id = /* id */0", strategy = SelectType.STREAM)
  def selectBooleanStream(id: Int)(f: Stream[Boolean] => Seq[Boolean]): Seq[Boolean]

  @Select(sql ="select byte from single_result where id = /* id */0", strategy = SelectType.STREAM)
  def selectByteStream(id: Int)(f: Stream[Byte] => Seq[Byte]): Seq[Byte]

  @Select(sql ="select short from single_result where id = /* id */0", strategy = SelectType.STREAM)
  def selectShortStream(id: Int)(f: Stream[Short] => Seq[Short]): Seq[Short]

  @Select(sql ="select int from single_result where id = /* id */0", strategy = SelectType.STREAM)
  def selectIntStream(id: Int)(f: Stream[Int] => Seq[Int]): Seq[Int]

  @Select(sql ="select long from single_result where id = /* id */0", strategy = SelectType.STREAM)
  def selectLongStream(id: Int)(f: Stream[Long] => Seq[Long]): Seq[Long]

  @Select(sql ="select float from single_result where id = /* id */0", strategy = SelectType.STREAM)
  def selectFloatStream(id: Int)(f: Stream[Float] => Seq[Float]): Seq[Float]

  @Select(sql ="select double from single_result where id = /* id */0", strategy = SelectType.STREAM)
  def selectDoubleStream(id: Int)(f: Stream[Double] => Seq[Double]): Seq[Double]

  @Select(sql ="select string from single_result where id = /* id */0", strategy = SelectType.STREAM)
  def selectStringStream(id: Int)(f: Stream[String] => Seq[String]): Seq[String]

  @Select(sql ="select bigdecimal from single_result where id = /* id */0", strategy = SelectType.STREAM)
  def selectBigDecimalStream(id: Int)(f: Stream[BigDecimal] => Seq[BigDecimal]): Seq[BigDecimal]

  @Select(sql ="select bigint from single_result where id = /* id */0", strategy = SelectType.STREAM)
  def selectBigIntStream(id: Int)(f: Stream[BigInt] => Seq[BigInt]): Seq[BigInt]

  @Select(sql ="select localdate from single_result where id = /* id */0", strategy = SelectType.STREAM)
  def selectLocalDateStream(id: Int)(f: Stream[LocalDate] => Seq[LocalDate]): Seq[LocalDate]

  @Select(sql ="select localtime from single_result where id = /* id */0", strategy = SelectType.STREAM)
  def selectLocalTimeStream(id: Int)(f: Stream[LocalTime] => Seq[LocalTime]): Seq[LocalTime]

  @Select(sql ="select localdatetime from single_result where id = /* id */0", strategy = SelectType.STREAM)
  def selectLocalDateTimeStream(id: Int)(f: Stream[LocalDateTime] => Seq[LocalDateTime]): Seq[LocalDateTime]
}
