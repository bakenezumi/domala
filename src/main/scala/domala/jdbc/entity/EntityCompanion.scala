package domala.jdbc.entity

trait EntityCompanion[ENTITY] {
  val entityDesc: EntityDesc[ENTITY]
}
