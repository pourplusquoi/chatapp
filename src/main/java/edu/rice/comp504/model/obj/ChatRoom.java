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
     * Constructor.
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

    /**
     * function.
     */
    public Map<Integer, String> getUsers() {
        Map<Integer, String> names = new TreeMap<>();
        IUserCmd cmd = CollectNamesCmd.makeCollectNamesCmd(names);
        this.setChanged();
        this.notifyObservers(cmd);
        return names;
    }

    /**
     * function.
     */
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

    /**
     * function.
     */
    public boolean applyFilter(User user) {
        int age = user.getAge();
        if (age < this.ageLowerBound || age > this.ageUpperBound) {
            return false;
        }
        return Arrays.asList(this.locations).contains(user.getLocation())
                && Arrays.asList(this.schools).contains(user.getSchool());
    }

    /**
     * function.
     */
    public void addUser(User user) {
        this.addObserver(user);

        String note = "User " + user.getName() + " joined.";
        this.notifications.add(note);
        this.refresh();
    }

    /**
     * function.
     */
    public void removeUser(User user, String reason) {
        if (user == this.owner) {
            this.deleteObservers();
        } else {
            this.deleteObserver(user);
        }

        String note = "User " + user.getName() + " left: " + reason;
        this.notifications.add(note);
        this.refresh();

        this.freeChatHistory(user);
    }

    /**
     * function.
     */
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
        if (!this.chatHistory.containsKey(key)) {
            this.chatHistory.put(key, new LinkedList<>());
        }
        List<Message> history = this.chatHistory.get(key);
        history.add(message);
        this.chatHistory.put(key, history);
    }

    /**
     * function.
     */
    private void freeChatHistory(User user) {
        // TODO: parse the key and remove chat history related to user
    }

    /**
     * Refresh the chat room to update notification and user list.
     */
    private void refresh() {
        Gson gson = new Gson();
        Map<String, String> info = new HashMap<>();
        IUserCmd cmd;

        // Refresh notification at chat room
        info.put("type", "roomNotifications");
        info.put("roomId", Integer.toString(this.id));
        info.put("content", gson.toJson(this.notifications));
        cmd = NotifyClientCmd.makeNotifyClientCmd(info, this.dis);
        this.setChanged();
        this.notifyObservers(cmd);

        // Refresh name of users at chat room
        info.put("type", "roomUsers");
        info.put("roomId", Integer.toString(this.id));
        info.put("content", gson.toJson(this.getUsers()));
        cmd = NotifyClientCmd.makeNotifyClientCmd(info, this.dis);
        this.setChanged();
        this.notifyObservers(cmd);
    }
}
