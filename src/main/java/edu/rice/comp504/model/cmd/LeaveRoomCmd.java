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

public class LeaveRoomCmd implements IUserCmd {

    private ChatRoom room;
    private User user;

    private static IUserCmd instance;

    private LeaveRoomCmd(ChatRoom room, User user) {
        this.room = room;
        this.user = user;
    }

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

            AResponse res = new UserRoomsResponse(context.getId(),
                    joinedRoomIds, availableRoomIds);
            dis.notifyClient(context, res);

        } else {
            AResponse res;
            List<String> notifications = this.room.getNotifications();
            Map<Integer, String> users = this.room.getUsers();

            // Remember to exclude the leaving user from users map
            users.remove(this.user.getId());

            res = new RoomNotificationsResponse(roomId, notifications);
            dis.notifyClient(context, res);

            res = new RoomUsersResponse(roomId, users);
            dis.notifyClient(context, res);
        }
    }
}
