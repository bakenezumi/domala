package domala.internal.macros.reflect.util

import java.math.BigInteger
import java.sql.{Blob, Clob, NClob, SQLXML, Time, Timestamp}
import java.time.{LocalDate, LocalDateTime, LocalTime}

import domala.jdbc.entity.EntityCompanion
import domala.jdbc.holder.{AbstractAnyValHolderDesc, AbstractHolderDesc}
import domala.wrapper.BigIntWrapper
import org.seasar.doma.internal.apt.meta.EntityConstructorMeta
import org.seasar.doma.jdbc.entity.{AbstractEntityType, EmbeddableType}
import org.seasar.doma.wrapper._

import scala.collection.mutable.ArrayBuffer
import scala.reflect.macros.blackbox

object TypeUtil {

  def isBasic[C <: blackbox.Context](c: C)(tpe: C#Type): Boolean = {
    import c.universe._
    tpe =:= typeOf[String] ||
    tpe =:= typeOf[Boolean] || tpe =:= typeOf[java.lang.Boolean] ||
    tpe =:= typeOf[Byte] || tpe =:= typeOf[java.lang.Byte] ||
    tpe =:= typeOf[Short] || tpe =:= typeOf[java.lang.Short] ||
    tpe =:= typeOf[Int] || tpe =:= typeOf[Integer] ||
    tpe =:= typeOf[Long] || tpe =:= typeOf[java.lang.Long] ||
    tpe =:= typeOf[Float] || tpe =:= typeOf[java.lang.Float] ||
    tpe =:= typeOf[Double] || tpe =:= typeOf[java.lang.Double] ||
    tpe =:= typeOf[Object] || tpe =:= typeOf[AnyRef] || tpe =:= typeOf[Any] ||
    tpe =:= typeOf[BigDecimal] ||
    tpe =:= typeOf[BigInteger] ||
    tpe =:= typeOf[BigInt] ||
    tpe =:= typeOf[Time] ||
    tpe =:= typeOf[Timestamp] ||
    tpe =:= typeOf[java.sql.Date] ||
    tpe =:= typeOf[java.util.Date] ||
    tpe =:= typeOf[LocalTime] ||
    tpe =:= typeOf[LocalDateTime] ||
    tpe =:= typeOf[LocalDate] ||
    tpe =:= typeOf[Array[Byte]] ||
    tpe =:= typeOf[Blob] ||
    tpe =:= typeOf[NClob] ||
    tpe =:= typeOf[Clob] ||
    tpe =:= typeOf[SQLXML]
  }

  def isEntity[C <: blackbox.Context](c: C)(tpe: C#Type): Boolean = {
    import c.universe._
    tpe.companion <:< typeOf[EntityCompanion]
  }

  def isHolder[C <: blackbox.Context](c: C)(tpe: C#Type): Boolean = {
    import c.universe._
    tpe.companion <:< typeOf[AbstractHolderDesc[_, _]]
  }

  def isAnyVal[C <: blackbox.Context](c: C)(tpe: C#Type): Boolean = {
    import c.universe._
    tpe <:< typeOf[AnyVal]
  }

  def isNumberHolder[C <: blackbox.Context](c: C)(tpe: C#Type): Boolean = {
    import c.universe._
    tpe.companion <:< typeOf[AbstractHolderDesc[_ <: Number, _]]
  }

  def isNumber[C <: blackbox.Context](c: C)(tpe: C#Type): Boolean = {
    import c.universe._
    tpe <:< typeOf[Number]
  }

  def isEmbeddable[C <: blackbox.Context](c: C)(tpe: C#Type): Boolean = {
    import c.universe._
    tpe.companion <:< typeOf[EmbeddableType[_]]
  }

  def isIterable[C <: blackbox.Context](c: C)(tpe: C#Type): Boolean = {
    import c.universe._
    tpe <:< typeOf[Iterable[_]]
  }

  def isOption[C <: blackbox.Context](c: C)(tpe: C#Type): Boolean = {
    import c.universe._
    tpe <:< typeOf[Option[_]]
  }

  def isMap[C <: blackbox.Context](c: C)(tpe: C#Type): Boolean = {
    import c.universe._
    tpe =:= typeOf[Map[String, Any]] || tpe =:= typeOf[Map[String, AnyRef]] || tpe =:= typeOf[Map[String, Object]]
  }

  def isSeq[C <: blackbox.Context](c: C)(tpe: C#Type): Boolean = {
    import c.universe._
    val arrayBufferType: C#Type = weakTypeOf[ArrayBuffer[_]].erasure
    tpe <:< typeOf[Seq[_]] && arrayBufferType <:< tpe.erasure
  }

