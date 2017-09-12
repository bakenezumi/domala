package domala.internal.macros
import scala.meta._

case class QueryDefDecl(
  _def: Decl.Def,
  trtName: Type.Name,
  name: Term.Name,
  tparams: Seq[Type.Param],
  paramss: Seq[Seq[Term.Param]],
  tpe: Type
)

object QueryDefDecl{
  def of(trtName: Type.Name, _def: Decl.Def) = {
    val Decl.Def(mods, name, tparams, paramss, tpe) = _def
      QueryDefDecl(_def, trtName, _def.name, _def.tparams, _def.paramss, tpe)
  }
}