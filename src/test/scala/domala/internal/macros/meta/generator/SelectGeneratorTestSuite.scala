package domala.internal.macros.meta.generator

import domala.internal.macros.MacrosAbortException
import domala.message.Message
import org.scalatest.FunSuite

import scala.meta._

class SelectGeneratorTestSuite extends FunSuite{

  test("wildcard type return") {
    val trt = q"""
trait WildcardTypeReturnDao {
  @Select("select height from emp")
  def select: Height[_]
}
"""
    val caught = intercept[MacrosAbortException] {
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
    val caught = intercept[MacrosAbortException] {
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
    val caught = intercept[MacrosAbortException] {
      DaoGenerator.generate(trt, null, None)
    }
    assert(caught.message == Message.DOMALA4249)
  }

  test("Simultaneous specification of SQL annotation and SQL file") {
    val trt = q"""
trait StreamNoFunctionParamDao {
  @Delete("delete from emp", sqlFile = true)
  def delete(entity: Emp): Result[Emp]
}
"""
    val caught = intercept[MacrosAbortException] {
      DaoGenerator.generate(trt, null, None)
    }
    assert(caught.message == Message.DOMALA6021)
  }

}
