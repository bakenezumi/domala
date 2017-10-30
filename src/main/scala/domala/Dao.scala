package domala

import domala.internal.macros.DaoGenerator
import domala.jdbc.Config

import scala.meta._

/** Indicates a DAO trait.
  *
  * The annotated trait must be a top level trait.
  *
  *
  * {{{
  *@literal @Dao
  * trait EmployeeDao {
  *
  *  @literal @Insert
  *   def insert(Employee employee): Int
  * }
  * }}}
  *
  *
  * @see [[domala.BatchDelete BatchDelete]]
  * @see [[domala.BatchInsert BatchInsert]]
  * @see [[domala.BatchUpdate BatchUpdate]]
  * @see [[domala.Delete Delete]]
  * @see [[domala.Insert Insert]]
  * @see [[domala.Select Select]]
  * @see [[domala.Script Script]]
  * @see [[domala.Update Update]]
  */
class Dao(config: Config = null) extends scala.annotation.StaticAnnotation {
  inline def apply(defn: Any): Any = meta {
    val q"new $_(..$params)" = this
    val config = params.collectFirst {
      case arg"config = $x" => x
      case arg"$x" => x.syntax.parse[Term.Arg].get
    }.orNull
    defn match {
      case trt: Defn.Trait => DaoGenerator.generate(trt, config)
      case _ => abort(domala.message.Message.DOMALA4014.getMessage())
    }
  }
}
