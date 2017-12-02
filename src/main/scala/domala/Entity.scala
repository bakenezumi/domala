package domala

import domala.internal.macros.EntityTypeGenerator
import domala.internal.macros.util.MacrosHelper
import org.seasar.doma.jdbc.entity.EntityListener
import org.seasar.doma.jdbc.entity.{NamingType, NullEntityListener}

import scala.collection.immutable.Seq
import scala.meta._

/** Indicates an entity class.
  *
  * The entity class represents a database relation (table or SQL result set). An
  * instance of the class represents a row.
  *
  * The entity class must be defined as immutable.
  *
  * {{{
  * @Entity
  * case class Employee (
  *
  *   @Id
  *   @Column(name = "ID")
  *   id: Int,
  *
  *   @Column(name = "EMPLOYEE_NAME")
  *   employeeName: String,
  *
  *   @Version
  *   @Column(name = "VERSION")
  *   version: Int,
  *
  *   ...
  * )
  * }}}
  *
  * The entity instance is not required to be thread safe.
  *
  * @see [[domala.Table Table]]
  * @see [[domala.Column Column]]
  * @see [[domala.Id Id]]
  * @see [[domala.TenantId TenantId]]
  * @see [[domala.Version Version]]
  */
//noinspection ScalaUnusedSymbol
class Entity(listener: Class[_ <: EntityListener[_ <: Any]] = classOf[NullEntityListener[_]], val naming: NamingType = NamingType.NONE) extends scala.annotation.StaticAnnotation {
  inline def apply(defn: Any): Any = meta {
    val q"new $_(..$params)" = this
    defn match {
      case Term.Block(Seq(cls: Defn.Class, companion: Defn.Object)) =>
        EntityTypeGenerator.generate(cls, Some(companion), params)
      case cls: Defn.Class =>
        EntityTypeGenerator.generate(cls, None, params)
      case _ => MacrosHelper.abort(domala.message.Message.DOMALA4015)
    }
  }
}
