package domala.internal.macros.reflect.util

import domala.jdbc.holder.HolderDesc
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
      val basicImport = MacroUtil.generateImport(c)(basicType).getOrElse(q"()")
      val holderImport = MacroUtil.generateImport(c)(tpe).getOrElse(q"()")
      c.Expr[HolderDesc[Any, T]](
        if (tpe.typeArgs.isEmpty) {
          val holderFactory =
            if(useApply)  q"${tpe.typeSymbol.name.toTermName}.apply (value)"
            else q"new $holderTypeName (value)"
          val newInstanceSupplier = q"""{
            new domala.jdbc.holder.AbstractAnyValHolderDesc[$basicTypeName, $holderTypeName](${MacroUtil.generateWrapperSupplier(c)(basicType)}) {
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
            new domala.jdbc.holder.AbstractAnyValHolderDesc[$basicTypeName, $holderTypeName [..${tpe.typeArgs}]](${MacroUtil.generateWrapperSupplier(c)(basicType)}) {
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
