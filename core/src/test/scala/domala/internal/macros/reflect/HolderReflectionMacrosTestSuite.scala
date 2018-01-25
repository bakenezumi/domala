package domala.internal.macros.reflect

import org.scalatest.FunSuite

class HolderReflectionMacrosTestSuite extends FunSuite {

  private[this] val errorMessage = "not unique"
  private[this] val mockHandler = () => throw new AssertionError(errorMessage)

  test("assertUnique OK Int") {
    HolderReflectionMacros.assertUnique(mockHandler)(1,2,3)
  }

  test("assertUnique OK String") {
    HolderReflectionMacros.assertUnique(mockHandler)("A", "B", "C")
  }

  test("assertUnique NG Int") {
    val caught = intercept[AssertionError] {
      HolderReflectionMacros.assertUnique(mockHandler)(1,2,1)
    }
    assert(caught.getMessage == errorMessage)
  }

  test("assertUnique NG String") {
    val caught = intercept[AssertionError] {
      HolderReflectionMacros.assertUnique(mockHandler)("A", "B", "B")
    }
    assert(caught.getMessage == errorMessage)
  }

}
