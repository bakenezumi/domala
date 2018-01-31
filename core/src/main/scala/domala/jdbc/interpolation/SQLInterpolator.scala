package domala.jdbc.interpolation

import domala.jdbc.Config
import domala.jdbc.builder.{SelectBuilder, UpdateBuilder}
import domala.message.Message
import org.seasar.doma.DomaException

object SQLInterpolator {
  def select(context: StringContext, args: Seq[Any], config: Config): SelectStatement = {
    val builder: SelectBuilder = SelectBuilder.newInstance(config)
    val params = args.toIterator
    context.parts.foreach{ part =>
      builder.sql(part)
      if(params.hasNext) {
        val (clazz, param, paramType) = extractParam(params.next)
        paramType match {
          case ParamType.Iterable =>
            builder.params(clazz, param.asInstanceOf[Iterable[Any]])
          case ParamType.Single =>
            builder.param(clazz, param)
        }
      }
    }

    SelectStatement(builder)
  }

  def script(context: StringContext, args: Seq[Any], config: Config): ScriptStatement = {
    if(args.nonEmpty) throw new DomaException(Message.DOMALA6013)
    ScriptStatement(context.parts.head, config)
  }

  def update(context: StringContext, args: Seq[Any], config: Config): UpdateStatement = {
    val builder = UpdateBuilder.newInstance(config)
    val params = args.toIterator
    context.parts.foreach { part =>
      builder.sql(part)
      if(params.hasNext) {
        val (clazz, param, paramType) = extractParam(params.next)
        paramType match {
          case ParamType.Iterable =>
            builder.params(clazz, param.asInstanceOf[Iterable[Any]])
          case ParamType.Single =>
            builder.param(clazz, param)
        }
      }
    }
    UpdateStatement(builder)
  }

  sealed trait ParamType
  object ParamType {
    object Iterable extends ParamType
    object Single extends ParamType
  }

  private def extractParam(param: Any): (Class[Any], Any, ParamType) = {
    if(classOf[Iterable[_]].isAssignableFrom(param.getClass)) {
      val list = param.asInstanceOf[Iterable[Any]]
      val clazz = list.headOption.map(_.getClass).getOrElse(classOf[Any])
      (clazz.asInstanceOf[Class[Any]], list, ParamType.Iterable)
    } else if(classOf[Option[_]].isAssignableFrom(param.getClass)) {
      val opt = param.asInstanceOf[Option[Any]]
      val clazz = opt.map(_.getClass).getOrElse(classOf[Any])
      (clazz.asInstanceOf[Class[Any]], opt.orNull, ParamType.Single)
    } else {
      (param.getClass.asInstanceOf[Class[Any]], param, ParamType.Single)
    }
  }

}
