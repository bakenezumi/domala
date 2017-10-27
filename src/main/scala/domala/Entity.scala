package domala

import domala.internal.macros.EntityTypeGenerator
import org.seasar.doma.jdbc.entity.EntityListener
import org.seasar.doma.jdbc.entity.{NamingType, NullEntityListener}

import scala.collection.immutable.Seq
import scala.meta._

class Entity(listener: Class[_ <: EntityListener[_ <: Any]] = classOf[NullEntityListener[_]], naming: NamingType = NamingType.NONE) extends scala.annotation.StaticAnnotation {
  inline def apply(defn: Any): Any = meta {
    val q"new $_(..$params)" = this
    val (cls, newCompanion) = defn match {
      case Term.Block(Seq(cls: Defn.Class, companion: Defn.Object)) => {
        val newCompanion = EntityTypeGenerator.generate(cls, params)
        (
          cls,
          newCompanion.copy(templ = newCompanion.templ.copy(
            stats = Some(newCompanion.templ.stats.getOrElse(Nil) ++ companion.templ.stats.getOrElse(Nil))
          ))
        )
      }
      case cls: Defn.Class => (cls, EntityTypeGenerator.generate(cls, params))
      case _ => abort(domala.message.Message.DOMALA4015.getMessage())
    }
    //logger.debug(newCompanion)
    Term.Block(Seq(
      // 処理済みアノテーション除去
      cls.copy(
        mods = cls.mods.filter {
          case mod"@Table(..$_)" => false
          case _ => true
        },
        ctor = cls.ctor.copy(paramss = cls.ctor.paramss.map(ps => ps.map(p => p.copy(mods = p.mods.filter{
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
