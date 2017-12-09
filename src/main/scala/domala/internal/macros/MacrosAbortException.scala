package domala.internal.macros

import org.seasar.doma.DomaException
import org.seasar.doma.message.MessageResource

class MacrosAbortException(val message: MessageResource, val cause: Throwable, val messageArgs: AnyRef*) extends DomaException(message, cause, messageArgs.toArray: _*)

object MacrosAbortException {
  def unapply(arg: MacrosAbortException): Option[(MessageResource, Throwable, Seq[AnyRef])] = Some((arg.message, arg.cause, arg.messageArgs))
}
