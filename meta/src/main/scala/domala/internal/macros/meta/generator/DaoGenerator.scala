package domala.internal.macros.meta.generator

import domala.internal.macros.meta.util.{MetaHelper, TypeUtil}
import domala.internal.macros.meta.util.NameConverters._
import domala.message.Message
import org.seasar.doma.internal.apt.meta.MetaConstants

import scala.collection.immutable.Seq
import scala.meta._

/**
  * @see [[https://github.com/domaframework/doma/blob/master/src/main/java/org/seasar/doma/internal/apt/DaoGenerator.java]]
  */
object DaoGenerator {

  def generate(trt: Defn.Trait, config: Term.Arg, maybeOriginalCompanion: Option[Defn.Object]): Term.Block = {
    val factory: Seq[Stat] = (
      if(config == null)
        Nil
      else
        Seq(q"def impl = new Internal(${Term.Name(config.syntax)}, ${Term.Name(config.syntax)}.getDataSource)")) ++
      q"""
      def impl(implicit config: domala.jdbc.Config): ${Type.Name(trt.name.syntax)} = new Internal(config, Option(config).getOrElse(throw new org.seasar.doma.DomaNullPointerException("config")).getDataSource)
      def impl(connection: java.sql.Connection)(implicit config: domala.jdbc.Config): ${Type.Name(trt.name.syntax)} = new Internal(config, connection)
      def impl(dataSource: javax.sql.DataSource)(implicit config: domala.jdbc.Config): ${Type.Name(trt.name.syntax)} = new Internal(config, dataSource)
      """.stats

    val (valStats, defStats) = trt.templ.stats.map {
      _.collect {
        case _def: Decl.Def => _def.copy(tparams = _def.tparams.map {
          // 型パラメータにClassTag付与
          case tp@Type.Param(_, _, _, _, _, Nil) => tp.copy(cbounds = Seq(Type.Name("scala.reflect.ClassTag")))
          case tp => tp
        })
      }.zip(from(0)).map{ t: (Decl.Def, Int) =>
        val internalMethodName = Term.Name(s"__method${t._2}")
        (generateMethodVal(trt.name, t._1,internalMethodName), generateDef(trt.name, t._1, internalMethodName))
      }.unzip
    }.getOrElse((Nil, Nil))

    val internalClass = q"""
      class Internal(___config: domala.jdbc.Config, dataSource: javax.sql.DataSource) extends org.seasar.doma.internal.jdbc.dao.AbstractDao(___config, dataSource)
      with ${Ctor.Ref.Name(trt.name.syntax)} {
        def this(config: domala.jdbc.Config, connection: java.sql.Connection) = this(config, org.seasar.doma.internal.jdbc.dao.DomalaAbstractDaoHelper.toDataSource(connection))
        import scala.collection.JavaConverters._
        implicit val __sqlNodeRepository: domala.jdbc.SqlNodeRepository = ___config.getSqlNodeRepository
        ..$defStats
      }
    """

    val newCompanion =
    q"""
    object ${Term.Name(trt.name.syntax)} {
      ..$factory
      ..$valStats
      $internalClass
    }
    """

    //logger.debug(companion)
    Term.Block(Seq(
      trt.copy(templ = trt.templ.copy(stats = trt.templ.stats.map(stat => stat.map {
        case _def: Decl.Def => _def.copy(
          // 型パラメータにClassTag付与
          tparams = _def.tparams.map{
            case tp@Type.Param(_,_,_,_,_,Nil) => tp.copy(cbounds = Seq(Type.Name("scala.reflect.ClassTag")))
            case tp => tp
          },
          // 警告抑制のため処理済みdefアノテーション除去
          // https://github.com/scala/bug/issues/9612
          mods = _def.mods.filter {
            case mod"@Select(..$_)" => false
            case mod"@Insert(..$_)" => false
            case mod"@Update(..$_)" => false
            case mod"@Delete(..$_)" => false
            case mod"@Script(..$_)" => false
            case mod"@BatchInsert(..$_)" => false
            case mod"@BatchUpdate(..$_)" => false
            case mod"@BatchDelete(..$_)" => false
            case _ => true
          }
        )
        case x => x
      }))),
      maybeOriginalCompanion.map(originalCompanion => originalCompanion.copy(
        templ = newCompanion.templ.copy(
          stats = Some(newCompanion.templ.stats.getOrElse(Nil) ++ originalCompanion.templ.stats.getOrElse(Nil))
      ))).getOrElse(newCompanion)
    ))
  }

  private def from(n: Int): Stream[Int] = n #:: from(n + 1)

  protected def generateMethodVal(trtName: Type.Name, _def: Decl.Def, internalMethodName: Term.Name): Defn.Val = {
    //noinspection ScalaUnusedSymbol
    val paramClasses: Seq[Term.ApplyType] = _def.paramss.flatten.map(p => {
      if(TypeUtil.isWildcardType(p.decltpe.get))
        MetaHelper.abort(Message.DOMALA4209, p.decltpe.get.syntax, trtName.syntax, _def.name.syntax)
      p.decltpe.get match {
        case t"$parameter => $_" =>
          q"classOf[${TypeUtil.toType(parameter)} => _]"
        case _ => q"classOf[${TypeUtil.toType(p.decltpe.get)}]"
      }
    }) ++ _def.tparams.map(_ => q"classOf[scala.reflect.ClassTag[_]]") // ClassTag型パラメータを付与した場合、実行時は実パラメータとなる
    q"""private[this] val ${Pat.Var.Term(internalMethodName)} =
          domala.internal.jdbc.dao.DaoUtil.getDeclaredMethod(classOf[$trtName], ${_def.name.literal}..$paramClasses)"""
  }
  protected def generateDef(trtName: Type.Name, _def: Decl.Def,  internalMethodName: Term.Name): Defn = {
    _def.paramss.flatten.foreach{p =>
      if(p.name.syntax.startsWith(MetaConstants.RESERVED_NAME_PREFIX)) {
        MetaHelper.abort(Message.DOMALA4025, MetaConstants.RESERVED_NAME_PREFIX, trtName.syntax, _def.name.syntax)
      }
    }

    val defImpl = _def.mods.collect {
      case mod"@Script" => (ScriptGenerator, Nil)
      case mod"@Script(..$modParams)" => (ScriptGenerator, modParams)
      case mod"@Select" => (SelectGenerator, Nil)
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
      case Nil => MetaHelper.abort(domala.message.Message.DOMALA4005, trtName.syntax, _def.name.syntax)
      case (generator, modParams) :: Nil =>
        generator.generate(trtName, _def, internalMethodName, modParams)
      case x => MetaHelper.abort(domala.message.Message.DOMALA4087, x.head._1.annotationName, x(1)._1.annotationName, trtName.syntax, _def.name.syntax)
    }
    defImpl.copy(
      tparams = _def.tparams,
      paramss = _def.paramss,
      decltpe = Some(_def.decltpe)
    )
  }

}
