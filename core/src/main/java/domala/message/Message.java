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
@SuppressWarnings("WeakerAccess")
public enum Message implements MessageResource {
    DOMALA2219(org.seasar.doma.message.Message.DOMA2219.getMessagePattern()),


    // macros
    DOMALA4001("戻り値の型はエンティティクラスをパラメータとする場合はdomala.jdbc.Result<E>、そうでない場合は更新件数を示すIntでなければいけません at {0}.{1}"),
    DOMALA4002(org.seasar.doma.message.Message.DOMA4002.getMessagePattern()),
    DOMALA4003(org.seasar.doma.message.Message.DOMA4003.getMessagePattern()),
    DOMALA4005(org.seasar.doma.message.Message.DOMA4005.getMessagePattern()),
    DOMALA4007("戻り値のSeqに対する実型引数の型[{0}]はサポートされていません。{3} at {1}.{2}"),
    DOMALA4008("戻り値の型[{0}]はサポートされていません。{3} at {1}.{2}"),
    DOMALA4014("@Daoはトレイト以外には注釈できません。"),
    DOMALA4015("クラス以外には注釈できません。"),
    DOMALA4019(org.seasar.doma.message.Message.DOMA4019.getMessagePattern()),
    DOMALA4020("{0}が空です。"),
    DOMALA4024(org.seasar.doma.message.Message.DOMA4024.getMessagePattern()),
    DOMALA4025(org.seasar.doma.message.Message.DOMA4025.getMessagePattern()),
    DOMALA4030(org.seasar.doma.message.Message.DOMA4030.getMessagePattern()),
    DOMALA4031(org.seasar.doma.message.Message.DOMA4031.getMessagePattern()),
    DOMALA4033(org.seasar.doma.message.Message.DOMA4033.getMessagePattern()),
    DOMALA4034(org.seasar.doma.message.Message.DOMA4034.getMessagePattern()),
    DOMALA4035(org.seasar.doma.message.Message.DOMA4035.getMessagePattern()),
    DOMALA4036(org.seasar.doma.message.Message.DOMA4036.getMessagePattern()),
    DOMALA4037(org.seasar.doma.message.Message.DOMA4037.getMessagePattern()),
    // TODO: DOMALA4038 EntityListenerの型パラメータ検査
    DOMALA4040("戻り値の型はエンティティクラスをパラメータとする場合はdomala.jdbc.BatchResult<E>、そうでない場合は更新件数を示すArray[Int]でなければいけません。 at {0}.{1}"),
    DOMALA4042("パラーメータの型は型パラメータを一つとる、scala.collection.Iterableのサブタイプでなければいけません。 at {0}.{1}"),
    DOMALA4043("scala.collection.Iterableのサブタイプに対する実型引数はエンティティクラスでなければいけません。 at {0}.{1}"),
    DOMALA4051(org.seasar.doma.message.Message.DOMA4051.getMessagePattern()),
    DOMALA4053(org.seasar.doma.message.Message.DOMA4053.getMessagePattern()),
    // TODO: ストアド系未実装 DOMA4059 - DOMA4066
    DOMALA4067(org.seasar.doma.message.Message.DOMA4067.getMessagePattern()),
    DOMALA4069("{0}の解析に失敗しました。原因は次のものです。{1}"),
    DOMALA4071(org.seasar.doma.message.Message.DOMA4071.getMessagePattern()),
    DOMALA4072(org.seasar.doma.message.Message.DOMA4072.getMessagePattern()),
    DOMALA4073(org.seasar.doma.message.Message.DOMA4073.getMessagePattern()),
    // TODO: ファクトリ系未実装 DOMA4076, DOMA4078
    DOMALA4084("include指定されているプロパティ[{0}]が、エンティティクラス[{1}]に見つかりません。 at {2}.{3}"),
    DOMALA4085("exclude指定されているプロパティ[{0}]が、エンティティクラス[{1}]に見つかりません。 at {2}.{3}"),
    DOMALA4086(org.seasar.doma.message.Message.DOMA4086.getMessagePattern()),
    DOMALA4087(org.seasar.doma.message.Message.DOMA4087.getMessagePattern()),
    DOMALA4088(org.seasar.doma.message.Message.DOMA4088.getMessagePattern()),
    DOMALA4089(org.seasar.doma.message.Message.DOMA4089.getMessagePattern()),
    //DOMALA4090
    //DOMALA4091
    DOMALA4092("{0}の妥当検査に失敗しました（[{2}]行目[{3}]番目の文字付近）。詳細は次のものです。{4} SQL[{1}]。"),
    DOMALA4093(org.seasar.doma.message.Message.DOMA4093.getMessagePattern()),
    DOMALA4095(org.seasar.doma.message.Message.DOMA4095.getMessagePattern()),
    // @ExternalDomainは未対応
    //DOMALA4096("クラス[{0}]は、永続対象の型としてサポートされていません。 at {1}.{2}。@ExternalDomainでマッピングすることを意図している場合、登録や設定が不足している可能性があります。@DomainConvertersを注釈したクラスと注釈処理のオプション（domala.domain.converters）を見直してください。"),
    DOMALA4096("クラス[{0}]は、永続対象の型としてサポートされていません。 at {1}.{2}。"),
    // TODO: ファクトリ系未実装 DOMA4097 - DOMA4101
    DOMALA4102(org.seasar.doma.message.Message.DOMA4102.getMessagePattern()),
    DOMALA4105("クラス以外には注釈できません。 at {0}"),
    // @HolderのfactoryMethod未実装 DOMA4106
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
    DOMALA4122("{0}の妥当検査に失敗しました。メソッドのパラメータ[{1}]がSQLで参照されていません。"),

