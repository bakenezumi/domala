package domala

import domala.internal.macros.meta.generator.DaoGenerator
import domala.jdbc.Config

import scala.collection.immutable.Seq
import scala.meta._

/** Indicates a DAO trait.
  *
  * The annotated trait must be a top level trait.
  *
  *
  * {{{
  * @Dao
  * trait EmployeeDao {
  *
  *   @Insert
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
  //noinspection ScalaUnusedSymbol
  inline def apply(defn: Any): Any = meta {
    val q"new $_(..$params)" = this
    val config = params.collectFirst {
      case arg"config = $x" => x
      case arg"$x" => x.syntax.parse[Term.Arg].get
    }.orNull
    defn match {
      case trt: Defn.Trait => DaoGenerator.generate(trt, config, None)
      case Term.Block(Seq(trt: Defn.Trait, companion: Defn.Object)) =>
        DaoGenerator.generate(trt, config, Some(companion))
      case _ => abort(domala.message.Message.DOMALA4014.getMessage())
    }
  }
}
