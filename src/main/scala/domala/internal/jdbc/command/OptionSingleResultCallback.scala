package domala.internal.jdbc.command

import org.seasar.doma.internal.util.AssertionUtil.assertNotNull
import org.seasar.doma.jdbc.{IterationCallback, IterationContext}

class OptionSingleResultCallback[TARGET](mapper: TARGET => Option[TARGET] = (x: TARGET) => Option(x)) extends IterationCallback[TARGET, Option[TARGET]] {
  assertNotNull(mapper, "")
  override def defaultResult: Option[TARGET] = None

  override def iterate(target: TARGET, context: IterationContext): Option[TARGET] = {
    context.exit()
    mapper(target)
  }
}
