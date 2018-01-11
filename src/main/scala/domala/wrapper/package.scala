package domala

import org.seasar.doma

package object wrapper {
  type Wrapper[T] = doma.wrapper.Wrapper[T]

  type StringWrapper = doma.wrapper.StringWrapper
  type IntegerWrapper = doma.wrapper.IntegerWrapper
  type ShortWrapper = doma.wrapper.ShortWrapper
  type LongWrapper = doma.wrapper.LongWrapper
  type UtilDateWrapper = doma.wrapper.UtilDateWrapper
  type LocalDateWrapper = doma.wrapper.LocalDateWrapper
  type LocalTimeWrapper = doma.wrapper.LocalTimeWrapper
  type LocalDateTimeWrapper = doma.wrapper.LocalDateTimeWrapper
  type DateWrapper = doma.wrapper.DateWrapper
  type TimestampWrapper = doma.wrapper.TimestampWrapper
  type TimeWrapper = doma.wrapper.TimeWrapper
  type BooleanWrapper = doma.wrapper.BooleanWrapper
  type ArrayWrapper = doma.wrapper.ArrayWrapper
  type BigIntegerWrapper = doma.wrapper.BigIntegerWrapper
  type BlobWrapper = doma.wrapper.BlobWrapper
  type BytesWrapper = doma.wrapper.BytesWrapper
  type ByteWrapper = doma.wrapper.ByteWrapper
  type ClobWrapper = doma.wrapper.ClobWrapper
  type DoubleWrapper = doma.wrapper.DoubleWrapper
  type FloatWrapper = doma.wrapper.FloatWrapper
  type NClobWrapper = doma.wrapper.NClobWrapper
  type SQLXMLWrapper = doma.wrapper.SQLXMLWrapper
  type ObjectWrapper = doma.wrapper.ObjectWrapper
}
