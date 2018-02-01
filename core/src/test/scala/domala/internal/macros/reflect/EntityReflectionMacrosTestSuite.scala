package domala.internal.macros.reflect

import domala._
import domala.internal.macros.reflect.mock.{MockEmbeddable, MockEntity, MockHolder, MockProperty}
import domala.jdbc.EntityDescProvider
import domala.jdbc.entity._
import org.scalatest.{BeforeAndAfter, FunSuite}

//noinspection NameBooleanParameters
class EntityReflectionMacrosTestSuite extends FunSuite with BeforeAndAfter {

  test("generatePropertyType basic") {
    val propertyType: Map[String, EntityPropertyDesc[MockEntity, _]] = EntityReflectionMacros.generatePropertyDesc[String, MockEntity, String](
      classOf[MockEntity],
      "basic",
      NamingType.NONE,
      false,
      false,
      null,
      false,
      false,
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
    val propertyType = EntityReflectionMacros.generatePropertyDesc[Int, MockEntity, Integer](
      classOf[MockEntity],
      "id",
      NamingType.NONE,
      true,
      true,
      __idGenerator,
      false,
      false,
      Column("",
        true,
        true,
        false)
    )
    assert(propertyType.values.head.isInstanceOf[GeneratedIdPropertyDesc[_, _, _, _]])
  }

  test("generatePropertyType version") {
    val propertyType = EntityReflectionMacros.generatePropertyDesc[Int, MockEntity, Integer](
      classOf[MockEntity],
      "version",
      NamingType.NONE,
      false,
      false,
      null,
      true,
      false,
      Column("",
        true,
        true,
        false)
    )
    assert(propertyType.values.head.isInstanceOf[VersionPropertyDesc[_, _, _, _]])
  }

  test("generateEntityDesc") {
    import scala.collection.JavaConverters._
    val desc = EntityDescProvider.get[MockEntity]
    val map = Map(
      "id" -> MockProperty.of[MockEntity, Int](new Integer(1)),
      "holder" -> MockProperty.of[MockEntity, MockHolder](MockHolder("foo").asInstanceOf[AnyRef]),
      "basic" -> MockProperty.of[MockEntity, String]("baz"),
      "embedded.value1" -> MockProperty.of[MockEntity, Int](new Integer(2)),
      "embedded.value2" -> MockProperty.of[MockEntity, String]("bar"),
      "version" -> MockProperty.of[MockEntity, Int](new Integer(3))
    ).asJava.asInstanceOf[java.util.Map[String, Property[MockEntity, _]]]
    val newEntity = desc.newEntity(map)
    assert(newEntity == MockEntity(1, MockHolder("foo"), "baz", MockEmbeddable(2, "bar"), 3))
  }

}

