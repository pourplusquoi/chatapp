package edu.rice.comp504.model.res;

import java.util.Map;

/**
 * Message covers the information of all users of a chat room.
 */
public class RoomUsersResponse extends AResponse {

    private int roomId;
    Map<Integer, String> users; // Maps from userId to userName
    private int ownerId;
    private String roomName;

    /**
     * Constructor.
     * @param roomId the id of the chat room
     * @param users the ids and names of users who is in the chat room
     */
    public RoomUsersResponse(int roomId, Map<Integer, String> users) {
        super("RoomUsers");
        this.roomId = roomId;
        this.users = users;
    }

    public RoomUsersResponse(int roomId, Map<Integer, String> users, int ownerId, String roomName) {
        super("RoomUsers");
        this.roomId = roomId;
        this.users = users;
        this.ownerId = ownerId;
        this.roomName = roomName;
    }

}
