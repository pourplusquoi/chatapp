package edu.rice.comp504.model.res;

import java.util.List;

import edu.rice.comp504.model.obj.Message;

/**
 * Message covers the information of all message histories.
 */
public class UserChatHistoryResponse extends AResponse {

    private List<Message> chatHistory;
    private String sender;
    private String receiver;
    private int senderId;
    private int receiverId;
    private String roomName;
    private int roomId;

    /**
     * Constructor.
     * @param chatHistory the message history
     */
    public UserChatHistoryResponse(List<Message> chatHistory, String sender, String receiver, int senderId, int receiverId, String roomName, int roomId) {
        super("UserChatHistory");
        this.chatHistory = chatHistory;
        this.sender = sender;
        this.receiver = receiver;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.roomName = roomName;
        this.roomId = roomId;
    }

    public UserChatHistoryResponse(List<Message> chatHistory) {
        super("UserChatHistory");
        this.chatHistory = chatHistory;
    }

}
