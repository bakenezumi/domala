package domala.tests.dao

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
      assert(dao.selectBooleanHolder(0) === null)
      assert(dao.selectBooleanHolder(1) === BooleanHolder(true))
      assert(dao.selectBooleanHolder(5) === null)
      assert(dao.selectBooleanVal(0) === BooleanVal(false))
      assert(dao.selectBooleanVal(1) === BooleanVal(true))
      assert(dao.selectBooleanVal(5) === BooleanVal(false))
      assert(dao.selectBooleanSeq(0) === Seq(false))
      assert(dao.selectBooleanSeq(1) === Seq(true))
      assert(dao.selectBooleanSeq(5) === Nil)
      assert(dao.selectBooleanStream(0)(_.toList) === Seq(false))
      assert(dao.selectBooleanStream(1)(_.toList) === Seq(true))
      assert(dao.selectBooleanStream(5)(_.toList) === Nil)
      assert(dao.selectBooleanIterator(0)(_.toList) === Seq(false))
      assert(dao.selectBooleanIterator(1)(_.toList) === Seq(true))
      assert(dao.selectBooleanIterator(5)(_.toList) === Nil)
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
      assert(dao.selectByteHolder(0) === null)
      assert(dao.selectByteHolder(1) === ByteHolder(1))
      assert(dao.selectByteHolder(5) === null)
      assert(dao.selectByteVal(0) === ByteVal(0))
      assert(dao.selectByteVal(1) === ByteVal(1))
      assert(dao.selectByteVal(5) === ByteVal(0))
      assert(dao.selectByteSeq(0) === Seq(0))
      assert(dao.selectByteSeq(1) === Seq(1))
      assert(dao.selectByteSeq(5) === Nil)
      assert(dao.selectByteStream(0)(_.toList) === Seq(0))
      assert(dao.selectByteStream(1)(_.toList) === Seq(1))
      assert(dao.selectByteStream(5)(_.toList) === Nil)
      assert(dao.selectByteIterator(0)(_.toList) === Seq(0))
      assert(dao.selectByteIterator(1)(_.toList) === Seq(1))
      assert(dao.selectByteIterator(5)(_.toList) === Nil)
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
      assert(dao.selectShortHolder(0) === null)
      assert(dao.selectShortHolder(1) === ShortHolder(1))
      assert(dao.selectShortHolder(5) === null)
      assert(dao.selectShortVal(0) === ShortVal(0))
      assert(dao.selectShortVal(1) === ShortVal(1))
      assert(dao.selectShortVal(5) === ShortVal(0))
      assert(dao.selectShortSeq(0) === Seq(0))
      assert(dao.selectShortSeq(1) === Seq(1))
      assert(dao.selectShortSeq(5) === Nil)
      assert(dao.selectShortStream(0)(_.toList) === Seq(0))
      assert(dao.selectShortStream(1)(_.toList) === Seq(1))
      assert(dao.selectShortStream(5)(_.toList) === Nil)
      assert(dao.selectShortIterator(0)(_.toList) === Seq(0))
      assert(dao.selectShortIterator(1)(_.toList) === Seq(1))
      assert(dao.selectShortIterator(5)(_.toList) === Nil)
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
      assert(dao.selectIntHolder(0) === null)
      assert(dao.selectIntHolder(1) === IntHolder(1))
      assert(dao.selectIntHolder(5) === null)
      assert(dao.selectIntVal(0) === IntVal(0))
      assert(dao.selectIntVal(1) === IntVal(1))
      assert(dao.selectIntVal(5) === IntVal(0))
      assert(dao.selectIntSeq(0) === Seq(0))
      assert(dao.selectIntSeq(1) === Seq(1))
      assert(dao.selectIntSeq(5) === Nil)
      assert(dao.selectIntStream(0)(_.toList) === Seq(0))
      assert(dao.selectIntStream(1)(_.toList) === Seq(1))
      assert(dao.selectIntStream(5)(_.toList) === Nil)
      assert(dao.selectIntIterator(0)(_.toList) === Seq(0))
      assert(dao.selectIntIterator(1)(_.toList) === Seq(1))
      assert(dao.selectIntIterator(5)(_.toList) === Nil)
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
      assert(dao.selectLongHolder(0) === null)
      assert(dao.selectLongHolder(1) === LongHolder(1))
      assert(dao.selectLongHolder(5) === null)      
      assert(dao.selectLongVal(0) === LongVal(0))
      assert(dao.selectLongVal(1) === LongVal(1))
      assert(dao.selectLongVal(5) === LongVal(0))      
      assert(dao.selectLongSeq(0) === Seq(0))
      assert(dao.selectLongSeq(1) === Seq(1))
      assert(dao.selectLongSeq(5) === Nil)
      assert(dao.selectLongStream(0)(_.toList) === Seq(0))
      assert(dao.selectLongStream(1)(_.toList) === Seq(1))
      assert(dao.selectLongStream(5)(_.toList) === Nil)
      assert(dao.selectLongIterator(0)(_.toList) === Seq(0))
      assert(dao.selectLongIterator(1)(_.toList) === Seq(1))
      assert(dao.selectLongIterator(5)(_.toList) === Nil)
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
      assert(dao.selectFloatHolder(0) === null)
      assert(dao.selectFloatHolder(1) === FloatHolder(1.1f))
      assert(dao.selectFloatHolder(5) === null)
      assert(dao.selectFloatVal(0) === FloatVal(0f))
      assert(dao.selectFloatVal(1) === FloatVal(1.1f))
      assert(dao.selectFloatVal(5) === FloatVal(0f))
      assert(dao.selectFloatSeq(0) === Seq(0))
      assert(dao.selectFloatSeq(1) === Seq(1.1f))
      assert(dao.selectFloatSeq(5) === Nil)
      assert(dao.selectFloatStream(0)(_.toList) === Seq(0))
      assert(dao.selectFloatStream(1)(_.toList) === Seq(1.1f))
      assert(dao.selectFloatStream(5)(_.toList) === Nil)
      assert(dao.selectFloatIterator(0)(_.toList) === Seq(0))
      assert(dao.selectFloatIterator(1)(_.toList) === Seq(1.1f))
      assert(dao.selectFloatIterator(5)(_.toList) === Nil)
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
      assert(dao.selectDoubleHolder(0) === null)
      assert(dao.selectDoubleHolder(1) === DoubleHolder(1.1))
      assert(dao.selectDoubleHolder(5) === null)
      assert(dao.selectDoubleVal(0) === DoubleVal(0d))
      assert(dao.selectDoubleVal(1) === DoubleVal(1.1))
      assert(dao.selectDoubleVal(5) === DoubleVal(0d))
      assert(dao.selectDoubleSeq(0) === Seq(0d))
      assert(dao.selectDoubleSeq(1) === Seq(1.1d))
      assert(dao.selectDoubleSeq(5) === Nil)
      assert(dao.selectDoubleStream(0)(_.toList) === Seq(0d))
      assert(dao.selectDoubleStream(1)(_.toList) === Seq(1.1d))
      assert(dao.selectDoubleStream(5)(_.toList) === Nil)
      assert(dao.selectDoubleIterator(0)(_.toList) === Seq(0d))
      assert(dao.selectDoubleIterator(1)(_.toList) === Seq(1.1d))
      assert(dao.selectDoubleIterator(5)(_.toList) === Nil)
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
      assert(dao.selectStringHolder(0) === null)
      assert(dao.selectStringHolder(1) === StringHolder("abc"))
      assert(dao.selectStringHolder(5) === null)
      assert(dao.selectStringVal(0) === StringVal(null))
      assert(dao.selectStringVal(1) === StringVal("abc"))
      assert(dao.selectStringVal(5) === StringVal(null))
      assert(dao.selectStringSeq(0) === Seq(null))
      assert(dao.selectStringSeq(1) === Seq("abc"))
      assert(dao.selectStringSeq(5) === Nil)
      assert(dao.selectStringStream(0)(_.toList) === Seq(null))
      assert(dao.selectStringStream(1)(_.toList) === Seq("abc"))
      assert(dao.selectStringStream(5)(_.toList) === Nil)
      assert(dao.selectStringIterator(0)(_.toList) === Seq(null))
      assert(dao.selectStringIterator(1)(_.toList) === Seq("abc"))
      assert(dao.selectStringIterator(5)(_.toList) === Nil)
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
      assert(dao.selectBigDecimalHolder(0) === null)
      assert(dao.selectBigDecimalHolder(1) === BigDecimalHolder(BigDecimal("1234567890.123456789")))
      assert(dao.selectBigDecimalHolder(5) === null)
      assert(dao.selectBigDecimalVal(0) === BigDecimalVal(null))
      assert(dao.selectBigDecimalVal(1) === BigDecimalVal(BigDecimal("1234567890.123456789")))
      assert(dao.selectBigDecimalVal(5) === BigDecimalVal(null))
      assert(dao.selectBigDecimalSeq(0) === Seq(null))
      assert(dao.selectBigDecimalSeq(1) === Seq(BigDecimal("1234567890.123456789")))
      assert(dao.selectBigDecimalSeq(5) === Nil)
      assert(dao.selectBigDecimalStream(0)(_.toList) === Seq(null))
      assert(dao.selectBigDecimalStream(1)(_.toList) === Seq(BigDecimal("1234567890.123456789")))
      assert(dao.selectBigDecimalStream(5)(_.toList) === Nil)
      assert(dao.selectBigDecimalIterator(0)(_.toList) === Seq(null))
      assert(dao.selectBigDecimalIterator(1)(_.toList) === Seq(BigDecimal("1234567890.123456789")))
      assert(dao.selectBigDecimalIterator(5)(_.toList) === Nil)
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
      assert(dao.selectBigIntHolder(0) === null)
      assert(dao.selectBigIntHolder(1) === BigIntHolder(BigInt("12345678901234567890")))
      assert(dao.selectBigIntHolder(5) === null)
      assert(dao.selectBigIntVal(0) === BigIntVal(null))
      assert(dao.selectBigIntVal(1) === BigIntVal(BigInt("12345678901234567890")))
      assert(dao.selectBigIntVal(5) === BigIntVal(null))
      assert(dao.selectBigIntSeq(0) === Seq(null))
      assert(dao.selectBigIntSeq(1) === Seq(BigInt("12345678901234567890")))
      assert(dao.selectBigIntSeq(5) === Nil)
      assert(dao.selectBigIntStream(0)(_.toList) === Seq(null))
      assert(dao.selectBigIntStream(1)(_.toList) === Seq(BigInt("12345678901234567890")))
      assert(dao.selectBigIntStream(5)(_.toList) === Nil)
      assert(dao.selectBigIntIterator(0)(_.toList) === Seq(null))
      assert(dao.selectBigIntIterator(1)(_.toList) === Seq(BigInt("12345678901234567890")))
      assert(dao.selectBigIntIterator(5)(_.toList) === Nil)
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
      assert(dao.selectLocalDateHolder(0) === null)
      assert(dao.selectLocalDateHolder(1) === LocalDateHolder(LocalDate.of(2017, 12, 31)))
      assert(dao.selectLocalDateHolder(5) === null)
      assert(dao.selectLocalDateVal(0) === LocalDateVal(null))
      assert(dao.selectLocalDateVal(1) === LocalDateVal(LocalDate.of(2017, 12, 31)))
      assert(dao.selectLocalDateVal(5) === LocalDateVal(null))
      assert(dao.selectLocalDateSeq(0) === Seq(null))
      assert(dao.selectLocalDateSeq(1) === Seq(LocalDate.of(2017, 12, 31)))
      assert(dao.selectLocalDateSeq(5) === Nil)
      assert(dao.selectLocalDateStream(0)(_.toList) === Seq(null))
      assert(dao.selectLocalDateStream(1)(_.toList) === Seq(LocalDate.of(2017, 12, 31)))
      assert(dao.selectLocalDateStream(5)(_.toList) === Nil)
      assert(dao.selectLocalDateIterator(0)(_.toList) === Seq(null))
      assert(dao.selectLocalDateIterator(1)(_.toList) === Seq(LocalDate.of(2017, 12, 31)))
      assert(dao.selectLocalDateIterator(5)(_.toList) === Nil)
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
      assert(dao.selectLocalTimeHolder(0) === null)
      assert(dao.selectLocalTimeHolder(1) === LocalTimeHolder(LocalTime.of(11, 59, 59)))
      assert(dao.selectLocalTimeHolder(5) === null)
      assert(dao.selectLocalTimeVal(0) === LocalTimeVal(null))
      assert(dao.selectLocalTimeVal(1) === LocalTimeVal(LocalTime.of(11, 59, 59)))
      assert(dao.selectLocalTimeVal(5) === LocalTimeVal(null))
      assert(dao.selectLocalTimeSeq(0) === Seq(null))
      assert(dao.selectLocalTimeSeq(1) === Seq(LocalTime.of(11, 59, 59)))
      assert(dao.selectLocalTimeSeq(5) === Nil)
      assert(dao.selectLocalTimeStream(0)(_.toList) === Seq(null))
      assert(dao.selectLocalTimeStream(1)(_.toList) === Seq(LocalTime.of(11, 59, 59)))
      assert(dao.selectLocalTimeStream(5)(_.toList) === Nil)
      assert(dao.selectLocalTimeIterator(0)(_.toList) === Seq(null))
      assert(dao.selectLocalTimeIterator(1)(_.toList) === Seq(LocalTime.of(11, 59, 59)))
      assert(dao.selectLocalTimeIterator(5)(_.toList) === Nil)
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
      assert(dao.selectLocalDateTimeHolder(0) === null)
      assert(dao.selectLocalDateTimeHolder(1) === LocalDateTimeHolder(LocalDateTime.of(2017, 12, 31, 11, 59, 59, 999999999)))
      assert(dao.selectLocalDateTimeHolder(5) === null)
      assert(dao.selectLocalDateTimeVal(0) === LocalDateTimeVal(null))
      assert(dao.selectLocalDateTimeVal(1) === LocalDateTimeVal(LocalDateTime.of(2017, 12, 31, 11, 59, 59, 999999999)))
      assert(dao.selectLocalDateTimeVal(5) === LocalDateTimeVal(null))
      assert(dao.selectLocalDateTimeSeq(0) === Seq(null))
      assert(dao.selectLocalDateTimeSeq(1) === Seq(LocalDateTime.of(2017, 12, 31, 11, 59, 59, 999999999)))
      assert(dao.selectLocalDateTimeSeq(5) === Nil)
      assert(dao.selectLocalDateTimeStream(0)(_.toList) === Seq(null))
      assert(dao.selectLocalDateTimeStream(1)(_.toList) === Seq(LocalDateTime.of(2017, 12, 31, 11, 59, 59, 999999999)))
      assert(dao.selectLocalDateTimeStream(5)(_.toList) === Nil)
      assert(dao.selectLocalDateTimeIterator(0)(_.toList) === Seq(null))
      assert(dao.selectLocalDateTimeIterator(1)(_.toList) === Seq(LocalDateTime.of(2017, 12, 31, 11, 59, 59, 999999999)))
      assert(dao.selectLocalDateTimeIterator(5)(_.toList) === Nil)
    }
  }
}

