package domala

import domala.internal.macros.DomainTypeGenerator

import scala.meta._

class Domain extends scala.annotation.StaticAnnotation {
  inline def apply(defn: Any): Any = meta {
    defn match {
      case cls: Defn.Class => DomainTypeGenerator.generate(cls)
      case _ => abort("@Domain most annotate a class")
    }
  }
}
