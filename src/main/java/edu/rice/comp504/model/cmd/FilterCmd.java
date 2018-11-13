package edu.rice.comp504.model.cmd;

import edu.rice.comp504.model.obj.ChatRoom;
import edu.rice.comp504.model.obj.User;

public class FilterCmd implements IUserCmd {

    private ChatRoom room;

    static private IUserCmd instance;

    /**
     * Constructor
     */
    private FilterCmd(ChatRoom room) {
        this.room = room;
    }

    static public IUserCmd makeFilterCmd(ChatRoom room) {
        if (instance == null)
            instance = new FilterCmd(room);
        else ((FilterCmd) instance).room = room;
        return instance;
    }

    @Override
    public void execute(User context) {
        // Filter out users and kick them out
        if (!room.applyFilter(context))
            room.removeUser(context);
    }
}
