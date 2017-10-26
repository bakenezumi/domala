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
    DOMALA4002(org.seasar.doma.message.Message.DOMA4002.getMessagePattern()),
    DOMALA4003(org.seasar.doma.message.Message.DOMA4003.getMessagePattern()),
    DOMALA4005(org.seasar.doma.message.Message.DOMA4005.getMessagePattern()),
    DOMALA4007("戻り値のSeqに対する実型引数の型[{0}]はサポートされていません。 at {1}.{2}"),
    DOMALA4008(org.seasar.doma.message.Message.DOMA4008.getMessagePattern()),
    DOMALA4014("@Daoはトレイト以外には注釈できません。"),
    DOMALA4015("クラス以外には注釈できません。"),
    DOMALA4020("[{0}.{1}]のSQLが空です。"),
    DOMALA4024(org.seasar.doma.message.Message.DOMA4024.getMessagePattern()),
    DOMALA4025(org.seasar.doma.message.Message.DOMA4025.getMessagePattern()),
    DOMALA4030(org.seasar.doma.message.Message.DOMA4030.getMessagePattern()),
    DOMALA4031(org.seasar.doma.message.Message.DOMA4031.getMessagePattern()),
    DOMALA4033(org.seasar.doma.message.Message.DOMA4033.getMessagePattern()),
    DOMALA4034(org.seasar.doma.message.Message.DOMA4034.getMessagePattern()),
    DOMALA4035(org.seasar.doma.message.Message.DOMA4035.getMessagePattern()),
    DOMALA4036(org.seasar.doma.message.Message.DOMA4036.getMessagePattern()),
    DOMALA4037(org.seasar.doma.message.Message.DOMA4037.getMessagePattern()),
    DOMALA4040("戻り値の型は更新件数を示すArray[Int]でなければいけません。 at {0}.{1}"),
    DOMALA4042("パラーメータの型はscala.collection.Iterableのサブタイプでなければいけません。 at {0}.{1}"),
    DOMALA4043("scala.collection.Iterableのサブタイプに対する実型引数はエンティティクラスでなければいけません。 at {0}.{1}"),
    DOMALA4051(org.seasar.doma.message.Message.DOMA4051.getMessagePattern()),
    DOMALA4053(org.seasar.doma.message.Message.DOMA4053.getMessagePattern()),
    // TODO: ストアド系未実装 DOMA4059 - DOMA4066
    DOMALA4067(org.seasar.doma.message.Message.DOMA4067.getMessagePattern()),
    DOMALA4069("[{0}.{1}]のSQLの解析に失敗しました。原因は次のものです。{2}"),
    DOMALA4071(org.seasar.doma.message.Message.DOMA4071.getMessagePattern()),
    DOMALA4072(org.seasar.doma.message.Message.DOMA4072.getMessagePattern()),
    DOMALA4073(org.seasar.doma.message.Message.DOMA4073.getMessagePattern()),
    // TODO: ファクトリ系未実装 DOMA4076, DOMA4078
    DOMALA4084(org.seasar.doma.message.Message.DOMA4084.getMessagePattern()),
    DOMALA4085(org.seasar.doma.message.Message.DOMA4085.getMessagePattern()),
    DOMALA4086(org.seasar.doma.message.Message.DOMA4086.getMessagePattern()),
    DOMALA4087(org.seasar.doma.message.Message.DOMA4087.getMessagePattern()),
    DOMALA4088(org.seasar.doma.message.Message.DOMA4088.getMessagePattern()),
    DOMALA4089(org.seasar.doma.message.Message.DOMA4089.getMessagePattern()),
    //DOMALA4090
    //DOMALA4091
    DOMALA4092("[{0}.{1}]のSQLの妥当検査に失敗しました（[{3}]行目[{4}]番目の文字付近）。詳細は次のものです。{5} SQL[{2}]。"),
    DOMALA4093(org.seasar.doma.message.Message.DOMA4093.getMessagePattern()),
    DOMALA4095(org.seasar.doma.message.Message.DOMA4095.getMessagePattern()),
    // @ExternalDomainは未対応
    //DOMALA4096("クラス[{0}]は、永続対象の型としてサポートされていません。 at {1}.{2}。@ExternalDomainでマッピングすることを意図している場合、登録や設定が不足している可能性があります。@DomainConvertersを注釈したクラスと注釈処理のオプション（domala.domain.converters）を見直してください。"),
    DOMALA4096("クラス[{0}]は、永続対象の型としてサポートされていません。 at {1}.{2}。"),
    // TODO: ファクトリ系未実装 DOMA4097 - DOMA4101
    DOMALA4102(org.seasar.doma.message.Message.DOMA4102.getMessagePattern()),
    DOMALA4105("クラス以外には注釈できません。 at {0}"),
    // TODO: ストアド系未実装 DOMA4108
    DOMALA4109("戻り値であるSeqのサブタイプ[{0}]には実型引数が必須です。 at {1}.{2}"),
    // TODO: ストアド系未実装 DOMA4111
    // TODO: ストアド系未実装 DOMA4112
    DOMALA4113(org.seasar.doma.message.Message.DOMA4113.getMessagePattern()),
    DOMALA4114(org.seasar.doma.message.Message.DOMA4114.getMessagePattern()),
    // 式内のConstructor未実装 DOMA4115
    DOMALA4116(org.seasar.doma.message.Message.DOMA4116.getMessagePattern()),
    DOMALA4117(org.seasar.doma.message.Message.DOMA4117.getMessagePattern()),
    DOMALA4118(org.seasar.doma.message.Message.DOMA4118.getMessagePattern()),
    DOMALA4119(org.seasar.doma.message.Message.DOMA4119.getMessagePattern()),
    DOMALA4120(org.seasar.doma.message.Message.DOMA4120.getMessagePattern()),
    DOMALA4121(org.seasar.doma.message.Message.DOMA4121.getMessagePattern()),
    DOMALA4122("[{0}.{1}]のSQLの妥当検査に失敗しました。メソッドのパラメータ[{2}]がSQLで参照されていません。"),

    // OriginalStates未実装 DOMA4125
    DOMALA4126(org.seasar.doma.message.Message.DOMA4126.getMessagePattern()),
    // 式内のConstructor未実装 DOMA4127
    // @HolderのfactoryMethod未実装 DOMA4132
    // OriginalStates未実装 DOMA4135
    // 式内のConstructor未実装 DOMA4138
    DOMALA4139(org.seasar.doma.message.Message.DOMA4139.getMessagePattern()),
    DOMALA4140("[{0}.{1}]のSQLの妥当検査に失敗しました（[{3}]行目[{4}]番目の文字付近）。/*%if ...*/の式[{5}]が型[{6}]として評価されましたが、Boolean型でなければいけません。SQL[{2}]"),
    DOMALA4141("[{0}.{1}]のSQLの妥当検査に失敗しました（[{3}]行目[{4}]番目の文字付近）。/*%elseif ...*/の式[{5}]が型[{6}]として評価されましたが、Boolean型でなければいけません。SQL[{2}]"),

    DOMALA4145(org.seasar.doma.message.Message.DOMA4145.getMessagePattern()),
    DOMALA4146(org.seasar.doma.message.Message.DOMA4146.getMessagePattern()),
    DOMALA4147(org.seasar.doma.message.Message.DOMA4147.getMessagePattern()),
    // 式内のstaticフィールド参照未対応 DOMA4148
    DOMALA4149("[{0}.{1}]のSQLの妥当検査に失敗しました（[{3}]行目[{4}]番目の文字付近）。/*%for ...*/の式[{5}]が型[{6}]として評価されましたが、scala.collection.Iterable型でなければいけません。SQL[{2}]"),
    DOMALA4150("[{0}.{1}]のSQLの妥当検査に失敗しました（[{3}]行目[{4}]番目の文字付近）。/*%for ...*/の式[{5}]の型[{6}]の実型引数が不明です。SQL[{2}]"),
    DOMALA4153("[{0}.{1}]のSQLの妥当検査に失敗しました（[{3}]行目[{4}]番目の文字付近）。バインド変数もしくはリテラル変数[{5}]に対応するパラメータの型は基本型もしくはホルダークラスでなければいけません。しかし、実際の型は[{6}]です。型を間違えていませんか？もしくはフィールドアクセスやメソッド呼び出しの記述を忘れていませんか？SQL[{2}]"),
    // 抽象型Entity未実装 DOMA4154-4157

    DOMALA4160("scala.collection.Iterableのサブタイプをワイルカード型にしてはいけません。 at {0}.{1}"),
    DOMALA4161("[{0}.{1}]のSQLの妥当検査に失敗しました（[{3}]行目[{4}]番目の文字付近）。括弧の前に位置するバインド変数もしくはリテラル変数[{5}]に対応するパラメータの型は基本型もしくはホルダークラスを要素としたscala.collection.Iterableのサブタイプでなければいけません。しかし、実際の型は[{6}]です。型を間違えていませんか？もしくはフィールドアクセスやメソッド呼び出しの記述を忘れていませんか？。SQL[{2}]"),

    /** SQLファイルに埋め込み変数コメントが含まれていることを示す警告メッセージ */
    DOMALA4181("[{0}.{1}]のSQLに埋め込み変数コメントが含まれています。バッチの中で実行されるSQLは一定であるため、埋め込み変数コメントにより動的なSQLを発行しようとしても意図したSQLにならない可能性があります。この警告を抑制するには、メソッドに@Suppress(messages = Seq(Message.DOMALA4181))と注釈してください。"),
    /** SQLファイルに条件コメントが含まれていることを示す警告メッセージ */
    DOMALA4182("[{0}.{1}]のSQLに条件コメントが含まれています。バッチの中で実行されるSQLは一定であるため、条件コメントにより動的なSQLを発行しようとしても意図したSQLにならない可能性があります。この警告を抑制するには、メソッドに@Suppress(messages = Seq(Message.DOMALA4182))と注釈してください。"),
    /** SQLファイルに繰り返しコメントが含まれていることを示す警告メッセージ */
    DOMALA4183("[{0}.{1}]のSQLに繰り返しコメントが含まれています。バッチの中で実行されるSQLは一定であるため、繰り返しコメントにより動的なSQLを発行しようとしても意図したSQLにならない可能性があります。この警告を抑制するには、メソッドに@Suppress(messages = Seq(Message.DOMALA4183))と注釈してください。"),



    DOMALA4185(org.seasar.doma.message.Message.DOMA4185.getMessagePattern()),
    DOMALA4222("エンティティクラスを@Insertや@Updateや@Deleteが注釈されたメソッドのパラメータとする場合、戻り値はdomala.jdbc.Result<E>でなければいけません。型パラメータ E の実型引数にはパラメータと同じエンティティクラスを指定してください。 at {0}.{1}"),
    DOMALA4223("エンティティクラスを@BatchInsertや@BatchUpdateや@BatchDeleteが注釈されたメソッドのパラメータとする場合、戻り値はdomala.jdbc.BatchResult<E>でなければいけません。型パラメータEの実型引数にはパラメータと同じエンティティクラスを指定してください。 at {0}.{1}"),
    DOMALA4235("Optionに対する実型引数の型[{0}]はサポートされていません。サポートされている型は次のものです。基本型、ホルダークラス、エンティティクラス。  at {1}.{2}"),
    DOMALA4244("Functionの1番目の実型引数の型は、Streamでなければいけません。 at {0}.{1}"),
    DOMALA4245("Streamの実型引数の型[{0}]はサポートされていません。 at {1}.{2}"),
    DOMALA4246(org.seasar.doma.message.Message.DOMA4246.getMessagePattern()),
    DOMALA4247(org.seasar.doma.message.Message.DOMA4247.getMessagePattern()),
    DOMALA4249(org.seasar.doma.message.Message.DOMA4249.getMessagePattern()),
    DOMALA4257("[{0}.{1}]のSQLの妥当検査に失敗しました（[{3}]行目[{4}]番目の文字付近）。/*%expand ...*/の式が使用されていますが、カラムの自動展開ができません。メソッドに@Selectが注釈され、結果セットのレコードがエンティティクラスにマッピングされていることを確認してください。SQL[{2}]"),
    DOMALA4270("[{0}.{1}]のSQLの妥当検査に失敗しました（[{3}]行目[{4}]番目の文字付近）。/*%populate */の式が使用されていますが、SET句の自動生成ができません。メソッドに@Updateまたは@BatchUpdateが注釈され、第一引数がエンティティクラスにマッピングされていることを確認してください。SQL[{2}]"),
    DOMALA4302(org.seasar.doma.message.Message.DOMA4302.getMessagePattern()),
    DOMALA4303(org.seasar.doma.message.Message.DOMA4303.getMessagePattern()),
    DOMALA4304(org.seasar.doma.message.Message.DOMA4304.getMessagePattern()),

    // macros(domala original)
    DOMALA6001("@Holderを注釈するcase classのパラメータは1つでなければなりません。"),
    DOMALA6002("@Holderを注釈するcase classのパラメータ名は`value`でなければいけません。"),
    DOMALA6003("式[{0}]（[{1}]番目の文字付近）staticメソッドの呼び出しはサポートされていません。"),
    DOMALA6004("式[{0}]（[{1}]番目の文字付近）staticフィールドへのアクセスはサポートされていません。"),;

    private final String messagePattern;

    Message(String messagePattern) {
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
