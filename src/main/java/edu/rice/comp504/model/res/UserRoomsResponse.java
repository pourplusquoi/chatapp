package edu.rice.comp504.model.res;

import edu.rice.comp504.model.obj.ChatRoom;
import edu.rice.comp504.model.obj.RoomRestrictionInfo;

import java.util.HashMap;
import java.util.List;

/**
 * Message covers the information of all chat rooms of a user.
 */
public class UserRoomsResponse extends AResponse {

    //userid
    private int userId;
    //joined room id list
    private List<Integer> joinedRoomIds;
    //available room id list
    private List<Integer> availableRoomIds;
    //joined rooms restriction info list
    private HashMap<Integer, RoomRestrictionInfo> joinedRooms;
    //available rooms restriction info list
    private HashMap<Integer, RoomRestrictionInfo> availableRooms;

    /**
     * Constructor.
     * @param userId the id of the user
     * @param joinedRoomIds the id of chat rooms which the user has joined
     * @param availableRoomIds he id of chat rooms which the user has not joined
     * @param joinedRooms the room info of chat rooms which the user has joined
     * @param availableRooms the room info of chat rooms which the user has not joined
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
