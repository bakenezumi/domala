package domala.wrapper

import org.seasar.doma.DomaNullPointerException
import org.seasar.doma.wrapper.{Wrapper, NumberWrapper, WrapperVisitor}

class BigDecimalWrapper(var value: BigDecimal = null) extends Wrapper[BigDecimal] with NumberWrapper[BigDecimal] {
  val basicClass = classOf[BigDecimal]

  override def set(v: Number) = {
    v match {
      case bd: BigDecimal => value = bd
      case jbd: java.math.BigDecimal => this.value = BigDecimal(jbd)
      case bi: BigInt => this.value = BigDecimal(bi)
      case jbi: java.math.BigInteger => this.value = BigDecimal(jbi)
      case _ => this.value = BigDecimal(v.doubleValue())
    }
  }

  override def increment() = {
    if (value != null) value = value + 1
  }

  override def decrement() = {
    if (value != null) value = value - 1
  }

  override def getBasicClass = basicClass

  override def getDefault = null

  override def set(basic: BigDecimal) = value = basic

  override def getCopy = value

  override def hasEqualValue(o: scala.Any) = o == value

  override def get() = value

  override def accept[R, P, Q, TH <: Throwable](visitor: WrapperVisitor[R, P, Q, TH], p: P, q: Q) = {
    if (visitor == null) throw new DomaNullPointerException("visitor")
    else  visitor.visitBigDecimalWrapper(
      new JavaBigDecimalWrapper(this, if(value == null) null else value.bigDecimal), p, q)
  }
}

private class JavaBigDecimalWrapper(originalWrapper: BigDecimalWrapper, value: java.math.BigDecimal) extends org.seasar.doma.wrapper.BigDecimalWrapper(value) {
  override def increment() = {
    super.increment()
    originalWrapper.increment()
  }

  override def decrement() = {
    super.decrement()
    originalWrapper.decrement()
  }

  override def getBasicClass = classOf[java.math.BigDecimal]

  override def getDefault = null

  override def doSet(value: java.math.BigDecimal) = {
    super.doSet(value)
    if (value != null) originalWrapper.set(BigDecimal(value))
  }

  override def set(basic: Number) = {
    super.set(basic)
    originalWrapper.set(basic)
  }
}