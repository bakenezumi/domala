package domala.internal.macros.reflect.mock

import domala.internal.macros.reflect.EntityReflectionMacros
import domala.jdbc.entity.EntityDesc
import domala.{GeneratedValue, GenerationType, Id, Version}

case class MockEntity(
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  id: Int,
  holder: MockHolder,
  basic: String,
  embedded: MockEmbeddable,
  @Version version: Int
)

object MockEntity{
  val entityDesc: EntityDesc[MockEntity] = EntityReflectionMacros.generateEntityDesc[MockEntity]
}
