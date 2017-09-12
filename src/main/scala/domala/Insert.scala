package domala

import domala.internal.macros.{AutoModifyQueryGenerator, QueryDefDecl}
import scala.collection.immutable.Seq
import scala.meta._

class Insert extends scala.annotation.StaticAnnotation

object InsertGenerator {
  def generate(trtName: Type.Name, _def: Decl.Def, internalMethodName: Term.Name): Defn.Def = {
    val defDecl = QueryDefDecl.of(trtName, _def)
    val (paramName, paramTpe) = AutoModifyQueryGenerator.extractParameter(defDecl)
    val query = q"getQueryImplementors.createAutoInsertQuery($internalMethodName, ${Term.Name(paramTpe.value)})"
    val command = q"getCommandImplementors.createInsertCommand($internalMethodName, __query)"
    val otherQuerySettings = Seq[Stat](
      q"__query.setNullExcluded(false)",
      q"__query.setIncludedPropertyNames()",
      q"__query.setExcludedPropertyNames()"
    )
    AutoModifyQueryGenerator.generate(defDecl, paramName, paramTpe, internalMethodName, query, otherQuerySettings, command)
  }
}
