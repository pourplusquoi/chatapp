package edu.rice.comp504.model.cmd;

import edu.rice.comp504.model.obj.User;
import edu.rice.comp504.model.res.AResponse;
import edu.rice.comp504.model.DispatcherAdapter;

public class NotifyClientCmd implements IUserCmd {

    private AResponse res;
    private DispatcherAdapter dis;

    private static IUserCmd instance;

    /**
     * Constructor.
     */
    private NotifyClientCmd(AResponse res, DispatcherAdapter dis) {
        this.res = res;
        this.dis = dis;
    }

    /**
     * Singleton.
     */
    public static IUserCmd makeNotifyClientCmd(AResponse res, DispatcherAdapter dis) {
        if (instance == null) {
            instance = new NotifyClientCmd(res, dis);
        } else {
            NotifyClientCmd cmd = (NotifyClientCmd) instance;
            cmd.res = res;
            cmd.dis = dis;
        }
        return instance;
    }

    @Override
    public void execute(User context) {
        this.dis.notifyClient(context, this.res);
    }
}
