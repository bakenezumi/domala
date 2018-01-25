package domala.jdbc.holder

import java.util.function.Supplier

import org.seasar.doma.jdbc.domain.AbstractDomainType
import org.seasar.doma.wrapper.Wrapper

abstract class AbstractHolderDesc[BASIC, HOLDER] protected (val wrapperProvider: Supplier[Wrapper[BASIC]]) extends AbstractDomainType[BASIC, HOLDER](wrapperProvider) with HolderDesc[BASIC, HOLDER]

