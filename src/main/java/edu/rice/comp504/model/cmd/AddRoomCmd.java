package edu.rice.comp504.model.cmd;

import java.util.List;

import edu.rice.comp504.model.obj.ChatRoom;
import edu.rice.comp504.model.obj.User;
import edu.rice.comp504.model.res.AResponse;
import edu.rice.comp504.model.res.UserRoomsResponse;

/**
 * The command to be used when a chatroom is created by a user
 */
public class AddRoomCmd implements IUserCmd {

    private ChatRoom room;      // The chatroom which is newly added(created)

    private static IUserCmd instance;   // The singleton instance of this cmd

    /**
     * Constructor.
     */
    private AddRoomCmd(ChatRoom room) {
        this.room = room;
    }


    /**
     * Singleton.
     */
    public static IUserCmd makeAddRoomCmd(ChatRoom room) {
        if (instance == null) {
            instance = new AddRoomCmd(room);
        } else {
            ((AddRoomCmd) instance).room = room;
        }
        return instance;
    }

    /**
     * Observers' action when they are notified a new room is created
     * @context a user which the command will operate on
     */
    @Override
    public void execute(User context) {
        context.addRoom(this.room);

        int userId = context.getId();
        List<Integer> joinedRoomIds = context.getJoinedRoomIds();
        List<Integer> availableRoomIds = context.getAvailableRoomIds();

        AResponse res = new UserRoomsResponse(userId, joinedRoomIds, availableRoomIds);
        this.room.getDispatcher().notifyByUser(context, res);
    }
}
