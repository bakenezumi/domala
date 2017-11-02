package domala.internal.macros

import org.seasar.doma.DomaException
import org.seasar.doma.message.MessageResource

class MacrosException(val message: MessageResource, val cause: Throwable, val messageArgs: AnyRef*) extends DomaException(message, cause, messageArgs.toArray: _*)

object MacrosException {
  def unapply(arg: MacrosException): Option[(MessageResource, Throwable, Seq[AnyRef])] = Some((arg.message, arg.cause, arg.messageArgs))
}
