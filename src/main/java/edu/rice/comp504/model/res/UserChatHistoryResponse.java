package edu.rice.comp504.model.res;

import java.util.List;

import edu.rice.comp504.model.obj.Message;

public class UserChatHistoryResponse extends AResponse {

    private List<Message> chatHistory;

    public UserChatHistoryResponse(List<Message> chatHistory) {
        super("UserChatHistory");
        this.chatHistory = chatHistory;
    }
}
