package edu.rice.comp504.model.res;

import java.util.List;

/**
 * Message covers the information of all chat rooms of a user.
 */
public class UserRoomsResponse extends AResponse {

    private int userId;
    private List<Integer> joinedRoomIds;
    private List<Integer> availableRoomIds;

    /**
     * Constructor.
     * @param userId the id of the user
     * @param joinedRoomIds the id of chat rooms which the user has joined
     * @param availableRoomIds he id of chat rooms which the user has not joined
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
