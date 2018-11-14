package edu.rice.comp504.model.cmd;

import edu.rice.comp504.model.obj.User;

public interface IUserCmd {
    void execute(User context);
}
