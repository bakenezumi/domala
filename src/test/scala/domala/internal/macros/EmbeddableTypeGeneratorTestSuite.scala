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
  def unapply(x: Address): Option[(String, String)] = Some((x.city, x.street))
  override def getEmbeddablePropertyTypes[ENTITY](embeddedPropertyName: String, entityClass: Class[ENTITY], namingType: org.seasar.doma.jdbc.entity.NamingType) = {
    java.util.Arrays.asList(domala.internal.macros.reflect.EntityReflectionMacros.generatePropertyType[String, ENTITY, String](entityClass, embeddedPropertyName + "." + "city", namingType, false, false, null, false, true, () => new org.seasar.doma.wrapper.StringWrapper(): org.seasar.doma.wrapper.Wrapper[String], "", true, true, false, domala.internal.macros.reflect.EntityCollections[ENTITY]()).asInstanceOf[org.seasar.doma.jdbc.entity.EntityPropertyType[ENTITY, _]], domala.internal.macros.reflect.EntityReflectionMacros.generatePropertyType[String, ENTITY, String](entityClass, embeddedPropertyName + "." + "street", namingType, false, false, null, false, true, () => new org.seasar.doma.wrapper.StringWrapper(): org.seasar.doma.wrapper.Wrapper[String], "", true, true, false, domala.internal.macros.reflect.EntityCollections[ENTITY]()).asInstanceOf[org.seasar.doma.jdbc.entity.EntityPropertyType[ENTITY, _]])
  }
  override def newEmbeddable[ENTITY](embeddedPropertyName: String, __args: java.util.Map[String, org.seasar.doma.jdbc.entity.Property[ENTITY, _]]) = {
    Address({
      (if (__args.get(embeddedPropertyName + "." + "city") != null) __args.get(embeddedPropertyName + "." + "city") else null).get().asInstanceOf[String]
    }, {
      (if (__args.get(embeddedPropertyName + "." + "street") != null) __args.get(embeddedPropertyName + "." + "street") else null).get().asInstanceOf[String]
    })
  }
}
"""
    val ret = EmbeddableTypeGenerator.generate(cls)
    assert(ret.syntax == expect.syntax)
  }
}