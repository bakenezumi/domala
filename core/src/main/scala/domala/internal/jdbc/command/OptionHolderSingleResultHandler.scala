package domala.internal.jdbc.command


import domala.internal.jdbc.scalar.OptionDomainBridgeScalar
import domala.jdbc.holder.HolderDesc
import org.seasar.doma.internal.jdbc.command.ScalarSingleResultHandler

class OptionHolderSingleResultHandler[BASIC, HOLDER](holderDesc: HolderDesc[BASIC, HOLDER]) extends
  ScalarSingleResultHandler[BASIC, Option[HOLDER]] (
    () => new OptionDomainBridgeScalar(holderDesc.createOptionalScalar())
  )
