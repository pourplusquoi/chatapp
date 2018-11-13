package edu.rice.comp504.model.cmd;

import edu.rice.comp504.model.obj.ChatRoom;
import edu.rice.comp504.model.obj.User;

public class EvictUserCmd implements IUserCmd {

    private ChatRoom room;
    private User victim;

    static private IUserCmd instance;

    /**
     * Constructor
     */
    private EvictUserCmd(ChatRoom room, User victim) {
        this.room = room;
        this.victim = victim;
    }

    static public IUserCmd makeEvictCmd(ChatRoom room, User victim) {
        if (instance == null)
            instance = new EvictUserCmd(room, victim);
        else {
            EvictUserCmd cmd = (EvictUserCmd) instance;
            cmd.room = room;
            cmd.victim = victim;
        }
        return instance;
    }

    @Override
    public void execute(User context) {
        // When victim is owner, evict all users; otherwise, only evict victim
        if (this.victim == this.room.getOwner() || this.victim == context) {
            context.unjoinRoom(this.room, this.victim);
        }
    }
}
