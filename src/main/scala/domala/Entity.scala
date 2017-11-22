package domala

import domala.internal.macros.EntityTypeGenerator
import domala.internal.macros.helper.MacrosHelper
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
  *@literal @Entity
  * case class Employee (
  *
  *  @literal @Id
  *  @literal @Column(name = "ID")
  *   id: Int,
  *
  *  @literal @Column(name = "EMPLOYEE_NAME")
  *   employeeName: String,
  *
  *  @literal @Version
  *  @literal @Column(name = "VERSION")
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
    val (cls, newCompanion) = defn match {
      case Term.Block(Seq(cls: Defn.Class, companion: Defn.Object)) =>
        val newCompanion = EntityTypeGenerator.generate(cls, params)
        (
          cls,
          newCompanion.copy(templ = newCompanion.templ.copy(
            stats = Some(newCompanion.templ.stats.getOrElse(Nil) ++ companion.templ.stats.getOrElse(Nil))
          ))
        )
      case cls: Defn.Class => (cls, EntityTypeGenerator.generate(cls, params))
      case _ => MacrosHelper.abort(domala.message.Message.DOMALA4015)
    }
    //logger.debug(newCompanion)
    Term.Block(Seq(
      // 警告抑制のため一部アノテーションを除去
      // https://github.com/scala/bug/issues/9612
      cls.copy(
        mods = cls.mods.filter {
          case mod"@Table(..$_)" => false
          case _ => true
        },
        ctor = cls.ctor.copy(paramss = cls.ctor.paramss.map(ps => ps.map(p => p.copy(mods = p.mods.filter {
          case mod"@Column(..$_)" => false
          case mod"@GeneratedValue(..$_)" => false
          case mod"@SequenceGenerator(..$_)" => false
          case mod"@TableGenerator(..$_)" => false
          case _ => true
        }))))
      ),
      newCompanion
    ))
  }
}