    // OriginalStates未実装 DOMA4125
    DOMALA4126(org.seasar.doma.message.Message.DOMA4126.getMessagePattern()),
    // 式内のConstructor未実装 DOMA4127
    // @HolderのfactoryMethod未実装 DOMA4132
    // OriginalStates未実装 DOMA4135
    // 式内のConstructor未実装 DOMA4138
    DOMALA4139(org.seasar.doma.message.Message.DOMA4139.getMessagePattern()),
    DOMALA4140("{0}の妥当検査に失敗しました（[{2}]行目[{3}]番目の文字付近）。/*%if ...*/の式[{4}]が型[{5}]として評価されましたが、Boolean型でなければいけません。SQL[{1}]"),
    DOMALA4141("{0}の妥当検査に失敗しました（[{2}]行目[{3}]番目の文字付近）。/*%elseif ...*/の式[{4}]が型[{5}]として評価されましたが、Boolean型でなければいけません。SQL[{1}]"),

    DOMALA4145(org.seasar.doma.message.Message.DOMA4145.getMessagePattern()),
    DOMALA4146(org.seasar.doma.message.Message.DOMA4146.getMessagePattern()),
    DOMALA4147(org.seasar.doma.message.Message.DOMA4147.getMessagePattern()),
    // 式内のstaticフィールド参照未対応 DOMA4148
    DOMALA4149("{0}の妥当検査に失敗しました（[{2}]行目[{3}]番目の文字付近）。/*%for ...*/の式[{4}]が型[{5}]として評価されましたが、scala.collection.Iterable型でなければいけません。SQL[{1}]"),
    DOMALA4150("{0}の妥当検査に失敗しました（[{2}]行目[{3}]番目の文字付近）。/*%for ...*/の式[{4}]の型[{5}]の実型引数が不明です。SQL[{1}]"),
    DOMALA4153("{0}の妥当検査に失敗しました（[{2}]行目[{3}]番目の文字付近）。バインド変数もしくはリテラル変数[{4}]に対応するパラメータの型は基本型もしくはホルダークラスでなければいけません。しかし、実際の型は[{5}]です。型を間違えていませんか？もしくはフィールドアクセスやメソッド呼び出しの記述を忘れていませんか？SQL[{1}]"),
    // 抽象型Entity未実装 DOMA4154-4157

