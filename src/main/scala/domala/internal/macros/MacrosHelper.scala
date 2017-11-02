package domala.internal.macros

import org.seasar.doma.message.MessageResource

object MacrosHelper {
  def abort(message: MessageResource, args: AnyRef*): Nothing = throw new MacrosException(message, null, args: _*)
}
