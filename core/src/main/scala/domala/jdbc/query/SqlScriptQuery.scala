package domala.jdbc.query

import org.seasar.doma.internal.util.AssertionUtil.assertNotNull
import org.seasar.doma.internal.util.AssertionUtil.assertUnreachable

import org.seasar.doma.jdbc.query.AbstractQuery
import org.seasar.doma.jdbc.query.ScriptQuery 
import org.seasar.doma.jdbc.SqlLogType


class SqlScriptQuery(val scripts: String) extends AbstractQuery with ScriptQuery {

  protected var blockDelimiter = ""

  protected var haltOnError = true

  protected var sqlLogType: SqlLogType = org.seasar.doma.jdbc.SqlLogType.FORMATTED

  def setBlockDelimiter(blockDelimiter: String) {
    this.blockDelimiter = blockDelimiter
  }

  def setHaltOnError(haltOnError: Boolean) {
    this.haltOnError = haltOnError
  }

  def setSqlLogType(sqlLogType: SqlLogType) {
    this.sqlLogType = sqlLogType
  }

  override def prepare(): Unit = {
    super.prepare()
    assertNotNull(blockDelimiter, "")
    if (blockDelimiter.isEmpty) {
      blockDelimiter = config.getDialect.getScriptBlockDelimiter
    }
  }

  override def complete(): Unit = {
  }

  override def getQueryTimeout: Int = {
    -1
  }

  override def getSql: Null = {
    assertUnreachable()
    null
  }

  override def getBlockDelimiter: String = {
    blockDelimiter
  }

  override def getHaltOnError: Boolean = {
    haltOnError
  }

  override def getSqlLogType: SqlLogType = {
    sqlLogType
  }

  override def getScriptFilePath: String = ""
  override def getScriptFileUrl: java.net.URL = null
}