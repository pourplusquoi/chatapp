package edu.rice.comp504.model.cmd;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.rice.comp504.model.DispatcherAdapter;
import edu.rice.comp504.model.obj.ChatRoom;
import edu.rice.comp504.model.obj.RoomRestrictionInfo;
import edu.rice.comp504.model.obj.User;
import edu.rice.comp504.model.res.AResponse;
import edu.rice.comp504.model.res.UserRoomsResponse;

/**
 * The command to be used when a chat room is created by a user.
 */
public class AddRoomCmd implements IUserCmd {

    //room to be added
    private ChatRoom room;
//    private Map<Integer, ChatRoom> rooms;



//    //private helper function to get id room name map
//    private HashMap<Integer, String> getRoomMap(List<Integer> roomIds){
//        HashMap<Integer,String> retRooms = new HashMap<>();
//        for (Integer roomId : roomIds) {
//            ChatRoom room = this.rooms.get(roomId);
//            retRooms.put(roomId,room.getName());
//        }
//        return retRooms;
//    }

    /**
     * Constructor.
     * @param room the chat room which is newly added/created.
     */
    public AddRoomCmd(ChatRoom room) {
        this.room = room;
    }

    /**
     * Observers' action when they are notified a new room is created.
     * @param context a user which the command will operate on
     */
    @Override
    public void execute(User context) {
        context.addRoom(this.room);

        int userId = context.getId();
        List<Integer> joinedRoomIds = context.getJoinedRoomIds();
        List<Integer> availableRoomIds = context.getAvailableRoomIds();


        HashMap<Integer, RoomRestrictionInfo> joinedRooms;
        HashMap<Integer, RoomRestrictionInfo> availableRooms;
        joinedRooms = context.getJoinedRooms();
        availableRooms = context.getAvailableRooms();

        AResponse res = new UserRoomsResponse(userId, joinedRoomIds, availableRoomIds, joinedRooms, availableRooms);
        DispatcherAdapter.notifyClient(context, res);
    }
}
