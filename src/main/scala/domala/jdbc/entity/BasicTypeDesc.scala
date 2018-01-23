package domala.jdbc.entity

import domala.wrapper.Wrapper

case class BasicTypeDesc[BASIC] (
  basicClass: Class[BASIC],
  override val wrapperProvider: java.util.function.Supplier[Wrapper[BASIC]]
) extends SingleTypeDesc[BASIC, Any] {
  override def getBasicClass: Class[BASIC] = basicClass
}
