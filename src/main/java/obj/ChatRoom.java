package obj;

import java.util.*;

import cmd.FilterCmd;
import cmd.IUserCmd;
import cmd.EvictCmd;

public class ChatRoom extends Observable {

    private int id;
    private User owner;

    private int ageLowerBound;
    private int ageUpperBound;
    private String[] locations;
    private String[] schools;

    // notifications contain why the user left, etc.
    private List<String> notifications;

    // Maps key("smallerID+largerID") to list of chat history strings
    private Map<String, List<String>> chatHistory;

    /**
     * Constructor
     */
    public ChatRoom(int id, User owner, int lower, int upper, String[] locations, String[] schools) {
        this.id = id;

        this.owner = owner;

        this.ageLowerBound = lower;
        this.ageUpperBound = upper;
        this.locations = locations;
        this.schools = schools;

        this.notifications = new LinkedList<>();
        this.chatHistory = new HashMap<>();
    }

    public int getId() {
        return this.id;
    }

    public User getOwner() {
        return this.owner;
    }

    public void modifyFilter(int lower, int upper, String[] locations, String[] schools) {
        this.ageLowerBound = lower;
        this.ageUpperBound = upper;
        this.locations = locations;
        this.schools = schools;

        IUserCmd cmd = FilterCmd.makeFilterCmd(this);
        this.setChanged();
        this.notifyObservers(cmd);
    }

    public boolean applyFilter(User user) {
        int age = user.getAge();
        if (age < this.ageLowerBound || age > this.ageUpperBound)
            return false;

        List<String> validLocations = Arrays.asList(this.locations);
        validLocations.retainAll(Arrays.asList(user.getLocations()));
        if (validLocations.isEmpty())
            return false;

        List<String> validSchools = Arrays.asList(this.schools);
        validSchools.retainAll(Arrays.asList(user.getSchools()));
        return !validSchools.isEmpty();
    }

    public void addUser(User user) {
        this.addObserver(user);
    }

    public void removeUser(User user) {
        IUserCmd cmd = EvictCmd.makeEvictCmd(this, user);
        this.setChanged();
        this.notifyObservers(cmd);
        if (user == this.owner)
            this.deleteObservers();
        else this.deleteObserver(user);
    }
}
