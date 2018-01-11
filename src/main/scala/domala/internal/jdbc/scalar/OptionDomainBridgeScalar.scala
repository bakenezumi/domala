package domala.internal.jdbc.scalar

import java.util.Optional

import domala.wrapper.Wrapper
import org.seasar.doma.internal.jdbc.scalar.Scalar
import org.seasar.doma.internal.util.AssertionUtil

class OptionDomainBridgeScalar[BASIC, DOMAIN](optionalScalar: Scalar[BASIC, Optional[DOMAIN]])
    extends Scalar[BASIC, Option[DOMAIN]] {

  AssertionUtil.assertNotNull(optionalScalar, "")

  override def getDomainClass: java.util.Optional[Class[_]] = {
    optionalScalar.getDomainClass
  }

  override def cast(value: AnyRef): Option[DOMAIN] =
    value.asInstanceOf[Option[DOMAIN]]

  override def get(): Option[DOMAIN] = {
    if (optionalScalar.get == null) null
    else Option(optionalScalar.get.orElse(null.asInstanceOf[DOMAIN]))
  }

  override def getDefault: Option[DOMAIN] = None

  override def set(option: Option[DOMAIN]): Unit = {
    if (option == null) {
      optionalScalar.set(Optional.of[DOMAIN](null.asInstanceOf[DOMAIN]))
    } else {
      optionalScalar.set(option.map(value => Optional.of(value))
        .getOrElse(Optional.empty())
      )
    }
  }

  override def getWrapper: Wrapper[BASIC] = optionalScalar.getWrapper
}