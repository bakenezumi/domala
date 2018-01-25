package domala.internal

import java.util.{Optional, OptionalDouble, OptionalInt, OptionalLong}

/**
  * Copyright (C) 2012-2015 Lightbend Inc. <http://www.lightbend.com>
  *
  * @see [[https://github.com/scala/scala-java8-compat/blob/master/src/main/scala/scala/compat/java8/OptionConverters.scala]]
  * @see [[https://github.com/scala/scala-java8-compat/blob/master/LICENSE]]
  */
object OptionConverters {

  /** Provides conversions from `java.util.Optional` to Scala `Option` or primitive `java.util.Optional` types */
  implicit class RichOptionalGeneric[A](val underlying: java.util.Optional[A]) extends AnyVal {
    /** Create a `scala.Option` version of this `Optional` */
    def asScala: Option[A] = if (underlying.isPresent) Some(underlying.get) else None
  }

  /** Provides conversions from `scala.Option` to Java `Optional` types, either generic or primitive */
  implicit class RichOptionForJava8[A](val underlying: Option[A]) extends AnyVal {
    /** Create a `java.util.Optional` version of this `Option` (not specialized) */
    def asJava: Optional[A] = underlying match { case Some(a) => Optional.ofNullable(a); case _ => Optional.empty[A] }
  }

  /** Provides conversions from `java.util.OptionalDouble` to the generic `Optional` and Scala `Option` */
  implicit class RichOptionalDouble(val underlying: OptionalDouble) extends AnyVal {
    /** Create a `scala.Option` version of this `OptionalDouble` */
    def asScala: Option[Double] = if (underlying.isPresent) Some(underlying.getAsDouble) else None
  }

  /** Provides conversions from `java.util.OptionalInt` to the generic `Optional` and Scala `Option` */
  implicit class RichOptionalInt(val underlying: OptionalInt) extends AnyVal {
    /** Create a `scala.Option` version of this `OptionalInt` */
    def asScala: Option[Int] = if (underlying.isPresent) Some(underlying.getAsInt) else None
  }

  /** Provides conversions from `java.util.OptionalLong` to the generic `Optional` and Scala `Option` */
  implicit class RichOptionalLong(val underlying: OptionalLong) extends AnyVal {
    /** Create a `scala.Option` version of this `OptionalLong` */
    def asScala: Option[Long] = if (underlying.isPresent) Some(underlying.getAsLong) else None
  }

  def asScala[A](underlying: java.util.Optional[A]): Option[A] = if (underlying.isPresent) Some(underlying.get) else None
}
