package domala.internal.macros.reflect

import org.seasar.doma.DomaException
import org.seasar.doma.message.MessageResource

class ReflectAbortException(val message: MessageResource, val cause: Throwable, val messageArgs: AnyRef*) extends DomaException(message, cause, messageArgs.toArray: _*)

object ReflectAbortException {
  def unapply(arg: ReflectAbortException): Option[(MessageResource, Throwable, Seq[AnyRef])] = Some((arg.message, arg.cause, arg.messageArgs))
}