  def isFunction[C <: blackbox.Context](c: C)(tpe: C#Type): Boolean = {
    import c.universe._
    !(tpe =:= typeOf[Nothing]) && (
      tpe <:< typeOf[() => _] ||
      tpe <:< typeOf[(_) => _] ||
      tpe <:< typeOf[(_, _) => _] ||
      tpe <:< typeOf[(_, _, _) => _] ||
      tpe <:< typeOf[(_, _, _, _) => _] ||
      tpe <:< typeOf[(_, _, _, _, _) => _] ||
      tpe <:< typeOf[(_, _, _, _, _, _) => _] ||
      tpe <:< typeOf[(_, _, _, _, _, _, _) => _] ||
      tpe <:< typeOf[(_, _, _, _, _, _, _, _) => _] ||
      tpe <:< typeOf[(_, _, _, _, _, _, _, _, _) => _] ||
      tpe <:< typeOf[(_, _, _, _, _, _, _, _, _, _) =>_] ||
      tpe <:< typeOf[(_, _, _, _, _, _, _, _, _, _, _) => _] ||
      tpe <:< typeOf[(_, _, _, _, _, _, _, _, _, _, _, _) => _] ||
      tpe <:< typeOf[(_, _, _, _, _, _, _, _, _, _, _, _, _) => _] ||
      tpe <:< typeOf[(_, _, _, _, _, _, _, _, _, _, _, _, _, _) => _] ||
      tpe <:< typeOf[(_, _, _, _, _, _, _, _, _, _, _, _, _, _, _) => _] ||
      tpe <:< typeOf[(_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _) => _] ||
      tpe <:< typeOf[(_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _) => _] ||
      tpe <:< typeOf[(_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _) => _] ||
      tpe <:< typeOf[(_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _) => _] ||
      tpe <:< typeOf[(_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _) => _] ||
      tpe <:< typeOf[(_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _) => _] ||
      tpe <:< typeOf[(_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _) => _] ||
      tpe <:< typeOf[PartialFunction[_, _]])
  }

  def box [C <: blackbox.Context](c: C)(tpe: c.universe.Type): c.universe.Type = {
    import c.universe._
    if(tpe =:= typeOf[Boolean]) typeOf[java.lang.Boolean]
    else if(tpe =:= typeOf[Byte]) typeOf[java.lang.Byte]
    else if(tpe =:= typeOf[Short]) typeOf[java.lang.Short]
    else if(tpe =:= typeOf[Int]) typeOf[java.lang.Integer]
    else if(tpe =:= typeOf[Long]) typeOf[java.lang.Long]
    else if(tpe =:= typeOf[Float]) typeOf[java.lang.Float]
    else if(tpe =:= typeOf[Double]) typeOf[java.lang.Double]
    else tpe
  }

  def generateWrapperSupplier[C <: blackbox.Context, T: c.WeakTypeTag](c: C)(tpe: c.universe.Type): c.universe.Expr[() => Wrapper[_]] = {
    import c.universe._
    if(tpe =:= typeOf[String]) reify {
        () => new StringWrapper(): Wrapper[String]
    }
    else if(tpe =:= typeOf[Boolean] || tpe =:= typeOf[java.lang.Boolean]) reify {
      () => new BooleanWrapper(): Wrapper[java.lang.Boolean]
    }
    else if(tpe =:= typeOf[Byte] || tpe =:= typeOf[java.lang.Byte]) reify {
      () => new ByteWrapper(): Wrapper[java.lang.Byte]
    }
    else if(tpe =:= typeOf[Short] || tpe =:= typeOf[java.lang.Short]) reify {
      () => new ShortWrapper(): Wrapper[java.lang.Short]
    }
    else if(tpe =:= typeOf[Int] || tpe =:= typeOf[Integer]) reify {
      () => new IntegerWrapper(): Wrapper[Integer]
    }
    else if(tpe =:= typeOf[Long] || tpe =:= typeOf[java.lang.Long]) reify {
      () => new LongWrapper(): Wrapper[java.lang.Long]
    }
    else if(tpe =:= typeOf[Float] || tpe =:= typeOf[java.lang.Float]) reify {
      () => new FloatWrapper(): Wrapper[java.lang.Float]
    }
    else if(tpe =:= typeOf[Double] || tpe =:= typeOf[java.lang.Double]) reify {
      () => new DoubleWrapper(): Wrapper[java.lang.Double]
    }
    else if(tpe =:= typeOf[Object] || tpe =:= typeOf[AnyRef] || tpe =:= typeOf[Any]) reify {
      () => new ObjectWrapper(): Wrapper[Object]
    }
    else if(tpe =:= typeOf[BigDecimal]) reify {
      () => new domala.wrapper.BigDecimalWrapper(): Wrapper[BigDecimal]
    }
    else if(tpe =:= typeOf[BigInteger] || tpe =:= typeOf[BigInt]) reify {
      () => new BigIntWrapper(): Wrapper[BigInt]
    }
    else if(tpe =:= typeOf[Time]) reify {
      () => new TimeWrapper(): Wrapper[java.sql.Time]
    }
    else if(tpe =:= typeOf[Timestamp]) reify {
      () => new TimestampWrapper(): Wrapper[java.sql.Timestamp]
    }
    else if(tpe =:= typeOf[java.sql.Date]) reify {
      () => new DateWrapper(): Wrapper[java.sql.Date]
    }
    else if(tpe =:= typeOf[java.util.Date]) reify {
      () => new UtilDateWrapper(): Wrapper[java.util.Date]
    }
    else if(tpe =:= typeOf[LocalTime]) reify {
      () => new LocalTimeWrapper(): Wrapper[java.time.LocalTime]
    }
    else if(tpe =:= typeOf[LocalDateTime]) reify {
      () => new LocalDateTimeWrapper(): Wrapper[java.time.LocalDateTime]
    }
    else if(tpe =:= typeOf[LocalDate]) reify {
      () => new LocalDateWrapper(): Wrapper[java.time.LocalDate]
    }
    else if(tpe =:= typeOf[Array[Byte]]) reify {
      () => new BytesWrapper(): (Wrapper[Array[Byte]])
    }
    else if(tpe =:= typeOf[Blob]) reify {
      () => new BlobWrapper(): Wrapper[java.sql.Blob]
    }
    else if(tpe =:= typeOf[NClob]) reify {
      () => new NClobWrapper(): Wrapper[java.sql.NClob]
    }
    else if(tpe =:= typeOf[Clob]) reify {
      () => new ClobWrapper(): Wrapper[java.sql.Clob]
    }
    else if(tpe =:= typeOf[SQLXML]) reify {
      () => new SQLXMLWrapper(): Wrapper[java.sql.SQLXML]
    } else {
      c.abort(c.enclosingPosition, "error")
    }
  }

