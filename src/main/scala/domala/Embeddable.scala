package domala

import domala.internal.macros.EmbeddableTypeGenerator

import scala.meta._

class Embeddable extends scala.annotation.StaticAnnotation {
  inline def apply(defn: Any): Any = meta {
    defn match {
      case cls: Defn.Class => EmbeddableTypeGenerator.generate(cls)
      case _ => abort("@Embeddable most annotate a class")
    }
  }
}
