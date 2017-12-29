package domala.jdbc.entity

import org.seasar.doma.jdbc.ClassHelper

object EntityDescFactory {
  def getEntityDesc[E](entityClass: Class[E], classHelper: ClassHelper): EntityDesc[E] = {
    classHelper
      .forName(entityClass.getName + "$")
      .getField("MODULE$").get(null).asInstanceOf[EntityCompanion[E]].entityDesc.asInstanceOf[EntityDesc[E]]
  }
}
