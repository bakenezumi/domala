package domala.internal.macros.reflect.decl

import scala.language.experimental.macros
import scala.reflect.macros.blackbox
object TypeDeclarationTestDriver {
  def getBinaryName(clazz: Class[_]): String = macro getBinaryNameImpl
  def getBinaryNameImpl(c: blackbox.Context)(
    clazz: c.Expr[Class[_]]): c.Expr[String] = {
    import c.universe._
    val tpe: c.Type = clazz.tree.tpe match {
      case ConstantType(Constant(x: c.Type)) => x
      case x => x
    }
    val r = new TypeDeclaration[c.type](c)(tpe).getBinaryName
    c.Expr(q"$r")
  }

  def isBooleanType(clazz: Class[_]): Boolean = macro isBooleanTypeImpl
  def isBooleanTypeImpl(c: blackbox.Context)(
    clazz: c.Expr[Class[_]]): c.Expr[Boolean] = {
    import c.universe._
    val tpe: c.Type = clazz.tree.tpe match {
      case ConstantType(Constant(x: c.Type)) => x
      case x => x
    }
    val r = new TypeDeclaration[c.type](c)(tpe).isBooleanType
    c.Expr(q"$r")
  }

  def isNullType(clazz: Class[_]): Boolean = macro isNullTypeImpl
  def isNullTypeImpl(c: blackbox.Context)(
    clazz: c.Expr[Class[_]]): c.Expr[Boolean] = {
    import c.universe._
    val tpe: c.Type = clazz.tree.tpe match {
      case ConstantType(Constant(x: c.Type)) => x
      case x => x
    }
    val r = new TypeDeclaration[c.type](c)(tpe).isNullType
    c.Expr(q"$r")
  }

  def isSameType(clazz1: Class[_], clazz2: Class[_]): Boolean = macro isSameTypeImpl
  def isSameTypeImpl(c: blackbox.Context)(
    clazz1: c.Expr[Class[_]], clazz2: c.Expr[Class[_]]): c.Expr[Boolean] = {
    import c.universe._
    val tpe1: c.Type = clazz1.tree.tpe match {
      case ConstantType(Constant(x: c.Type)) => x
      case x => x
    }
    val tpe2: c.Type = clazz2.tree.tpe match {
      case ConstantType(Constant(x: c.Type)) => x
      case x => x
    }
    val r = new TypeDeclaration[c.type](c)(tpe1).isSameType(new TypeDeclaration[c.type](c)(tpe2))
    c.Expr(q"$r")
  }

}
