package domala.jdbc.entity

import org.seasar.doma.jdbc.ClassHelper
import org.seasar.doma.jdbc.entity.EntityType

object EntityTypeFactory {
  def getEntityType[E](entityClass: Class[E], classHelper: ClassHelper): EntityType[E] = {
    classHelper
      .forName(entityClass.getName + "$")
      .getField("MODULE$").get(null).asInstanceOf[EntityType[E]]
  }
}
