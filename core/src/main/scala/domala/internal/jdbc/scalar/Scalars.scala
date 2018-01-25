package domala.internal.jdbc.scalar

import java.math.BigInteger
import java.sql.{Blob, Clob, Date, NClob, SQLXML, Time, Timestamp}
import java.time.{LocalDate, LocalDateTime, LocalTime}
import java.util.function.Supplier

import domala.internal.jdbc.holder.AnyValHolderDescRepository
import domala.jdbc.holder.AbstractHolderDesc
import domala.wrapper._
import org.seasar.doma.internal.WrapException
import org.seasar.doma.internal.jdbc.scalar.{BasicScalar, Scalar, ScalarException}
import org.seasar.doma.internal.util.AssertionUtil.{assertNotNull, assertTrue}
import org.seasar.doma.internal.util.ClassUtil
import org.seasar.doma.jdbc.ClassHelper
import org.seasar.doma.message.Message

import scala.reflect.ClassTag

object Scalars {

  def wrap(value: Object,
           valueClass: Class[_],
           optional: Boolean,
           classHelper: ClassHelper): Supplier[Scalar[_, _]] = {
    assertNotNull(valueClass, classHelper)
    val boxedClass = ClassUtil.toBoxedPrimitiveTypeIfPossible(valueClass)
    assertTrue(value == null || boxedClass.isInstance(value))
    if (classOf[Scalar[_, _]].isAssignableFrom(boxedClass))
      return () => value.asInstanceOf[Scalar[_, _]]
    var result = wrapBasicObject(value, boxedClass, optional, valueClass.isPrimitive)
    if (result == null)
      result = wrapHolderObject(value, boxedClass, optional, classHelper)
    if (result == null)
      result = wrapAnyValHolderObject(value, boxedClass.asInstanceOf[Class[Any]], optional, classHelper)
    if (result == null)
      throw new ScalarException(Message.DOMA1007, valueClass.getName, value)
    result
  }

  protected def wrapBasicObject(value: Any,
                                valueClass: Class[_],
                                optional: Boolean,
                                primitive: Boolean): Supplier[Scalar[_, _]] = {
    assertNotNull(valueClass, "")
    valueClass match {
      case _ if valueClass == classOf[String] =>
        val supplier = () => new StringWrapper(value.asInstanceOf[String])
        createBasicScalarSupplier(supplier, optional, primitive)
      case _ if (valueClass == classOf[Int]) || (valueClass eq classOf[Integer]) =>
        val supplier = () => new IntegerWrapper(value.asInstanceOf[Integer])
        createBasicScalarSupplier(supplier, optional, primitive)
      case _ if (valueClass == classOf[Long]) || (valueClass eq classOf[java.lang.Long]) =>
        val supplier = () => new LongWrapper(value.asInstanceOf[java.lang.Long])
        createBasicScalarSupplier(supplier, optional, primitive)
      case _ if valueClass == classOf[BigDecimal] =>
        val supplier = () => new domala.wrapper.BigDecimalWrapper(value.asInstanceOf[BigDecimal])
        createBasicScalarSupplier(supplier, optional, primitive)
      case _ if valueClass == classOf[java.math.BigDecimal] =>
        val supplier = () => new BigDecimalWrapper(value.asInstanceOf[java.math.BigDecimal])
        createBasicScalarSupplier(supplier, optional, primitive)
      case _ if valueClass == classOf[java.util.Date] =>
        val supplier = () => new UtilDateWrapper(value.asInstanceOf[java.util.Date])
        createBasicScalarSupplier(supplier, optional, primitive)
      case _ if valueClass == classOf[LocalDate] =>
        val supplier = () => new LocalDateWrapper(value.asInstanceOf[LocalDate])
        createBasicScalarSupplier(supplier, optional, primitive)
      case _ if valueClass == classOf[LocalTime] =>
        val supplier = () => new LocalTimeWrapper(value.asInstanceOf[LocalTime])
        createBasicScalarSupplier(supplier, optional, primitive)
      case _ if valueClass == classOf[LocalDateTime] =>
        val supplier = () => new LocalDateTimeWrapper(value.asInstanceOf[LocalDateTime])
        createBasicScalarSupplier(supplier, optional, primitive)
      case _ if valueClass == classOf[Date] =>
        val supplier = () => new DateWrapper(value.asInstanceOf[Date])
        createBasicScalarSupplier(supplier, optional, primitive)
      case _ if valueClass == classOf[Timestamp] =>
        val supplier = () => new TimestampWrapper(value.asInstanceOf[Timestamp])
        createBasicScalarSupplier(supplier, optional, primitive)
      case _ if valueClass == classOf[Time] =>
        val supplier = () => new TimeWrapper(value.asInstanceOf[Time])
        createBasicScalarSupplier(supplier, optional, primitive)
      case _ if (valueClass == classOf[Boolean]) || (valueClass == classOf[java.lang.Boolean]) =>
        val supplier = () => new BooleanWrapper(value.asInstanceOf[java.lang.Boolean])
        createBasicScalarSupplier(supplier, optional, primitive)
      case _ if valueClass == classOf[java.sql.Array] =>
        val supplier = () => new ArrayWrapper(value.asInstanceOf[java.sql.Array])
        createBasicScalarSupplier(supplier, optional, primitive)
      case _ if valueClass == classOf[BigInt] =>
        val supplier = () => new BigIntWrapper(value.asInstanceOf[BigInt])
        createBasicScalarSupplier(supplier, optional, primitive)
      case _ if valueClass == classOf[BigInteger] =>
        val supplier = () => new BigIntegerWrapper(value.asInstanceOf[BigInteger])
        createBasicScalarSupplier(supplier, optional, primitive)
      case _ if valueClass == classOf[Blob] =>
        val supplier = () => new BlobWrapper(value.asInstanceOf[Blob])
        createBasicScalarSupplier(supplier, optional, primitive)
      case _ if (valueClass == classOf[Array[Byte]]) || (valueClass == classOf[Array[java.lang.Byte]]) =>
        val supplier = () => new BytesWrapper(value.asInstanceOf[Array[Byte]])
        createBasicScalarSupplier(supplier, optional, primitive)
      case _ if (valueClass == classOf[Byte]) || (valueClass eq classOf[java.lang.Byte]) =>
        val supplier = () => new ByteWrapper(value.asInstanceOf[java.lang.Byte])
        createBasicScalarSupplier(supplier, optional, primitive)
      case _ if valueClass == classOf[Clob] =>
        val supplier = () => new ClobWrapper(value.asInstanceOf[Clob])
        createBasicScalarSupplier(supplier, optional, primitive)
      case _ if (valueClass == classOf[Double]) || (valueClass == classOf[java.lang.Double]) =>
        val supplier = () => new DoubleWrapper(value.asInstanceOf[java.lang.Double])
        createBasicScalarSupplier(supplier, optional, primitive)
      case _ if (valueClass == classOf[Float]) || (valueClass == classOf[java.lang.Float]) =>
        val supplier = () => new FloatWrapper(value.asInstanceOf[java.lang.Float])
        createBasicScalarSupplier(supplier, optional, primitive)
      case _ if valueClass == classOf[NClob] =>
        val supplier = () => new NClobWrapper(value.asInstanceOf[NClob])
        createBasicScalarSupplier(supplier, optional, primitive)
      case _ if (valueClass == classOf[Short]) || (valueClass == classOf[java.lang.Short]) =>
        val supplier = () => new ShortWrapper(value.asInstanceOf[java.lang.Short])
        createBasicScalarSupplier(supplier, optional, primitive)
      case _ if valueClass == classOf[SQLXML] =>
        val supplier = () => new SQLXMLWrapper(value.asInstanceOf[SQLXML])
        createBasicScalarSupplier(supplier, optional, primitive)
      case _ if valueClass == classOf[Object] =>
        val supplier = () => new ObjectWrapper(value)
        createBasicScalarSupplier(supplier, optional, primitive)
      case _ => null
    }
  }

