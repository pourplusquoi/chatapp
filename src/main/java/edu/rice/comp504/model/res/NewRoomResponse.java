package edu.rice.comp504.model.res;

/**
 * Message covers the information that a chatroom is created by a user
 */
public class NewRoomResponse extends AResponse {

    private int roomId;         // The id of the created room
    private int ownerId;        // The id of the owner of the created room
    private String roomName;    // The name of the created room

    /**
     * Constructor.
     */
    public NewRoomResponse(int roomId, int ownerId, String roomName) {
        super("NewRoom");
        this.roomId = roomId;
        this.ownerId = ownerId;
        this.roomName = roomName;
    }
}
