package domala.internal.macros.reflect

import domala.internal.macros.reflect.mock._
import domala.jdbc.entity.NamingType
import org.scalatest.FunSuite

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
        "embedded.value1" -> MockProperty.of[MockEntity, Int](new Integer(1)),
        "embedded.value2" -> MockProperty.of[MockEntity, String]("foo")),
      classOf[MockEntity])
    assert(newEmbeddable == MockEmbeddable(1, "foo"))
  }

  test("generateEmbeddableDesc with nested") {
    val desc = EmbeddableReflectionMacros.generateEmbeddableDesc(classOf[mock.MockNestEmbeddable])
    val newEmbeddable = desc.newEmbeddable[MockNestEntity]("embedded",
      Map(
        "embedded.nest.value1" -> MockProperty.of[MockNestEntity, Int](new Integer(1)),
        "embedded.nest.value2" -> MockProperty.of[MockNestEntity, String]("foo"),
        "embedded.value3" -> MockProperty.of[MockNestEntity, Double](Some(123.456)),
        "embedded.value4" -> MockProperty.of[MockNestEntity, String](Some(mock.MockHolder("baz")))
      ),
      classOf[MockNestEntity]
    )
    assert(newEmbeddable == mock.MockNestEmbeddable(MockEmbeddable(1, "foo"), Some(123.456), Some(MockHolder("baz"))))
  }

}
