package edu.rice.comp504.model.res;

import java.util.List;

import edu.rice.comp504.model.obj.Message;

/**
 * Message covers the information of all message histories.
 */
public class UserChatHistoryResponse extends AResponse {

    //chat history
    private List<Message> chatHistory;

    //sender name
    private String sender;

    //receiver name
    private String receiver;

    //sender id
    private int senderId;

    //receiver id
    private int receiverId;

    //roomname
    private String roomName;

    //roomid
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

    /**
     * Constructor.
     * @param chatHistory the message history
     */
    public UserChatHistoryResponse(List<Message> chatHistory) {
        super("UserChatHistory");
        this.chatHistory = chatHistory;
    }

}
