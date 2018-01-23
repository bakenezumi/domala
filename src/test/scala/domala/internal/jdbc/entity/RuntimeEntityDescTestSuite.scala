package domala.internal.jdbc.entity

import java.time.LocalDateTime

import domala.Table
import domala.jdbc.entity.EntityDesc
import org.scalatest.FunSuite

class RuntimeEntityDescTestSuite extends FunSuite{
  test("Basic") {
    assert(BasicEntity.entityDesc.getEntityPropertyTypes.size == 3)
    val p0 = BasicEntity.entityDesc.getEntityPropertyTypes.get(0)
    val p1 = BasicEntity.entityDesc.getEntityPropertyTypes.get(1)
    val p2 = BasicEntity.entityDesc.getEntityPropertyTypes.get(2)
    assert(p0 == BasicEntity.entityDesc.getEntityPropertyType("id"))
    assert(p1 == BasicEntity.entityDesc.getEntityPropertyType("name"))
    assert(p2 == BasicEntity.entityDesc.getEntityPropertyType("time"))
  }

  test("Option Basic") {
    assert(OptionBasicEntity.entityDesc.getEntityPropertyTypes.size == 3)
    val p0 = OptionBasicEntity.entityDesc.getEntityPropertyTypes.get(0)
    val p1 = OptionBasicEntity.entityDesc.getEntityPropertyTypes.get(1)
    val p2 = OptionBasicEntity.entityDesc.getEntityPropertyTypes.get(2)
    assert(p0 == OptionBasicEntity.entityDesc.getEntityPropertyType("id"))
    assert(p1 == OptionBasicEntity.entityDesc.getEntityPropertyType("name"))
    assert(p2 == OptionBasicEntity.entityDesc.getEntityPropertyType("time"))
  }

  test("AnyVal") {
    assert(AnyValHolderEntity.entityDesc.getEntityPropertyTypes.size == 3)
    val p0 = AnyValHolderEntity.entityDesc.getEntityPropertyTypes.get(0)
    val p1 = AnyValHolderEntity.entityDesc.getEntityPropertyTypes.get(1)
    val p2 = AnyValHolderEntity.entityDesc.getEntityPropertyTypes.get(2)
    assert(p0 == AnyValHolderEntity.entityDesc.getEntityPropertyType("id"))
    assert(p1 == AnyValHolderEntity.entityDesc.getEntityPropertyType("name"))
    assert(p2 == AnyValHolderEntity.entityDesc.getEntityPropertyType("time"))
  }

  test("Option AnyVal") {
    assert(OptionAnyValHolderEntity.entityDesc.getEntityPropertyTypes.size == 3)
    val p0 = OptionAnyValHolderEntity.entityDesc.getEntityPropertyTypes.get(0)
    val p1 = OptionAnyValHolderEntity.entityDesc.getEntityPropertyTypes.get(1)
    val p2 = OptionAnyValHolderEntity.entityDesc.getEntityPropertyTypes.get(2)
    assert(p0 == OptionAnyValHolderEntity.entityDesc.getEntityPropertyType("id"))
    assert(p1 == OptionAnyValHolderEntity.entityDesc.getEntityPropertyType("name"))
    assert(p2 == OptionAnyValHolderEntity.entityDesc.getEntityPropertyType("time"))
  }

}

case class BasicEntity(
  id: Int,
  name: String,
  time: LocalDateTime
)

object BasicEntity {
  val entityDesc: EntityDesc[BasicEntity] = RuntimeEntityDesc.of[BasicEntity]
}

case class OptionBasicEntity(
  id: Option[Int],
  name: Option[String],
  time: Option[LocalDateTime]
)

object OptionBasicEntity {
  val entityDesc: EntityDesc[OptionBasicEntity] = RuntimeEntityDesc.of[OptionBasicEntity]
}

case class IDAnyVal[T](value: Int) extends AnyVal
case class NameAnyVal(value: String) extends AnyVal
case class TimeAnyVal(value: LocalDateTime) extends AnyVal

case class AnyValHolderEntity(
  id: IDAnyVal[AnyValHolderEntity],
  name: NameAnyVal,
  time: TimeAnyVal
)

object AnyValHolderEntity {
  val entityDesc: EntityDesc[AnyValHolderEntity] = RuntimeEntityDesc.of[AnyValHolderEntity]
}

case class OptionAnyValHolderEntity(
  id: Option[IDAnyVal[OptionAnyValHolderEntity]],
  name: Option[NameAnyVal],
  time: Option[TimeAnyVal]
)

object OptionAnyValHolderEntity {
  val entityDesc: EntityDesc[OptionAnyValHolderEntity] = RuntimeEntityDesc.of[OptionAnyValHolderEntity]
}