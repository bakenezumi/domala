package domala.internal.jdbc.entity

import java.util

import domala.jdbc.entity.{EntityPropertyDesc, NamingType, Property}

trait MacroEmbeddableDesc[EMBEDDABLE] {
  def getEmbeddablePropertyTypes[ENTITY](embeddedPropertyName: String, namingType: NamingType, entityClass: Class[ENTITY]): util.List[EntityPropertyDesc[ENTITY, _]]

  def newEmbeddable[ENTITY](embeddedPropertyName: String, __args: Map[String, Property[ENTITY, _]], entityClass: Class[ENTITY]): EMBEDDABLE
}

object MacroEmbeddableDesc {

  private[this] val embeddableDescCache = scala.collection.concurrent.TrieMap[String, MacroEmbeddableDesc[_]]()

  def of[EMBEDDABLE](clazz: Class[EMBEDDABLE], op: => MacroEmbeddableDesc[EMBEDDABLE]): MacroEmbeddableDesc[EMBEDDABLE] = {
    embeddableDescCache.getOrElseUpdate(clazz.getName + clazz.hashCode(), op).asInstanceOf[MacroEmbeddableDesc[EMBEDDABLE]]
  }

}
