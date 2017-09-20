package domala.jdbc.holder

import java.util.function.Supplier

import org.seasar.doma.jdbc.domain.AbstractDomainType
import org.seasar.doma.wrapper.Wrapper

abstract class AbstractHolderDesc[BASIC, HOLDER] protected (wrapperSupplier: Supplier[Wrapper[BASIC]]) extends AbstractDomainType[BASIC, HOLDER](wrapperSupplier) {
  def wrapper: java.util.function.Supplier[org.seasar.doma.wrapper.Wrapper[BASIC]]
}