    DOMALA4160("scala.collection.Iterableのサブタイプをワイルカード型にしてはいけません。 at {0}.{1}"),
    DOMALA4161("{0}の妥当検査に失敗しました（[{2}]行目[{3}]番目の文字付近）。括弧の前に位置するバインド変数もしくはリテラル変数[{4}]に対応するパラメータの型は基本型もしくはホルダークラスを要素としたscala.collection.Iterableのサブタイプでなければいけません。しかし、実際の型は[{5}]です。型を間違えていませんか？もしくはフィールドアクセスやメソッド呼び出しの記述を忘れていませんか？。SQL[{1}]"),
    // DOMALA4163, DOMALA4164 抽象型Configは未チェック
    DOMALA4166(org.seasar.doma.message.Message.DOMA4166.getMessagePattern()),
    DOMALA4167(org.seasar.doma.message.Message.DOMA4167.getMessagePattern()),
    DOMALA4168(org.seasar.doma.message.Message.DOMA4168.getMessagePattern()),
    DOMALA4169(org.seasar.doma.message.Message.DOMA4169.getMessagePattern()),
    DOMALA4170(org.seasar.doma.message.Message.DOMA4170.getMessagePattern()),
    DOMALA4171(org.seasar.doma.message.Message.DOMA4171.getMessagePattern()),
    DOMALA4172("戻り値の型はUnitでなければいけません。 at {0}.{1}"),
    DOMALA4173(org.seasar.doma.message.Message.DOMA4173.getMessagePattern()),
    /** SQLファイルに埋め込み変数コメントが含まれていることを示す警告メッセージ */
    DOMALA4181("{0}に埋め込み変数コメントが含まれています。バッチの中で実行されるSQLは一定であるため、埋め込み変数コメントにより動的なSQLを発行しようとしても意図したSQLにならない可能性があります。この警告を抑制するには、メソッドに@Suppress(messages = Seq(Message.DOMALA4181))と注釈してください。"),
    /** SQLファイルに条件コメントが含まれていることを示す警告メッセージ */
    DOMALA4182("{0}に条件コメントが含まれています。バッチの中で実行されるSQLは一定であるため、条件コメントにより動的なSQLを発行しようとしても意図したSQLにならない可能性があります。この警告を抑制するには、メソッドに@Suppress(messages = Seq(Message.DOMALA4182))と注釈してください。"),
    /** SQLファイルに繰り返しコメントが含まれていることを示す警告メッセージ */
    DOMALA4183("{0}に繰り返しコメントが含まれています。バッチの中で実行されるSQLは一定であるため、繰り返しコメントにより動的なSQLを発行しようとしても意図したSQLにならない可能性があります。この警告を抑制するには、メソッドに@Suppress(messages = Seq(Message.DOMALA4183))と注釈してください。"),
    // DOMALA4184 列挙型Holderは未対応
    DOMALA4185(org.seasar.doma.message.Message.DOMA4185.getMessagePattern()),
    // TODO: ストアド系未実装 DOMA4186
    // TODO: DOMA4188 Daoの継承は未対応
    // カスタム関数は未対応 DOMA4189, DOMA4190
    // @ExternalDomainは未対応 DOMA4191 - DOMA4201
    // TODO: DOMALA4202 EntityListenerの型パラメータ検査
    // @ExternalDomainは未対応 DOMA4203
    DOMALA4205(org.seasar.doma.message.Message.DOMA4205.getMessagePattern()),
    DOMALA4207(org.seasar.doma.message.Message.DOMA4207.getMessagePattern()),
    DOMALA4209(org.seasar.doma.message.Message.DOMA4209.getMessagePattern()),
    // パラメータのワイルドカード検査メッセージはDOMALA4209に統合 DOMA4211 -DOMA4218
    DOMALA4222("エンティティクラスを@Insertや@Updateや@Deleteが注釈されたメソッドのパラメータとする場合、戻り値はdomala.jdbc.Result<E>でなければいけません。型パラメータ E の実型引数にはパラメータと同じエンティティクラスを指定してください。 at {0}.{1}"),
    DOMALA4223("エンティティクラスを@BatchInsertや@BatchUpdateや@BatchDeleteが注釈されたメソッドのパラメータとする場合、戻り値はdomala.jdbc.BatchResult<E>でなければいけません。型パラメータEの実型引数にはパラメータと同じエンティティクラスを指定してください。 at {0}.{1}"),
    // OriginalStates未実装 DOMA4224
    DOMALA4225("エンティティクラスの永続対象フィールドにはvar修飾子を使用できません。 at {0}.{1}"),
    // 未検査 DOMA4226
    DOMALA4229(org.seasar.doma.message.Message.DOMA4229.getMessagePattern()),
    // Entityの継承未実装 DOMA4227 - DOMA4231
    // エンティティのワイルドカード検査メッセージはDOMALA4205に統合 DOMA4232 - DOMA4233
    // 抽象型Entity未実装 DOMA4234
    DOMALA4235("Optionに対する実型引数の型[{0}]はサポートされていません。{3}サポートされている型は次のものです。基本型、ホルダークラス、エンティティクラス。 at {1}.{2}"),
    // パラメータのワイルドカード検査メッセージはDOMALA4209に統合 DOMA4236 - DOMA4242
    DOMALA4243(org.seasar.doma.message.Message.DOMA4243.getMessagePattern()),
    DOMALA4244("Functionの1番目の実型引数の型は、Streamでなければいけません。 at {0}.{1}"),
    DOMALA4245("Streamの実型引数の型[{0}]はサポートされていません。{3} at {1}.{2}"),
    DOMALA4246(org.seasar.doma.message.Message.DOMA4246.getMessagePattern()),
    DOMALA4247("@Selectのstrategy要素にSelectStrategyType.STREAMを設定した場合、Stream[?] => ?型のパラメータが必要です。 at {0}.{1}"),
    DOMALA4249("Stream[?] => ?型のパラメータは複数指定できません。"),
    // 抽象型Entity未実装 DOMA4250
    // acceptNull非対応 DOMA4251
    // acceptNull非対応 DOMA4251
    // 未検査 DOMA4252
    // @SingletonConfig非対応（object推奨） DOMA4253 - DOMA4256
    DOMALA4257("{0}の妥当検査に失敗しました（[{2}]行目[{3}]番目の文字付近）。/*%expand ...*/の式が使用されていますが、カラムの自動展開ができません。メソッドに@Selectが注釈され、結果セットのレコードがエンティティクラスにマッピングされていることを確認してください。SQL[{1}]"),
    // SelectStrategyType.COLLECT 非対応 DOMA4258 - DOMA4266
    // TODO: 戻り値Stream未対応 DOMA4267
    // trait Holder未対応 DOMA4268
    DOMALA4270("{0}の妥当検査に失敗しました（[{2}]行目[{3}]番目の文字付近）。/*%populate */の式が使用されていますが、SET句の自動生成ができません。メソッドに@Updateまたは@BatchUpdateが注釈され、第一引数がエンティティクラスにマッピングされていることを確認してください。SQL[{1}]"),
    // TODO: 戻り値Stream未対応 DOMA4271 - DOMA4274
    // TODO: 囲む型未チェック DOMA4275 - DOMA4277
    // @ExternalDomainは未対応 DOMA4278 - DOMA4280
    DOMALA4283(org.seasar.doma.message.Message.DOMA4283.getMessagePattern()),
    DOMALA4285(org.seasar.doma.message.Message.DOMA4285.getMessagePattern()),
    // OriginalStates未実装 DOMA4286
    DOMALA4289(org.seasar.doma.message.Message.DOMA4289.getMessagePattern()),
    DOMALA4290(org.seasar.doma.message.Message.DOMA4290.getMessagePattern()),
    DOMALA4291(org.seasar.doma.message.Message.DOMA4291.getMessagePattern()),
    DOMALA4297(org.seasar.doma.message.Message.DOMA4297.getMessagePattern()),

