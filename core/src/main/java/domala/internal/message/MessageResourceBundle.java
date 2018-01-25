package domala.internal.message;

import domala.message.Message;

/**
 * Resource bundle for {@link Message}.
 *
 * @author bakenezumi
 *
 */
public class MessageResourceBundle extends org.seasar.doma.internal.message.AbstractMessageResourceBundle<Message> {
    /**
     * Constructor
     */
    public MessageResourceBundle() {
        super(Message.class);
    }
}
