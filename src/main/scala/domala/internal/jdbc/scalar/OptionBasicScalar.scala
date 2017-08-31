package domala.internal.jdbc.scalar

import java.util.function.Supplier
import org.seasar.doma.internal.jdbc.scalar.Scalar
import org.seasar.doma.internal.util.AssertionUtil
import org.seasar.doma.wrapper.Wrapper

class OptionBasicScalar[BASIC](supplier: Supplier[Wrapper[BASIC]])
    extends Scalar[BASIC, Option[BASIC]] {

  protected val wrapper: Wrapper[BASIC] = supplier.get

  AssertionUtil.assertNotNull(supplier, "")
  AssertionUtil.assertNotNull(wrapper, "")

  override def getDomainClass: java.util.Optional[Class[_]] = java.util.Optional.empty()

  override def cast(value: AnyRef): Option[BASIC] =
    value.asInstanceOf[Option[BASIC]]

  override def get(): Option[BASIC] = Option(wrapper.get)

  override def getDefault: Option[BASIC] = None

  override def set(optional: Option[BASIC]): Unit = {
    if (optional == null) {
      wrapper.set(null.asInstanceOf[BASIC])
    } else {
      wrapper.set(optional.getOrElse(null).asInstanceOf[BASIC])
    }
  }

  override def getWrapper: Wrapper[BASIC] = wrapper

}