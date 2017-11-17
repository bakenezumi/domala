package domala.jdbc.interpolation

import domala.jdbc.Config
import domala.jdbc.builder.SelectBuilder

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
}
