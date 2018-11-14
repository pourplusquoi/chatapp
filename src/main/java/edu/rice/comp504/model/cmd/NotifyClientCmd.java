package edu.rice.comp504.model.cmd;

import edu.rice.comp504.model.obj.User;
import edu.rice.comp504.model.DispatcherAdapter;

import java.util.Map;

public class NotifyClientCmd implements IUserCmd {

    private Map<String, String> info;
    private DispatcherAdapter dis;

    private static IUserCmd instance;

    /**
     * Constructor.
     */
    private NotifyClientCmd(Map<String, String> info, DispatcherAdapter dis) {
        this.info = info;
        this.dis = dis;
    }

    /**
     * Singleton.
     */
    public static IUserCmd makeNotifyClientCmd(Map<String, String> info, DispatcherAdapter dis) {
        if (instance == null) {
            instance = new NotifyClientCmd(info, dis);
        } else {
            NotifyClientCmd cmd = (NotifyClientCmd) instance;
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
