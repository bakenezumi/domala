package domala.internal.jdbc.command

import org.seasar.doma.MapKeyNamingType
import org.seasar.doma.internal.jdbc.command.{AbstractSingleResultHandler, MapIterationHandler}

class OptionMapSingleResultHandler(keyNamingType: MapKeyNamingType) extends
  AbstractSingleResultHandler[Option[java.util.Map[String, AnyRef]]](
    new MapIterationHandler(
      keyNamingType,
      new OptionSingleResultCallback[java.util.Map[String, AnyRef]]()))
