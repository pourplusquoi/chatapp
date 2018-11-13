package cmd;

import obj.ChatRoom;
import obj.User;

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
        // TODO: apply filters
        // TODO: kick out users that are filtered out
    }
}
