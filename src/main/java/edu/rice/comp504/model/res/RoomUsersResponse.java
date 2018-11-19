package edu.rice.comp504.model.res;

import java.util.Map;

public class RoomUsersResponse extends AResponse {

    private int roomId;
    Map<Integer, String> users; // Maps from userId to userName

    public RoomUsersResponse(int roomId, Map<Integer, String> users) {
        super("RoomUsers");
        this.roomId = roomId;
        this.users = users;
    }
}
