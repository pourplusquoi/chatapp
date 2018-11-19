package edu.rice.comp504.model.res;

import java.util.Map;

import edu.rice.comp504.model.obj.User;

public class RoomUsersResponse extends AResponse {

    private int roomId;
    Map<Integer, User> users; // Maps from userId to userName

    public RoomUsersResponse(int roomId, Map<Integer, User> users) {
        super("RoomUsers");
        this.roomId = roomId;
        this.users = users;
    }
}
