package domala.jdbc

import java.math.BigInteger
import java.sql.{Blob, Clob, NClob, SQLXML, Time, Timestamp}
import java.time.{LocalDate, LocalDateTime, LocalTime}
import java.util.function.Supplier

import domala.wrapper._

package object `type` {
  sealed trait Types {
    val isBasic: Boolean = false
    val isEntity: Boolean = false
    val isMacroEntity: Boolean = false
    val isHolder: Boolean = false
    val isAnyValHolder: Boolean = false
    val isNumber: Boolean = false
    val isEmbeddable: Boolean = false
    val isMacroEmbeddable: Boolean = false
    val isIterable: Boolean = false
    val isOption: Boolean = false
    val isBasicOption: Boolean = false
    val isHolderOption: Boolean = false
    val isMap: Boolean = false
    val isSeq: Boolean = false
    val isFunction: Boolean = false
  }
  object Types {
    trait Basic[BASIC] extends Types {
      override val isBasic: Boolean = true
      val isPrimitive: Boolean = false
      def wrapperSupplier: Supplier[Wrapper[BASIC]]
    }
    case object BigDecimalType extends Types.Basic[BigDecimal] {
      override val isNumber = true
      override def wrapperSupplier: Supplier[Wrapper[BigDecimal]] = () => new BigDecimalWrapper()
    }
    case object JavaBigDecimalType extends Types.Basic[java.math.BigDecimal] {
      override val isNumber = true
      override def wrapperSupplier: Supplier[Wrapper[java.math.BigDecimal]] = () => new org.seasar.doma.wrapper.BigDecimalWrapper()
    }
    case object BigIntType extends Types.Basic[BigInt] {
      override val isNumber = true
      override def wrapperSupplier: Supplier[Wrapper[BigInt]] = () => new BigIntWrapper()
    }
    case object BigIntegerType extends Types.Basic[BigInteger] {
      override val isNumber = true
      override def wrapperSupplier: Supplier[Wrapper[BigInteger]] = () => new BigIntegerWrapper()
    }
    case object IntType extends Types.Basic[Integer]{
      override val isPrimitive = true
      override val isNumber = true
      override def wrapperSupplier: Supplier[Wrapper[Integer]] = () => new IntegerWrapper()
    }
    case object AnyType extends Types.Basic[Object] {
      override def wrapperSupplier: Supplier[Wrapper[Object]] = () => new ObjectWrapper()
    }
    case object BytesType extends Types.Basic[Array[Byte]] {
      override def wrapperSupplier: Supplier[Wrapper[Array[Byte]]] = () => new BytesWrapper()
    }
    case object LongType extends Types.Basic[java.lang.Long] {
      override val isPrimitive = true
      override val isNumber = true
      override def wrapperSupplier: Supplier[Wrapper[java.lang.Long]] = () => new LongWrapper()
    }
    case object DoubleType extends Types.Basic[java.lang.Double] {
      override val isPrimitive = true
      override val isNumber = true
      override def wrapperSupplier: Supplier[Wrapper[java.lang.Double]] = () => new DoubleWrapper()
    }
    case object BooleanType extends Types.Basic[java.lang.Boolean] {
      override val isPrimitive = true
      override val isNumber = true
      override def wrapperSupplier: Supplier[Wrapper[java.lang.Boolean]] = () => new BooleanWrapper()
    }
    case object ByteType extends Types.Basic[java.lang.Byte] {
      override val isPrimitive = true
      override val isNumber = true
      override def wrapperSupplier: Supplier[Wrapper[java.lang.Byte]] = () => new ByteWrapper()
    }
    case object ShortType extends Types.Basic[java.lang.Short] {
      override val isPrimitive = true
      override val isNumber = true
      override def wrapperSupplier: Supplier[Wrapper[java.lang.Short]] = () => new ShortWrapper()
    }
    case object FloatType extends Types.Basic[java.lang.Float] {
      override val isPrimitive = true
      override val isNumber = true
      override def wrapperSupplier: Supplier[Wrapper[java.lang.Float]] = () => new FloatWrapper()
    }
    case object StringType extends Types.Basic[String] {
      override def wrapperSupplier: Supplier[Wrapper[java.lang.String]] = () => new StringWrapper()
    }
    case object LocalDateType extends Types.Basic[LocalDate] {
      override def wrapperSupplier: Supplier[Wrapper[LocalDate]] = () => new LocalDateWrapper()
    }
    case object LocalTimeType extends Types.Basic[LocalTime] {
      override def wrapperSupplier: Supplier[Wrapper[LocalTime]] = () => new LocalTimeWrapper()
    }
    case object LocalDateTimeType extends Types.Basic[LocalDateTime] {
      override def wrapperSupplier: Supplier[Wrapper[LocalDateTime]] = () => new LocalDateTimeWrapper()
    }
    case object DateType extends Types.Basic[java.sql.Date] {
      override def wrapperSupplier: Supplier[Wrapper[java.sql.Date]] = () => new DateWrapper()
    }
    case object UtilDateType extends Types.Basic[java.util.Date] {
      override def wrapperSupplier: Supplier[Wrapper[java.util.Date]] = () => new UtilDateWrapper()
    }
    case object TimeType extends Types.Basic[Time] {
      override def wrapperSupplier: Supplier[Wrapper[java.sql.Time]] = () => new TimeWrapper()
    }
    case object TimestampType extends Types.Basic[Timestamp] {
      override def wrapperSupplier: Supplier[Wrapper[java.sql.Timestamp]] = () => new TimestampWrapper()
    }
    case object BlobType extends Types.Basic[Blob] {
      override def wrapperSupplier: Supplier[Wrapper[java.sql.Blob]] = () => new BlobWrapper()
    }
    case object ClobType extends Types.Basic[Clob] {
      override def wrapperSupplier: Supplier[Wrapper[java.sql.Clob]] = () => new ClobWrapper()
    }
    case object NClobType extends Types.Basic[NClob] {
      override def wrapperSupplier: Supplier[Wrapper[java.sql.NClob]] = () => new NClobWrapper()
    }
    case object SQLXMLType extends Types.Basic[SQLXML] {
      override def wrapperSupplier: Supplier[Wrapper[java.sql.SQLXML]] = () => new SQLXMLWrapper()

    }
    sealed trait Holder[BASIC, HOLDER] extends Types {
      override val isHolder: Boolean = true
      val basic: Types
      override val isNumber: Boolean = basic.isNumber
    }
    final case class GeneratedHolderType[BASIC, HOLDER](basic: Types.Basic[BASIC]) extends Types.Holder[BASIC, HOLDER]
    final case class AnyValHolderType[BASIC, HOLDER](basic: Types.Basic[BASIC]) extends Types.Holder[BASIC, HOLDER] {
      override val isAnyValHolder: Boolean = true
    }

    sealed trait Entity extends Types {
      override val isEntity: Boolean = true
    }
    object GeneratedEntityType extends Types.Entity
    object MacroEntityType extends Types.Entity with Types.Embeddable {
      override val isMacroEntity: Boolean = true
      override val isMacroEmbeddable : Boolean = true
    }

    sealed trait Embeddable extends Types {
      override val isEmbeddable: Boolean = true
    }

    final case object Map extends Types {
      override val isMap: Boolean = true
    }

    final case object Function extends Types {
      override val isFunction: Boolean = true
    }

    sealed trait ContainerType extends Types {
      val elementType: Types
    }
    final case class Option(elementType: Types) extends ContainerType {
      override val isOption: Boolean = true
      override val isBasicOption: Boolean = elementType.isBasic
      override val isHolderOption: Boolean = elementType.isHolder
    }

    final case class Iterable(elementType: Types) extends ContainerType {
      override val isIterable: Boolean = true
    }

    final case class Seq(elementType: Types) extends ContainerType{
      override val isIterable: Boolean = true
      override val isSeq: Boolean = true
    }

    final case object Other extends Types

  }

}
