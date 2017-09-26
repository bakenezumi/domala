package domala.jdbc.query

import org.seasar.doma.jdbc.entity.EntityType

case class EntityAndEntityType[E](name: String, entity: E, entityType: EntityType[E])
