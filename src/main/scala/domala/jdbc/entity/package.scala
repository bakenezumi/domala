package domala.jdbc

import org.seasar.doma.jdbc.entity.{EmbeddableType, EntityPropertyType, EntityType}

package object entity {
  // Alias of Doma type
  type EntityPropertyDesc[ENTITY, BASIC] = EntityPropertyType[ENTITY, BASIC]
  type EntityDesc[ENTITY] = EntityType[ENTITY]
  type NamingType = org.seasar.doma.jdbc.entity.NamingType
  type EmbeddableDesc[EMBEDDABLE] = EmbeddableType[EMBEDDABLE]
  type Property[ENTITY, BASIC] = org.seasar.doma.jdbc.entity.Property[ENTITY, BASIC]
}
