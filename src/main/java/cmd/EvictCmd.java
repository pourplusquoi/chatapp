package cmd;

import obj.ChatRoom;
import obj.User;

public class EvictCmd implements IUserCmd {

    private ChatRoom room;
    private User victim;

    static private IUserCmd instance;

    /**
     * Constructor
     */
    private EvictCmd(ChatRoom room, User victim) {
        this.room = room;
        this.victim = victim;
    }

    static public IUserCmd makeEvictCmd(ChatRoom room, User victim) {
        if (instance == null)
            instance = new EvictCmd(room, victim);
        else {
            EvictCmd cmd = (EvictCmd) instance;
            cmd.room = room;
            cmd.victim = victim;
        }
        return instance;
    }

    @Override
    public void execute(User context) {
        // When victim is the owner, evict all users;
        // When victim is not the owner, only evict the victim
        if (this.victim == this.room.getOwner() || this.victim == context) {
            context.unjoinRoom(this.room, this.victim);
        }
    }
}
