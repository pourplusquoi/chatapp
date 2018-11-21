package edu.rice.comp504.model.cmd;

import edu.rice.comp504.model.obj.ChatRoom;
import edu.rice.comp504.model.obj.User;

/**
 * The command to be used when a chat room sets/resets its restriction.
 */
public class EnforceFilterCmd implements IUserCmd {

    private ChatRoom room;

    /**
     * Constructor.
     * @param room the chat room which sets new restriction
     */
    public EnforceFilterCmd(ChatRoom room) {
        this.room = room;
    }

    /**
     * Observers' action when they are notified the restriction of the chat room is changed.
     * @param context a user which the command will operate on
     */
    @Override
    public void execute(User context) {
        // Filter out users and kick them out
        if (!this.room.applyFilter(context)) {
            this.room.removeUser(context, "Forced to leave due to chat room filter.");
        }
    }
}
