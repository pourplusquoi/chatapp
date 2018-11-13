package edu.rice.comp504.model.cmd;

import edu.rice.comp504.model.DispatcherAdapter;
import edu.rice.comp504.model.obj.ChatRoom;
import edu.rice.comp504.model.obj.User;

public class AppendRoomCmd implements IUserCmd {

    private ChatRoom room;

    static private IUserCmd instance;

    private DispatcherAdapter dis;

    /**
     * Constructor
     */
    private AppendRoomCmd(ChatRoom room, DispatcherAdapter dis) {
        this.room = room;
        this.dis = dis;
    }

    static public IUserCmd makeAppendCmd(ChatRoom room, DispatcherAdapter dis) {
        if (instance == null)
            instance = new AppendRoomCmd(room, dis);
        else {
            ((AppendRoomCmd) instance).room = room;
            ((AppendRoomCmd) instance).dis = dis;
        }
        return instance;
    }

    @Override
    public void execute(User context) {
//        System.out.println("owner name is " + this.room.getOwner().getName());
//        System.out.println("the user name is " + context.getName());
        if (context != this.room.getOwner()) {
            context.addAvailable(this.room);
            dis.notifyClient(context, "New room available " + this.room.getName() + " owner is " + this.room.getOwner().getName());
        }
    }
}
