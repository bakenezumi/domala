package domala.internal.macros.meta.generator

import org.scalatest.FunSuite

import scala.meta._

class EmbeddableDescGeneratorTestSuite extends FunSuite {
  test("normal embeddable") {
    val cls = q"""
case class Address(city: String, street: String)
"""
    val expect = q"""
object Address extends domala.jdbc.entity.EmbeddableCompanion[Address] {
  val embeddableDesc: domala.jdbc.entity.EmbeddableDesc[Address] = EmbeddableDesc
  object EmbeddableDesc extends domala.jdbc.entity.EmbeddableDesc[Address] {
    override def getEmbeddablePropertyTypes[ENTITY](embeddedPropertyName: String, entityClass: Class[ENTITY], namingType: domala.jdbc.entity.NamingType): java.util.List[domala.jdbc.entity.EntityPropertyDesc[ENTITY, _]] = {
      java.util.Arrays.asList(domala.internal.macros.reflect.EmbeddableReflectionMacros.generatePropertyDesc[Address, String, ENTITY, String](classOf[Address], "city", entityClass, embeddedPropertyName + "." + "city", namingType, domala.Column("", true, true, false)).values.head, domala.internal.macros.reflect.EmbeddableReflectionMacros.generatePropertyDesc[Address, String, ENTITY, String](classOf[Address], "street", entityClass, embeddedPropertyName + "." + "street", namingType, domala.Column("", true, true, false)).values.head)
    }
    override def newEmbeddable[ENTITY](embeddedPropertyName: String, __args: java.util.Map[String, domala.jdbc.entity.Property[ENTITY, _]]): Address = {
      Address({
        Option(__args.get(embeddedPropertyName + "." + "city")).map(_.get()).orNull.asInstanceOf[String]
      }, {
        Option(__args.get(embeddedPropertyName + "." + "street")).map(_.get()).orNull.asInstanceOf[String]
      })
    }
  }
  def apply(city: String, street: String): Address = new Address(city, street)
  def unapply(x: Address): Option[(String, String)] = if (x == null) None else Some((x.city, x.street))

}
"""
    val ret = EmbeddableDescGenerator.generate(cls, None)
    assert(ret.syntax == expect.syntax)
  }

  test("companion merge") {
    val cls = q"""
case class Address(city: String, street: String)
"""
    val companion = q"""
object Address {
  def foo = println("bar")
}
"""


    val expect = q"""
object Address extends domala.jdbc.entity.EmbeddableCompanion[Address] {
  val embeddableDesc: domala.jdbc.entity.EmbeddableDesc[Address] = EmbeddableDesc
  object EmbeddableDesc extends domala.jdbc.entity.EmbeddableDesc[Address] {
    override def getEmbeddablePropertyTypes[ENTITY](embeddedPropertyName: String, entityClass: Class[ENTITY], namingType: domala.jdbc.entity.NamingType): java.util.List[domala.jdbc.entity.EntityPropertyDesc[ENTITY, _]] = {
      java.util.Arrays.asList(domala.internal.macros.reflect.EmbeddableReflectionMacros.generatePropertyDesc[Address, String, ENTITY, String](classOf[Address], "city", entityClass, embeddedPropertyName + "." + "city", namingType, domala.Column("", true, true, false)).values.head, domala.internal.macros.reflect.EmbeddableReflectionMacros.generatePropertyDesc[Address, String, ENTITY, String](classOf[Address], "street", entityClass, embeddedPropertyName + "." + "street", namingType, domala.Column("", true, true, false)).values.head)
    }
    override def newEmbeddable[ENTITY](embeddedPropertyName: String, __args: java.util.Map[String, domala.jdbc.entity.Property[ENTITY, _]]): Address = {
      Address({
        Option(__args.get(embeddedPropertyName + "." + "city")).map(_.get()).orNull.asInstanceOf[String]
      }, {
        Option(__args.get(embeddedPropertyName + "." + "street")).map(_.get()).orNull.asInstanceOf[String]
      })
    }
  }
  def apply(city: String, street: String): Address = new Address(city, street)
  def unapply(x: Address): Option[(String, String)] = if (x == null) None else Some((x.city, x.street))
  def foo = println("bar")

}
"""

    val ret = EmbeddableDescGenerator.generate(cls, Some(companion))
    assert(ret.syntax == expect.syntax)
  }

}
