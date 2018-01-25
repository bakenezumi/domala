package domala.jdbc.query

import domala.jdbc.entity.EntityDesc

case class EntityAndEntityDesc[E](name: String, entity: E, entityDesc: EntityDesc[E])
