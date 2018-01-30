package domala.internal.macros.reflect.mock

import java.util.Optional

import domala.jdbc.entity.Property
import org.seasar.doma.jdbc.InParameter
import org.seasar.doma.wrapper.Wrapper

class MockProperty[ENTITY, BASIC](getValue: AnyRef) extends Property[ENTITY, BASIC]{
  override def save(entity: ENTITY): Property[ENTITY, BASIC] = throw new NotImplementedError

  override def load(entity: ENTITY): Property[ENTITY, BASIC] = throw new NotImplementedError

  override def get(): AnyRef = getValue

  override def asInParameter(): InParameter[BASIC] = throw new NotImplementedError

  override def getWrapper: Wrapper[BASIC] = throw new NotImplementedError

  override def getDomainClass: Optional[Class[_]] = throw new NotImplementedError
}

object MockProperty {
  def of[ENTITY, BASIC](getValue: AnyRef) = new MockProperty[ENTITY, BASIC](getValue)
}