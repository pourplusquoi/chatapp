package edu.rice.comp504.model.res;

import java.util.Map;

/**
 * Message covers the information of all users of a chatroom
 */
public class RoomUsersResponse extends AResponse {

    private int roomId;     // The id of the chatroom
    Map<Integer, String> users; // The ids and names of users who is in the chatroom

    /**
     * Constructor.
     */
    public RoomUsersResponse(int roomId, Map<Integer, String> users) {
        super("RoomUsers");
        this.roomId = roomId;
        this.users = users;
    }
}
