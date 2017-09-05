package domala.wrapper

import java.math.BigInteger

import org.seasar.doma.DomaNullPointerException
import org.seasar.doma.wrapper.{NumberWrapper, Wrapper, WrapperVisitor}

class BigIntWrapper(var value: BigInt = null) extends Wrapper[BigInt] with NumberWrapper[BigInt] {
  val basicClass = classOf[BigInt]

  override def set(v: Number) = {
    v match {
      case bd: BigDecimal => this.value = bd.toBigInt
      case jbd: java.math.BigDecimal => this.value = BigInt(jbd.toBigInteger)
      case bi: BigInt => this.value = bi
      case jbi: BigInteger => this.value = BigInt(jbi)
      case _ => this.value = BigInt(v.intValue())
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

  override def set(basic: BigInt) = value = basic

  override def getCopy = value

  override def hasEqualValue(o: scala.Any) = o == value

  override def get() = value

  override def accept[R, P, Q, TH <: Throwable](visitor: WrapperVisitor[R, P, Q, TH], p: P, q: Q) = {
    if (visitor == null) throw new DomaNullPointerException("visitor")
    else  visitor.visitBigIntegerWrapper(
      new JavaBigIntegerWrapper(this, if(value == null) null else value.bigInteger), p, q)
  }
}

private class JavaBigIntegerWrapper(originalWrapper: BigIntWrapper, value: BigInteger) extends org.seasar.doma.wrapper.BigIntegerWrapper(value) {
  override def increment() = {
    super.increment()
    originalWrapper.increment()
  }

  override def decrement() = {
    super.decrement()
    originalWrapper.decrement()
  }

  override def getBasicClass = classOf[BigInteger]

  override def getDefault = null

  override def doSet(value: BigInteger) = {
    super.doSet(value)
    if (value != null) originalWrapper.set(BigInt(value))
  }

  override def set(basic: Number) = {
    super.set(basic)
    originalWrapper.set(basic)
  }
}