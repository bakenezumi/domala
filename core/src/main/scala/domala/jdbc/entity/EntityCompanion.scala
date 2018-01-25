package domala.jdbc.entity

import org.seasar.doma.jdbc.entity.EntityType

trait EntityCompanion[ENTITY] {
  val entityDesc: EntityType[ENTITY]
}