@Holder
case class BooleanHolder(value: Boolean)
@Holder
case class ByteHolder(id: Byte)
@Holder
case class ShortHolder(id: Short)
@Holder
case class IntHolder(id: Int)
@Holder
case class LongHolder(id: Long)
@Holder
case class FloatHolder(id: Float)
@Holder
case class DoubleHolder(id: Double)
@Holder
case class StringHolder(id: String)
@Holder
case class BigDecimalHolder(id: BigDecimal)
@Holder
case class BigIntHolder(id: BigInt)
@Holder
case class LocalDateHolder(id: LocalDate)
@Holder
case class LocalTimeHolder(id: LocalTime)
@Holder
case class LocalDateTimeHolder(id: LocalDateTime)

case class BooleanVal(value: Boolean) extends AnyVal
case class ByteVal(id: Byte) extends AnyVal
case class ShortVal(id: Short) extends AnyVal
case class IntVal(id: Int) extends AnyVal
case class LongVal(id: Long) extends AnyVal
case class FloatVal(id: Float) extends AnyVal
case class DoubleVal(id: Double) extends AnyVal
case class StringVal(id: String) extends AnyVal
case class BigDecimalVal(id: BigDecimal) extends AnyVal
case class BigIntVal(id: BigInt) extends AnyVal
case class LocalDateVal(id: LocalDate) extends AnyVal
case class LocalTimeVal(id: LocalTime) extends AnyVal
case class LocalDateTimeVal(id: LocalDateTime) extends AnyVal

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
  big_decimal decimal,
  bigint decimal,
  local_date date,
  localtime time,
  local_date_time timestamp
);

