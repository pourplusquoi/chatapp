package edu.rice.comp504.model.res;

import edu.rice.comp504.model.obj.ChatRoom;
import edu.rice.comp504.model.obj.RoomRestrictionInfo;

import java.util.HashMap;
import java.util.List;

/**
 * Message covers the information of all chat rooms of a user.
 */
public class UserRoomsResponse extends AResponse {

    private int userId;
    private List<Integer> joinedRoomIds;
    private List<Integer> availableRoomIds;
    private HashMap<Integer, RoomRestrictionInfo> joinedRooms;
    private HashMap<Integer, RoomRestrictionInfo> availableRooms;

    /**
     * Constructor.
     * @param userId the id of the user
     * @param joinedRoomIds the id of chat rooms which the user has joined
     * @param availableRoomIds he id of chat rooms which the user has not joined
     */
    public UserRoomsResponse(int userId,
                             List<Integer> joinedRoomIds,
                             List<Integer> availableRoomIds,
                             HashMap<Integer, RoomRestrictionInfo> joinedRooms,
                             HashMap<Integer, RoomRestrictionInfo> availableRooms) {
        super("UserRooms");
        this.userId = userId;
        this.joinedRoomIds = joinedRoomIds;
        this.availableRoomIds = availableRoomIds;
        this.joinedRooms = joinedRooms;
        this.availableRooms = availableRooms;
    }

    public UserRoomsResponse(int userId,
                             List<Integer> joinedRoomIds,
                             List<Integer> availableRoomIds) {
        super("UserRooms");
        this.userId = userId;
        this.joinedRoomIds = joinedRoomIds;
        this.availableRoomIds = availableRoomIds;
    }

}
