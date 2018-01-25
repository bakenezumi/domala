package domala.internal.jdbc.scalar

import java.util.function.Supplier

import domala.wrapper.Wrapper
import org.seasar.doma.internal.jdbc.scalar.Scalar
import org.seasar.doma.internal.util.AssertionUtil

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

  override def set(option: Option[BASIC]): Unit = {
    if (option == null) {
      wrapper.set(null.asInstanceOf[BASIC])
    } else {
      wrapper.set(option.getOrElse(null.asInstanceOf[BASIC]))
    }
  }

  override def getWrapper: Wrapper[BASIC] = wrapper
}