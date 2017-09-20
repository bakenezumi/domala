package domala.internal.macros

import domala._
import domala.jdbc.entity.{
  DefaultPropertyType,
  GeneratedIdPropertyType,
  VersionPropertyType
}
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.seasar.doma.jdbc.entity.{
  EntityPropertyType,
  NamingType
}
import org.seasar.doma.wrapper.{IntegerWrapper, StringWrapper, Wrapper}

class EntityReflectionMacrosTestSuite extends FunSuite with BeforeAndAfter {

  test("generatePropertyType basic") {
    val __idList = new java.util.ArrayList[EntityPropertyType[DummyEntity, _]]
    val __list = new java.util.ArrayList[EntityPropertyType[DummyEntity, _]]
    val __map =
      new java.util.HashMap[String, EntityPropertyType[DummyEntity, _]]
    val propertyType = EntityReflectionMacros.generatePropertyType(
      classOf[String],
      classOf[DummyEntity],
      "basic",
      NamingType.NONE,
      false,
      null,
      false,
      classOf[String],
      () => new StringWrapper(): Wrapper[String],
      "",
      true,
      true,
      false,
      __list,
      __map,
      __idList
    )
    assert(
      propertyType
        .isInstanceOf[DefaultPropertyType[_, _, _, _]])
    assert(__idList.isEmpty)
    assert(__list.get(0) == propertyType)
    assert(__map.get("basic") == propertyType)
  }

  test("generatePropertyType id") {
    val __idList = new java.util.ArrayList[EntityPropertyType[DummyEntity, _]]
    val __list = new java.util.ArrayList[EntityPropertyType[DummyEntity, _]]
    val __map =
      new java.util.HashMap[String, EntityPropertyType[DummyEntity, _]]
    val __idGenerator = new org.seasar.doma.jdbc.id.BuiltinIdentityIdGenerator()
    val propertyType = EntityReflectionMacros.generatePropertyType(
      classOf[Int],
      classOf[DummyEntity],
      "id",
      NamingType.NONE,
      true,
      __idGenerator,
      false,
      classOf[String],
      () => new StringWrapper(): Wrapper[String],
      "",
      true,
      true,
      false,
      __list,
      __map,
      __idList
    )
    assert(propertyType.isInstanceOf[GeneratedIdPropertyType[_, _, _, _]])
    assert(__idList.get(0) == propertyType)
    assert(__list.get(0) == propertyType)
    assert(__map.get("id") == propertyType)
  }

  test("generatePropertyType version") {
    val __idList = new java.util.ArrayList[EntityPropertyType[DummyEntity, _]]
    val __list = new java.util.ArrayList[EntityPropertyType[DummyEntity, _]]
    val __map =
      new java.util.HashMap[String, EntityPropertyType[DummyEntity, _]]
    val propertyType = EntityReflectionMacros.generatePropertyType(
      classOf[Int],
      classOf[DummyEntity],
      "version",
      NamingType.NONE,
      false,
      null,
      true,
      classOf[Integer],
      () => new IntegerWrapper(): Wrapper[Integer],
      "",
      true,
      true,
      false,
      __list,
      __map,
      __idList
    )
    assert(propertyType.isInstanceOf[VersionPropertyType[_, _, _, _]])
    assert(__idList.isEmpty)
    assert(__list.get(0) == propertyType)
    assert(__map.get("version") == propertyType)
  }

  test("generatePropertyType domain") {
    val __idList = new java.util.ArrayList[EntityPropertyType[DummyEntity, _]]
    val __list = new java.util.ArrayList[EntityPropertyType[DummyEntity, _]]
    val __map =
      new java.util.HashMap[String, EntityPropertyType[DummyEntity, _]]
    val propertyType = EntityReflectionMacros.generatePropertyType(
      classOf[DummyDomain],
      classOf[DummyEntity],
      "domain",
      NamingType.NONE,
      false,
      null,
      false,
      classOf[DummyDomain],
      null,
      "",
      true,
      true,
      false,
      __list,
      __map,
      __idList
    )
    assert(propertyType.isInstanceOf[DefaultPropertyType[_, _, _, _]])
    assert(__idList.isEmpty)
    assert(__list.get(0) == propertyType)
    assert(__map.get("domain") == propertyType)
  }

}

@Entity
case class DummyEntity(
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) id: Int,
  domain: DummyDomain,
  basic: Option[String],
  @Version version: Int
)

@Domain
case class DummyDomain(value: String)



@Embeddable
case class DummyEmbeddable(value1: Int, value2: String)
