package domala.jdbc.command

import java.io.IOException
import org.seasar.doma.internal.util.AssertionUtil._
import org.seasar.doma.internal.jdbc.command.ScriptTokenizer
import org.seasar.doma.internal.jdbc.command.ScriptTokenType
import org.seasar.doma.internal.jdbc.command.ScriptTokenType._
import org.seasar.doma.jdbc.ScriptBlockContext
import org.seasar.doma.jdbc.JdbcException
import org.seasar.doma.message.Message

import domala.jdbc.query.SqlScriptQuery

class ScriptReader(query: SqlScriptQuery) {

  assertNotNull(query, "")
  
  protected val tokenizer: ScriptTokenizer = new ScriptTokenizer(query.getBlockDelimiter())
  protected val reader = new java.util.Scanner(query.scripts)
  protected var lineCount: Int = 0
  protected var lineNumber: Int = 0
  protected var endOfFile: Boolean = false
  protected var endOfLine: Boolean = true;

  def readSql(): String = {
    if (endOfFile) {
      return null;
    }
    try {
      val builder = new SqlBuilder()
      // TODO: Scalaっぽくする
      while (true) {
        if (endOfLine) {
          lineCount = lineCount + 1
          tokenizer.addLine(reader.nextLine())
          builder.notifyLineChanged()
        }
        var break = true
        while (break) {
          builder.build(tokenizer.nextToken(), tokenizer.getToken())
          if (builder.isTokenRequired()) {
            //
          } else if (builder.isLineRequired()) {
            break = false
          } else if (builder.isCompleted()) {
            return builder.getSql()
          } else {
            assertUnreachable()
          }
        }
      }
      assertUnreachable()
      ""
    } catch {
      case e: IOException =>
        throw new JdbcException(Message.DOMA2078, e,
                null, e)
    }
  }

  def getLineNumber(): Int = {
      lineNumber
  }

  def close() = {
    //
  }

  private class SqlBuilder {
    var tokenRequired = false
    var lineRequired = false
    var completed = false
    var buf = new StringBuilder(300)
    val wordList = new java.util.ArrayList[String]()
    var sqlBlockContext: ScriptBlockContext = null
    var lineChanged = false
    def SqlBuilder() = {
        sqlBlockContext = query.getConfig().getDialect()
                .createScriptBlockContext()
    }

    def build(tokenType: ScriptTokenType, token: String) = {
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
          appendToken(token);
          requireToken();
      case END_OF_LINE =>
          endOfLine = true
          requireLine()
      case STATEMENT_DELIMITER =>
          if (isInBlock()) {
              appendToken(token)
              requireToken()
          } else {
              complete()
          }
      case BLOCK_DELIMITER =>
          if (isSqlEmpty()) {
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

    def reset() = {
      endOfLine = false
      requireToken()
    }

    def isTokenRequired() = {
      tokenRequired
    }

    def requireToken() = {
      tokenRequired = true
      lineRequired = false
      completed = false
    }

    def isLineRequired() = {
      lineRequired
    }

    def requireLine() = {
      lineRequired = true
      tokenRequired = false
      completed = false
    }

    def isCompleted() = {
      completed
    }

    def complete() {
      completed = true
      tokenRequired = false
      lineRequired = false
    }

    def appendWord(word: String) = {
      sqlBlockContext.addKeyword(word)
    }

    def appendToken(token: String) = {
      appendWhitespaceIfNecessary()
      buf.append(token)
    }

    def appendWhitespaceIfNecessary() = {
      if (lineChanged) {
        if (buf.length() > 0) {
          var lastChar = buf.charAt(buf.length() - 1)
          if (!Character.isWhitespace(lastChar)) {
            buf.append(' ')
          }
        }
        lineChanged = false
      }
    }

    def notifyLineChanged() = {
      lineChanged = true
    }

    def isInBlock() = {
      sqlBlockContext.isInBlock()
    }

    def isSqlEmpty() = {
      buf.toString().trim().length() == 0
    }

    def getSql(): String = {
      if (!completed) {
        assertUnreachable();
      }
      val sql = buf.toString().trim()
      if (endOfFile && sql.length() == 0) null else sql
    }
  }


}

