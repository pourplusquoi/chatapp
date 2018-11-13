package cmd;

import obj.ChatRoom;
import obj.User;

public class AppendCmd implements IUserCmd {

    private ChatRoom room;

    static private IUserCmd instance;

    /**
     * Constructor
     */
    private AppendCmd(ChatRoom room) {
        this.room = room;
    }

    static public IUserCmd makeAppendCmd(ChatRoom room) {
        if (instance == null)
            instance = new AppendCmd(room);
        else ((AppendCmd) instance).room = room;
        return instance;
    }

    @Override
    public void execute(User context) {
        if (context != this.room.getOwner())
            context.addAvailable(this.room);
    }
}
