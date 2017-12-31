package domala.internal.macros.reflect

import domala._
import domala.jdbc.entity.{DefaultPropertyDesc, EntityPropertyDesc, GeneratedIdPropertyDesc, VersionPropertyDesc}
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.seasar.doma.jdbc.entity.NamingType
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
      "",
      true,
      true,
      false
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
      "",
      true,
      true,
      false
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
      "",
      true,
      true,
      false
    )
    assert(propertyType.values.head.isInstanceOf[VersionPropertyDesc[_, _, _, _]])
  }

  test("generatePropertyType domain") {
    val propertyType = EntityReflectionMacros.generatePropertyDesc[DummyDomain, DummyEntity, DummyDomain](
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
      "",
      true,
      true,
      false
    )
    assert(propertyType.values.head.isInstanceOf[DefaultPropertyDesc[_, _, _, _]])
  }

}

@Entity
case class DummyEntity(
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) id: Int,
  domain: DummyDomain,
  basic: String,
  @Version version: Int
)

@Holder
case class DummyDomain(value: String)

@Embeddable
case class DummyEmbeddable(value1: Int, value2: String)
