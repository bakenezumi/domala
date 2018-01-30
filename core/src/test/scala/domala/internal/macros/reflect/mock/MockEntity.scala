package domala.internal.macros.reflect.mock

import domala.internal.jdbc.entity.RuntimeEntityDesc
import domala.jdbc.entity.EntityDesc
import domala.{GeneratedValue, GenerationType, Id, Version}

case class MockEntity(
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) id: Int,
  domain: MockHolder,
  basic: String,
  embedded: MockEmbeddable,
  @Version version: Int
)

object MockEntity{
  val entityDesc: EntityDesc[MockEntity] = RuntimeEntityDesc.of[MockEntity]
}
