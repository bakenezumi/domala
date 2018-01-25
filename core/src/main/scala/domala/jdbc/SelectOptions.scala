package domala.jdbc

import org.seasar.doma.jdbc.SelectOptionsAccessor

/**
  * The options for an SQL SELECT statement.
  *
  * {{{
  * SelectOptions options = SelectOptions.get.offset(10).limit(50).forUpdate
  * }}}
  */
class SelectOptions extends org.seasar.doma.jdbc.SelectOptions {
  override def forUpdate(): SelectOptions = {
    super.forUpdate()
    this
  }


  override def forUpdate(aliases: String*): SelectOptions = {
    super.forUpdate(aliases: _*)
    this
  }

  override def offset(offset: Int): SelectOptions = {
    super.offset(offset)
    this
  }

  override def forUpdateWait(waitSeconds: Int): SelectOptions = {
    super.forUpdateWait(waitSeconds)
    this
  }

  override def forUpdateWait(waitSeconds: Int, aliases: String*): SelectOptions = {
    super.forUpdateWait(waitSeconds, aliases: _*)
    this
  }

  override def count(): SelectOptions = {
    super.count()
    this
  }

  override def forUpdateNowait(): SelectOptions = {
    super.forUpdateNowait()
    this
  }

  override def forUpdateNowait(aliases: String*): SelectOptions = {
    super.forUpdateNowait(aliases: _*)
    this
  }

  override def limit(limit: Int): SelectOptions = {
    super.limit(limit)
    this
  }

  override def clone: SelectOptions = {
    val newInstance = SelectOptions.get
    val offsetValue = SelectOptionsAccessor.getOffset(this)
    if(offsetValue > 0) offset(offsetValue.toInt)
    val limitValue = SelectOptionsAccessor.getLimit(this)
    if(limitValue > 0) limit(limitValue.toInt)
    if (SelectOptionsAccessor.isCount(this)) newInstance.count()
    newInstance.forUpdateType = this.forUpdateType
    newInstance.waitSeconds = this.waitSeconds
    newInstance.aliases = this.aliases
    newInstance
  }

}

object SelectOptions {
  def get: SelectOptions = {
    new SelectOptions()
  }
}
