package domala.jdbc.interpolation

import domala.jdbc.Config
import domala.jdbc.builder.{SelectBuilder, UpdateBuilder}
import domala.message.Message
import org.seasar.doma.DomaException

object SQLInterpolator {
  def select(context: StringContext, args: Seq[Any], config: Config): SelectStatement = {
    val builder = SelectBuilder.newInstance(config)
    val params = args.toIterator
    context.parts.foreach{ part =>
      builder.sql(part)
      if(params.hasNext) {
        val param = params.next
        if(classOf[Iterable[_]].isAssignableFrom(param.getClass)) {
          val list = param.asInstanceOf[Iterable[Any]]
          val clazz = list.headOption.map(_.getClass).getOrElse(classOf[Any])
          builder.params(clazz.asInstanceOf[Class[Any]], list)
        } else {
          builder.param(param.getClass.asInstanceOf[Class[Any]], param)
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
        val param = params.next
          builder.param(param.getClass.asInstanceOf[Class[Any]], param)
      }
    }
    UpdateStatement(builder)
  }

}
