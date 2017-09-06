package domala.internal.message;

import domala.message.Message;

/**
 * {@link Message} を扱うリソースバンドルです。
 *
 * @author bakenezumi
 *
 */public class MessageResourceBundle extends org.seasar.doma.internal.message.AbstractMessageResourceBundle<Message> {
    /**
     * インスタンスを構築します。
     */
    public MessageResourceBundle() {
        super(Message.class);
    }
}
