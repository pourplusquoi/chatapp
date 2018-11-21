package edu.rice.comp504.model.res;

import java.util.List;

import edu.rice.comp504.model.obj.Message;

/**
 * Message covers the information of all message histories.
 */
public class UserChatHistoryResponse extends AResponse {

    private List<Message> chatHistory;

    /**
     * Constructor.
     * @param chatHistory the message history
     */
    public UserChatHistoryResponse(List<Message> chatHistory) {
        super("UserChatHistory");
        this.chatHistory = chatHistory;
    }
}
