package domala.internal.jdbc.command

import java.util.function.Supplier

import domala.internal.jdbc.scalar.OptionBasicScalar
import org.seasar.doma.internal.jdbc.command.ScalarSingleResultHandler
import org.seasar.doma.wrapper.Wrapper

class OptionBasicSingleResultHandler[BASIC](supplier: Supplier[Wrapper[BASIC]], primitive: Boolean) extends
  ScalarSingleResultHandler[BASIC, Option[BASIC]](() => new OptionBasicScalar(supplier))
