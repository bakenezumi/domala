package domala.jdbc

import org.seasar.doma.jdbc.domain.DomainType

package object holder {
  // Alias of Doma type
  type HolderDesc[BASIC, HOLDER] = DomainType[BASIC, HOLDER]
}
