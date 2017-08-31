package domala

import domala.internal.macros.EntityTypeGenerator

import scala.meta._

class Entity(val name: String = null) extends scala.annotation.StaticAnnotation {
  inline def apply(defn: Any): Any = meta {
    val q"new $_(..$params)" = this
    val name = params.collectFirst{ case arg"name = $x" => x }.orNull
    defn match {
      case cls: Defn.Class => EntityTypeGenerator.generate(cls, name)
      case _ => abort("@Entity most annotate a class")
    }
  }
}
