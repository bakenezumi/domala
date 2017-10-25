package domala.internal.macros.reflect.decl

import org.scalatest.{BeforeAndAfter, FunSuite}

class TypeDeclarationTestSuite extends FunSuite with BeforeAndAfter {
  test("getBinaryName") {
    assert(TypeDeclarationTestDriver.getBinaryName(classOf[String]) == "String")
  }

  test("isBooleanType") {
    assert(TypeDeclarationTestDriver.isBooleanType(classOf[Boolean]))
    assert(!TypeDeclarationTestDriver.isBooleanType(classOf[String]))
  }

  test("isNullType") {
    assert(TypeDeclarationTestDriver.isNullType(null))
    assert(!TypeDeclarationTestDriver.isNullType(classOf[String]))
  }

  test("isSameType") {
    assert(TypeDeclarationTestDriver.isSameType(classOf[List[String]], classOf[List[String]]))
    assert(TypeDeclarationTestDriver.isSameType(classOf[Int], classOf[Integer]))
    assert(!TypeDeclarationTestDriver.isSameType(classOf[List[String]], classOf[List[Int]]))
    assert(!TypeDeclarationTestDriver.isSameType(classOf[Int], classOf[Long]))
  }

}
