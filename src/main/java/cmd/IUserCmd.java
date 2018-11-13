package cmd;

import obj.User;

public interface IUserCmd {
    void execute(User context);
}
