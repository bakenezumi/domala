package domala.jdbc.holder

trait HolderCompanion[BASIC, HOLDER] {
  val holderDesc: HolderDesc[BASIC, HOLDER]

}
