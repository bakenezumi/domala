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
            val Right(desc) = domala.jdbc.entity.RuntimeEntityDesc.of[$entityTypeName]
            desc
          }
          """
      )
    }
    Some(entityDesc)
  }

}
