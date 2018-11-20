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
 * The command to be used when a user wants to leave a chatroom
 */
public class LeaveRoomCmd implements IUserCmd {

    private ChatRoom room;    // The chatroom which the user wants to leave
    private User user;        // The user who wants to leave the chatroom

    private static IUserCmd instance;   // The singleton instance of this cmd

    /**
     * Constructor.
     */
    private LeaveRoomCmd(ChatRoom room, User user) {
        this.room = room;
        this.user = user;
    }

    /**
     * Singleton.
     */
    public static IUserCmd makeLeaveRoomCmd(ChatRoom room, User user) {
        if (instance == null) {
            instance = new LeaveRoomCmd(room, user);
        } else {
            LeaveRoomCmd cmd = (LeaveRoomCmd) instance;
            cmd.room = room;
            cmd.user = user;
        }
        return instance;
    }

    /**
     * Observers' action when they are notified a user has left a chatroom
     * @context a user which the command will operate on
     */
    @Override
    public void execute(User context) {
        int userId = this.user.getId();
        int roomId = this.room.getId();
        DispatcherAdapter dis = this.room.getDispatcher();

        // When the user to leave is the room owner or context
        if (this.user == this.room.getOwner() || this.user == context) {
            context.moveToAvailable(this.room);

            List<Integer> joinedRoomIds = context.getJoinedRoomIds();
            List<Integer> availableRoomIds = context.getAvailableRoomIds();

            // Notify client only when session is still connected
            if (dis.containsSession(context.getSession())) {
                AResponse res = new UserRoomsResponse(context.getId(),
                        joinedRoomIds, availableRoomIds);
                dis.notifyClient(context, res);
            }

        } else {
            AResponse res;
            List<String> notifications = this.room.getNotifications();
            Map<Integer, String> users = this.room.getUsers();

            // Remember to exclude the leaving user from users map
            users.remove(userId);

            res = new RoomNotificationsResponse(roomId, notifications);
            dis.notifyClient(context, res);

            res = new RoomUsersResponse(roomId, users);
            dis.notifyClient(context, res);
        }
    }
}
