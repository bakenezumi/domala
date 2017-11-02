package domala.internal.macros

import domala.message.Message
import org.scalatest.FunSuite

import scala.meta._

class HolderTypeGeneratorTestSuite extends FunSuite {
  test("normal holder") {
    val cls = q"""
case class Name(value: String)
"""
    val expect = q"""
object Name extends domala.jdbc.holder.AbstractHolderDesc[String, Name]((() => new org.seasar.doma.wrapper.StringWrapper(): org.seasar.doma.wrapper.Wrapper[String]): java.util.function.Supplier[org.seasar.doma.wrapper.Wrapper[String]]) {
  def getSingletonInternal() = this
  override def wrapper: java.util.function.Supplier[org.seasar.doma.wrapper.Wrapper[String]] = () => new org.seasar.doma.wrapper.StringWrapper(): org.seasar.doma.wrapper.Wrapper[String]
  def apply(value: String): Name = new Name(value)
  def unapply(x: Name): Option[String] = Some(x.value)
  override protected def newDomain(value: String): Name = {
    if (value == null) null else Name(value)
  }
  override protected def getBasicValue(domain: Name): String = {
    if (domain == null) null else domain.value
  }
  override def getBasicClass() = {
    classOf[String]
  }
  override def getDomainClass() = {
    classOf[Name]
  }
}
"""
    val ret = HolderTypeGenerator.generate(cls)
    assert(ret.syntax == expect.syntax)
  }

  test("unsupported value type") {
    val cls = q"""
case class UnsupportedValueTypeHolder(value: Seq[String])
"""
    val caught = intercept[MacrosException] {
      HolderTypeGenerator.generate(cls)
    }
    assert(caught.message == Message.DOMALA4102)
  }
}
