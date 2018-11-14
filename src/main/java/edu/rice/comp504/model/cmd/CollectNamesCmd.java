package edu.rice.comp504.model.cmd;

import edu.rice.comp504.model.obj.User;

import java.util.List;

public class CollectNamesCmd implements IUserCmd {

    private List<String> names;

    static private IUserCmd instance;

    /**
     * Constructor
     */
    private CollectNamesCmd(List<String> names) {
        this.names = names;
    }

    static public IUserCmd makeCollectNamesCmd(List<String> names) {
        if (instance == null)
            instance = new CollectNamesCmd(names);
        else ((CollectNamesCmd) instance).names = names;
        return instance;
    }

    @Override
    public void execute(User context) {
        String name = context.getName() + "(" + context.getId() + ")";
        this.names.add(name);
    }
}
