package domala.internal.macros.reflect

import domala._
import domala.internal.macros.reflect.mock.{MockEntity, MockHolder}
import domala.jdbc.entity._
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.seasar.doma.wrapper.{IntegerWrapper, StringWrapper, Wrapper}

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

  test("generatePropertyType domain") {
    val propertyType = EntityReflectionMacros.generatePropertyDesc[MockHolder, MockEntity, MockHolder](
      classOf[MockEntity],
      "domain",
      NamingType.NONE,
      false,
      false,
      null,
      false,
      false,
      Column("",
        true,
        true,
        false)
    )
    assert(propertyType.values.head.isInstanceOf[DefaultPropertyDesc[_, _, _, _]])
  }

}

