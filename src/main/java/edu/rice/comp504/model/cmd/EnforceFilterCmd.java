package edu.rice.comp504.model.cmd;

import edu.rice.comp504.model.obj.ChatRoom;
import edu.rice.comp504.model.obj.User;

/**
 * The command to be used when a chatroom sets/resets its restriction
 */
public class EnforceFilterCmd implements IUserCmd {

    private ChatRoom room;      // The chatroom which sets new restriction

    private static IUserCmd instance;   // The singleton instance of this cmd

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

    /**
     * Observers' action when they are notified the restriction of the chatroom is changed
     * @context a user which the command will operate on
     */
    @Override
    public void execute(User context) {
        // Filter out users and kick them out
        if (!this.room.applyFilter(context)) {
            this.room.removeUser(context, "Forced to leave due to chat room filter.");
        }
    }
}