    DOMALA4302(org.seasar.doma.message.Message.DOMA4302.getMessagePattern()),
    DOMALA4303(org.seasar.doma.message.Message.DOMA4303.getMessagePattern()),
    DOMALA4304(org.seasar.doma.message.Message.DOMA4304.getMessagePattern()),
    // lombokには非対応 DOMA4418 - DOMA4432

    DOMALA4442(org.seasar.doma.message.Message.DOMA4442.getMessagePattern()),
    DOMALA4443(org.seasar.doma.message.Message.DOMA4443.getMessagePattern()),

    // macros(domala original)
    DOMALA6001("@Holderを注釈するclassのコンストラクタパラメータは1つでなければなりません。"),
    DOMALA6003("式[{0}]（[{1}]番目の文字付近）staticメソッドの呼び出しはサポートされていません。"),
    DOMALA6004("式[{0}]（[{1}]番目の文字付近）staticフィールドへのアクセスはサポートされていません。"),
    DOMALA6005("@Holderはcase class、もしくはsealed abstract classにのみ注釈できます。"),
    DOMALA6006("@Holderをsealed abstract classに注釈する場合、コンストラクタパラメータを(val value: ...)とし、immutableなフィールドとして公開しなければなりません。"),
    DOMALA6007("@Holderをsealed abstract classに注釈する場合、1つ以上の継承したobjectが必要です。at {0}"),
    DOMALA6008("@Holderをsealed abstract classに注釈する場合、全ての継承先はobjectにする必要があります。at {0}"),
    DOMALA6009("@Selectのstrategy要素にSelectStrategyType.ITERATORを設定した場合、Iterator[?] => ?型のパラメータが必要です。 at {0}.{1}"),
    DOMALA6010("Iterator[?] => ?型のパラメータは複数指定できません。"),
    DOMALA6011("Functionの1番目の実型引数の型は、Iteratorでなければいけません。 at {0}.{1}"),
    DOMALA6012("Iteratorの実型引数の型[{0}]はサポートされていません。{3} at {1}.{2}"),
    DOMALA6013("script\"...\"内での変数利用はできません。"),
    DOMALA6014("AnyValのサブクラスを永続対象とする場合、要素の型は基本型でなければなりません。 at {0}.{1}"),
    DOMALA6015("sqlは文字列リテラルでなければなりません。 at {0}.{1}"),
    DOMALA6016("@Holderをsealed abstract classに注釈する場合、継承するobjectは全て異なる値を持たなければなりません。at {0}"),
    DOMALA6017("AnyValのサブクラスを永続対象とする場合、コンストラクタ、コンパニオンオブジェクトのapplyメソッドのどちらかはpublicでなければなりません。 at {0}"),
    DOMALA6018("{0} はDaoトレイトでないため実装クラスを取得できません。"),
    DOMALA6019("Stream[?] => ?型のパラメータは@Selectのstrategy要素にSelectStrategyType.STREAMを設定した場合のみ使用できます。at {0}.{1}"),
    DOMALA6020("Iterator[?] => ?型のパラメータは@Selectのstrategy要素にSelectStrategyType.ITERATORを設定した場合のみ使用できます。at {0}.{1}"),
    DOMALA6021("sql要素に値を設定した場合、sqlFile要素にはtrueを指定できません。 at {0}.{1}"),
    DOMALA6022("AdHocConfigはトランザクションをサポートしていません。"),
    DOMALA6023("AdHocConfigを利用する場合、クエリは発行時に自動コミットされます。トランザクション制御が必要な場合はdomala.jdbc.LocalTransactionConfigを利用して下さい。"),
    DOMALA6024("エンティティクラス[{0}]のプロパティ{1}に結果セットのカラムの値がマッピングされませんでした。マッピングには結果セットがカラム{2}を含んでいる必要があります。SQLが正しいことを確認してください。"),
    DOMALA6025("クラス[{0}]はエンティティクラスとして利用できません。{1}"),
    DOMALA6026("@GeneratedValue(strategy = {2})を指定する場合はエンテティクラスに@Entityを注釈しなければいけません。 at {0}.{1}"),

    // a part of message
    DOMALA9901("[{0}.{1}]のSQL"),
    DOMALA9902("SQLファイル[{0}]"),
    DOMALA9903("プロパティ[{0}]の型[{1}]はサポートされていません。"),
    ;

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
