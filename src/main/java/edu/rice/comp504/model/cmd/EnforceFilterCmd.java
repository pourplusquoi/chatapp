package edu.rice.comp504.model.cmd;

import edu.rice.comp504.model.obj.ChatRoom;
import edu.rice.comp504.model.obj.User;

public class EnforceFilterCmd implements IUserCmd {

    private ChatRoom room;

    private static IUserCmd instance;

    /**
     * Constructor.
     */
    private EnforceFilterCmd(ChatRoom room) {
        this.room = room;
    }

    /**
     * Singleton.
     */
    public static IUserCmd makeFilterCmd(ChatRoom room) {
        if (instance == null) {
            instance = new EnforceFilterCmd(room);
        } else {
            ((EnforceFilterCmd) instance).room = room;
        }
        return instance;
    }

    @Override
    public void execute(User context) {
        // Filter out users and kick them out
        if (!room.applyFilter(context)) {
            room.removeUser(context, "Forced to leave due to chat room filter.");
        }
    }
}
