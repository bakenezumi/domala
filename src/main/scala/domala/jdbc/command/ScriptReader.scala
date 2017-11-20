package domala.jdbc.command

import java.io.{BufferedReader, StringReader}

import domala.jdbc.query.SqlScriptQuery
import org.seasar.doma.internal.util.AssertionUtil._
import org.seasar.doma.internal.jdbc.command.ScriptTokenizer
import org.seasar.doma.internal.jdbc.command.ScriptTokenType
import org.seasar.doma.internal.jdbc.command.ScriptTokenType._
import org.seasar.doma.internal.util.IOUtil
import org.seasar.doma.jdbc.ScriptBlockContext

class ScriptReader(query: SqlScriptQuery) {

  assertNotNull(query, "")

  protected val tokenizer: ScriptTokenizer = new ScriptTokenizer(
    query.getBlockDelimiter())
  protected val reader = new BufferedReader(new StringReader(query.scripts))
  protected var lineCount: Int = 0
  protected var lineNumber: Int = 0
  protected var endOfFile: Boolean = false
  protected var endOfLine: Boolean = true

  def readSql(): String = {
    if (endOfFile) {
      return null
    }
    val builder = new SqlBuilder()
    while (true) {
      if (endOfLine) {
        lineCount = lineCount + 1
        tokenizer.addLine(reader.readLine())
        builder.notifyLineChanged()
      }
      var break = true
      while (break) {
        builder.build(tokenizer.nextToken(), tokenizer.getToken)
        if (builder.isTokenRequired) {
          //
        } else if (builder.isLineRequired) {
          break = false
        } else if (builder.isCompleted) {
          return builder.getSql
        } else {
          assertUnreachable()
        }
      }
    }
    assertUnreachable()
  }

  def getLineNumber: Int = lineNumber

  def close(): Unit = {IOUtil.close(reader)}

  private class SqlBuilder {
    var tokenRequired = false
    var lineRequired = false
    var completed = false
    val buf = new StringBuilder(300)
    val wordList = new java.util.ArrayList[String]()
    val sqlBlockContext: ScriptBlockContext = query.getConfig.getDialect.createScriptBlockContext
    var lineChanged = false

    def build(tokenType: ScriptTokenType, token: String): Unit = {
      reset()
      if (buf.length() == 0) {
        lineNumber = lineCount
      }
      tokenType match {
        case WORD =>
          appendWord(token)
          appendToken(token)
          requireToken()
        case QUOTE =>
          appendToken(token)
          requireToken()
        case OTHER =>
          appendToken(token)
          requireToken()
        case END_OF_LINE =>
          endOfLine = true
          requireLine()
        case STATEMENT_DELIMITER =>
          if (isInBlock) {
            appendToken(token)
            requireToken()
          } else {
            complete()
          }
        case BLOCK_DELIMITER =>
          if (isSqlEmpty) {
            requireToken()
          } else {
            complete()
          }
        case END_OF_FILE =>
          endOfFile = true
          complete()
        case _ =>
          requireToken()
      }
    }

    def reset(): Unit = {
      endOfLine = false
      requireToken()
    }

    def isTokenRequired: Boolean = tokenRequired

    def requireToken(): Unit = {
      tokenRequired = true
      lineRequired = false
      completed = false
    }

    def isLineRequired: Boolean = lineRequired

    def requireLine(): Unit = {
      lineRequired = true
      tokenRequired = false
      completed = false
    }

    def isCompleted: Boolean = {
      completed
    }

    def complete(): Unit = {
      completed = true
      tokenRequired = false
      lineRequired = false
    }

    def appendWord(word: String): Unit = {
      sqlBlockContext.addKeyword(word)
    }

    def appendToken(token: String): StringBuilder = {
      appendWhitespaceIfNecessary()
      buf.append(token)
    }

    def appendWhitespaceIfNecessary(): Unit = {
      if (lineChanged) {
        if (buf.length() > 0) {
          val lastChar = buf.charAt(buf.length() - 1)
          if (!Character.isWhitespace(lastChar)) {
            buf.append(' ')
          }
        }
        lineChanged = false
      }
    }

    def notifyLineChanged(): Unit = {
      lineChanged = true
    }

    def isInBlock: Boolean = sqlBlockContext.isInBlock

    def isSqlEmpty: Boolean = buf.toString().trim().length() == 0

    def getSql: String = {
      if (!completed) {
        assertUnreachable()
      }
      val sql = buf.toString().trim()
      if (endOfFile && sql.length() == 0) null else sql
    }
  }

}
