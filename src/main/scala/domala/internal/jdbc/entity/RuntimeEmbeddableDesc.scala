package domala.internal.jdbc.entity

import java.util

import domala.Column
import domala.internal.reflect.util.RuntimeTypeConverter
import domala.internal.jdbc.entity.RuntimeEntityDesc.toTypeTag
import domala.jdbc.entity._
import domala.message.Message
import org.seasar.doma.DomaException

import scala.collection.JavaConverters._
import scala.language.existentials
import scala.reflect._
import scala.reflect.runtime.{universe => ru}
import ru._

class RuntimeEmbeddableDesc[EMBEDDABLE: TypeTag] {
  def getEmbeddablePropertyTypes[ENTITY: TypeTag : ClassTag](embeddedPropertyName: String, namingType: NamingType): util.List[EntityPropertyDesc[ENTITY, _]] = {
    val tpe = typeOf[EMBEDDABLE]
    val constructor = tpe.decl(termNames.CONSTRUCTOR).asMethod

    constructor.paramLists.flatten.map { (p: Symbol) =>
      val annotations = p.annotations
      val column: Column = annotations.collectFirst {
        case a: ru.Annotation if a.tree.tpe =:= typeOf[domala.Column] =>
          Column.reflect(ru)(a)
      }.getOrElse(Column())
      RuntimeEntityDesc.generateDefaultPropertyDesc[ENTITY](embeddedPropertyName + "." + p.name.toString, p.typeSignature, column, namingType).head._2
    }.asJava
  }

  def newEmbeddable[ENTITY: TypeTag : ClassTag](embeddedPropertyName: String, __args: Map[String, Property[ENTITY, _]]): EMBEDDABLE =
    RuntimeEmbeddableDesc.fromMap[EMBEDDABLE, ENTITY](embeddedPropertyName, __args)
}

object RuntimeEmbeddableDesc {

  private[this] val embeddableDescCache = scala.collection.concurrent.TrieMap[String,  RuntimeEmbeddableDesc[_]]()
  private[this] val embeddableConstructorMirrorCache = scala.collection.concurrent.TrieMap[String, ru.MethodMirror]()
  private[this] val mirror = ru.runtimeMirror(Thread.currentThread.getContextClassLoader)

  def of[E](implicit tTag: TypeTag[E]): RuntimeEmbeddableDesc[E] = {
    embeddableDescCache.getOrElseUpdate(
    tTag.toString() + tTag.hashCode(),
    {
      new RuntimeEmbeddableDesc[E]
    }).asInstanceOf[RuntimeEmbeddableDesc[E]]
  }

  def clear(): Unit = {
    embeddableDescCache.clear()
    embeddableConstructorMirrorCache.clear()
  }

  def fromMap[EMBEDDABLE: TypeTag, ENTITY: TypeTag: ClassTag](embeddedPropertyName: String, propertyMap: Map[String, Property[ENTITY, _]]): EMBEDDABLE = {
    val constructorMirror = embeddableConstructorMirrorCache.getOrElseUpdate(
    typeTag[EMBEDDABLE].toString() + typeTag[EMBEDDABLE].hashCode(),
    {
      val classTest = typeOf[EMBEDDABLE].typeSymbol.asClass
      val classMirror = mirror.reflectClass(classTest)
      val constructor = typeOf[EMBEDDABLE].decl(termNames.CONSTRUCTOR).asMethod
      classMirror.reflectConstructor(constructor)
    })
    val constructorArgs = constructorMirror.symbol.paramLists.flatten.map((param: Symbol) => {
      val paramName = param.name.toString
      val propertyName = embeddedPropertyName + "." + paramName
      RuntimeTypeConverter.toType(param.typeSignature) match {
        case t if t.isRuntimeEmbeddable =>
          val embeddableTypeTag = toTypeTag(param.typeSignature)
          val embeddableDesc = of(embeddableTypeTag)
          embeddableDesc
            .newEmbeddable[ENTITY](propertyName, propertyMap)
        case _ => propertyMap.get(propertyName).map(_.get).getOrElse(
          throw new DomaException(Message.DOMALA6024, typeOf[ENTITY], propertyName, paramName))
      }
    })
    constructorMirror(constructorArgs:_*).asInstanceOf[EMBEDDABLE]
  }

}

