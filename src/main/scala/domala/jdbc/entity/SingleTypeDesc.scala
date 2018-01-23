package domala.jdbc.entity

import org.seasar.doma

trait SingleTypeDesc[BASIC, HOLDER] {
  def getBasicClass: Class[BASIC]
  val wrapperProvider: java.util.function.Supplier[doma.wrapper.Wrapper[BASIC]]
}
