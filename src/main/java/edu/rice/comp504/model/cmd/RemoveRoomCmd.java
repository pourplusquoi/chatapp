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

        int userId = context.getId();
        List<Integer> joinedRoomIds = context.getJoinedRoomIds();
        List<Integer> availableRoomIds = context.getAvailableRoomIds();

        HashMap<Integer, RoomRestrictionInfo> joinedRooms, availableRooms;
        joinedRooms = context.getJoinedRooms();
        availableRooms = context.getAvailableRooms();

        System.out.println("start - name is "+context.getName() + " id is " + context.getId());
        AResponse res = new UserRoomsResponse(userId, joinedRoomIds, availableRoomIds, joinedRooms, availableRooms);
        System.out.println("middle - name is "+context.getName() + " id is " + context.getId());
        DispatcherAdapter.notifyClient(context, res);
        System.out.println("end - name is "+context.getName() + " id is " + context.getId());
    }
}
