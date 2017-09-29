package domala.internal.macros

import scala.collection.immutable.Seq
import scala.meta._

/**
  * @see [[https://github.com/domaframework/doma/blob/master/src/main/java/org/seasar/doma/internal/apt/DaoGenerator.java]]
  */
object DaoGenerator {

  def generate(trt: Defn.Trait, config: Term.Arg): Term.Block = {
    val stats = trt.templ.stats.map(l =>
      l.collect {
        case _def: Decl.Def => _def
      }.zip(from(0)).flatMap { t: (Decl.Def, Int) =>
        generateDef(trt.name, t._1, t._2)
      }
    )

    val obj =
    q"""
    object ${Term.Name(trt.name.syntax)} {
      def impl = new Internal( ${Term.Name(config.syntax)})
      def impl(config: org.seasar.doma.jdbc.Config): ${Type.Name(trt.name.syntax)} = new Internal(config)

      class Internal(config: org.seasar.doma.jdbc.Config) extends org.seasar.doma.internal.jdbc.dao.AbstractDao(config)
      with ${Ctor.Ref.Name(trt.name.syntax)} {
        import scala.collection.JavaConverters._
        import scala.compat.java8.OptionConverters._
        import scala.compat.java8.StreamConverters._
        ..${stats.get}
      }
    }
    """
    //logger.debug(obj)
    Term.Block(Seq(
      // 処理済みdefアノテーション除去
      trt.copy(templ = trt.templ.copy(stats = trt.templ.stats.map(stat => stat.map(_ match {
        case _def: Decl.Def => _def.copy(mods = Nil)
        case x => x
      })))),
      obj))
  }

  private def from(n: Int): Stream[Int] = n #:: from(n + 1)

  protected def generateDef(trtName: Type.Name, _def: Decl.Def, idx: Int): Seq[Defn] = {
    val internalMethodName = Term.Name(s"__method$idx")
    List(
      {
        val paramClasses = _def.paramss.flatten.map(p => {
          p.decltpe.get match {
            case t"$container[..$inner]" => {
              val placeHolder = inner.map(_ => t"_")
              q"classOf[${Type.Name(container.toString)}[..$placeHolder]]"
            }
            case t"$_ => $_" => {
              q"classOf[scala.Function1[_, _]]"
            }
            case _ => q"classOf[${Type.Name(p.decltpe.get.toString)}]"
          }
        })
        q"""private val ${Pat.Var.Term(internalMethodName)} =
            org.seasar.doma.internal.jdbc.dao.AbstractDao.getDeclaredMethod(classOf[$trtName], ${_def.name.syntax}..$paramClasses)"""
      }, {
        _def.mods.collectFirst {
          case mod"@Script(sql = $sql)" => ScriptGenerator.generate(trtName, _def, internalMethodName, sql)
          case mod"@Select(..$modParams)" => SelectGenerator.generate(trtName, _def, internalMethodName, modParams)
          case mod"@Insert" => InsertGenerator.generate(trtName, _def, internalMethodName, Nil)
          case mod"@Insert(..$modParams)" => InsertGenerator.generate(trtName, _def, internalMethodName, modParams)
          case mod"@Update" => UpdateGenerator.generate(trtName, _def, internalMethodName, Nil)
          case mod"@Update(..$modParams)" => UpdateGenerator.generate(trtName, _def, internalMethodName, modParams)
          case mod"@Delete" => DeleteGenerator.generate(trtName, _def, internalMethodName, Nil)
          case mod"@Delete(..$modParams)" => DeleteGenerator.generate(trtName, _def, internalMethodName, modParams)
          case mod"@BatchInsert" => BatchInsertGenerator.generate(trtName, _def, internalMethodName, Nil)
          case mod"@BatchInsert(..$modParams)" => BatchInsertGenerator.generate(trtName, _def, internalMethodName, modParams)
          case mod"@BatchUpdate" => BatchUpdateGenerator.generate(trtName, _def, internalMethodName, Nil)
          case mod"@BatchUpdate(..$modParams)" => BatchUpdateGenerator.generate(trtName, _def, internalMethodName, modParams)
          case mod"@BatchDelete" => BatchDeleteGenerator.generate(trtName, _def, internalMethodName, Nil)
          case mod"@BatchDelete(..$modParams)" => BatchDeleteGenerator.generate(trtName, _def, internalMethodName, modParams)
        }.getOrElse(
          abort(_def.pos, org.seasar.doma.message.Message.DOMA4005.getMessage(trtName.syntax, _def.name.syntax))
        ).copy(
          tparams = _def.tparams,
          paramss = _def.paramss,
          decltpe = Some(_def.decltpe)
        )
      }
    )
  }
}
