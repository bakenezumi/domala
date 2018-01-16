package domala.internal.macros

import domala.internal.macros.meta.generator.EntityDescGenerator
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
      object ListenerHolder {
        domala.internal.macros.reflect.EntityReflectionMacros.validateListener(classOf[Person], classOf[org.seasar.doma.jdbc.entity.NullEntityListener[Person]])
        val listener = new org.seasar.doma.jdbc.entity.NullEntityListener[Person]()
      }
      private[this] val __namingType: domala.jdbc.entity.NamingType = null
      private[this] val __idGenerator = new org.seasar.doma.jdbc.id.BuiltinIdentityIdGenerator()
      import scala.collection.JavaConverters._
      private[this] val __propertyMap = Seq(domala.internal.macros.reflect.EntityReflectionMacros.generatePropertyDesc[Option[ID[Person]], Person, ID[Person]](classOf[Person], "id", __namingType, true, true, __idGenerator, false, false, false, null, "", true, true, false), domala.internal.macros.reflect.EntityReflectionMacros.generatePropertyDesc[Option[Name], Person, Name](classOf[Person], "name", __namingType, false, false, __idGenerator, false, false, false, null, "", true, false, false), domala.internal.macros.reflect.EntityReflectionMacros.generatePropertyDesc[Option[Int], Person, Integer](classOf[Person], "age", __namingType, false, false, __idGenerator, false, false, true, () => new org.seasar.doma.wrapper.IntegerWrapper(): org.seasar.doma.wrapper.Wrapper[Integer], "", true, true, false), domala.internal.macros.reflect.EntityReflectionMacros.generatePropertyDesc[Address, Person, Address](classOf[Person], "address", __namingType, false, false, __idGenerator, false, false, false, null, "", true, true, false), domala.internal.macros.reflect.EntityReflectionMacros.generatePropertyDesc[Option[Int], Person, Integer](classOf[Person], "departmentId", __namingType, false, false, __idGenerator, false, false, true, () => new org.seasar.doma.wrapper.IntegerWrapper(): org.seasar.doma.wrapper.Wrapper[Integer], "", true, true, false), domala.internal.macros.reflect.EntityReflectionMacros.generatePropertyDesc[Option[Int], Person, Integer](classOf[Person], "version", __namingType, false, false, __idGenerator, true, false, true, () => new org.seasar.doma.wrapper.IntegerWrapper(): org.seasar.doma.wrapper.Wrapper[Integer], "", true, true, false)).flatten.toMap
      private[this] val __idList = __propertyMap.filterKeys(Set("id")).values.toList.asJava
      private[this] val __list = __propertyMap.values.toList.asJava
      private[this] val __map = __propertyMap.asJava
      private[this] val __listenerSupplier: java.util.function.Supplier[org.seasar.doma.jdbc.entity.NullEntityListener[Person]] = () => ListenerHolder.listener
      private[this] val __immutable = true
      private[this] val __name = "Person"
      private[this] val __catalogName = ""
      private[this] val __schemaName = ""
      private[this] val __tableName = ""
      private[this] val __isQuoteRequired = false
      private[this] val __idPropertyTypes = java.util.Collections.unmodifiableList(__idList)
      private[this] val __entityPropertyTypes = java.util.Collections.unmodifiableList(__list)
      private[this] val __entityPropertyTypeMap: java.util.Map[String, domala.jdbc.entity.EntityPropertyDesc[Person, _]] = java.util.Collections.unmodifiableMap(__map)
      override def getNamingType: domala.jdbc.entity.NamingType = __namingType
      override def isImmutable: Boolean = __immutable
      override def getName: String = __name
      override def getCatalogName: String = __catalogName
      override def getSchemaName: String = __schemaName
      override def getTableName: String = getTableName(org.seasar.doma.jdbc.Naming.DEFAULT.apply _)
      override def getTableName(namingFunction: java.util.function.BiFunction[domala.jdbc.entity.NamingType, String, String]): String = {
        if (__tableName.isEmpty) {
          namingFunction.apply(__namingType, __name)
        } else {
          __tableName
        }
      }
      override def isQuoteRequired: Boolean = __isQuoteRequired
      override def preInsert(entity: Person, context: org.seasar.doma.jdbc.entity.PreInsertContext[Person]): Unit = {
        val __listenerClass = classOf[org.seasar.doma.jdbc.entity.NullEntityListener[Person]]
        val __listener = context.getConfig.getEntityListenerProvider.get[Person, org.seasar.doma.jdbc.entity.NullEntityListener[Person]](__listenerClass, __listenerSupplier)
        __listener.preInsert(entity, context)
      }
      override def preUpdate(entity: Person, context: org.seasar.doma.jdbc.entity.PreUpdateContext[Person]): Unit = {
        val __listenerClass = classOf[org.seasar.doma.jdbc.entity.NullEntityListener[Person]]
        val __listener = context.getConfig.getEntityListenerProvider.get[Person, org.seasar.doma.jdbc.entity.NullEntityListener[Person]](__listenerClass, __listenerSupplier)
        __listener.preUpdate(entity, context)
      }
      override def preDelete(entity: Person, context: org.seasar.doma.jdbc.entity.PreDeleteContext[Person]): Unit = {
        val __listenerClass = classOf[org.seasar.doma.jdbc.entity.NullEntityListener[Person]]
        val __listener = context.getConfig.getEntityListenerProvider.get[Person, org.seasar.doma.jdbc.entity.NullEntityListener[Person]](__listenerClass, __listenerSupplier)
        __listener.preDelete(entity, context)
      }
      override def postInsert(entity: Person, context: org.seasar.doma.jdbc.entity.PostInsertContext[Person]): Unit = {
        val __listenerClass = classOf[org.seasar.doma.jdbc.entity.NullEntityListener[Person]]
        val __listener = context.getConfig.getEntityListenerProvider.get[Person, org.seasar.doma.jdbc.entity.NullEntityListener[Person]](__listenerClass, __listenerSupplier)
        __listener.postInsert(entity, context)
      }
      override def postUpdate(entity: Person, context: org.seasar.doma.jdbc.entity.PostUpdateContext[Person]): Unit = {
        val __listenerClass = classOf[org.seasar.doma.jdbc.entity.NullEntityListener[Person]]
        val __listener = context.getConfig.getEntityListenerProvider.get[Person, org.seasar.doma.jdbc.entity.NullEntityListener[Person]](__listenerClass, __listenerSupplier)
        __listener.postUpdate(entity, context)
      }
      override def postDelete(entity: Person, context: org.seasar.doma.jdbc.entity.PostDeleteContext[Person]): Unit = {
        val __listenerClass = classOf[org.seasar.doma.jdbc.entity.NullEntityListener[Person]]
        val __listener = context.getConfig.getEntityListenerProvider.get[Person, org.seasar.doma.jdbc.entity.NullEntityListener[Person]](__listenerClass, __listenerSupplier)
        __listener.postDelete(entity, context)
      }
      override def getEntityPropertyTypes: java.util.List[domala.jdbc.entity.EntityPropertyDesc[Person, _]] = __entityPropertyTypes
      override def getEntityPropertyType(__name: String): domala.jdbc.entity.EntityPropertyDesc[Person, _] = __entityPropertyTypeMap.get(__name)
      override def getIdPropertyTypes: java.util.List[domala.jdbc.entity.EntityPropertyDesc[Person, _]] = __idPropertyTypes
      override def getGeneratedIdPropertyType: domala.jdbc.entity.GeneratedIdPropertyDesc[_ >: Person, Person, _, _] = __propertyMap("id").asInstanceOf[domala.jdbc.entity.GeneratedIdPropertyDesc[Person, Person, _ <: Number, _]]
      override def getVersionPropertyType: domala.jdbc.entity.VersionPropertyDesc[_ >: Person, Person, _, _] = __propertyMap("version").asInstanceOf[domala.jdbc.entity.VersionPropertyDesc[Person, Person, _ <: Number, _]]
      override def getTenantIdPropertyType: domala.jdbc.entity.TenantIdPropertyDesc[_ >: Person, Person, _, _] = null
      override def newEntity(__args: java.util.Map[String, domala.jdbc.entity.Property[Person, _]]) = new Person(domala.internal.macros.reflect.EntityReflectionMacros.readProperty[Option[ID[Person]], Person](classOf[Person], __args, "id"), domala.internal.macros.reflect.EntityReflectionMacros.readProperty[Option[Name], Person](classOf[Person], __args, "name"), domala.internal.macros.reflect.EntityReflectionMacros.readProperty[Option[Int], Person](classOf[Person], __args, "age"), domala.internal.macros.reflect.EntityReflectionMacros.readProperty[Address, Person](classOf[Person], __args, "address"), domala.internal.macros.reflect.EntityReflectionMacros.readProperty[Option[Int], Person](classOf[Person], __args, "departmentId"), domala.internal.macros.reflect.EntityReflectionMacros.readProperty[Option[Int], Person](classOf[Person], __args, "version"))
      override def getEntityClass: Class[Person] = classOf[Person]
      override def getOriginalStates(__entity: Person): Person = null
      override def saveCurrentStates(__entity: Person): Unit = {}
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