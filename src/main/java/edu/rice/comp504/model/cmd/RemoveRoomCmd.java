package edu.rice.comp504.model.cmd;

import java.util.List;

import edu.rice.comp504.model.obj.ChatRoom;
import edu.rice.comp504.model.obj.User;
import edu.rice.comp504.model.res.AResponse;
import edu.rice.comp504.model.res.UserRoomsResponse;

/**
 * The command to be used when a chatroom is removed
 */
public class RemoveRoomCmd implements IUserCmd {

    private ChatRoom room;       // The chatroom which is removed

    private static IUserCmd instance;   // The singleton instance of this cmd

    /**
     * Constructor.
     */
    private RemoveRoomCmd(ChatRoom room) {
        this.room = room;
    }

    /**
     * Singleton.
     */
    public static IUserCmd makeRemoveRoomCmd(ChatRoom room) {
        if (instance == null) {
            instance = new RemoveRoomCmd(room);
        } else {
            ((RemoveRoomCmd) instance).room = room;
        }
        return instance;
    }

    /**
     * Observers' action when they are notified a room is removed
     * @context a user which the command will operate on
     */
    @Override
    public void execute(User context) {
        context.removeRoom(this.room);

        int userId = context.getId();
        List<Integer> joinedRoomIds = context.getJoinedRoomIds();
        List<Integer> availableRoomIds = context.getAvailableRoomIds();

        AResponse res = new UserRoomsResponse(userId, joinedRoomIds, availableRoomIds);
        this.room.getDispatcher().notifyClient(context, res);
    }
}
