package edu.rice.comp504.model.obj;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import edu.rice.comp504.model.cmd.*;
import edu.rice.comp504.model.DispatcherAdapter;

/*
The Chatroom class defines a chat room object and private fileds of a chat room
*/
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
     * @param id the identity number of the chat room
     * @param name the name of the chat room
     * @param owner the chat room owner
     * @param lower the lower bound of age restriction
     * @param upper the upper bound of age restriction
     * @param locations the location restriction
     * @param schools the school restriction
     * @param dispatcher the adapter
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

    /**
     * Get the chat room id
     * @return the chat room id
     * */
    public int getId() {
        return this.id;
    }

    /**
     * Get the chat room name
     * @return the chat room name
     * */
    public String getName() {
        return this.name;
    }

    /**
     * Get the chat room owner
     * @return a User object which is the owner of the chat room
     * */
    public User getOwner() {
        return this.owner;
    }

    /**
     * Get a list of notifications
     * @return notification list
     * */
    public List<String> getNotifications() {
        return this.notifications;
    }

    /**
     * Get the chat history between two users
     * @return chat history
     * */
    public Map<String, List<Message>> getChatHistory() {
        return this.chatHistory;
    }

    /**
     * @return the dispatcher
     * */
    public DispatcherAdapter getDispatcher() {
        return this.dis;
    }

    /**
     * Return users in the chat room
     */
    public Map<Integer, String> getUsers() {
        Map<Integer, String> users = new TreeMap<>();
        IUserCmd cmd = CollectNamesCmd.makeCollectNamesCmd(users);
        this.setChanged();
        this.notifyObservers(cmd);
        return users;
    }

    /**
     * Check if user satisfy the age, location and school restriction
     * @return boolean value indicating whether the user is eligible to join the room
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
     * Modify the current room age, location or school restriction
     * Then apply the new restriction to all users in the chat room
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
     * If user satisfy all restrictions and has the room in his available room list
     * Make a user joined notification and then add user into the observer list
     */
    public boolean addUser(User user) {
        if (this.applyFilter(user) && user.getAvailableRoomIds().contains(this.id)) {
            // Make notification
            String note = "User " + user.getName() + " joined.";
            this.notifications.add(note);

            // Add user to the room obs list
            this.addObserver(user);

            IUserCmd cmd = JoinRoomCmd.makeJoinRoomCmd(this, user);
            this.setChanged();
            this.notifyObservers(cmd);
            return true;

        } else {
            return false;
        }
    }

    /**
     * Remove user from the chat room
     * Set notification indicating the user left reason
     * Delete user from observer list
     */
    public boolean removeUser(User user, String reason) {
        if (user.getJoinedRoomIds().contains(this.id)) {
            // Make notification
            String note = "User " + user.getName() + " left: " + reason;
            this.notifications.add(note);

            IUserCmd cmd = LeaveRoomCmd.makeLeaveRoomCmd(this, user);
            this.setChanged();
            this.notifyObservers(cmd);

            // Remove user from the room obs list
            if (user == this.getOwner()) { // When room owner leaves, unload the room
                this.deleteObservers();
                this.dis.unloadRoom(this.id);
            } else { // otherwise, remove the user from obs
                this.deleteObserver(user);
            }
            this.freeChatHistory(user);
            return true;

        } else {
            return false;
        }
    }

    /**
     * Append chat message into chat history list
     * Map the single message body with key value (senderID&receiverID)
     */
    public void storeMessage(User sender, User receiver, Message message) {
        int userAId = sender.getId();
        int userBId = receiver.getId();

        // Ensure userIdA < userIdB
        if (userAId > userBId) {
            int temp = userBId;
            userBId = userAId;
            userAId = temp;
        }

        // Key format (senderID&ReceiverID)
        String key = Integer.toString(userAId) + "&" + Integer.toString(userBId);
        if (!this.chatHistory.containsKey(key)) {
            this.chatHistory.put(key, new LinkedList<>());
        }
        List<Message> history = this.chatHistory.get(key);
        history.add(message);
        this.chatHistory.put(key, history);
    }

    /**
     * Parse the key and remove chat history related to user
     */
    private void freeChatHistory(User user) {
        // TODO: parse the key and remove chat history related to user
    }
}
