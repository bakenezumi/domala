package domala.internal.macros.reflect

import domala._
import domala.internal.jdbc.entity.RuntimeEntityDesc
import domala.jdbc.entity._
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.seasar.doma.wrapper.{IntegerWrapper, StringWrapper, Wrapper}

//noinspection NameBooleanParameters
class EntityReflectionMacrosTestSuite extends FunSuite with BeforeAndAfter {

  test("generatePropertyType basic") {
    val propertyType: Map[String, EntityPropertyDesc[DummyEntity, _]] = EntityReflectionMacros.generatePropertyDesc[String, DummyEntity, String](
      classOf[DummyEntity],
      "basic",
      NamingType.NONE,
      false,
      false,
      null,
      false,
      false,
      true,
      () => new StringWrapper(): Wrapper[String],
      Column(
        "",
        true,
        true,
        false)
    )
    assert(
      propertyType.values.head
        .isInstanceOf[DefaultPropertyDesc[_, _, _, _]])
  }

  test("generatePropertyType id") {
    val __idGenerator = new org.seasar.doma.jdbc.id.BuiltinIdentityIdGenerator()
    val propertyType = EntityReflectionMacros.generatePropertyDesc[Int, DummyEntity, Integer](
      classOf[DummyEntity],
      "id",
      NamingType.NONE,
      true,
      true,
      __idGenerator,
      false,
      false,
      true,
      () => new IntegerWrapper(): Wrapper[Integer],
      Column("",
        true,
        true,
        false)
    )
    assert(propertyType.values.head.isInstanceOf[GeneratedIdPropertyDesc[_, _, _, _]])
  }

  test("generatePropertyType version") {
    val propertyType = EntityReflectionMacros.generatePropertyDesc[Int, DummyEntity, Integer](
      classOf[DummyEntity],
      "version",
      NamingType.NONE,
      false,
      false,
      null,
      true,
      false,
      true,
      () => new IntegerWrapper(): Wrapper[Integer],
      Column("",
        true,
        true,
        false)
    )
    assert(propertyType.values.head.isInstanceOf[VersionPropertyDesc[_, _, _, _]])
  }

  test("generatePropertyType domain") {
    val propertyType = EntityReflectionMacros.generatePropertyDesc[DummyHolder, DummyEntity, DummyHolder](
      classOf[DummyEntity],
      "domain",
      NamingType.NONE,
      false,
      false,
      null,
      false,
      false,
      false,
      null,
      Column("",
        true,
        true,
        false)
    )
    assert(propertyType.values.head.isInstanceOf[DefaultPropertyDesc[_, _, _, _]])
  }

}

case class DummyEntity(
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) id: Int,
  domain: DummyHolder,
  basic: String,
  @Version version: Int
)
object DummyEntity{
  val entityDesc: EntityDesc[DummyEntity] = RuntimeEntityDesc.of[DummyEntity]
}

case class DummyHolder(value: String) extends AnyVal

case class DummyEmbeddable(value1: Int, value2: String)
