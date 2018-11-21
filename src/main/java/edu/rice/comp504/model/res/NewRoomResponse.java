package edu.rice.comp504.model.res;

/**
 * Message covers the information that a chat room is created by a user.
 */
public class NewRoomResponse extends AResponse {

    private int roomId;
    private int ownerId;
    private String roomName;

    /**
     * Constructor.
     * @param roomId the id of the created room
     * @param ownerId the id of the owner of the created room
     * @param roomName the name of the created room
     */
    public NewRoomResponse(int roomId, int ownerId, String roomName) {
        super("NewRoom");
        this.roomId = roomId;
        this.ownerId = ownerId;
        this.roomName = roomName;
    }
}
