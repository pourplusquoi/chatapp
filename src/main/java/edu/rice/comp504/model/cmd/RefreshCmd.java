package edu.rice.comp504.model.cmd;

import edu.rice.comp504.model.obj.User;
import edu.rice.comp504.model.DispatcherAdapter;

import java.util.Map;

public class RefreshCmd implements IUserCmd {

    private Map<String, String> info;
    private DispatcherAdapter dis;

    static private IUserCmd instance;

    /**
     * Constructor
     */
    private RefreshCmd(Map<String, String> info, DispatcherAdapter dis) {
        this.info = info;
        this.dis = dis;
    }

    static public IUserCmd makeRefreshRoomCmd(Map<String, String> info, DispatcherAdapter dis) {
        if (instance == null)
            instance = new RefreshCmd(info, dis);
        else {
            RefreshCmd cmd = (RefreshCmd) instance;
            cmd.info = info;
            cmd.dis = dis;
        }
        return instance;
    }

    @Override
    public void execute(User context) {
        this.dis.notifyClient(context, this.info);
    }
}
