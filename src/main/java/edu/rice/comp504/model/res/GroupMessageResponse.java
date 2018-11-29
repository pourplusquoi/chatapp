package edu.rice.comp504.model.res;

import edu.rice.comp504.model.obj.Message;

import java.util.List;

/**
 * Message covers the information of group message. no response for group message
 */
public class GroupMessageResponse extends AResponse {

    //chat history list
    private List<Message> chatHistory;

    //sender name
    private String sender;

    //receiver name
    private String receiver;

    //sender id
    private int senderId;

    //receiver id
    private int receiverId;

    //room name
    private String roomName;

    //room id
    private int roomId;

    /**
     * Constructor.
     *
     */
    public GroupMessageResponse(List<Message> chatHistory, String sender, String receiver, int senderId, int receiverId, String roomName, int roomId) {
        super("GroupMessage");
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
    public GroupMessageResponse(List<Message> chatHistory) {
        super("GroupMessage");
        this.chatHistory = chatHistory;
    }

}