  def generateImport[C <: blackbox.Context, T: c.WeakTypeTag](c: C)(tpe: c.universe.Type): Option[c.universe.Import ] = {
    import c.universe._
    val fullName = tpe.typeSymbol.fullName.split('.').toList
    if(fullName.length <= 1) None
    else {
      def getOwner(s: Symbol): Symbol = {
        if (s.isPackage) s
        else getOwner(s.owner)
      }
      val owner = getOwner(c.internal.enclosingOwner)
      val ownerPackage = owner.fullName.split('.').toList
      val className = TermName(fullName.last)
      val packageNameList = fullName.init
      if(packageNameList.take(ownerPackage.length) == ownerPackage) None
      else {
        val packageNameIterator = packageNameList.toIterator
        val top: Tree = Ident(TermName(packageNameIterator.next))
        val packageSelect = packageNameIterator.foldLeft(top)((acc, name) => Select(acc, TermName(name)))
        Some(
          Import(packageSelect, List(ImportSelector(className, -1, className, -1)))
        )
      }
    }
  }

  def newAnyValHolderDesc[C <: blackbox.Context, T: c.WeakTypeTag](c: C)(tpe: c.universe.Type): (c.universe.Type, Option[c.Expr[AbstractAnyValHolderDesc[Any, T]]]) = {
    import c.universe._
    val valueType = tpe.members.find(_.isConstructor).get.asMethod.paramLists.flatten.head.typeSignature
    val basicType = box(c)(valueType)
    if(!isBasic(c)(basicType)) return (basicType, None)
    val holder: c.Expr[AbstractAnyValHolderDesc[Any, T]] = {
      val holderTypeName = tpe.typeSymbol.name.toTypeName
      val basicTypeName = basicType.typeSymbol.name.toTypeName
      val holderConstructor = tpe.members.find(_.isConstructor).get.asMethod
      val useApply =
        if (holderConstructor.isPublic) false
        else {
          val applyMethod = tpe.companion.member(TermName("apply"))
          if (applyMethod.typeSignature =:= NoType || !applyMethod.isPublic) {
            return (basicType, None)
          } else true
        }
      val holderValueName  = TermName(holderConstructor.paramLists.flatten.head.name.toString)
      val basicImport = generateImport(c)(basicType)
      val holderImport = generateImport(c)(tpe)
      c.Expr[AbstractAnyValHolderDesc[Any, T]](
        if (tpe.typeArgs.isEmpty) {
          val holderFactory =
            if(useApply)  q"${tpe.typeSymbol.name.toTermName}.apply (value)"
            else q"new $holderTypeName (value)"
          q""" {
            ${basicImport.getOrElse(q"()")}
            ${holderImport.getOrElse(q"()")}
            new domala.jdbc.holder.AbstractAnyValHolderDesc[$basicTypeName, $holderTypeName](${generateWrapperSupplier(c)(basicType)}) {
              override def newHolder(value: $basicTypeName): $holderTypeName = $holderFactory
              override def getBasicValue(holder: $holderTypeName) = holder.$holderValueName
            }
          } """
        } else {
          val holderFactory =
            if(useApply)  q"${tpe.typeSymbol.name.toTermName}.apply [..${tpe.typeArgs}] (value)"
            else q"new $holderTypeName [..${tpe.typeArgs}] (value)"

          q""" {
            ${basicImport.getOrElse(q"()")}
            ${holderImport.getOrElse(q"()")}
            new domala.jdbc.holder.AbstractAnyValHolderDesc[$basicTypeName, $holderTypeName [..${tpe.typeArgs}]](${generateWrapperSupplier(c)(basicType)}) {
              override def newHolder(value: $basicTypeName): $holderTypeName [..${tpe.typeArgs}] = $holderFactory
              override def getBasicValue(holder: $holderTypeName [..${tpe.typeArgs}]) = holder.$holderValueName
            }
          } """
        }
      )
    }
    (basicType, Some(holder))
  }
}
