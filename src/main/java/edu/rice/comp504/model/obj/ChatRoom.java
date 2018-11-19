package edu.rice.comp504.model.obj;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import edu.rice.comp504.model.cmd.*;
import edu.rice.comp504.model.DispatcherAdapter;
import edu.rice.comp504.model.res.AResponse;
import edu.rice.comp504.model.res.RoomNotificationsResponse;
import edu.rice.comp504.model.res.RoomUsersResponse;

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
    public Map<Integer, User> getUsers() {
        Map<Integer, User> users = new TreeMap<>();
        IUserCmd cmd = CollectNamesCmd.makeCollectNamesCmd(users);
        this.setChanged();
        this.notifyObservers(cmd);
        return users;
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
        if (user == this.owner) { // When room owner leaves, unload the room
            this.deleteObservers();
            this.dis.unloadRoom(this.id);
        } else { // otherwise, remove the user from obs
            this.deleteObserver(user);
            user.moveToAvailable(this);
        }

        String note = "User " + user.getName() + " left: " + reason;
        this.notifications.add(note);
        this.refresh();

        this.freeChatHistory(user);
    }

    /**
     * function.
     * @param note
     */
    public void storeNotification(String note) {
        this.notifications.add(note);
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
     * function.
     */
    public void notifyUsers(IUserCmd cmd) {
        this.setChanged();
        this.notifyObservers(cmd);
    }

    /**
     * Refresh the chat room to update notification and user list.
     */
    private void refresh() {
        IUserCmd cmd;
        AResponse res;

        // Refresh notification at chat room
        res = new RoomNotificationsResponse(this.id, this.notifications);
        cmd = NotifyClientCmd.makeNotifyClientCmd(res, this.dis);
        this.setChanged();
        this.notifyObservers(cmd);

        // Refresh name of users at chat room
        res = new RoomUsersResponse(this.id, this.getUsers());
        cmd = NotifyClientCmd.makeNotifyClientCmd(res, this.dis);
        this.setChanged();
        this.notifyObservers(cmd);
    }
}
