package edu.rice.comp504.model.cmd;

import edu.rice.comp504.model.obj.ChatRoom;
import edu.rice.comp504.model.obj.User;

public class DeleteRoomCmd implements IUserCmd {

    private ChatRoom room;

    static private IUserCmd instance;

    /**
     * Constructor
     */
    private DeleteRoomCmd(ChatRoom room) {
        this.room = room;
    }

    static public IUserCmd makeDeteleCmd(ChatRoom room) {
        if (instance == null)
            instance = new DeleteRoomCmd(room);
        else ((DeleteRoomCmd) instance).room = room;
        return instance;
    }

    @Override
    public void execute(User context) {
        context.removeAvailable(this.room);
    }
}
