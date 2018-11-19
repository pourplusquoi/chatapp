package edu.rice.comp504.model.cmd;

import java.util.Map;

import edu.rice.comp504.model.obj.User;

public class CollectNamesCmd implements IUserCmd {

    private Map<Integer, String> users;

    private static IUserCmd instance;

    /**
     * Constructor.
     */
    private CollectNamesCmd(Map<Integer, String> users) {
        this.users = users;
    }

    /**
     * Singleton.
     */
    public static IUserCmd makeCollectNamesCmd(Map<Integer, String> users) {
        if (instance == null) {
            instance = new CollectNamesCmd(users);
        } else {
            ((CollectNamesCmd) instance).users = users;
        }
        return instance;
    }

    @Override
    public void execute(User context) {
        this.users.put(context.getId(), context.getName());
    }
}
