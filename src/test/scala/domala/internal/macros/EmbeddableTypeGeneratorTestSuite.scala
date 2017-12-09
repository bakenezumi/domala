package domala.internal.macros

import org.scalatest.FunSuite
import scala.meta._

class EmbeddableTypeGeneratorTestSuite extends FunSuite {
  test("normal embeddable") {
    val cls = q"""
case class Address(city: String, street: String)
"""
    val expect = q"""
object Address extends org.seasar.doma.jdbc.entity.EmbeddableType[Address] {
  def apply(city: String, street: String): Address = new Address(city, street)
  def unapply(x: Address): Option[(String, String)] = if (x == null) None else Some((x.city, x.street))
  override def getEmbeddablePropertyTypes[ENTITY](embeddedPropertyName: String, entityClass: Class[ENTITY], namingType: org.seasar.doma.jdbc.entity.NamingType): java.util.List[org.seasar.doma.jdbc.entity.EntityPropertyType[ENTITY, _]] = {
    java.util.Arrays.asList(domala.internal.macros.reflect.EmbeddableReflectionMacros.generatePropertyType[Address, String, ENTITY, String](classOf[Address], "city", entityClass, embeddedPropertyName + "." + "city", namingType, true, () => new org.seasar.doma.wrapper.StringWrapper(): org.seasar.doma.wrapper.Wrapper[String], "", true, true, false, domala.internal.macros.reflect.EntityCollections[ENTITY]()).asInstanceOf[org.seasar.doma.jdbc.entity.EntityPropertyType[ENTITY, _]], domala.internal.macros.reflect.EmbeddableReflectionMacros.generatePropertyType[Address, String, ENTITY, String](classOf[Address], "street", entityClass, embeddedPropertyName + "." + "street", namingType, true, () => new org.seasar.doma.wrapper.StringWrapper(): org.seasar.doma.wrapper.Wrapper[String], "", true, true, false, domala.internal.macros.reflect.EntityCollections[ENTITY]()).asInstanceOf[org.seasar.doma.jdbc.entity.EntityPropertyType[ENTITY, _]])
  }
  override def newEmbeddable[ENTITY](embeddedPropertyName: String, __args: java.util.Map[String, org.seasar.doma.jdbc.entity.Property[ENTITY, _]]): Address = {
    Address({
      Option(__args.get(embeddedPropertyName + "." + "city")).map(_.get()).orNull.asInstanceOf[String]
    }, {
      Option(__args.get(embeddedPropertyName + "." + "street")).map(_.get()).orNull.asInstanceOf[String]
    })
  }
}
"""
    val ret = EmbeddableTypeGenerator.generate(cls, None)
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
object Address extends org.seasar.doma.jdbc.entity.EmbeddableType[Address] {
  def apply(city: String, street: String): Address = new Address(city, street)
  def unapply(x: Address): Option[(String, String)] = if (x == null) None else Some((x.city, x.street))
  override def getEmbeddablePropertyTypes[ENTITY](embeddedPropertyName: String, entityClass: Class[ENTITY], namingType: org.seasar.doma.jdbc.entity.NamingType): java.util.List[org.seasar.doma.jdbc.entity.EntityPropertyType[ENTITY, _]] = {
    java.util.Arrays.asList(domala.internal.macros.reflect.EmbeddableReflectionMacros.generatePropertyType[Address, String, ENTITY, String](classOf[Address], "city", entityClass, embeddedPropertyName + "." + "city", namingType, true, () => new org.seasar.doma.wrapper.StringWrapper(): org.seasar.doma.wrapper.Wrapper[String], "", true, true, false, domala.internal.macros.reflect.EntityCollections[ENTITY]()).asInstanceOf[org.seasar.doma.jdbc.entity.EntityPropertyType[ENTITY, _]], domala.internal.macros.reflect.EmbeddableReflectionMacros.generatePropertyType[Address, String, ENTITY, String](classOf[Address], "street", entityClass, embeddedPropertyName + "." + "street", namingType, true, () => new org.seasar.doma.wrapper.StringWrapper(): org.seasar.doma.wrapper.Wrapper[String], "", true, true, false, domala.internal.macros.reflect.EntityCollections[ENTITY]()).asInstanceOf[org.seasar.doma.jdbc.entity.EntityPropertyType[ENTITY, _]])
  }
  override def newEmbeddable[ENTITY](embeddedPropertyName: String, __args: java.util.Map[String, org.seasar.doma.jdbc.entity.Property[ENTITY, _]]): Address = {
    Address({
      Option(__args.get(embeddedPropertyName + "." + "city")).map(_.get()).orNull.asInstanceOf[String]
    }, {
      Option(__args.get(embeddedPropertyName + "." + "street")).map(_.get()).orNull.asInstanceOf[String]
    })
  }
  def foo = println("bar")
}
"""

    val ret = EmbeddableTypeGenerator.generate(cls, Some(companion))
    assert(ret.syntax == expect.syntax)
  }

}
