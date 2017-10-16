package domala

import domala.internal.macros.DaoGenerator
import domala.jdbc.Config

import scala.meta._

class Dao(config: Config = null) extends scala.annotation.StaticAnnotation {
  inline def apply(defn: Any): Any = meta {
    val q"new $_(..$params)" = this
    val config = params.collectFirst{
      case arg"config = $x" => x
      case arg"$x" => x.syntax.parse[Term.Arg].get
    }.orNull
    defn match {
      case trt: Defn.Trait => DaoGenerator.generate(trt, config)
      case _ => abort(domala.message.Message.DOMALA4014.getMessage())
    }
  }
}
