package edu.rice.comp504.model.obj;

public class Message {

    private int id;

    private int roomId;
    private int senderId;
    private int receiverId;
    private String message;

    private boolean isReceived;

    /**
     * Constructor
     */
    public Message(int id, int roomId, int senderId, int receiverId, String message) {
        this.id = id;
        this.roomId = roomId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.isReceived = false;
    }

    public int getId() {
        return this.id;
    }

    public int getRoomId() {
        return this.roomId;
    }

    public int getSenderId() {
        return this.senderId;
    }

    public int getReceiverId() {
        return this.receiverId;
    }

    public String getMessage() {
        return this.message;
    }

    public boolean isReceived() {
        return this.isReceived;
    }

    public void setReceived(boolean val) {
        this.isReceived = val;
    }
}
