package domala.jdbc

import org.seasar.doma

package object entity {
  // Alias of Doma type
  type EntityDesc[ENTITY] = doma.jdbc.entity.EntityType[ENTITY]
  type EntityPropertyDesc[ENTITY, BASIC] = doma.jdbc.entity.EntityPropertyType[ENTITY, BASIC]
  type NamingType = doma.jdbc.entity.NamingType
  object NamingType {
    val NONE = doma.jdbc.entity.NamingType.NONE
    val SNAKE_UPPER_CASE = doma.jdbc.entity.NamingType.SNAKE_UPPER_CASE
    val SNAKE_LOWER_CASE = doma.jdbc.entity.NamingType.SNAKE_LOWER_CASE
    val LENIENT_SNAKE_UPPER_CASE = doma.jdbc.entity.NamingType.LENIENT_SNAKE_UPPER_CASE
    val LENIENT_SNAKE_LOWER_CASE = doma.jdbc.entity.NamingType.LENIENT_SNAKE_LOWER_CASE
    val UPPER_CASE = doma.jdbc.entity.NamingType.UPPER_CASE
    val LOWER_CASE = doma.jdbc.entity.NamingType.LOWER_CASE
  }
  type EmbeddableDesc[EMBEDDABLE] = doma.jdbc.entity.EmbeddableType[EMBEDDABLE]
  type EmbeddedPropertyDesc[ENTITY, EMBEDDABLE] = doma.jdbc.entity.EmbeddedPropertyType[ENTITY, EMBEDDABLE]

  type Property[ENTITY, BASIC] = doma.jdbc.entity.Property[ENTITY, BASIC]
  type EntityListener[ENTITY] = doma.jdbc.entity.EntityListener[ENTITY]
  type NullEntityListener[ENTITY] = doma.jdbc.entity.NullEntityListener[ENTITY]
}
