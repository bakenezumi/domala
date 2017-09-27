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
    DOMALA4001("戻り値の型は更新件数を示すIntでなければいけません。 at {0}.{1}"),
    DOMALA4007("戻り値のSeqに対する実型引数の型[{0}]はサポートされていません。 at {1}.{2}"),
    DOMALA4020("[{0}.{1}]のSQLが空です。"),
    DOMALA4040("戻り値の型は更新件数を示すArray[Int]でなければいけません。 at {0}.{1}"),
    DOMALA4069("[{0}.{1}]のSQLの解析に失敗しました。原因は次のものです。{2}"),
    // TODO: @ExternalDomainは未対応
    //DOMALA4096("クラス[{0}]は、永続対象の型としてサポートされていません。 at {1}.{2}。@ExternalDomainでマッピングすることを意図している場合、登録や設定が不足している可能性があります。@DomainConvertersを注釈したクラスと注釈処理のオプション（domala.domain.converters）を見直してください。"),
    DOMALA4092("[{0}.{1}]のSQLの妥当検査に失敗しました（[{3}]行目[{4}]番目の文字付近）。詳細は次のものです。{5} SQL[{2}]。"),
    DOMALA4096("クラス[{0}]は、永続対象の型としてサポートされていません。 at {1}.{2}。"),
    DOMALA4122("[{0}.{1}]のSQLの妥当検査に失敗しました。メソッドのパラメータ[{2}]がSQLで参照されていません。"),
    DOMALA4140("[{0}.{1}]のSQLの妥当検査に失敗しました（[{3}]行目[{4}]番目の文字付近）。/*%if ...*/の式[{5}]が型[{6}]として評価されましたが、boolean/Boolean型でなければいけません。SQL[{2}]"),
    DOMALA4141("[{0}.{1}]のSQLの妥当検査に失敗しました（[{3}]行目[{4}]番目の文字付近）。/*%elseif ...*/の式[{5}]が型[{6}]として評価されましたが、boolean/Boolean型でなければいけません。SQL[{2}]"),
    DOMALA4153("[{0}.{1}]のSQLの妥当検査に失敗しました（[{3}]行目[{4}]番目の文字付近）。バインド変数もしくはリテラル変数[{5}]に対応するパラメータの型は基本型もしくはドメインクラスでなければいけません。しかし、実際の型は[{6}]です。型を間違えていませんか？もしくはフィールドアクセスやメソッド呼び出しの記述を忘れていませんか？SQL[{2}]"),
    DOMALA4161("[{0}.{1}]のSQLの妥当検査に失敗しました（[{3}]行目[{4}]番目の文字付近）。括弧の前に位置するバインド変数もしくはリテラル変数[{5}]に対応するパラメータの型は基本型もしくはドメインクラスを要素としたjava.lang.Iterableのサブタイプでなければいけません。しかし、実際の型は[{6}]です。型を間違えていませんか？もしくはフィールドアクセスやメソッド呼び出しの記述を忘れていませんか？。SQL[{2}]"),
    DOMALA4222("エンティティクラスを@Insertや@Updateや@Deleteが注釈されたメソッドのパラメータとする場合、戻り値はdomala.jdbc.Result<E>でなければいけません。型パラメータ E の実型引数にはパラメータと同じエンティティクラスを指定してください。 at {0}.{1}"),
    DOMALA4223("エンティティクラスを@BatchInsertや@BatchUpdateや@BatchDeleteが注釈されたメソッドのパラメータとする場合、戻り値はdomala.jdbc.BatchResult<E>でなければいけません。型パラメータEの実型引数にはパラメータと同じエンティティクラスを指定してください。 at {0}.{1}"),
    DOMALA4235("Optionに対する実型引数の型[{0}]はサポートされていません。サポートされている型は次のものです。基本型、ドメインクラス、エンティティクラス。  at {1}.{2}"),
    DOMALA4244("Functionの1番目の実型引数の型は、Streamでなければいけません。 at {0}.{1}"),
    DOMALA4245("Streamの実型引数の型[{0}]はサポートされていません。 at {1}.{2}"),
    DOMALA4257("[{0}.{1}]のSQLの妥当検査に失敗しました（[{3}]行目[{4}]番目の文字付近）。/*%expand ...*/の式が使用されていますが、カラムの自動展開ができません。メソッドに@Selectが注釈され、結果セットのレコードがエンティティクラスにマッピングされていることを確認してください。SQL[{2}]"),
    DOMALA4270("[{0}.{1}]のSQLの妥当検査に失敗しました（[{3}]行目[{4}]番目の文字付近）。/*%populate */の式が使用されていますが、SET句の自動生成ができません。メソッドに@Updateまたは@BatchUpdateが注釈され、第一引数がエンティティクラスにマッピングされていることを確認してください。SQL[{2}]"),


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
