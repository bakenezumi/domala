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
package domala.message;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import domala.internal.message.MessageResourceBundle;
import org.seasar.doma.message.MessageResource;

/**
 * デフォルトロケール用のメッセージの列挙です。
 *
 * @author bakenezumi
 *
 */
public enum Message implements MessageResource {
    // macros
    DOMALA4008("戻り値のSeqに対する実型引数の型[{0}]はサポートされていません。 at {1}.{2}"),
    DOMALA4020("[{0}.{1}]のSQLが空です。"),
    // TODO: @ExternalDomainは未対応
    //DOMALA4096("クラス[{0}]は、永続対象の型としてサポートされていません。 at {1}.{2}。@ExternalDomainでマッピングすることを意図している場合、登録や設定が不足している可能性があります。@DomainConvertersを注釈したクラスと注釈処理のオプション（domala.domain.converters）を見直してください。"),
    DOMALA4096("クラス[{0}]は、永続対象の型としてサポートされていません。 at {1}.{2}。"),
    DOMALA4222("エンティティクラスを@Insertや@Updateや@Deleteが注釈されたメソッドのパラメータとする場合、戻り値はdomala.jdbc.Result<E>でなければいけません。型パラメータ E の実型引数にはパラメータと同じエンティティクラスを指定してください。 at {0}.{1}"),
    DOMALA4235("Optionに対する実型引数の型[{0}]はサポートされていません。サポートされている型は次のものです。基本型、ドメインクラス、エンティティクラス。  at {1}.{2}"),
    DOMALA4244("Functionの1番目の実型引数の型は、Streamでなければいけません。 at {0}.{1}"),
    DOMALA4245("Streamの実型引数の型[{0}]はサポートされていません。 at {1}.{2}"),


    // macros(domala original)
    DOMALA6001("@Domainを注釈するcase classのパラメータは1つでなければなりません。"),
    DOMALA6002("@Domainを注釈するcase classのパラメータ名は`value`でなければいけません。"),;

    private final String messagePattern;

    private Message(String messagePattern) {
        this.messagePattern = messagePattern;
    }

    @Override
    public String getCode() {
        return name();
    }

    @Override
    public String getMessagePattern() {
        return messagePattern;
    }

    @Override
    public String getMessage(Object... args) {
        String simpleMessage = getSimpleMessageInternal(args);
        String code = name();
        return "[" + code + "] " + simpleMessage;
    }

    @Override
    public String getSimpleMessage(Object... args) {
        return getSimpleMessageInternal(args);
    }

    protected String getSimpleMessageInternal(Object... args) {
        try {
            boolean fallback = false;
            ResourceBundle bundle;
            try {
                bundle = ResourceBundle
                        .getBundle(MessageResourceBundle.class.getName());
            } catch (MissingResourceException ignored) {
                fallback = true;
                bundle = new MessageResourceBundle();
            }
            String code = name();
            String pattern = bundle.getString(code);
            String message = MessageFormat.format(pattern, args);
            return fallback ? "(This is a fallback message) " + message
                    : message;
        } catch (Throwable throwable) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            StringBuilder arguments = new StringBuilder();
            for (Object a : args) {
                arguments.append(a);
                arguments.append(", ");
            }
            return "[DOMALA9001] Failed to get a message because of following error : "
                    + sw + " : " + arguments;
        }
    }
}
