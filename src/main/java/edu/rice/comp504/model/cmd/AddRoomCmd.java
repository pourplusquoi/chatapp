package edu.rice.comp504.model.cmd;

import java.util.List;

import edu.rice.comp504.model.DispatcherAdapter;
import edu.rice.comp504.model.obj.ChatRoom;
import edu.rice.comp504.model.obj.User;
import edu.rice.comp504.model.res.AResponse;
import edu.rice.comp504.model.res.UserRoomsResponse;

/**
 * The command to be used when a chat room is created by a user.
 */
public class AddRoomCmd implements IUserCmd {

    private ChatRoom room;

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

        AResponse res = new UserRoomsResponse(userId, joinedRoomIds, availableRoomIds);
        DispatcherAdapter.notifyClient(context, res);
    }
}
