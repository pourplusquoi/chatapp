package edu.rice.comp504.model.cmd;

import edu.rice.comp504.model.obj.ChatRoom;
import edu.rice.comp504.model.obj.User;

public class AppendRoomCmd implements IUserCmd {

    private ChatRoom room;

    private static IUserCmd instance;

    /**
     * Constructor.
     */
    private AppendRoomCmd(ChatRoom room) {
        this.room = room;
    }


    /**
     * Singleton.
     */
    public static IUserCmd makeAppendCmd(ChatRoom room) {
        if (instance == null) {
            instance = new AppendRoomCmd(room);
        } else {
            ((AppendRoomCmd) instance).room = room;
        }
        return instance;
    }

    @Override
    public void execute(User context) {
        context.addRoom(this.room);
    }
}
