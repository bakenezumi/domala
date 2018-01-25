package domala.internal.macros.meta.generator

import scala.annotation.StaticAnnotation
import scala.collection.immutable.Seq
import scala.meta.{Decl, Defn, Term, Type}

trait DaoMethodGenerator {
  def generate(trtName: Type.Name, _def: Decl.Def, internalMethodName: Term.Name, args: Seq[Term.Arg]): Defn.Def
  def annotationClass: Class[_ <: StaticAnnotation]
  def annotationName: String = "@" + annotationClass.getSimpleName
}
