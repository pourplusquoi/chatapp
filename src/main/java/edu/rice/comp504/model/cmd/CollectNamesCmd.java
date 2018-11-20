package edu.rice.comp504.model.cmd;

import java.util.Map;

import edu.rice.comp504.model.obj.User;

/**
 * The command to be used when need to collect users' id and name
 */
public class CollectNamesCmd implements IUserCmd {

    private Map<Integer, String> users; // The map between user id and user name

    private static IUserCmd instance;   // The singleton instance of this cmd

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

    /**
     * Observers' action when they need to collect user id and user name
     * @context a user which the command will operate on
     */
    @Override
    public void execute(User context) {
        this.users.put(context.getId(), context.getName());
    }
}
