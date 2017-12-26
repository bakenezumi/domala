package domala.internal.jdbc.command


import domala.internal.jdbc.scalar.OptionDomainBridgeScalar
import org.seasar.doma.internal.jdbc.command.ScalarSingleResultHandler
import org.seasar.doma.jdbc.domain.DomainType

class OptionHolderSingleResultHandler[BASIC, HOLDER](holderType: DomainType[BASIC, HOLDER]) extends
  ScalarSingleResultHandler[BASIC, Option[HOLDER]] (
    () => new OptionDomainBridgeScalar(holderType.createOptionalScalar())
  )
