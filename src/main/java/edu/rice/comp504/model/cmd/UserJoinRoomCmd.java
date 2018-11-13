package edu.rice.comp504.model.cmd;

import edu.rice.comp504.model.DispatcherAdapter;
import edu.rice.comp504.model.obj.ChatRoom;
import edu.rice.comp504.model.obj.User;

public class UserJoinRoomCmd implements IUserCmd {

    private ChatRoom room;
    private User newUser;
    private DispatcherAdapter dis;

    static private IUserCmd instance;

    /**
     * Constructor
     */
    private UserJoinRoomCmd(ChatRoom room, User newUser, DispatcherAdapter dis) {
        this.room = room;
        this.newUser = newUser;
        this.dis = dis;
    }

    static public IUserCmd makeUserJoinRoomCmd(ChatRoom room, User newUser, DispatcherAdapter dis) {
        if (instance == null)
            instance = new UserJoinRoomCmd(room, newUser, dis);
        else {
            UserJoinRoomCmd cmd = (UserJoinRoomCmd) instance;
            cmd.room = room;
            cmd.newUser = newUser;
            cmd.dis = dis;
        }
        return instance;
    }

    @Override
    public void execute(User context) {
        // When victim is owner, evict all users; otherwise, only evict victim
        System.out.println();
        if (this.newUser != context) {
//            System.out.println(this.newUser.getName());
            dis.notifyClient(context,"A new user " + context.getName() + " has been added to room " + this.room.getName());
        }
    }
}
