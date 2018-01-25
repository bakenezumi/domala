package domala.jdbc.holder

import domala.jdbc.entity.SingleTypeDesc
import org.seasar.doma

trait HolderDesc[BASIC, HOLDER] extends doma.jdbc.domain.DomainType[BASIC, HOLDER] with SingleTypeDesc[BASIC, HOLDER] {
  val wrapperProvider: java.util.function.Supplier[doma.wrapper.Wrapper[BASIC]]
  override def getBasicClass: Class[BASIC]
}

object HolderDesc {
  def of[BASIC, HOLDER](desc: SingleTypeDesc[BASIC, HOLDER]): HolderDesc[BASIC, HOLDER] = desc match {
    case h: HolderDesc[BASIC, HOLDER] => h
    case _ => null
  }
}
