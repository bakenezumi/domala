package domala.internal.jdbc.scalar

import java.math.BigInteger
import java.sql.{Blob, Clob, Date, NClob, SQLXML, Time, Timestamp}
import java.time.{LocalDate, LocalDateTime, LocalTime}
import java.util.function.Supplier

import domala.jdbc.holder.{AbstractAnyValHolderDesc, AbstractHolderDesc}
import domala.wrapper.{BigDecimalWrapper, BigIntWrapper}
import org.seasar.doma.internal.jdbc.scalar.{BasicScalar, OptionalBasicScalar, Scalar, ScalarException}
import org.seasar.doma.internal.util.AssertionUtil.{assertNotNull, assertTrue}
import org.seasar.doma.internal.util.ClassUtil
import org.seasar.doma.jdbc.ClassHelper
import org.seasar.doma.message.Message
import org.seasar.doma.wrapper._

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
    if (result == null) {
      result = wrapHolderObject(value, boxedClass, optional, classHelper)
      if (result == null)
        result = wrapAnyValObject(value, boxedClass, optional, classHelper)
        if (result == null)
          throw new ScalarException(Message.DOMA1007, valueClass.getName, value)
    }
    result
  }

  protected def wrapBasicObject(value: Any,
                                valueClass: Class[_],
                                optional: Boolean,
                                primitive: Boolean): Supplier[Scalar[_, _]] = {
    assertNotNull(valueClass, "")
    if (valueClass eq classOf[String]) {
      val supplier = () => new StringWrapper(value.asInstanceOf[String])
      return createBasicScalarSupplier(supplier, optional, primitive)
    }
    if ((valueClass eq classOf[Int]) || (valueClass eq classOf[Integer])) {
      val supplier = () => new IntegerWrapper(value.asInstanceOf[Integer])
      return createBasicScalarSupplier(supplier, optional, primitive)
    }
    if ((valueClass eq classOf[Long]) || (valueClass eq classOf[java.lang.Long])) {
      val supplier = () => new LongWrapper(value.asInstanceOf[java.lang.Long])
      return createBasicScalarSupplier(supplier, optional, primitive)
    }
    if (valueClass eq classOf[BigDecimal]) {
      val supplier = () => new domala.wrapper.BigDecimalWrapper(value.asInstanceOf[BigDecimal])
      return createBasicScalarSupplier(supplier, optional, primitive)
    }
    if (valueClass eq classOf[java.math.BigDecimal]) {
      val supplier = () => new BigDecimalWrapper(value.asInstanceOf[java.math.BigDecimal])
      return createBasicScalarSupplier(supplier, optional, primitive)
    }
    if (valueClass eq classOf[java.util.Date]) {
      val supplier = () => new UtilDateWrapper(value.asInstanceOf[java.util.Date])
      return createBasicScalarSupplier(supplier, optional, primitive)
    }
    if (valueClass eq classOf[LocalDate]) {
      val supplier = () => new LocalDateWrapper(value.asInstanceOf[LocalDate])
      return createBasicScalarSupplier(supplier, optional, primitive)
    }
    if (valueClass eq classOf[LocalTime]) {
      val supplier = () => new LocalTimeWrapper(value.asInstanceOf[LocalTime])
      return createBasicScalarSupplier(supplier, optional, primitive)
    }
    if (valueClass eq classOf[LocalDateTime]) {
      val supplier = () =>
        new LocalDateTimeWrapper(value.asInstanceOf[LocalDateTime])
      return createBasicScalarSupplier(supplier, optional, primitive)
    }
    if (valueClass eq classOf[Date]) {
      val supplier = () => new DateWrapper(value.asInstanceOf[Date])
      return createBasicScalarSupplier(supplier, optional, primitive)
    }
    if (valueClass eq classOf[Timestamp]) {
      val supplier = () => new TimestampWrapper(value.asInstanceOf[Timestamp])
      return createBasicScalarSupplier(supplier, optional, primitive)
    }
    if (valueClass eq classOf[Time]) {
      val supplier = () => new TimeWrapper(value.asInstanceOf[Time])
      return createBasicScalarSupplier(supplier, optional, primitive)
    }
    if ((valueClass eq classOf[Boolean]) || (valueClass eq classOf[java.lang.Boolean])) {
      val supplier = () => new BooleanWrapper(value.asInstanceOf[java.lang.Boolean])
      return createBasicScalarSupplier(supplier, optional, primitive)
    }
    if (valueClass eq classOf[java.sql.Array]) {
      val supplier = () => new ArrayWrapper(value.asInstanceOf[java.sql.Array])
      return createBasicScalarSupplier(supplier, optional, primitive)
    }
    if (valueClass eq classOf[BigInt]) {
      val supplier = () => new BigIntWrapper(value.asInstanceOf[BigInt])
      return createBasicScalarSupplier(supplier, optional, primitive)
    }
    if (valueClass eq classOf[BigInteger]) {
      val supplier = () => new BigIntegerWrapper(value.asInstanceOf[BigInteger])
      return createBasicScalarSupplier(supplier, optional, primitive)
    }
    if (valueClass eq classOf[Blob]) {
      val supplier = () => new BlobWrapper(value.asInstanceOf[Blob])
      return createBasicScalarSupplier(supplier, optional, primitive)
    }
    if ((valueClass eq classOf[Array[Byte]]) || (valueClass eq classOf[Array[java.lang.Byte]])) {
      val supplier = () => new BytesWrapper(value.asInstanceOf[Array[Byte]])
      return createBasicScalarSupplier(supplier, optional, primitive)
    }
    if ((valueClass eq classOf[Byte]) || (valueClass eq classOf[java.lang.Byte])) {
      val supplier = () => new ByteWrapper(value.asInstanceOf[java.lang.Byte])
      return createBasicScalarSupplier(supplier, optional, primitive)
    }
    if (valueClass eq classOf[Clob]) {
      val supplier = () => new ClobWrapper(value.asInstanceOf[Clob])
      return createBasicScalarSupplier(supplier, optional, primitive)
    }
    if ((valueClass eq classOf[Double]) || (valueClass eq classOf[java.lang.Double])) {
      val supplier = () => new DoubleWrapper(value.asInstanceOf[java.lang.Double])
      return createBasicScalarSupplier(supplier, optional, primitive)
    }
    if ((valueClass eq classOf[Float]) || (valueClass eq classOf[java.lang.Float])) {
      val supplier = () => new FloatWrapper(value.asInstanceOf[java.lang.Float])
      return createBasicScalarSupplier(supplier, optional, primitive)
    }
    if (valueClass eq classOf[NClob]) {
      val supplier = () => new NClobWrapper(value.asInstanceOf[NClob])
      return createBasicScalarSupplier(supplier, optional, primitive)
    }
    if ((valueClass eq classOf[Short]) || (valueClass eq classOf[java.lang.Short])) {
      val supplier = () => new ShortWrapper(value.asInstanceOf[java.lang.Short])
      return createBasicScalarSupplier(supplier, optional, primitive)
    }
    if (valueClass eq classOf[SQLXML]) {
      val supplier = () => new SQLXMLWrapper(value.asInstanceOf[SQLXML])
      return createBasicScalarSupplier(supplier, optional, primitive)
    }
    if (valueClass eq classOf[Object]) {
      val supplier = () => new ObjectWrapper(value)
      return createBasicScalarSupplier(supplier, optional, primitive)
    }
    null
  }

  protected def createBasicScalarSupplier[BASIC](
      wrapperSupplier: () => Wrapper[BASIC],
      optional: Boolean,
      primitive: Boolean): Supplier[Scalar[_, _]] =
    if (optional)() => new OptionalBasicScalar[BASIC](() => wrapperSupplier())
    else () => new BasicScalar[BASIC](() => wrapperSupplier(), primitive)

  protected def wrapHolderObject[BASIC, HOLDER](value: Object, valueClass: Class[HOLDER], optional: Boolean, classHelper: ClassHelper): Supplier[Scalar[_, _]] = {
    val companionClass = try {
      classHelper.forName(valueClass.getName + "$")
    } catch {
      case _: ClassNotFoundException => return null
    }
    val holderDesc = if(classOf[AbstractHolderDesc[_, _]].isAssignableFrom(companionClass)) {
      companionClass
        .getField("MODULE$")
        .get(null)
        .asInstanceOf[AbstractHolderDesc[_, HOLDER]]
    } else return null
    if (holderDesc == null) return null
    val holder = valueClass.cast(value)
    if (optional) () => holderDesc.createOptionalScalar(holder)
    else () => holderDesc.createScalar(holder)
  }

  protected def wrapAnyValObject[BASIC <: Object, HOLDER](value: Object, valueClass: Class[HOLDER], optional: Boolean, classHelper: ClassHelper)(implicit bTag: ClassTag[BASIC], hTag: ClassTag[HOLDER]): Supplier[Scalar[_, _]] = {
    val holderDesc = AnyValHolderDescRepository.get(valueClass, classHelper)(bTag, hTag)
    if (holderDesc == null) return null
    val holder = valueClass.cast(value)
    if (optional) () => holderDesc.createOptionalScalar(holder)
    else () => holderDesc.createScalar(holder)
  }

  object AnyValHolderDescRepository {

    private[this] val cache = scala.collection.concurrent.TrieMap[String, AbstractAnyValHolderDesc[_,_]]()

    def get[BASIC <: Object, HOLDER](valueClass: Class[HOLDER], classHelper: ClassHelper)(implicit bTag: ClassTag[BASIC], hTag: ClassTag[HOLDER]): AbstractAnyValHolderDesc[BASIC, HOLDER] =
      cache.getOrElseUpdate(valueClass.getName, {
        val constructors = valueClass.getConstructors
        if (constructors.length != 1) return null
        val parameterTypes = constructors.head.getParameterTypes
        if (parameterTypes.length != 1) return null
        val parameterType = parameterTypes.head
        val fieldName = constructors.head.getParameters.head.getName
        val wrapperSupplier: Supplier[Wrapper[BASIC]] =
          if (parameterType eq classOf[String]) {
            () => new StringWrapper().asInstanceOf[Wrapper[BASIC]]

          } else if ((parameterType eq classOf[Int]) || (parameterType eq classOf[Integer])) {
            () => new IntegerWrapper().asInstanceOf[Wrapper[BASIC]]

          } else if ((parameterType eq classOf[Long]) || (parameterType eq classOf[java.lang.Long])) {
            () => new LongWrapper().asInstanceOf[Wrapper[BASIC]]

          } else if (parameterType eq classOf[BigDecimal]) {
            () => new domala.wrapper.BigDecimalWrapper().asInstanceOf[Wrapper[BASIC]]

          } else if (parameterType eq classOf[java.math.BigDecimal]) {
            () => new BigDecimalWrapper().asInstanceOf[Wrapper[BASIC]]

          } else if (parameterType eq classOf[java.util.Date]) {
            () => new UtilDateWrapper().asInstanceOf[Wrapper[BASIC]]

          } else if (parameterType eq classOf[LocalDate]) {
            () => new LocalDateWrapper().asInstanceOf[Wrapper[BASIC]]

          } else if (parameterType eq classOf[LocalTime]) {
            () => new LocalTimeWrapper().asInstanceOf[Wrapper[BASIC]]

          } else if (parameterType eq classOf[LocalDateTime]) {
            () => new LocalDateTimeWrapper().asInstanceOf[Wrapper[BASIC]]

          } else if (parameterType eq classOf[Date]) {
            () => new DateWrapper().asInstanceOf[Wrapper[BASIC]]

          } else if (parameterType eq classOf[Timestamp]) {
            () => new TimestampWrapper().asInstanceOf[Wrapper[BASIC]]

          } else if (parameterType eq classOf[Time]) {
            () => new TimeWrapper().asInstanceOf[Wrapper[BASIC]]

          } else if ((parameterType eq classOf[Boolean]) || (parameterType eq classOf[java.lang.Boolean])) {
            () => new BooleanWrapper().asInstanceOf[Wrapper[BASIC]]

          } else if (parameterType eq classOf[java.sql.Array]) {
            () => new ArrayWrapper().asInstanceOf[Wrapper[BASIC]]

          } else if (parameterType eq classOf[BigInt]) {
            () => new BigIntWrapper().asInstanceOf[Wrapper[BASIC]]

          } else if (parameterType eq classOf[BigInteger]) {
            () => new BigIntegerWrapper().asInstanceOf[Wrapper[BASIC]]

          } else if (parameterType eq classOf[Blob]) {
            () => new BlobWrapper().asInstanceOf[Wrapper[BASIC]]

          } else if ((parameterType eq classOf[Array[Byte]]) || (parameterType eq classOf[Array[java.lang.Byte]]))  {
            () => new BytesWrapper().asInstanceOf[Wrapper[BASIC]]

          } else if ((parameterType eq classOf[Byte]) || (parameterType eq classOf[java.lang.Byte])) {
            () => new ByteWrapper().asInstanceOf[Wrapper[BASIC]]

          } else if (parameterType eq classOf[Clob]) {
            () => new ClobWrapper().asInstanceOf[Wrapper[BASIC]]

          } else if ((parameterType eq classOf[Double]) || (parameterType eq classOf[java.lang.Double])) {
            () => new DoubleWrapper().asInstanceOf[Wrapper[BASIC]]

          } else if ((parameterType eq classOf[Float]) || (parameterType eq classOf[java.lang.Float])) {
            () => new FloatWrapper().asInstanceOf[Wrapper[BASIC]]

          } else if (parameterType eq classOf[NClob]) {
            () => new NClobWrapper().asInstanceOf[Wrapper[BASIC]]

          } else if ((parameterType eq classOf[Short]) || (parameterType eq classOf[java.lang.Short])) {
            () => new ShortWrapper().asInstanceOf[Wrapper[BASIC]]

          } else if (parameterType eq classOf[SQLXML]) {
            () => new SQLXMLWrapper().asInstanceOf[Wrapper[BASIC]]

          } else if (parameterType eq classOf[Object]) {
            () => new ObjectWrapper().asInstanceOf[Wrapper[BASIC]]

          } else {
            return null
          }
        val holderDesc = new AbstractAnyValHolderDesc[BASIC, HOLDER](wrapperSupplier) {
          override protected def newHolder(value: BASIC): HOLDER =
            constructors.head.newInstance(value.asInstanceOf[BASIC]).asInstanceOf[HOLDER]
          override protected def getBasicValue(holder: HOLDER): BASIC =
            (if (holder == null) null else valueClass.getMethod(fieldName).invoke(holder)).asInstanceOf[BASIC]
        }
        holderDesc
      }).asInstanceOf[AbstractAnyValHolderDesc[BASIC, HOLDER]]

    def clearCache(): Unit = cache.clear()

  }

}

