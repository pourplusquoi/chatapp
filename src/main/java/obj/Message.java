package obj;

public class Message {

    private int senderId;
    private int receiverId;
    private String message;

    /**
     * Constructor
     */
    public Message(int from, int to, String msg) {
        this.senderId = from;
        this.receiverId = to;
        this.message = msg;
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
}
