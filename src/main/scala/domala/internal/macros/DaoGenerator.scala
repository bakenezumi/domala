package domala.internal.macros

import domala.internal.macros.helper.{MacrosHelper, TypeHelper}
import domala.message.Message
import org.seasar.doma.internal.apt.meta.MetaConstants

import scala.collection.immutable.Seq
import scala.meta._

/**
  * @see [[https://github.com/domaframework/doma/blob/master/src/main/java/org/seasar/doma/internal/apt/DaoGenerator.java]]
  */
object DaoGenerator {

  def generate(trt: Defn.Trait, config: Term.Arg): Term.Block = {
    val stats = trt.templ.stats.map(l =>
      l.collect {
        case _def: Decl.Def => _def.copy(tparams = _def.tparams.map{
          // 型パラメータにClassTag付与
          case tp@Type.Param(_,_,_,_,_,Nil) => tp.copy(cbounds = Seq(Type.Name("scala.reflect.ClassTag")))
          case tp => tp
        })
      }.zip(from(0)).flatMap { t: (Decl.Def, Int) =>
        generateDef(trt.name, t._1, t._2)
      }
    )
    val defaultImpl =
      if(config == null)
        q"()"
      else
        q"def impl = new Internal(${Term.Name(config.syntax)})"
    val obj =
    q"""
    object ${Term.Name(trt.name.syntax)} {
      $defaultImpl
      def impl(implicit config: domala.jdbc.Config): ${Type.Name(trt.name.syntax)} = new Internal(config)

      class Internal(___config: domala.jdbc.Config) extends org.seasar.doma.internal.jdbc.dao.AbstractDao(___config)
      with ${Ctor.Ref.Name(trt.name.syntax)} {
        import scala.collection.JavaConverters._
        implicit val __sqlNodeRepository: domala.jdbc.SqlNodeRepository = ___config.getSqlNodeRepository
        ..${stats.get}
      }
    }
    """
    //logger.debug(obj)
    Term.Block(Seq(
      trt.copy(templ = trt.templ.copy(stats = trt.templ.stats.map(stat => stat.map {
        case _def: Decl.Def => _def.copy(
          // 型パラメータにClassTag付与
          tparams = _def.tparams.map{
            case tp@Type.Param(_,_,_,_,_,Nil) => tp.copy(cbounds = Seq(Type.Name("scala.reflect.ClassTag")))
            case tp => tp
          },
          // 処理済みdefアノテーション除去
          mods = Nil
        )
        case x => x
      }))),
      obj))
  }

  private def from(n: Int): Stream[Int] = n #:: from(n + 1)

  protected def generateDef(trtName: Type.Name, _def: Decl.Def, idx: Int): Seq[Defn] = {
    _def.paramss.flatten.foreach{p =>
      if(p.name.syntax.startsWith(MetaConstants.RESERVED_NAME_PREFIX)) {
        MacrosHelper.abort(Message.DOMALA4025, MetaConstants.RESERVED_NAME_PREFIX, trtName.syntax, _def.name.syntax)
      }
    }
    val internalMethodName = Term.Name(s"__method$idx")
    List(
      {
        //noinspection ScalaUnusedSymbol
        val paramClasses: Seq[Term.ApplyType] = _def.paramss.flatten.map(p => {
          if(TypeHelper.isWildcardType(p.decltpe.get))
            MacrosHelper.abort(Message.DOMALA4209, p.decltpe.get.syntax, trtName.syntax, _def.name.syntax)
          p.decltpe.get match {
            case t"$container[..$inner]" =>
              val placeHolder = inner.map(_ => t"_")
              q"classOf[${Type.Name(container.toString)}[..$placeHolder]]"
            case t"$_ => $_" =>
              q"classOf[scala.Function1[_, _]]"
            case _ => q"classOf[${TypeHelper.toType(p.decltpe.get)}]"
          }
        }) ++ _def.tparams.map(_ => q"classOf[scala.reflect.ClassTag[_]]") // ClassTag型パラメータを付与した場合、実行時は実パラメータとなる
        q"""private[this] val ${Pat.Var.Term(internalMethodName)} =
            org.seasar.doma.internal.jdbc.dao.AbstractDao.getDeclaredMethod(classOf[$trtName], ${_def.name.syntax}..$paramClasses)"""
      }, {
        val defImpl = _def.mods.collect {
          case mod"@Script(..$modParams)" => (ScriptGenerator, modParams)
          case mod"@Select(..$modParams)" => (SelectGenerator, modParams)
          case mod"@Insert" => (InsertGenerator, Nil)
          case mod"@Insert(..$modParams)" => (InsertGenerator, modParams)
          case mod"@Update" => (UpdateGenerator, Nil)
          case mod"@Update(..$modParams)" => (UpdateGenerator, modParams)
          case mod"@Delete" => (DeleteGenerator, Nil)
          case mod"@Delete(..$modParams)" => (DeleteGenerator, modParams)
          case mod"@BatchInsert" => (BatchInsertGenerator, Nil)
          case mod"@BatchInsert(..$modParams)" => (BatchInsertGenerator, modParams)
          case mod"@BatchUpdate" => (BatchUpdateGenerator, Nil)
          case mod"@BatchUpdate(..$modParams)" => (BatchUpdateGenerator, modParams)
          case mod"@BatchDelete" => (BatchDeleteGenerator, Nil)
          case mod"@BatchDelete(..$modParams)" => (BatchDeleteGenerator, modParams)
        } match {
          case Nil => MacrosHelper.abort(domala.message.Message.DOMALA4005, trtName.syntax, _def.name.syntax)
          case (generator, modParams) :: Nil =>
            generator.generate(trtName, _def, internalMethodName, modParams)
          case x => MacrosHelper.abort(domala.message.Message.DOMALA4087, x.head._1.annotationName, x(1)._1.annotationName, trtName.syntax, _def.name.syntax)
        }
        defImpl.copy(
          tparams = _def.tparams,
          paramss = _def.paramss,
          decltpe = Some(_def.decltpe)
        )
      }
    )
  }
}
