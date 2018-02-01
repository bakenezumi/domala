package domala.jdbc

import domala.internal.macros.reflect.EntityReflectionMacros.generateEntityDescImpl
import domala.jdbc.entity.EntityDesc

import scala.language.experimental.macros

object EntityDescProvider {
  def get[E]: EntityDesc[E] = macro generateEntityDescImpl[E]
}
