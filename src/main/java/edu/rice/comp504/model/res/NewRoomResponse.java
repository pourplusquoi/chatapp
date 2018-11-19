package edu.rice.comp504.model.res;

public class NewRoomResponse extends AResponse {

    private int roomId;
    private int ownerId;
    private String roomName;

    public NewRoomResponse(int roomId, int ownerId, String roomName) {
        super("NewRoom");
        this.roomId = roomId;
        this.ownerId = ownerId;
        this.roomName = roomName;
    }
}