insert into single_result (id, boolean, byte, short, int, long, float, double,
string, big_decimal, bigint, local_date, localtime, local_date_time)
values(0, null, null, null, null, null, null, null,
  null, null, null, null, null, null);
insert into single_result (id, boolean, byte, short, int, long, float, double,
  string, big_decimal, bigint, local_date, localtime, local_date_time)
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

  @Select(sql ="select big_decimal from single_result where id = /* id */0")
  def selectBigDecimal(id: Int): BigDecimal

  @Select(sql ="select bigint from single_result where id = /* id */0")
  def selectBigInt(id: Int): BigInt

  @Select(sql ="select local_date from single_result where id = /* id */0")
  def selectLocalDate(id: Int): LocalDate

  @Select(sql ="select localtime from single_result where id = /* id */0")
  def selectLocalTime(id: Int): LocalTime

  @Select(sql ="select local_date_time from single_result where id = /* id */0")
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

  @Select(sql ="select big_decimal from single_result where id = /* id */0")
  def selectBigDecimalOption(id: Int): Option[BigDecimal]

  @Select(sql ="select bigint from single_result where id = /* id */0")
  def selectBigIntOption(id: Int): Option[BigInt]

  @Select(sql ="select local_date from single_result where id = /* id */0")
  def selectLocalDateOption(id: Int): Option[LocalDate]

  @Select(sql ="select localtime from single_result where id = /* id */0")
  def selectLocalTimeOption(id: Int): Option[LocalTime]

  @Select(sql ="select local_date_time from single_result where id = /* id */0")
  def selectLocalDateTimeOption(id: Int): Option[LocalDateTime]

  @Select(sql ="select boolean from single_result where id = /* id */0")
  def selectBooleanHolder(id: Int): BooleanHolder

  @Select(sql ="select byte from single_result where id = /* id */0")
  def selectByteHolder(id: Int): ByteHolder

  @Select(sql ="select short from single_result where id = /* id */0")
  def selectShortHolder(id: Int): ShortHolder

  @Select(sql ="select int from single_result where id = /* id */0")
  def selectIntHolder(id: Int): IntHolder

  @Select(sql ="select long from single_result where id = /* id */0")
  def selectLongHolder(id: Int): LongHolder

  @Select(sql ="select float from single_result where id = /* id */0")
  def selectFloatHolder(id: Int): FloatHolder

  @Select(sql ="select double from single_result where id = /* id */0")
  def selectDoubleHolder(id: Int): DoubleHolder

  @Select(sql ="select string from single_result where id = /* id */0")
  def selectStringHolder(id: Int): StringHolder

  @Select(sql ="select big_decimal from single_result where id = /* id */0")
  def selectBigDecimalHolder(id: Int): BigDecimalHolder

  @Select(sql ="select bigint from single_result where id = /* id */0")
  def selectBigIntHolder(id: Int): BigIntHolder

  @Select(sql ="select local_date from single_result where id = /* id */0")
  def selectLocalDateHolder(id: Int): LocalDateHolder

  @Select(sql ="select localtime from single_result where id = /* id */0")
  def selectLocalTimeHolder(id: Int): LocalTimeHolder

  @Select(sql ="select local_date_time from single_result where id = /* id */0")
  def selectLocalDateTimeHolder(id: Int): LocalDateTimeHolder

  @Select(sql ="select boolean from single_result where id = /* id */0")
  def selectBooleanVal(id: Int): BooleanVal

  @Select(sql ="select byte from single_result where id = /* id */0")
  def selectByteVal(id: Int): ByteVal

  @Select(sql ="select short from single_result where id = /* id */0")
  def selectShortVal(id: Int): ShortVal

  @Select(sql ="select int from single_result where id = /* id */0")
  def selectIntVal(id: Int): IntVal

  @Select(sql ="select long from single_result where id = /* id */0")
  def selectLongVal(id: Int): LongVal

  @Select(sql ="select float from single_result where id = /* id */0")
  def selectFloatVal(id: Int): FloatVal

  @Select(sql ="select double from single_result where id = /* id */0")
  def selectDoubleVal(id: Int): DoubleVal

  @Select(sql ="select string from single_result where id = /* id */0")
  def selectStringVal(id: Int): StringVal

  @Select(sql ="select big_decimal from single_result where id = /* id */0")
  def selectBigDecimalVal(id: Int): BigDecimalVal

  @Select(sql ="select bigint from single_result where id = /* id */0")
  def selectBigIntVal(id: Int): BigIntVal

  @Select(sql ="select local_date from single_result where id = /* id */0")
  def selectLocalDateVal(id: Int): LocalDateVal

  @Select(sql ="select localtime from single_result where id = /* id */0")
  def selectLocalTimeVal(id: Int): LocalTimeVal

  @Select(sql ="select local_date_time from single_result where id = /* id */0")
  def selectLocalDateTimeVal(id: Int): LocalDateTimeVal

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

  @Select(sql ="select big_decimal from single_result where id = /* id */0")
  def selectBigDecimalSeq(id: Int): Seq[BigDecimal]

  @Select(sql ="select bigint from single_result where id = /* id */0")
  def selectBigIntSeq(id: Int): Seq[BigInt]

  @Select(sql ="select local_date from single_result where id = /* id */0")
  def selectLocalDateSeq(id: Int): Seq[LocalDate]

  @Select(sql ="select localtime from single_result where id = /* id */0")
  def selectLocalTimeSeq(id: Int): Seq[LocalTime]

  @Select(sql ="select local_date_time from single_result where id = /* id */0")
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

  @Select(sql ="select big_decimal from single_result where id = /* id */0", strategy = SelectType.STREAM)
  def selectBigDecimalStream(id: Int)(f: Stream[BigDecimal] => Seq[BigDecimal]): Seq[BigDecimal]

  @Select(sql ="select bigint from single_result where id = /* id */0", strategy = SelectType.STREAM)
  def selectBigIntStream(id: Int)(f: Stream[BigInt] => Seq[BigInt]): Seq[BigInt]

  @Select(sql ="select local_date from single_result where id = /* id */0", strategy = SelectType.STREAM)
  def selectLocalDateStream(id: Int)(f: Stream[LocalDate] => Seq[LocalDate]): Seq[LocalDate]

  @Select(sql ="select localtime from single_result where id = /* id */0", strategy = SelectType.STREAM)
  def selectLocalTimeStream(id: Int)(f: Stream[LocalTime] => Seq[LocalTime]): Seq[LocalTime]

  @Select(sql ="select local_date_time from single_result where id = /* id */0", strategy = SelectType.STREAM)
  def selectLocalDateTimeStream(id: Int)(f: Stream[LocalDateTime] => Seq[LocalDateTime]): Seq[LocalDateTime]

  @Select(sql ="select boolean from single_result where id = /* id */0", strategy = SelectType.ITERATOR)
  def selectBooleanIterator(id: Int)(f: Iterator[Boolean] => Seq[Boolean]): Seq[Boolean]

  @Select(sql ="select byte from single_result where id = /* id */0", strategy = SelectType.ITERATOR)
  def selectByteIterator(id: Int)(f: Iterator[Byte] => Seq[Byte]): Seq[Byte]

  @Select(sql ="select short from single_result where id = /* id */0", strategy = SelectType.ITERATOR)
  def selectShortIterator(id: Int)(f: Iterator[Short] => Seq[Short]): Seq[Short]

  @Select(sql ="select int from single_result where id = /* id */0", strategy = SelectType.ITERATOR)
  def selectIntIterator(id: Int)(f: Iterator[Int] => Seq[Int]): Seq[Int]

  @Select(sql ="select long from single_result where id = /* id */0", strategy = SelectType.ITERATOR)
  def selectLongIterator(id: Int)(f: Iterator[Long] => Seq[Long]): Seq[Long]

  @Select(sql ="select float from single_result where id = /* id */0", strategy = SelectType.ITERATOR)
  def selectFloatIterator(id: Int)(f: Iterator[Float] => Seq[Float]): Seq[Float]

  @Select(sql ="select double from single_result where id = /* id */0", strategy = SelectType.ITERATOR)
  def selectDoubleIterator(id: Int)(f: Iterator[Double] => Seq[Double]): Seq[Double]

  @Select(sql ="select string from single_result where id = /* id */0", strategy = SelectType.ITERATOR)
  def selectStringIterator(id: Int)(f: Iterator[String] => Seq[String]): Seq[String]

  @Select(sql ="select big_decimal from single_result where id = /* id */0", strategy = SelectType.ITERATOR)
  def selectBigDecimalIterator(id: Int)(f: Iterator[BigDecimal] => Seq[BigDecimal]): Seq[BigDecimal]

  @Select(sql ="select bigint from single_result where id = /* id */0", strategy = SelectType.ITERATOR)
  def selectBigIntIterator(id: Int)(f: Iterator[BigInt] => Seq[BigInt]): Seq[BigInt]

  @Select(sql ="select local_date from single_result where id = /* id */0", strategy = SelectType.ITERATOR)
  def selectLocalDateIterator(id: Int)(f: Iterator[LocalDate] => Seq[LocalDate]): Seq[LocalDate]

  @Select(sql ="select localtime from single_result where id = /* id */0", strategy = SelectType.ITERATOR)
  def selectLocalTimeIterator(id: Int)(f: Iterator[LocalTime] => Seq[LocalTime]): Seq[LocalTime]

  @Select(sql ="select local_date_time from single_result where id = /* id */0", strategy = SelectType.ITERATOR)
  def selectLocalDateTimeIterator(id: Int)(f: Iterator[LocalDateTime] => Seq[LocalDateTime]): Seq[LocalDateTime]
}