  protected def createBasicScalarSupplier[BASIC](
      wrapperSupplier: () => Wrapper[BASIC],
      optional: Boolean,
      primitive: Boolean): Supplier[Scalar[_, _]] =
    if (optional)() => new OptionBasicScalar[BASIC](() => wrapperSupplier())
    else () => new BasicScalar[BASIC](() => wrapperSupplier(), primitive)

  protected def wrapHolderObject[BASIC, HOLDER](value: Object, holderClass: Class[HOLDER], optional: Boolean, classHelper: ClassHelper): Supplier[Scalar[_, _]] = {
    val companionClass = try {
      classHelper.forName(holderClass.getName + "$")
    } catch {
      case e: WrapException => e.getCause match {
          case _: ClassNotFoundException => return null
          case _ => throw e
        }
    }
    val holderDesc = if(classOf[AbstractHolderDesc[_, _]].isAssignableFrom(companionClass)) {
      companionClass
        .getField("MODULE$")
        .get(null)
        .asInstanceOf[AbstractHolderDesc[BASIC, HOLDER]]
    } else return null
    if (holderDesc == null) return null
    val holder = holderClass.cast(value)
    if (optional) () => new OptionDomainBridgeScalar(holderDesc.createOptionalScalar(holder))
    else () => holderDesc.createScalar(holder)
  }

  protected def wrapAnyValHolderObject[BASIC, HOLDER](value: BASIC, valueClass: Class[HOLDER], optional: Boolean, classHelper: ClassHelper)(implicit bTag: ClassTag[BASIC], hTag: ClassTag[HOLDER]): Supplier[Scalar[_, _]] = {
    val holderDesc = AnyValHolderDescRepository.getByClass(valueClass, classHelper)
    if (holderDesc == null) return null
    val holder = valueClass.cast(value)
    if (optional) () => new OptionDomainBridgeScalar(holderDesc.createOptionalScalar(holder))
    else () => holderDesc.createScalar(holder)
  }

}

