package domala

import scala.meta._

import domala.internal.macros.EmbeddableTypeGenerator

class Embeddable extends scala.annotation.StaticAnnotation {
  inline def apply(defn: Any): Any = meta {
    defn match {
      case cls: Defn.Class => EmbeddableTypeGenerator.generate(cls)
      case _ => abort("@Embeddable most annotate a class")
    }
  }
}
