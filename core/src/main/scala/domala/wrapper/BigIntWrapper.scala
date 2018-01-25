package domala.wrapper

import java.math.BigInteger

import org.seasar.doma.DomaNullPointerException
import org.seasar.doma.wrapper.{NumberWrapper, WrapperVisitor}

class BigIntWrapper(var value: BigInt = null) extends Wrapper[BigInt] with NumberWrapper[BigInt] {
  private val basicClass = classOf[BigInt]

  override def set(v: Number): Unit = {
    v match {
      case bd: BigDecimal => this.value = bd.toBigInt
      case jbd: java.math.BigDecimal => this.value = BigInt(jbd.toBigInteger)
      case bi: BigInt => this.value = bi
      case jbi: BigInteger => this.value = BigInt(jbi)
      case _ => this.value = BigInt(v.longValue())
    }
  }

  override def increment(): Unit = {
    if (value != null) value = value + 1
  }

  override def decrement(): Unit = {
    if (value != null) value = value - 1
  }

  override def getBasicClass: Class[BigInt] = basicClass

  override def getDefault: Null = null

  override def set(basic: BigInt): Unit = value = basic

  override def getCopy: BigInt = value

  override def hasEqualValue(o: scala.Any): Boolean = o == value

  override def get(): BigInt = value

  override def accept[R, P, Q, TH <: Throwable](visitor: WrapperVisitor[R, P, Q, TH], p: P, q: Q): R = {
    if (visitor == null) throw new DomaNullPointerException("visitor")
    else  visitor.visitBigIntegerWrapper(
      new JavaBigIntegerWrapper(this, if(value == null) null else value.bigInteger), p, q)
  }
}

private class JavaBigIntegerWrapper(originalWrapper: BigIntWrapper, value: BigInteger) extends org.seasar.doma.wrapper.BigIntegerWrapper(value) {
  override def increment(): Unit = {
    super.increment()
    originalWrapper.increment()
  }

  override def decrement(): Unit = {
    super.decrement()
    originalWrapper.decrement()
  }

  override def getBasicClass: Class[BigInteger] = classOf[BigInteger]

  override def getDefault: Null = null

  override def doSet(value: BigInteger): Unit = {
    super.doSet(value)
    if (value != null) originalWrapper.set(BigInt(value))
  }

  override def set(basic: Number): Unit = {
    super.set(basic)
    originalWrapper.set(basic)
  }
}