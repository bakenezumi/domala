package domala.internal.jdbc.entity

import domala.internal.reflect.util.{ReflectionUtil, RuntimeTypeConverter}
import domala.jdbc.`type`.Types
import domala.jdbc.entity.{EntityDesc, RuntimeEntityDesc}
import domala.message.Message
import org.seasar.doma.DomaException
import org.seasar.doma.jdbc.ClassHelper

import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

object EntityDescRepository {
  def get[E: TypeTag](implicit classHelper: ClassHelper, cTag: ClassTag[E]): EntityDesc[E] = {
    RuntimeTypeConverter.toType(typeOf[E]) match {
      case Types.GeneratedEntityType =>
          ReflectionUtil.getEntityDesc(cTag)
      case Types.RuntimeEntityType =>
        RuntimeEntityDesc.of[E]
      case _ => throw new DomaException(
              Message.DOMALA6025, cTag.runtimeClass.getName)
    }

  }
}
