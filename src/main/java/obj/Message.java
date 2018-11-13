package obj;

public class Message {

    private User sender;
    private User receiver;
    private String message;

    /**
     * Constructor
     */
    public Message(User from, User to, String msg) {
        this.sender = from;
        this.receiver = to;
        this.message = msg;
    }

    public User getSenderId() {
        return this.sender;
    }

    public User getReceiverId() {
        return this.receiver;
    }

    public String getMessage() {
        return this.message;
    }
}
