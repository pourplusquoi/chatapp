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
 * The command to be used when a user wants to join a chatroom
 */
public class JoinRoomCmd implements IUserCmd {

    private ChatRoom room;  // The chatroom which the user wants to join
    private User user;      // The user who wants to join the chatroom

    private static IUserCmd instance;   // The singleton instance of this cmd

    /**
     * Constructor.
     */
    private JoinRoomCmd(ChatRoom room, User user) {
        this.room = room;
        this.user = user;
    }

    /**
     * Singleton.
     */
    public static IUserCmd makeJoinRoomCmd(ChatRoom room, User user) {
        if (instance == null) {
            instance = new JoinRoomCmd(room, user);
        } else {
            JoinRoomCmd cmd = (JoinRoomCmd) instance;
            cmd.room = room;
            cmd.user = user;
        }
        return instance;
    }

    /**
     * Observers' action when they are notified a user has joined a chatroom
     * @context a user which the command will operate on
     */
    @Override
    public void execute(User context) {
        int userId = this.user.getId();
        int roomId = this.room.getId();
        DispatcherAdapter dis = this.room.getDispatcher();

        // When the user to be joined is the current user
        if (this.user == context) {
            List<Integer> joinedRoomIds, availableRoomIds;
            availableRoomIds = context.getAvailableRoomIds();

            if (availableRoomIds.contains(roomId) && this.room.applyFilter(context)) {
                context.moveToJoined(this.room);

                joinedRoomIds = context.getJoinedRoomIds();
                availableRoomIds = context.getAvailableRoomIds();

                AResponse res = new UserRoomsResponse(userId, joinedRoomIds, availableRoomIds);
                dis.notifyByUser(context, res);
            }
        }

        AResponse res;
        List<String> notifications = this.room.getNotifications();
        Map<Integer, String> users = this.room.getUsers();

        res = new RoomNotificationsResponse(roomId, notifications);
        dis.notifyByUser(context, res);

        res = new RoomUsersResponse(roomId, users);
        dis.notifyByUser(context, res);
    }
}
