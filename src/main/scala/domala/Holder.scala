package domala

import domala.internal.macros.meta.generator.HolderDescGenerator

import scala.collection.immutable.Seq
import scala.meta._

/** Indicates a value holder class.
  *
  * The holder class is the user defined type that wraps a basic value. It can be
  * mapped to a database column.
  *
  * Instantiation by constructor:
  *
  * {{{
  * @Holder
  * case class PhoneNumber(value: String)
  * }}}
  *
  */
class Holder extends scala.annotation.StaticAnnotation {
  inline def apply(defn: Any): Any = meta {
    val (cls, newCompanion) = defn match {
      case Term.Block(Seq(cls: Defn.Class, companion: Defn.Object)) =>
        (cls, HolderDescGenerator.generate(cls, Some(companion)))
      case cls: Defn.Class =>
        (cls, HolderDescGenerator.generate(cls, None))
      case _ => abort(domala.message.Message.DOMALA4105.getMessage())
    }
    //logger.debug(newCompanion)
    Term.Block(Seq(
      cls,
      newCompanion
    ))
  }
}
