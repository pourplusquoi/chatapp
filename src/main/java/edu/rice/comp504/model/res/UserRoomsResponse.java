package edu.rice.comp504.model.res;

import java.util.List;

/**
 * Message covers the information of all chatrooms of a user
 */
public class UserRoomsResponse extends AResponse {

    private int userId;      // The id of the user
    private List<Integer> joinedRoomIds;    // The id of chatrooms which the user has joined
    private List<Integer> availableRoomIds; // The id of chatrooms which the user has not joined

    /**
     * Constructor.
     */
    public UserRoomsResponse(int userId,
                             List<Integer> joinedRoomIds,
                             List<Integer> availableRoomIds) {
        super("UserRooms");
        this.userId = userId;
        this.joinedRoomIds = joinedRoomIds;
        this.availableRoomIds = availableRoomIds;
    }
}
