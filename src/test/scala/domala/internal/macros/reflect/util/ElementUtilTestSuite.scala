package domala.internal.macros.reflect.util
import org.scalatest.{BeforeAndAfter, FunSuite}

class ElementUtilTestSuite extends FunSuite with BeforeAndAfter {
  test("getTypeElement") {
    assert(ElementUtilTestDriver.getTypeElement("java.lang.String")== Some("java.lang.String"))
    assert(ElementUtilTestDriver.getTypeElement("String").isEmpty)
    assert(ElementUtilTestDriver.getTypeElement("domala.internal.macros.reflect.util.Outer").contains("domala.internal.macros.reflect.util.Outer"))
    assert(ElementUtilTestDriver.getTypeElement("domala.internal.macros.reflect.util.Outer$Inner").contains("domala.internal.macros.reflect.util.Outer.Inner"))
    assert(ElementUtilTestDriver.getTypeElement("domala.internal.macros.reflect.util.Outer$Inner$InInner").contains("domala.internal.macros.reflect.util.Outer.Inner.InInner"))
  }
}

class Outer(x: Int) {
  class Inner(y: Int) {
    class InInner(z: Int)
  }
}