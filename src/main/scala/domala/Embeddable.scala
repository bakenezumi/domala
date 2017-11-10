package domala

import domala.internal.macros.EmbeddableTypeGenerator

import scala.collection.immutable.Seq
import scala.meta._

/** Indicates an embeddable class.
  *
  * The embeddable class can be embedded into an entity class.
  *
  * The embeddable class must have a non-private constructor that accepts all
  * properties of the class as arguments.
  *
  * {{{
  *@literal @Embeddable
  * case class Address (
  *
  *  @literal @Column(name = "CITY")
  *   city: String,
  *
  *  @literal @Column(name = "STREET")
  *   street: String
  *
  * )
  * }}}
  *
  * {{{
  *@literal @Entity
  * case class Employee(
  *
  *  @literal @Id
  *  @literal @Column(name = "ID")
  *   id: Int,
  *
  *   address: Address,
  *
  *  @literal @Version
  *  @literal @Column(name = "VERSION")
  *   version: Int,
  *
  *   ...
  * )
  * }}}
  *
  * The embeddable instance is not required to be thread safe.
  */
class Embeddable extends scala.annotation.StaticAnnotation {
  inline def apply(defn: Any): Any = meta {
    val (cls, newCompanion) = defn match {
      case Term.Block(Seq(cls: Defn.Class, companion: Defn.Object)) =>
        val newCompanion = EmbeddableTypeGenerator.generate(cls)
        (
          cls,
          newCompanion.copy(templ = newCompanion.templ.copy(
            stats = Some(newCompanion.templ.stats.getOrElse(Nil) ++ companion.templ.stats.getOrElse(Nil))
          ))
        )
      case cls: Defn.Class => (cls, EmbeddableTypeGenerator.generate(cls))
      case _ => abort(domala.message.Message.DOMALA4283.getMessage())
    }
    //logger.debug(newCompanion)
    Term.Block(Seq(
      cls,
      newCompanion
    ))
  }
}
