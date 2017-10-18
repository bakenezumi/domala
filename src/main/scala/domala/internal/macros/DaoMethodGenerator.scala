package domala.internal.macros

import scala.collection.immutable.Seq
import scala.meta.{Decl, Defn, Term, Type}

trait DaoMethodGenerator {
  def generate(trtName: Type.Name, _def: Decl.Def, internalMethodName: Term.Name, args: Seq[Term.Arg]): Defn.Def
  def anotationName : String
}
