package domala

import org.scalatest.FunSuite

class HolderTestSuite extends FunSuite {
  test("Numeric Int") {
    val holders = (1 to 10).map(v => IntHolder(v))
    assert(holders.sum == IntHolder(55))
    assert(holders.product == IntHolder(3628800))
    assert(holders.max == IntHolder(10))
    assert(holders.min == IntHolder(1))

    import Numeric.Implicits._
    assert(IntHolder(1) + IntHolder(2) == IntHolder(3))
    assert(IntHolder(10) - IntHolder(1) == IntHolder(9))
    assert(- IntHolder(4) == IntHolder(-4))
    assert(IntHolder(-5).abs == IntHolder(5))
    assert(IntHolder(5).abs == IntHolder(5))
    assert(IntHolder(5).signum == 1)
    assert(IntHolder(0).signum == 0)
    assert(IntHolder(-5).signum == -1)
    assert(IntHolder(3).toInt() == 3)
    assert(IntHolder(3).toLong() == 3L)
    assert(IntHolder(3).toFloat() == 3.0F)
    assert(IntHolder(3).toDouble == 3.0D)
  }

  test("Numeric Long") {
    val holders = (1 to 10).map(v => LongHolder(v))
    assert(holders.sum == LongHolder(55))
    assert(holders.product == LongHolder(3628800))
    assert(holders.max == LongHolder(10))
    assert(holders.min == LongHolder(1))

    import Numeric.Implicits._
    assert(LongHolder(1) + LongHolder(2) == LongHolder(3))
    assert(LongHolder(10) - LongHolder(1) == LongHolder(9))
    assert(- LongHolder(4) == LongHolder(-4))
    assert(LongHolder(-5).abs == LongHolder(5))
    assert(LongHolder(5).abs == LongHolder(5))
    assert(LongHolder(5).signum == 1)
    assert(LongHolder(0).signum == 0)
    assert(LongHolder(-5).signum == -1)
    assert(LongHolder(3).toInt() == 3)
    assert(LongHolder(3).toLong() == 3L)
    assert(LongHolder(3).toFloat() == 3.0F)
    assert(LongHolder(3).toDouble == 3.0D)
  }

  test("Numeric Double") {
    val holders = (1 to 10).map(v => DoubleHolder(v))
    assert(holders.sum == DoubleHolder(55))
    assert(holders.product == DoubleHolder(3628800))
    assert(holders.max == DoubleHolder(10))
    assert(holders.min == DoubleHolder(1))

    import Numeric.Implicits._
    assert(DoubleHolder(1) + DoubleHolder(2) == DoubleHolder(3))
    assert(DoubleHolder(10) - DoubleHolder(1) == DoubleHolder(9))
    assert(- DoubleHolder(4) == DoubleHolder(-4))
    assert(DoubleHolder(-5).abs == DoubleHolder(5))
    assert(DoubleHolder(5).abs == DoubleHolder(5))
    assert(DoubleHolder(5).signum == 1)
    assert(DoubleHolder(0).signum == 0)
    assert(DoubleHolder(-5).signum == -1)
    assert(DoubleHolder(3).toInt() == 3)
    assert(DoubleHolder(3).toLong() == 3L)
    assert(DoubleHolder(3).toFloat() == 3.0F)
    assert(DoubleHolder(3).toDouble == 3.0D)
  }

  test("Numeric BigDecimal") {
    val holders = (1 to 10).map(v => BigDecimalHolder(v))
    assert(holders.sum == BigDecimalHolder(55))
    assert(holders.product == BigDecimalHolder(3628800))
    assert(holders.max == BigDecimalHolder(10))
    assert(holders.min == BigDecimalHolder(1))

    import Numeric.Implicits._
    assert(BigDecimalHolder(1) + BigDecimalHolder(2) == BigDecimalHolder(3))
    assert(BigDecimalHolder(10) - BigDecimalHolder(1) == BigDecimalHolder(9))
    assert(- BigDecimalHolder(4) == BigDecimalHolder(-4))
    assert(BigDecimalHolder(-5).abs == BigDecimalHolder(5))
    assert(BigDecimalHolder(5).abs == BigDecimalHolder(5))
    assert(BigDecimalHolder(5).signum == 1)
    assert(BigDecimalHolder(0).signum == 0)
    assert(BigDecimalHolder(-5).signum == -1)
    assert(BigDecimalHolder(3).toInt() == 3)
    assert(BigDecimalHolder(3).toLong() == 3L)
    assert(BigDecimalHolder(3).toFloat() == 3.0F)
    assert(BigDecimalHolder(3).toDouble == 3.0D)
  }

  test("Fractional Double") {
    val holders = (1 to 10).map(v => DoubleHolder2(v))
    assert(holders.sum == DoubleHolder2(55))
    assert(holders.product == DoubleHolder2(3628800))
    assert(holders.max == DoubleHolder2(10))
    assert(holders.min == DoubleHolder2(1))

    import Fractional.Implicits._
    assert(DoubleHolder2(1) + DoubleHolder2(2) == DoubleHolder2(3))
    assert(DoubleHolder2(10) - DoubleHolder2(1) == DoubleHolder2(9))
    assert(- DoubleHolder2(4) == DoubleHolder2(-4))
    assert(DoubleHolder2(-5).abs == DoubleHolder2(5))
    assert(DoubleHolder2(5).abs == DoubleHolder2(5))
    assert(DoubleHolder2(5).signum == 1)
    assert(DoubleHolder2(0).signum == 0)
    assert(DoubleHolder2(-5).signum == -1)
    assert(DoubleHolder2(3).toInt() == 3)
    assert(DoubleHolder2(3).toLong() == 3L)
    assert(DoubleHolder2(3).toFloat() == 3.0F)
    assert(DoubleHolder2(3).toDouble == 3.0D)

    assert(DoubleHolder2(10.0) / DoubleHolder2(2.0) == DoubleHolder2(5.0))

  }
}

@Holder
case class IntHolder(value: Int)

@Holder
case class LongHolder(value: Long)

@Holder
case class DoubleHolder(value: Double)
object DoubleHolder

@Holder
case class BigDecimalHolder(value: BigDecimal)

@Holder
case class DoubleHolder2(value: Double)

object DoubleHolder2 {
  implicit val numeric: Fractional[DoubleHolder2] = new  Fractional[DoubleHolder2] {
    override def plus(x: DoubleHolder2, y: DoubleHolder2) = DoubleHolder2(x.value + y.value)

    override def minus(x: DoubleHolder2, y: DoubleHolder2) = DoubleHolder2(x.value - y.value)

    override def times(x: DoubleHolder2, y: DoubleHolder2) = DoubleHolder2(x.value * y.value)

    override def negate(x: DoubleHolder2) = DoubleHolder2(- x.value)

    override def fromInt(x: Int) = DoubleHolder2(x)

    override def toInt(x: DoubleHolder2): Int = x.value.toInt

    override def toLong(x: DoubleHolder2): Long = x.value.toLong

    override def toFloat(x: DoubleHolder2): Float = x.value.toFloat

    override def toDouble(x: DoubleHolder2): Double = x.value

    override def compare(x: DoubleHolder2, y: DoubleHolder2): Int = x.value compare y.value

    override def div(x: DoubleHolder2, y: DoubleHolder2) = DoubleHolder2(x.value / y.value)
  }
}