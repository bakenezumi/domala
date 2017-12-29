package domala.jdbc.holder

import org.seasar.doma

trait HolderDesc[BASIC, HOLDER] extends doma.jdbc.domain.DomainType[BASIC, HOLDER] {
  val wrapperProvider: java.util.function.Supplier[doma.wrapper.Wrapper[BASIC]]
  override def getBasicClass: Class[BASIC]

}
