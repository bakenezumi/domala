package domala.internal.jdbc.scalar

import java.util.Optional

import org.seasar.doma.internal.jdbc.scalar.Scalar
import org.seasar.doma.internal.util.AssertionUtil
import org.seasar.doma.wrapper.Wrapper

class OptionDomainBridgeScalar[BASIC, DOMAIN](optionalScalar: Scalar[BASIC, Optional[DOMAIN]])
    extends Scalar[BASIC, Option[DOMAIN]] {

  protected val wrapper: Wrapper[BASIC] = optionalScalar.getWrapper

  AssertionUtil.assertNotNull(optionalScalar, "")
  AssertionUtil.assertNotNull(wrapper, "")

  override def getDomainClass: java.util.Optional[Class[_]] = java.util.Optional.empty()

  override def cast(value: AnyRef): Option[DOMAIN] =
    value.asInstanceOf[Option[DOMAIN]]

  override def get(): Option[DOMAIN] = Option(optionalScalar.get.get())

  override def getDefault: Option[DOMAIN] = None

  override def set(optional: Option[DOMAIN]): Unit = {
    if (optional == null) {
      wrapper.set(null.asInstanceOf[BASIC])
    } else {
      wrapper.set(optional.getOrElse(null).asInstanceOf[BASIC])
    }
  }

  override def getWrapper: Wrapper[BASIC] = wrapper
}