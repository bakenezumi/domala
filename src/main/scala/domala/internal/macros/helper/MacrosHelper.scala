package domala.internal.macros.helper

import domala.internal.macros.MacrosException
import org.seasar.doma.message.MessageResource

object MacrosHelper {
  def abort(message: MessageResource, args: AnyRef*): Nothing = throw new MacrosException(message, null, args: _*)
}
