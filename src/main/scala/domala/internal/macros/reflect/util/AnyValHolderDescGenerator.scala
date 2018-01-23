package domala.internal.macros.reflect.util

import java.util.function.Supplier

import domala.jdbc.`type`.Types
import domala.jdbc.holder.HolderDesc
import domala.wrapper._

import scala.reflect.macros.blackbox

object AnyValHolderDescGenerator {

  private def box[C <: blackbox.Context](c: C)(tpe: c.universe.Type): c.universe.Type = {
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

  private def generateWrapperSupplier[C <: blackbox.Context, T: c.WeakTypeTag](c: C)(tpe: c.universe.Type): c.universe.Expr[Supplier[_ <: Wrapper[_]]] = {
    import c.universe._
    MacroTypeConverter.of(c).toType(tpe) match {
      case Types.BigDecimalType => reify { Types.BigDecimalType.wrapperSupplier }
      case Types.JavaBigDecimalType => reify { Types.JavaBigDecimalType.wrapperSupplier }
      case Types.BigIntType => reify { Types.BigIntType.wrapperSupplier }
      case Types.BigIntegerType => reify { Types.BigIntegerType.wrapperSupplier }
      case Types.IntType => reify { Types.IntType.wrapperSupplier }
      case Types.AnyType => reify { Types.AnyType.wrapperSupplier }
      case Types.BytesType => reify { Types.BytesType.wrapperSupplier }
      case Types.LongType => reify { Types.LongType.wrapperSupplier }
      case Types.DoubleType => reify { Types.DoubleType.wrapperSupplier }
      case Types.BooleanType => reify { Types.BooleanType.wrapperSupplier }
      case Types.ByteType => reify { Types.ByteType.wrapperSupplier }
      case Types.ShortType => reify { Types.ShortType.wrapperSupplier }
      case Types.FloatType => reify { Types.FloatType.wrapperSupplier }
      case Types.StringType => reify { Types.StringType.wrapperSupplier }
      case Types.LocalDateType => reify { Types.LocalDateType.wrapperSupplier }
      case Types.LocalTimeType => reify { Types.LocalTimeType.wrapperSupplier }
      case Types.LocalDateTimeType => reify { Types.LocalDateTimeType.wrapperSupplier }
      case Types.DateType => reify { Types.DateType.wrapperSupplier }
      case Types.UtilDateType => reify { Types.UtilDateType.wrapperSupplier }
      case Types.TimeType => reify { Types.TimeType.wrapperSupplier }
      case Types.TimestampType => reify { Types.TimestampType.wrapperSupplier }
      case Types.BlobType => reify { Types.BlobType.wrapperSupplier }
      case Types.ClobType => reify { Types.ClobType.wrapperSupplier }
      case Types.NClobType => reify { Types.NClobType.wrapperSupplier }
      case Types.SQLXMLType => reify { Types.SQLXMLType.wrapperSupplier }
      case _ => c.abort(c.enclosingPosition, "error")
    }
  }

  private def generateImport[C <: blackbox.Context, T: c.WeakTypeTag](c: C)(tpe: c.universe.Type): Option[c.universe.Import ] = {
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

  def get[C <: blackbox.Context, T: c.WeakTypeTag](c: C)(tpe: c.universe.Type): Option[c.Expr[HolderDesc[Any, T]]] = {
    import c.universe._
    val valueType = tpe.members.find(_.isConstructor).get.asMethod.paramLists.flatten.head.typeSignature
    val basicType = box(c)(valueType)
    val holderDesc: c.Expr[HolderDesc[Any, T]] = {
      val holderTypeName = tpe.typeSymbol.name.toTypeName
      val basicTypeName = basicType.typeSymbol.name.toTypeName
      val holderConstructor = tpe.members.find(_.isConstructor).get.asMethod
      val useApply =
        if (holderConstructor.isPublic) false
        else {
          val applyMethod = tpe.companion.member(TermName("apply"))
          if (applyMethod.typeSignature =:= NoType || !applyMethod.isPublic) {
            return None
          } else true
        }
      val holderValueName  = TermName(holderConstructor.paramLists.flatten.head.name.toString)
      val basicImport = generateImport(c)(basicType).getOrElse(q"()")
      val holderImport = generateImport(c)(tpe).getOrElse(q"()")
      c.Expr[HolderDesc[Any, T]](
        if (tpe.typeArgs.isEmpty) {
          val holderFactory =
            if(useApply)  q"${tpe.typeSymbol.name.toTermName}.apply (value)"
            else q"new $holderTypeName (value)"
          val newInstanceSupplier = q"""{
            new domala.jdbc.holder.AbstractAnyValHolderDesc[$basicTypeName, $holderTypeName](${generateWrapperSupplier(c)(basicType)}) {
              override def newHolder(value: $basicTypeName): $holderTypeName = $holderFactory
              override def getBasicValue(holder: $holderTypeName) = holder.$holderValueName
            }: domala.jdbc.holder.HolderDesc[$basicTypeName, $holderTypeName [..${tpe.typeArgs}]]
          }"""
          q"""{
            $basicImport
            $holderImport
            domala.internal.jdbc.holder.AnyValHolderDescRepository.getByType[$basicTypeName, $holderTypeName](classOf[$holderTypeName], $newInstanceSupplier)
          }
          """
        } else {
          val holderFactory =
            if(useApply)  q"${tpe.typeSymbol.name.toTermName}.apply [..${tpe.typeArgs}] (value)"
            else q"new $holderTypeName [..${tpe.typeArgs}] (value)"

          val newInstanceSupplier = q"""{
            new domala.jdbc.holder.AbstractAnyValHolderDesc[$basicTypeName, $holderTypeName [..${tpe.typeArgs}]](${generateWrapperSupplier(c)(basicType)}) {
              override def newHolder(value: $basicTypeName): $holderTypeName [..${tpe.typeArgs}] = $holderFactory
              override def getBasicValue(holder: $holderTypeName [..${tpe.typeArgs}]) = holder.$holderValueName
            }: domala.jdbc.holder.HolderDesc[$basicTypeName, $holderTypeName [..${tpe.typeArgs}]]
          }"""
          q"""{
            $basicImport
            $holderImport
            domala.internal.jdbc.holder.AnyValHolderDescRepository.getByType[$basicTypeName, $holderTypeName [..${tpe.typeArgs}]](classOf[$holderTypeName[..${tpe.typeArgs}]], $newInstanceSupplier)
          }"""
        }
      )
    }
    Some(holderDesc)
  }
}
