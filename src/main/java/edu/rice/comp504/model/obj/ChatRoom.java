package edu.rice.comp504.model.obj;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;

import edu.rice.comp504.model.cmd.*;
import edu.rice.comp504.model.DispatcherAdapter;

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

    // Maps key("smallId&largeId") to list of chat history strings
    private Map<String, List<Message>> chatHistory;

    /**
     * Constructor
     */
    public ChatRoom(int id, String name, User owner,
                    int lower, int upper, String[] locations, String[] schools,
                    DispatcherAdapter dispatcher) {
        this.id = id;

        this.name = name;
        this.owner = owner;

        this.ageLowerBound = lower;
        this.ageUpperBound = upper;
        this.locations = locations;
        this.schools = schools;

        this.dis = dispatcher;

        this.notifications = new LinkedList<>();
        this.chatHistory = new ConcurrentHashMap<>();
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

    public List<String> getNotifications() {
        return this.notifications;
    }

    public DispatcherAdapter getDispatcher() {
        return this.dis;
    }

    public Map<Integer, String> getUsers() {
        Map<Integer, String> names = new TreeMap<>();
        IUserCmd cmd = CollectNamesCmd.makeCollectNamesCmd(names);
        this.setChanged();
        this.notifyObservers(cmd);
        return names;
    }

    public void modifyFilter(int lower, int upper, String[] locations, String[] schools) {
        this.ageLowerBound = lower;
        this.ageUpperBound = upper;
        this.locations = locations;
        this.schools = schools;

        // Enforce the filter on all users in chat room
        IUserCmd cmd = EnforceFilterCmd.makeFilterCmd(this);
        this.setChanged();
        this.notifyObservers(cmd);
    }

    public boolean applyFilter(User user) {
        int age = user.getAge();
        if (age < this.ageLowerBound || age > this.ageUpperBound)
            return false;

        Set<String> validLocations = new HashSet<>(Arrays.asList(this.locations));
        Set<String> actualLocations = new HashSet<>(Arrays.asList(user.getLocations()));

        if (Collections.disjoint(validLocations, actualLocations))
            return false;

        Set<String> validSchools = new HashSet<>(Arrays.asList(this.schools));
        Set<String> actualSchools = new HashSet<>(Arrays.asList(user.getSchools()));
        return !Collections.disjoint(validSchools, actualSchools);
    }

    public void addUser(User user) {
        this.addObserver(user);

        String note = "User " + user.getName() + " joined.";
        this.notifications.add(note);
        this.refreshRoom();
    }

    public void removeUser(User user, String reason) {
        IUserCmd cmd = EvictUserCmd.makeEvictCmd(this, user);
        this.setChanged();
        this.notifyObservers(cmd);

        if (user == this.owner)
            this.deleteObservers();
        else this.deleteObserver(user);

        String note = "User " + user.getName() + " left: " + reason;
        this.notifications.add(note);
        this.refreshRoom();

        this.freeChatHistory(user);
    }

    public void storeMessage(User sender, User receiver, Message message) {
        int userIdA = sender.getId();
        int userIdB = receiver.getId();

        // Ensure userIdA < userIdB
        if (userIdA > userIdB) {
            int temp = userIdB;
            userIdB = userIdA;
            userIdA = temp;
        }

        String key = Integer.toString(userIdA) + "&" + Integer.toString(userIdB);
        if (!this.chatHistory.containsKey(key))
            this.chatHistory.put(key, new LinkedList<>());
        List<Message> history = this.chatHistory.get(key);
        history.add(message);
        this.chatHistory.put(key, history);
    }

    private void freeChatHistory(User user) {
        // TODO: parse the key and remove chat history related to user
    }

    /**
     * Refresh the chat room to update notification and user list
     */
    private void refreshRoom() {
        Gson gson = new Gson();
        Map<String, String> info = new HashMap<>();
        IUserCmd cmd;

        // Refresh notification at chat room
        info.put("type", "notifications");
        info.put("roomId", Integer.toString(this.id));
        info.put("content", gson.toJson(this.notifications));
        cmd = RefreshCmd.makeRefreshRoomCmd(info, this.dis);
        this.setChanged();
        this.notifyObservers(cmd);

        // Refresh name of users at chat room
        info.put("type", "users");
        info.put("roomId", Integer.toString(this.id));
        info.put("content", gson.toJson(this.getUsers()));
        cmd = RefreshCmd.makeRefreshRoomCmd(info, this.dis);
        this.setChanged();
        this.notifyObservers(cmd);
    }
}
