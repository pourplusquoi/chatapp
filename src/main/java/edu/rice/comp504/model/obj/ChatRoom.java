package edu.rice.comp504.model.obj;

import java.util.*;

import edu.rice.comp504.model.DispatcherAdapter;
import edu.rice.comp504.model.cmd.FilterCmd;
import edu.rice.comp504.model.cmd.IUserCmd;
import edu.rice.comp504.model.cmd.EvictUserCmd;
import edu.rice.comp504.model.cmd.UserJoinRoomCmd;

public class ChatRoom extends Observable {

    private int id;
    private String name;
    private User owner;

    private int ageLowerBound;
    private int ageUpperBound;
    private String[] locations;
    private String[] schools;

    private DispatcherAdapter dis;

    // notifications contain why the user left, etc.
    private List<String> notifications;

    // Maps key("smallerID+largerID") to list of chat history strings
    private Map<String, List<String>> chatHistory;

    /**
     * Constructor
     */
    public ChatRoom(int id, String name, User owner, int lower, int upper, String[] locations, String[] schools, DispatcherAdapter dis) {
        this.id = id;
        this.name = name;
        this.owner = owner;

        this.ageLowerBound = lower;
        this.ageUpperBound = upper;
        this.locations = locations;
        this.schools = schools;

        this.dis = dis;

        this.notifications = new LinkedList<>();
        this.chatHistory = new HashMap<>();

        //remember to add owner to the chatroom's observer
        this.addObserver(owner);
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
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
//        System.out.println("age is "+ age);
        if (age < this.ageLowerBound || age > this.ageUpperBound)
            return false;

        List<String> validLocations = Arrays.asList(this.locations);
//        System.out.println("if validlocation contains " + validLocations.contains(user.getLocation()));
        validLocations.retainAll(Arrays.asList(user.getLocation()));
        if (validLocations.isEmpty())
            return false;

        List<String> validSchools = Arrays.asList(this.schools);
        validSchools.retainAll(Arrays.asList(user.getSchool()));
        return !validSchools.isEmpty();
    }

    public void addUser(User user) {
        IUserCmd cmd = UserJoinRoomCmd.makeUserJoinRoomCmd(this, user, dis);
        this.addObserver(user);
        this.setChanged();
        this.notifyObservers(cmd);
    }

    public void removeUser(User user) {
        IUserCmd cmd = EvictUserCmd.makeEvictCmd(this, user);
        this.setChanged();
        this.notifyObservers(cmd);
        if (user == this.owner)
            this.deleteObservers();
        else this.deleteObserver(user);
    }

    public void sendMsg(User target, String msg){
        dis.notifyClient(target, msg);
    }
}
