package edu.rice.comp504.model.cmd;

import java.util.List;
import java.util.Map;

import edu.rice.comp504.model.DispatcherAdapter;
import edu.rice.comp504.model.obj.ChatRoom;
import edu.rice.comp504.model.obj.User;
import edu.rice.comp504.model.res.AResponse;
import edu.rice.comp504.model.res.RoomNotificationsResponse;
import edu.rice.comp504.model.res.RoomUsersResponse;
import edu.rice.comp504.model.res.UserRoomsResponse;

/**
 * The command to be used when a user wants to join a chat room.
 */
public class JoinRoomCmd implements IUserCmd {

    private ChatRoom room;
    private User user;

    /**
     * Constructor.
     * @param room the chat room which the user wants to join
     * @param user the user who wants to join the chat room
     */
    public JoinRoomCmd(ChatRoom room, User user) {
        this.room = room;
        this.user = user;
    }

    /**
     * Observers' action when they are notified a user has joined a chat room.
     * @param context a user which the command will operate on
     */
    @Override
    public void execute(User context) {
        int userId = this.user.getId();
        int roomId = this.room.getId();

        // When the user to be joined is the current user
        if (this.user == context) {
            List<Integer> joinedRoomIds, availableRoomIds;
            availableRoomIds = context.getAvailableRoomIds();

            if (availableRoomIds.contains(roomId) && this.room.applyFilter(context)) {
                context.moveToJoined(this.room);

                joinedRoomIds = context.getJoinedRoomIds();
                availableRoomIds = context.getAvailableRoomIds();

                AResponse res = new UserRoomsResponse(userId, joinedRoomIds, availableRoomIds);
                DispatcherAdapter.notifyClient(context, res);
            }
        }

        AResponse res;
        List<String> notifications = this.room.getNotifications();
        Map<Integer, String> users = this.room.getUsers();

        res = new RoomNotificationsResponse(roomId, notifications);
        DispatcherAdapter.notifyClient(context, res);

        res = new RoomUsersResponse(roomId, users);
        DispatcherAdapter.notifyClient(context, res);
    }
}
