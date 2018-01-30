package domala.internal.macros.reflect

import java.util.Optional

import domala.internal.macros.reflect.mock.{MockEmbeddable, MockEntity, MockHolder, MockNestEntity}
import domala.jdbc.entity.{NamingType, Property}
import org.scalatest.FunSuite
import org.seasar.doma.jdbc.InParameter
import org.seasar.doma.wrapper.Wrapper

class EmbeddableReflectionMacrosTestSuite extends FunSuite {
  test("generatePropertyDescMap") {
    val map = EmbeddableReflectionMacros.generatePropertyDescMap(classOf[MockEmbeddable], classOf[MockEntity], "embedded", NamingType.NONE)
    val value1 = map("embedded.value1")
    val value2 = map("embedded.value2")
    assert(value1.getColumnName() == "value1")
    assert(value2.getColumnName() == "value2")
  }

  test("generateEmbeddableDesc") {
    val desc = EmbeddableReflectionMacros.generateEmbeddableDesc(classOf[MockEmbeddable])
    val newEmbeddable = desc.newEmbeddable[MockEntity]("embedded", Map(
        "embedded.value1" -> new Property[MockEntity, Int]() {
          override def get(): AnyRef = new Integer(1)
          override def save(entity: MockEntity): Property[MockEntity, Int] = ???
          override def load(entity: MockEntity): Property[MockEntity, Int] = ???
          override def asInParameter(): InParameter[Int] = ???
          override def getWrapper: Wrapper[Int] = ???
          override def getDomainClass: Optional[Class[_]] = ???
        },
        "embedded.value2" -> new Property[MockEntity, String]() {
          override def get(): AnyRef = "foo"
          override def save(entity: MockEntity): Property[MockEntity, String] = ???
          override def load(entity: MockEntity): Property[MockEntity, String] = ???
          override def asInParameter(): InParameter[String] = ???
          override def getWrapper: Wrapper[String] = ???
          override def getDomainClass: Optional[Class[_]] = ???
        }),
      classOf[MockEntity])
    assert(newEmbeddable == MockEmbeddable(1, "foo"))
  }

  test("generateEmbeddableDesc with nested") {
    val desc = EmbeddableReflectionMacros.generateEmbeddableDesc(classOf[mock.MockNestEmbeddable])
    val newEmbeddable = desc.newEmbeddable[MockNestEntity]("embedded",
      Map(
        "embedded.nest.value1" -> new Property[MockNestEntity, Int]() {
          override def get(): AnyRef = new Integer(1)
          override def save(entity: MockNestEntity): Property[MockNestEntity, Int] = ???
          override def load(entity: MockNestEntity): Property[MockNestEntity, Int] = ???
          override def asInParameter(): InParameter[Int] = ???
          override def getWrapper: Wrapper[Int] = ???
          override def getDomainClass: Optional[Class[_]] = ???
        },
        "embedded.nest.value2" -> new Property[MockNestEntity, String]() {
          override def get(): AnyRef = "foo"
          override def save(entity: MockNestEntity): Property[MockNestEntity, String] = ???
          override def load(entity: MockNestEntity): Property[MockNestEntity, String] = ???
          override def asInParameter(): InParameter[String] = ???
          override def getWrapper: Wrapper[String] = ???
          override def getDomainClass: Optional[Class[_]] = ???
        },
        "embedded.value3" -> new Property[MockNestEntity, Double]() {
          override def get(): AnyRef = Some(123.456)
          override def save(entity: MockNestEntity): Property[MockNestEntity, Double] = ???
          override def load(entity: MockNestEntity): Property[MockNestEntity, Double] = ???
          override def asInParameter(): InParameter[Double] = ???
          override def getWrapper: Wrapper[Double] = ???
          override def getDomainClass: Optional[Class[_]] = ???
        },
        "embedded.value4" -> new Property[MockNestEntity, Double]() {
          override def get(): AnyRef = Some(mock.MockHolder("baz"))
          override def save(entity: MockNestEntity): Property[MockNestEntity, Double] = ???
          override def load(entity: MockNestEntity): Property[MockNestEntity, Double] = ???
          override def asInParameter(): InParameter[Double] = ???
          override def getWrapper: Wrapper[Double] = ???
          override def getDomainClass: Optional[Class[_]] = ???
        }
      ),
      classOf[MockNestEntity]
    )
    assert(newEmbeddable == mock.MockNestEmbeddable(MockEmbeddable(1, "foo"), Some(123.456), Some(MockHolder("baz"))))
  }

}
