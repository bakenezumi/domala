package domala.internal.macros.meta.generator

import domala.internal.macros._
import domala.message.Message
import org.scalatest.FunSuite

import scala.meta._

class HolderDescGeneratorTestSuite extends FunSuite {
  test("normal holder") {
    val cls = q"""
case class Name(value: String)
"""
    val expect = q"""
object Name extends domala.jdbc.holder.HolderCompanion[String, Name] {
  val holderDesc: domala.jdbc.holder.HolderDesc[String, Name] = HolderDesc
  object HolderDesc extends domala.jdbc.holder.AbstractHolderDesc[String, Name]((() => new org.seasar.doma.wrapper.StringWrapper(): org.seasar.doma.wrapper.Wrapper[String]): java.util.function.Supplier[org.seasar.doma.wrapper.Wrapper[String]]) {
    override protected def newDomain(value: String): Name = {
      if (value == null) null else Name(value)
    }
    override protected def getBasicValue(holder: Name): String = {
      if (holder == null) null else holder.value
    }
    override def getBasicClass: Class[String] = {
      classOf[String]
    }
    override def getDomainClass: Class[Name] = {
      classOf[Name]
    }
  }
  def apply(value: String): Name = new Name(value)
  def unapply(x: Name): Option[String] = if (x == null) None else Some(x.value)
}
"""
    val ret = HolderDescGenerator.generate(cls, None)
    assert(ret.syntax == expect.syntax)
  }

  test("unsupported value type") {
    val cls = q"""
case class UnsupportedValueTypeHolder(value: Seq[String])
"""
    val caught = intercept[MacrosAbortException] {
      HolderDescGenerator.generate(cls, None)
    }
    assert(caught.message == Message.DOMALA4102)
  }

}


