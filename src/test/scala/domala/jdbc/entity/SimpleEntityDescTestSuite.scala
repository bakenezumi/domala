package domala.jdbc.entity

import java.time.LocalDateTime

import org.scalatest.FunSuite

class RuntimeEntityDescTestSuite extends FunSuite{
  test("Basic") {
    assert(BasicEntity.getEntityPropertyTypes.size == 3)
    val p0 = BasicEntity.getEntityPropertyTypes.get(0)
    val p1 = BasicEntity.getEntityPropertyTypes.get(1)
    val p2 = BasicEntity.getEntityPropertyTypes.get(2)
    assert(p0 == BasicEntity.getEntityPropertyType("id"))
    assert(p1 == BasicEntity.getEntityPropertyType("name"))
    assert(p2 == BasicEntity.getEntityPropertyType("time"))
  }

  test("Option Basic") {
    assert(OptionBasicEntity.getEntityPropertyTypes.size == 3)
    val p0 = OptionBasicEntity.getEntityPropertyTypes.get(0)
    val p1 = OptionBasicEntity.getEntityPropertyTypes.get(1)
    val p2 = OptionBasicEntity.getEntityPropertyTypes.get(2)
    assert(p0 == OptionBasicEntity.getEntityPropertyType("id"))
    assert(p1 == OptionBasicEntity.getEntityPropertyType("name"))
    assert(p2 == OptionBasicEntity.getEntityPropertyType("time"))
  }

  test("AnyVal") {
    assert(AnyValHolderEntity.getEntityPropertyTypes.size == 3)
    val p0 = AnyValHolderEntity.getEntityPropertyTypes.get(0)
    val p1 = AnyValHolderEntity.getEntityPropertyTypes.get(1)
    val p2 = AnyValHolderEntity.getEntityPropertyTypes.get(2)
    assert(p0 == AnyValHolderEntity.getEntityPropertyType("id"))
    assert(p1 == AnyValHolderEntity.getEntityPropertyType("name"))
    assert(p2 == AnyValHolderEntity.getEntityPropertyType("time"))
  }

  test("Option AnyVal") {
    assert(OptionAnyValHolderEntity.getEntityPropertyTypes.size == 3)
    val p0 = OptionAnyValHolderEntity.getEntityPropertyTypes.get(0)
    val p1 = OptionAnyValHolderEntity.getEntityPropertyTypes.get(1)
    val p2 = OptionAnyValHolderEntity.getEntityPropertyTypes.get(2)
    assert(p0 == OptionAnyValHolderEntity.getEntityPropertyType("id"))
    assert(p1 == OptionAnyValHolderEntity.getEntityPropertyType("name"))
    assert(p2 == OptionAnyValHolderEntity.getEntityPropertyType("time"))
  }

}

case class BasicEntity(
  id: Int,
  name: String,
  time: LocalDateTime
)

object BasicEntity extends RuntimeEntityDesc[BasicEntity]

case class OptionBasicEntity(
  id: Option[Int],
  name: Option[String],
  time: Option[LocalDateTime]
)

object OptionBasicEntity extends RuntimeEntityDesc[OptionBasicEntity]

case class IDAnyVal[T](value: Int) extends AnyVal
case class NameAnyVal(value: String) extends AnyVal
case class TimeAnyVal(value: LocalDateTime) extends AnyVal

case class AnyValHolderEntity(
  id: IDAnyVal[AnyValHolderEntity],
  name: NameAnyVal,
  time: TimeAnyVal
)

object AnyValHolderEntity extends RuntimeEntityDesc[AnyValHolderEntity]

case class OptionAnyValHolderEntity(
  id: Option[IDAnyVal[OptionAnyValHolderEntity]],
  name: Option[NameAnyVal],
  time: Option[TimeAnyVal]
)

object OptionAnyValHolderEntity extends RuntimeEntityDesc[OptionAnyValHolderEntity]