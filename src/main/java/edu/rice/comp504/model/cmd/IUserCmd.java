package edu.rice.comp504.model.cmd;

import edu.rice.comp504.model.obj.User;

public interface IUserCmd {
    /**
     * Execute.
     */
    void execute(User context);
}
