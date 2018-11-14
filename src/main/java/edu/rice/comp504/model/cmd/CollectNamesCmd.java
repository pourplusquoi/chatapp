package edu.rice.comp504.model.cmd;

import edu.rice.comp504.model.obj.User;

import java.util.Map;

public class CollectNamesCmd implements IUserCmd {

    private Map<Integer, String> names;

    private static IUserCmd instance;

    /**
     * Constructor.
     */
    private CollectNamesCmd(Map<Integer, String> names) {
        this.names = names;
    }

    /**
     * Singleton.
     */
    public static IUserCmd makeCollectNamesCmd(Map<Integer, String> names) {
        if (instance == null) {
            instance = new CollectNamesCmd(names);
        } else {
            ((CollectNamesCmd) instance).names = names;
        }
        return instance;
    }

    @Override
    public void execute(User context) {
        this.names.put(context.getId(), context.getName());
    }
}
