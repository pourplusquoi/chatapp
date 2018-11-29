package edu.rice.comp504.model.cmd;

import java.util.HashMap;
import java.util.List;

import edu.rice.comp504.model.DispatcherAdapter;
import edu.rice.comp504.model.obj.ChatRoom;
import edu.rice.comp504.model.obj.RoomRestrictionInfo;
import edu.rice.comp504.model.obj.User;
import edu.rice.comp504.model.res.AResponse;
import edu.rice.comp504.model.res.UserRoomsResponse;

/**
 * The command to be used when a chat room is removed.
 */
public class RemoveRoomCmd implements IUserCmd {

    //the room to be removed
    private ChatRoom room;

    /**
     * Constructor.
     * @param room the chat room which is being removed
     */
    public RemoveRoomCmd(ChatRoom room) {
        this.room = room;
    }

    /**
     * Observers' action when they are notified a room is removed.
     * @param context a user which the command will operate on
     */
    @Override
    public void execute(User context) {
        context.removeRoom(this.room);
        DispatcherAdapter dis = this.room.getDispatcher();

        int userId = context.getId();
        List<Integer> joinedRoomIds = context.getJoinedRoomIds();
        List<Integer> availableRoomIds = context.getAvailableRoomIds();

        HashMap<Integer, RoomRestrictionInfo> joinedRooms;
        HashMap<Integer, RoomRestrictionInfo> availableRooms;
        joinedRooms = context.getJoinedRooms();
        availableRooms = context.getAvailableRooms();

        if (dis.containsSession(context.getSession())) {
            AResponse res = new UserRoomsResponse(userId, joinedRoomIds, availableRoomIds, joinedRooms, availableRooms);
            DispatcherAdapter.notifyClient(context, res);
        }
    }
}
