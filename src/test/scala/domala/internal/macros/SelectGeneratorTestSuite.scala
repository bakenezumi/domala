package domala.internal.macros

import domala.message.Message
import org.scalatest.FunSuite

import scala.meta._

class SelectGeneratorTestSuite extends FunSuite{

  test("empty sql") {
    val trt = q"""
trait EmptySqlDao {
  @Select("")
  def select(id: Int): Emp
}
"""
    val caught = intercept[MacrosException] {
      DaoGenerator.generate(trt, null, None)
    }
    assert(caught.message == Message.DOMALA4020)
  }

  test("wildcard type return") {
    val trt = q"""
trait WildcardTypeReturnDao {
  @Select("select height from emp")
  def select: Height[_]
}
"""
    val caught = intercept[MacrosException] {
      DaoGenerator.generate(trt, null, None)
    }
    assert(caught.message == Message.DOMALA4207)
  }

  test("no stream function parameters") {
    val trt = q"""
trait StreamNoFunctionParamDao {
  @Select("select * from emp", strategy = SelectType.STREAM)
  def select(mapper: Int => Int): Int
}
"""
    val caught = intercept[MacrosException] {
      DaoGenerator.generate(trt, null, None)
    }
    assert(caught.message == Message.DOMALA4247)
  }

  test("multiple stream function parameters") {
    val trt = q"""
trait StreamNoFunctionParamDao {
  @Select("select * from emp", strategy = SelectType.STREAM)
  def select(mapper1: Stream[Emp] => Int, mapper2: Stream[Emp] => Int): Int
}
"""
    val caught = intercept[MacrosException] {
      DaoGenerator.generate(trt, null, None)
    }
    assert(caught.message == Message.DOMALA4249)
  }
}
