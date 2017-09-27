package domala

import domala.internal.macros.DaoGenerator
import domala.jdbc.Config

import scala.meta._

class Dao(config: Config) extends scala.annotation.StaticAnnotation {
  inline def apply(defn: Any): Any = meta {
    val q"new $_(..$params)" = this
    val config = params.collectFirst{ case arg"config = $x" => x }.orNull
    defn match {
      case trt: Defn.Trait => DaoGenerator.generate(trt, config)
      case _ => abort("@Dao most annotate a trait")
    }
  }
}
