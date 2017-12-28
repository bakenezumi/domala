package domala.jdbc

import org.seasar.doma

package object holder {
  // Alias of Doma type
  type HolderDesc[BASIC, HOLDER] = doma.jdbc.domain.DomainType[BASIC, HOLDER]
}
