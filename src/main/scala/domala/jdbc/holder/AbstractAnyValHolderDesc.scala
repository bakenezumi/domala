package domala.jdbc.holder

import java.util.Optional
import java.util.function.Supplier

import org.seasar.doma.internal.jdbc.scalar.Scalar
import org.seasar.doma.jdbc.domain.DomainType
import org.seasar.doma.wrapper.Wrapper

import scala.reflect.ClassTag

abstract class AbstractAnyValHolderDesc[BASIC, HOLDER](val wrapperSupplier: Supplier[Wrapper[BASIC]])(implicit basicTag: ClassTag[BASIC], holderTag: ClassTag[HOLDER]) extends DomainType[BASIC, HOLDER]{
  val self: AbstractAnyValHolderDesc[BASIC, HOLDER] = this

  override def createOptionalScalar(): Scalar[BASIC, Optional[HOLDER]] = new OptionalHolderScalar(wrapperSupplier.get)

  override def createOptionalScalar(value: HOLDER): Scalar[BASIC, Optional[HOLDER]] = {
    val wrapper = wrapperSupplier.get
    wrapper.set(getBasicValue(value))
    new OptionalHolderScalar(wrapper)
  }

  override def createScalar(): Scalar[BASIC, HOLDER] = new HolderScalar(wrapperSupplier.get)

  override def createScalar(value: HOLDER): Scalar[BASIC, HOLDER] = {
    val wrapper = wrapperSupplier.get
    wrapper.set(getBasicValue(value))
    new HolderScalar(wrapper)
  }

  override def getDomainClass: Class[HOLDER] = holderTag.runtimeClass.asInstanceOf[Class[HOLDER]]

  override def getBasicClass: Class[BASIC] = basicTag.runtimeClass.asInstanceOf[Class[BASIC]]

  protected def newHolder(value: BASIC): HOLDER

  protected def getBasicValue(holder: HOLDER): BASIC

  class HolderScalar private[holder](val wrapper: Wrapper[BASIC]) extends Scalar[BASIC, HOLDER] {
    override def getDomainClass: Optional[Class[_]] = {
      val c = self.getDomainClass
      Optional.of[Class[_]](c)
    }

    override def cast(value: Any): HOLDER = {
      self.newHolder(value.asInstanceOf[BASIC])
    }

    override def get: HOLDER = newHolder(wrapper.get)

    override def getDefault: HOLDER = self.newHolder(null.asInstanceOf[BASIC])

    override def set(domain: HOLDER): Unit = {
      val value = getBasicValue(domain)
      wrapper.set(value)
    }

    override def getWrapper: Wrapper[BASIC] = wrapper

  }

  class OptionalHolderScalar private[holder](val wrapper: Wrapper[BASIC]) extends Scalar[BASIC, Optional[HOLDER]] {
    override def getDomainClass: Optional[Class[_]] = {
      val clazz = self.getDomainClass
      Optional.of[Class[_]](clazz)
    }

    override def cast(value: Any): Optional[HOLDER] = value.asInstanceOf[Optional[HOLDER]]

    override def get: Optional[HOLDER] = {
      val value = wrapper.get
      if (value == null && !getBasicClass.isPrimitive) return getDefaultInternal
      Optional.of(self.newHolder(value))
    }

    override def getDefault: Optional[HOLDER] = getDefaultInternal

    protected def getDefaultInternal: Optional[HOLDER] = Optional.empty[HOLDER]

    override def set(optional: Optional[HOLDER]): Unit = {
      if (optional != null && optional.isPresent) wrapper.set(getBasicValue(optional.get))
      else wrapper.set(null.asInstanceOf[BASIC])
    }

    override def getWrapper: Wrapper[BASIC] = wrapper

  }
}
