package domala.internal.macros.reflect.util

import domala.jdbc.entity.EntityDesc

import scala.reflect.macros.blackbox

object RuntimeEntityDescGenerator {

  def get[C <: blackbox.Context, T: c.WeakTypeTag](c: C)(tpe: c.universe.Type): Option[c.Expr[EntityDesc[T]]] = {
    import c.universe._
    val entityDesc: c.Expr[EntityDesc[T]] = {
      val entityTypeName = tpe.typeSymbol.name.toTypeName
      c.Expr[EntityDesc[T]](
         q"""{
            object Desc extends domala.internal.jdbc.entity.RuntimeEntityDesc[$entityTypeName]
            Desc
          }
          """
      )
    }
    Some(entityDesc)
  }

}
