package domala.internal.jdbc.command

import domala.jdbc.entity.EntityDesc
import org.seasar.doma.internal.jdbc.command.{AbstractSingleResultHandler, EntityIterationHandler}

class OptionEntitySingleResultHandler[ENTITY](entityDesc: EntityDesc[ENTITY]) extends AbstractSingleResultHandler[Option[ENTITY]](new EntityIterationHandler(
  entityDesc, new OptionSingleResultCallback[ENTITY]()
))
