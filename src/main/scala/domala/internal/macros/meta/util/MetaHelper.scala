package domala.internal.macros.meta.util

import domala.internal.macros.MacrosAbortException
import org.seasar.doma.message.MessageResource

import scala.meta.Defn

object MetaHelper {
  def abort(message: MessageResource, args: AnyRef*): Nothing = throw new MacrosAbortException(message, null, args: _*)

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
