package domala

import domala.internal.macros.EmbeddableTypeGenerator

import scala.collection.immutable.Seq
import scala.meta._

class Embeddable extends scala.annotation.StaticAnnotation {
  inline def apply(defn: Any): Any = meta {
    val (cls, newCompanion) = defn match {
      case Term.Block(Seq(cls: Defn.Class, companion: Defn.Object)) => {
        val newCompanion = EmbeddableTypeGenerator.generate(cls)
        (
          cls,
          newCompanion.copy(templ = newCompanion.templ.copy(
            stats = Some(newCompanion.templ.stats.getOrElse(Nil) ++ companion.templ.stats.getOrElse(Nil))
          ))
        )
      }
      case cls: Defn.Class => (cls, EmbeddableTypeGenerator.generate(cls))
      case _ => abort("@Embeddable most annotate a class")
    }
    //logger.debug(newCompanion)
    Term.Block(Seq(
      cls,
      newCompanion
    ))
  }
}
