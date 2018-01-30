package domala.internal.macros.reflect

import java.util.Optional

import domala._
import domala.internal.macros.reflect.mock.{MockEmbeddable, MockEntity, MockHolder}
import domala.jdbc.entity._
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.seasar.doma.jdbc.InParameter
import org.seasar.doma.wrapper.Wrapper

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
    val desc = EntityReflectionMacros.generateEntityDesc[MockEntity]
    val map = Map(
      "id" -> new Property[MockEntity, Int]() {
        override def get(): AnyRef = new Integer(1)
        override def save(entity: MockEntity): Property[MockEntity, Int] = ???
        override def load(entity: MockEntity): Property[MockEntity, Int] = ???
        override def asInParameter(): InParameter[Int] = ???
        override def getWrapper: Wrapper[Int] = ???
        override def getDomainClass: Optional[Class[_]] = ???
      }.asInstanceOf[Property[MockEntity, _]],
      "holder" -> new Property[MockEntity, MockHolder]() {
        override def get(): AnyRef = MockHolder("foo").asInstanceOf[AnyRef]
        override def save(entity: MockEntity): Property[MockEntity, MockHolder] = ???
        override def load(entity: MockEntity): Property[MockEntity, MockHolder] = ???
        override def asInParameter(): InParameter[MockHolder] = ???
        override def getWrapper: Wrapper[MockHolder] = ???
        override def getDomainClass: Optional[Class[_]] = ???
      }.asInstanceOf[Property[MockEntity, _]],
      "basic" -> new Property[MockEntity, String]() {
        override def get(): AnyRef = "baz"
        override def save(entity: MockEntity): Property[MockEntity, String] = ???
        override def load(entity: MockEntity): Property[MockEntity, String] = ???
        override def asInParameter(): InParameter[String] = ???
        override def getWrapper: Wrapper[String] = ???
        override def getDomainClass: Optional[Class[_]] = ???
      }.asInstanceOf[Property[MockEntity, _]],
      "embedded.value1" -> new Property[MockEntity, Int]() {
        override def get(): AnyRef = new Integer(2)
        override def save(entity: MockEntity): Property[MockEntity, Int] = ???
        override def load(entity: MockEntity): Property[MockEntity, Int] = ???
        override def asInParameter(): InParameter[Int] = ???
        override def getWrapper: Wrapper[Int] = ???
        override def getDomainClass: Optional[Class[_]] = ???
      }.asInstanceOf[Property[MockEntity, _]],
      "embedded.value2" -> new Property[MockEntity, String]() {
        override def get(): AnyRef = "bar"
        override def save(entity: MockEntity): Property[MockEntity, String] = ???
        override def load(entity: MockEntity): Property[MockEntity, String] = ???
        override def asInParameter(): InParameter[String] = ???
        override def getWrapper: Wrapper[String] = ???
        override def getDomainClass: Optional[Class[_]] = ???
      }.asInstanceOf[Property[MockEntity, _]],
      "version" -> new Property[MockEntity, Int]() {
        override def get(): AnyRef = new Integer(3)
        override def save(entity: MockEntity): Property[MockEntity, Int] = ???
        override def load(entity: MockEntity): Property[MockEntity, Int] = ???
        override def asInParameter(): InParameter[Int] = ???
        override def getWrapper: Wrapper[Int] = ???
        override def getDomainClass: Optional[Class[_]] = ???
      }.asInstanceOf[Property[MockEntity, _]]
    ).asJava.asInstanceOf[java.util.Map[String, Property[MockEntity, _]]]
    val newEntity = desc.newEntity(map)
    assert(newEntity == MockEntity(1, MockHolder("foo"), "baz", MockEmbeddable(2, "bar"), 3))
  }

}

