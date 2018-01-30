package domala.internal.macros.reflect.util

import domala.jdbc.entity.EntityDesc

import scala.reflect.macros.blackbox

object RuntimeEntityDescGenerator {

  def get[C <: blackbox.Context, T: c.WeakTypeTag](c: C)(tpe: c.universe.Type): c.Expr[EntityDesc[T]] = {
    import c.universe._
    c.Expr[EntityDesc[T]] {
      val entityTypeName = tpe.typeSymbol.name.toTypeName
      q"""
      {
        domala.internal.macros.reflect.EntityReflectionMacros.generateEntityDesc[$entityTypeName]
      }
      """
    }
  }

}
