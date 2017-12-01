package domala.internal.macros.helper

import domala.internal.macros.MacrosException
import org.seasar.doma.message.MessageResource

import scala.meta.Defn

object MacrosHelper {
  def abort(message: MessageResource, args: AnyRef*): Nothing = throw new MacrosException(message, null, args: _*)

  def mergeObject(maybeOriginal: Option[Defn.Object], generated: Defn.Object): Defn.Object = {
    maybeOriginal.map { original =>
      original.copy(
        templ = generated.templ.copy(
          stats = Some(generated.templ.stats.getOrElse(Nil)
            ++ original.templ.stats.getOrElse(Nil))
        )
      )
    }.getOrElse(generated)
  }

}
