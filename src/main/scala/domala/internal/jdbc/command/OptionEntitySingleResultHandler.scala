package domala.internal.jdbc.command

import org.seasar.doma.internal.jdbc.command.{AbstractSingleResultHandler, EntityIterationHandler}
import org.seasar.doma.jdbc.entity.EntityType

class OptionEntitySingleResultHandler[ENTITY](entityType: EntityType[ENTITY]) extends AbstractSingleResultHandler[Option[ENTITY]](new EntityIterationHandler(
  entityType, new OptionSingleResultCallback[ENTITY]()
))
