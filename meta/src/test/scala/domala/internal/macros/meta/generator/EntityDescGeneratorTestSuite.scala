package domala.internal.macros.meta.generator

import domala.internal.macros.MacrosAbortException
import domala.message.Message
import org.scalatest.FunSuite

import scala.meta._

class EntityDescGeneratorTestSuite extends FunSuite {
  test("normal entity") {
    val cls = q"""
case class Person(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  id: Option[ID[Person]] = None,
  @Column(updatable = false)
  name: Option[Name],
  age: Option[Int],
  address: Address,
  departmentId: Option[Int],
  @Version
  version: Option[Int] = Option(-1)
)
"""
    val expect = q"""{
  case class Person(@Id id: Option[ID[Person]] = None, name: Option[Name], age: Option[Int], address: Address, departmentId: Option[Int], @Version version: Option[Int] = Option(-1))
  object Person extends domala.jdbc.entity.EntityCompanion[Person] {
    val entityDesc: domala.jdbc.entity.EntityDesc[Person] = EntityDesc
    object EntityDesc extends domala.jdbc.entity.AbstractEntityDesc[Person] {
      private[this] val __idGenerator = new org.seasar.doma.jdbc.id.BuiltinIdentityIdGenerator()
      override type ENTITY_LISTENER = org.seasar.doma.jdbc.entity.NullEntityListener[Person]
      override protected val table: domala.Table = domala.Table("", "", "", false)
      override protected val propertyDescMap: Map[String, domala.jdbc.entity.EntityPropertyDesc[Person, _]] = Seq(domala.internal.macros.reflect.EntityReflectionMacros.generatePropertyDesc[Option[ID[Person]], Person, ID[Person]](classOf[Person], "id", getNamingType, true, true, __idGenerator, false, false, domala.Column("", true, true, false)), domala.internal.macros.reflect.EntityReflectionMacros.generatePropertyDesc[Option[Name], Person, Name](classOf[Person], "name", getNamingType, false, false, __idGenerator, false, false, domala.Column("", true, false, false)), domala.internal.macros.reflect.EntityReflectionMacros.generatePropertyDesc[Option[Int], Person, Integer](classOf[Person], "age", getNamingType, false, false, __idGenerator, false, false, domala.Column("", true, true, false)), domala.internal.macros.reflect.EntityReflectionMacros.generatePropertyDesc[Address, Person, Address](classOf[Person], "address", getNamingType, false, false, __idGenerator, false, false, domala.Column("", true, true, false)), domala.internal.macros.reflect.EntityReflectionMacros.generatePropertyDesc[Option[Int], Person, Integer](classOf[Person], "departmentId", getNamingType, false, false, __idGenerator, false, false, domala.Column("", true, true, false)), domala.internal.macros.reflect.EntityReflectionMacros.generatePropertyDesc[Option[Int], Person, Integer](classOf[Person], "version", getNamingType, false, false, __idGenerator, true, false, domala.Column("", true, true, false))).flatten.toMap
      override protected val listener = new org.seasar.doma.jdbc.entity.NullEntityListener[Person]()
      override protected val idPropertyDescList: List[domala.jdbc.entity.EntityPropertyDesc[Person, _]] = propertyDescMap.filterKeys(Set("id")).values.toList
      override def getNamingType: domala.jdbc.entity.NamingType = null
      override def getGeneratedIdPropertyType: domala.jdbc.entity.GeneratedIdPropertyDesc[_ >: Person, Person, _, _] = propertyDescMap("id").asInstanceOf[domala.jdbc.entity.GeneratedIdPropertyDesc[Person, Person, _ <: Number, _]]
      override def getVersionPropertyType: domala.jdbc.entity.VersionPropertyDesc[_ >: Person, Person, _, _] = propertyDescMap("version").asInstanceOf[domala.jdbc.entity.VersionPropertyDesc[Person, Person, _ <: Number, _]]
      override def getTenantIdPropertyType: domala.jdbc.entity.TenantIdPropertyDesc[_ >: Person, Person, _, _] = null
      override def newEntity(__args: java.util.Map[String, domala.jdbc.entity.Property[Person, _]]) = new Person(domala.internal.macros.reflect.EntityReflectionMacros.readProperty[Option[ID[Person]], Person](classOf[Person], __args, "id"), domala.internal.macros.reflect.EntityReflectionMacros.readProperty[Option[Name], Person](classOf[Person], __args, "name"), domala.internal.macros.reflect.EntityReflectionMacros.readProperty[Option[Int], Person](classOf[Person], __args, "age"), domala.internal.macros.reflect.EntityReflectionMacros.readProperty[Address, Person](classOf[Person], __args, "address"), domala.internal.macros.reflect.EntityReflectionMacros.readProperty[Option[Int], Person](classOf[Person], __args, "departmentId"), domala.internal.macros.reflect.EntityReflectionMacros.readProperty[Option[Int], Person](classOf[Person], __args, "version"))
      override def getEntityClass: Class[Person] = classOf[Person]
    }
    def apply(id: Option[ID[Person]] = None, name: Option[Name], age: Option[Int], address: Address, departmentId: Option[Int], version: Option[Int] = Option(-1)): Person = new Person(id, name, age, address, departmentId, version)
    def unapply(x: Person): Option[(Option[ID[Person]], Option[Name], Option[Int], Address, Option[Int], Option[Int])] = if (x == null) None else Some((x.id, x.name, x.age, x.address, x.departmentId, x.version))
  }
}"""
    val ret = EntityDescGenerator.generate(cls, None, Nil)
    assert(ret.syntax == expect.syntax)
  }

