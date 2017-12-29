package domala.jdbc.entity

trait EntityCompanion {
  type ENTITY
  val entityDesc: EntityDesc[ENTITY]
}
