/*
 * Copyright 2004-2010 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package domala.internal.expr;

import static org.seasar.doma.internal.expr.ExpressionTokenType.*;

import org.seasar.doma.internal.expr.ExpressionException;
import org.seasar.doma.message.Message;

/**
 * Function型のパラメータをapplyを付けずに実行できるように拡張.
 * at `if (Character.isJavaIdentifierStart(c)))` block
 * <p>
 * Copyright 2004-2010 the Seasar Foundation and the Others.
 *
 * @see <a href="https://github.com/domaframework/doma/blob/2.19.0/src/main/java/org/seasar/doma/internal/expr/ExpressionTokenizer.java">original</a>
 */
@SuppressWarnings("UnnecessaryReturnStatement")
class ExpressionTokenizer extends org.seasar.doma.internal.expr.ExpressionTokenizer{

    public ExpressionTokenizer(String expression) {
        super(expression);
    }

    protected void peekOneChar(char c) {
        if (binaryOpAvailable) {
            if (c == '>') {
                type = GT_OPERATOR;
                binaryOpAvailable = false;
                return;
            } else if (c == '<') {
                type = LT_OPERATOR;
                binaryOpAvailable = false;
                return;
            } else if (c == '+') {
                type = ADD_OPERATOR;
                binaryOpAvailable = false;
                return;
            } else if (c == '-') {
                type = SUBTRACT_OPERATOR;
                binaryOpAvailable = false;
                return;
            } else if (c == '*') {
                type = MULTIPLY_OPERATOR;
                binaryOpAvailable = false;
                return;
            } else if (c == '/') {
                type = DIVIDE_OPERATOR;
                binaryOpAvailable = false;
                return;
            } else if (c == '%') {
                type = MOD_OPERATOR;
                binaryOpAvailable = false;
                return;
            }
        }
        if (Character.isWhitespace(c)) {
            type = WHITESPACE;
            return;
        } else if (c == ',') {
            type = COMMA_OPERATOR;
            return;
        } else if (c == '(') {
            type = OPENED_PARENS;
            return;
        } else if (c == ')') {
            type = CLOSED_PARENS;
            binaryOpAvailable = true;
            return;
        } else if (c == '!') {
            type = NOT_OPERATOR;
            return;
        } else if (c == '\'') {
            type = CHAR_LITERAL;
            if (buf.hasRemaining()) {
                buf.get();
                if (buf.hasRemaining()) {
                    char c3 = buf.get();
                    if (c3 == '\'') {
                        binaryOpAvailable = true;
                        return;
                    }
                }
            }
            throw new ExpressionException(Message.DOMA3016, expression,
                    buf.position());
        } else if (c == '"') {
            type = STRING_LITERAL;
            boolean closed = false;
            while (buf.hasRemaining()) {
                char c2 = buf.get();
                if (c2 == '"') {
                    if (buf.hasRemaining()) {
                        buf.mark();
                        char c3 = buf.get();
                        if (c3 != '"') {
                            buf.reset();
                            closed = true;
                            break;
                        }
                    } else {
                        closed = true;
                    }
                }
            }
            if (!closed) {
                throw new ExpressionException(Message.DOMA3004, expression,
                        buf.position());
            }
            binaryOpAvailable = true;
        } else if ((c == '+' || c == '-')) {
            buf.mark();
            if (buf.hasRemaining()) {
                char c2 = buf.get();
                if (Character.isDigit(c2)) {
                    peekNumber();
                    return;
                }
                buf.reset();
            }
            type = ILLEGAL_NUMBER_LITERAL;
        } else if (Character.isDigit(c)) {
            peekNumber();
        } else if (Character.isJavaIdentifierStart(c)) {
            type = VARIABLE;
            binaryOpAvailable = true;
            while (buf.hasRemaining()) {
                buf.mark();
                char c2 = buf.get();
                if (!Character.isJavaIdentifierPart(c2)) {
                    //Domala add
                    if (c2 == '(') {
                        type = FUNCTION_OPERATOR;
                        binaryOpAvailable = false;
                    }
                    buf.reset();
                    break;
                }
            }
        } else if (c == '.') {
            type = FIELD_OPERATOR;
            binaryOpAvailable = true;
            if (!buf.hasRemaining()) {
                throw new ExpressionException(Message.DOMA3021, expression,
                        buf.position());
            }
            buf.mark();
            char c2 = buf.get();
            if (Character.isJavaIdentifierStart(c2)) {
                while (buf.hasRemaining()) {
                    buf.mark();
                    char c3 = buf.get();
                    if (!Character.isJavaIdentifierPart(c3)) {
                        if (c3 == '(') {
                            type = METHOD_OPERATOR;
                            binaryOpAvailable = false;
                        }
                        buf.reset();
                        return;
                    }
                }
            } else {
                throw new ExpressionException(Message.DOMA3022, expression,
                        buf.position(), c2);
            }
        } else if (c == '@') {
            if (!buf.hasRemaining()) {
                throw new ExpressionException(Message.DOMA3023, expression,
                        buf.position());
            }
            buf.mark();
            char c2 = buf.get();
            if (Character.isJavaIdentifierStart(c2)) {
                while (buf.hasRemaining()) {
                    buf.mark();
                    char c3 = buf.get();
                    if (!Character.isJavaIdentifierPart(c3)) {
                        if (c3 == '(') {
                            type = FUNCTION_OPERATOR;
                            binaryOpAvailable = false;
                            buf.reset();
                            return;
                        } else if (c3 == '@') {
                            peekStaticMember();
                            return;
                        } else if (c3 == '.') {
                            while (buf.hasRemaining()) {
                                buf.mark();
                                char c4 = buf.get();
                                if (!Character.isJavaIdentifierPart(c4)) {
                                    if (c4 == '.') {
                                        continue;
                                    } else if (c4 == '@') {
                                        peekStaticMember();
                                        return;
                                    }
                                    throw new ExpressionException(
                                            Message.DOMA3031, expression,
                                            buf.position(), c4);
                                }
                            }
                            throw new ExpressionException(Message.DOMA3032,
                                    expression, buf.position());
                        }
                        throw new ExpressionException(Message.DOMA3025,
                                expression, buf.position());
                    }
                }
            } else {
                throw new ExpressionException(Message.DOMA3024, expression,
                        buf.position(), c2);
            }
        } else {
            type = OTHER;
        }
    }

}