  test("annotation  conflicted") {
    val cls = q"""
case class AnnotationConflictedEntity(
  @Id
  @Version
  id: Int
)
"""
    val caught = intercept[MacrosAbortException] {
      EntityDescGenerator.generate(cls, None, Nil)
    }
    assert(caught.message == Message.DOMALA4086)
  }

  test("generated value with composite ID") {
    val cls = q"""
case class GeneratedValueWithCompositeIdEntity(
  @Id
  id1: Int,
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  id2: Int
)
"""
    val caught = intercept[MacrosAbortException] {
      EntityDescGenerator.generate(cls, None, Nil)
    }
    assert(caught.message == Message.DOMALA4036)
  }

  test("generated value without ID") {
    val cls = q"""
case class GeneratedValueWithoutIdEntity(
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  id: Int
)
"""
    val caught = intercept[MacrosAbortException] {
      EntityDescGenerator.generate(cls, None, Nil)
    }
    assert(caught.message == Message.DOMALA4033)
  }

  test("sequence generator without generated value") {
    val cls = q"""
case class SequenceGeneratorWithoutGeneratedValueEntity(
  @SequenceGenerator(sequence = "SEQ")
  id: Int
)
"""
    val caught = intercept[MacrosAbortException] {
      EntityDescGenerator.generate(cls, None, Nil)
    }
    assert(caught.message == Message.DOMALA4030)
  }

  test("table generator without generated value") {
    val cls = q"""
case class TableGeneratorWithoutGeneratedValueEntity(
  @TableGenerator(pkColumnValue = "TableGeneratorWithoutGeneratedValueEntity")
  id: Int
)
"""
    val caught = intercept[MacrosAbortException] {
      EntityDescGenerator.generate(cls, None, Nil)
    }
    assert(caught.message == Message.DOMALA4031)
  }

  test("version duplicated") {
    val cls = q"""
case class VersionDuplicatedEntity(
  @Id
  id: Int,
  @Version
  version: Int,
  @Version
  version2: Int
)
"""
    val caught = intercept[MacrosAbortException] {
      EntityDescGenerator.generate(cls, None, Nil)
    }
    assert(caught.message == Message.DOMALA4024)
  }

  test("property name reserved") {
    val cls = q"""
case class PropertyNameReservedEntity(
  __name: String
)
"""
    val caught = intercept[MacrosAbortException] {
      EntityDescGenerator.generate(cls, None, Nil)
    }
    assert(caught.message == Message.DOMALA4025)
  }

  test("unsupported property") {
    val cls = q"""
case class UnsupportedPropertyEntity(
  intMap: Map[Int, AnyRef]
)
"""
    val caught = intercept[MacrosAbortException] {
      EntityDescGenerator.generate(cls, None, Nil)
    }
    assert(caught.message == Message.DOMALA4096)
  }

  test("wildcard property") {
    val cls = q"""
case class WildcardPropertyEntity(
  wight: Weight[_]
)
"""
    val caught = intercept[MacrosAbortException] {
      EntityDescGenerator.generate(cls, None, Nil)
    }
    assert(caught.message == Message.DOMALA4205)
  }

  test("var property") {
    val cls = q"""
case class VarPropertyEntity(
  aaa: String,
  var bbb: Int,
  ccc: Int
)
"""
    val caught = intercept[MacrosAbortException] {
      EntityDescGenerator.generate(cls, None, Nil)
    }
    assert(caught.message == Message.DOMALA4225)
  }

}
