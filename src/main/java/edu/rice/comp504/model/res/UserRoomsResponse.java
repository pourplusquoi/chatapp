package edu.rice.comp504.model.res;

import java.util.List;

public class UserRoomsResponse extends AResponse {

    private int userId;
    private List<Integer> joinedRoomIds;
    private List<Integer> availableRoomIds;

    public UserRoomsResponse(int userId,
                             List<Integer> joinedRoomIds,
                             List<Integer> availableRoomIds) {
        super("UserRooms");
        this.userId = userId;
        this.joinedRoomIds = joinedRoomIds;
        this.availableRoomIds = availableRoomIds;
    }
}
