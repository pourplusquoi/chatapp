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

public class JoinRoomCmd implements IUserCmd {

    private ChatRoom room;
    private User user;

    private static IUserCmd instance;

    private JoinRoomCmd(ChatRoom room, User user) {
        this.room = room;
        this.user = user;
    }

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

    @Override
    public void execute(User context) {
        int userId = this.user.getId();
        int roomId = this.room.getId();
        DispatcherAdapter dis = this.room.getDispatcher();

        // When the user to be joined is the current user
        if (this.user == context) {
            List<Integer> joinedRoomIds = this.user.getJoinedRoomIds();
            List<Integer> availableRoomIds = this.user.getAvailableRoomIds();

            if (availableRoomIds.contains(roomId) && room.applyFilter(this.user)) {
                this.user.moveToJoined(this.room);
                AResponse res = new UserRoomsResponse(userId, joinedRoomIds, availableRoomIds);
                dis.notifyClient(this.user, res);
            }
        }

        String note = "User " + user.getName() + " joined.";
        this.room.storeNotification(note);

        AResponse res;
        List<String> notifications = this.room.getNotifications();
        Map<Integer, User> users = this.room.getUsers();

        res = new RoomNotificationsResponse(roomId, notifications);
        dis.notifyClient(this.user, res);

        res = new RoomUsersResponse(roomId, users);
        dis.notifyClient(this.user, res);
    }
}