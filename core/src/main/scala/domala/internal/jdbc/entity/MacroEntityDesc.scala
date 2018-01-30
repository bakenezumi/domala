package domala.internal.jdbc.entity

import domala.jdbc.entity.EntityDesc

object MacroEntityDesc {
  private[this] val entityDescCache = scala.collection.concurrent.TrieMap[String, EntityDesc[_]]()

  def of[ENTITY](clazz: Class[ENTITY], op: => EntityDesc[ENTITY]): EntityDesc[ENTITY] =
    entityDescCache.getOrElseUpdate(clazz.getName + clazz.hashCode(), op).asInstanceOf[EntityDesc[ENTITY]]

}

