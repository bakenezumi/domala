package domala.wrapper

import org.seasar.doma.DomaNullPointerException
import org.seasar.doma.wrapper.{NumberWrapper, WrapperVisitor}

class BigDecimalWrapper(var value: BigDecimal = null) extends Wrapper[BigDecimal] with NumberWrapper[BigDecimal] {
  private val basicClass = classOf[BigDecimal]

  override def set(v: Number): Unit = {
    v match {
      case bd: BigDecimal => value = bd
      case jbd: java.math.BigDecimal => this.value = BigDecimal(jbd)
      case bi: BigInt => this.value = BigDecimal(bi)
      case jbi: java.math.BigInteger => this.value = BigDecimal(jbi)
      case _ => this.value = BigDecimal(v.doubleValue())
    }
  }

  override def increment(): Unit = {
    if (value != null) value = value + 1
  }

  override def decrement(): Unit = {
    if (value != null) value = value - 1
  }

  override def getBasicClass: Class[BigDecimal] = basicClass

  override def getDefault: Null = null

  override def set(basic: BigDecimal): Unit = value = basic

  override def getCopy: BigDecimal = value

  override def hasEqualValue(o: scala.Any): Boolean = o == value

  override def get(): BigDecimal = value

  override def accept[R, P, Q, TH <: Throwable](visitor: WrapperVisitor[R, P, Q, TH], p: P, q: Q): R = {
    if (visitor == null) throw new DomaNullPointerException("visitor")
    else  visitor.visitBigDecimalWrapper(
      new JavaBigDecimalWrapper(this, if(value == null) null else value.bigDecimal), p, q)
  }
}

private class JavaBigDecimalWrapper(originalWrapper: BigDecimalWrapper, value: java.math.BigDecimal) extends org.seasar.doma.wrapper.BigDecimalWrapper(value) {
  override def increment(): Unit = {
    super.increment()
    originalWrapper.increment()
  }

  override def decrement(): Unit = {
    super.decrement()
    originalWrapper.decrement()
  }

  override def getBasicClass: Class[java.math.BigDecimal] = classOf[java.math.BigDecimal]

  override def getDefault: Null = null

  override def doSet(value: java.math.BigDecimal): Unit = {
    super.doSet(value)
    if (value != null) originalWrapper.set(BigDecimal(value))
  }

  override def set(basic: Number): Unit = {
    super.set(basic)
    originalWrapper.set(basic)
  }
}