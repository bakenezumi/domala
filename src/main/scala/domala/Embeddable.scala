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
  * @Embeddable
  * case class Address (
  *
  *   @Column(name = "CITY")
  *   city: String,
  *
  *   @Column(name = "STREET")
  *   street: String
  *
  * )
  * }}}
  *
  * {{{
  * @Entity
  * case class Employee(
  *
  *   @Id
  *   @Column(name = "ID")
  *   id: Int,
  *
  *   address: Address,
  *
  *   @Version
  *   @Column(name = "VERSION")
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
        (cls, EmbeddableTypeGenerator.generate(cls, Some(companion)))
      case cls: Defn.Class =>
        (cls, EmbeddableTypeGenerator.generate(cls, None))
      case _ => abort(domala.message.Message.DOMALA4283.getMessage())
    }
    //logger.debug(newCompanion)
    Term.Block(Seq(
      cls,
      newCompanion
    ))
  }
}
