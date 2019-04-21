package edu.rice.comp504.model.obj;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import edu.rice.comp504.model.cmd.*;
import edu.rice.comp504.model.DispatcherAdapter;

/**
 * The ChatRoom class defines a chat room object and private fields of a chat room.
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

    private Map<Integer, String> userNameFromUserId;
    private List<String> notifications;
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

        this.userNameFromUserId = new ConcurrentHashMap<>();

        this.notifications = new LinkedList<>();
        this.chatHistory = new ConcurrentHashMap<>();
    }

    // Getter for age lower bound.
    public int getAgeLowerBound() {
        return ageLowerBound;
    }


    // Getter for age upper bound.
    public int getAgeUpperBound() {
        return ageUpperBound;
    }

    // Getter for locations.
    public String[] getLocations() {
        return locations;
    }

    // Getter for schools.
    public String[] getSchools() {
        return schools;
    }

    /**
     * Get the chat room id.
     * @return the chat room id
     */
    public int getId() {
        return this.id;
    }

    /**
     * Get the chat room name.
     * @return the chat room name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get the chat room owner.
     * @return a User object which is the owner of the chat room
     */
    public User getOwner() {
        return this.owner;
    }

    /**
     * Get a list of notifications.
     * @return notification list
     */
    public List<String> getNotifications() {
        return this.notifications;
    }

    /**
     * Get the chat history between two users.
     * @return chat history
     */
    public Map<String, List<Message>> getChatHistory() {
        return this.chatHistory;
    }

    /**
     * Get the dispatcher adapter.
     * @return the dispatcher
     */
    public DispatcherAdapter getDispatcher() {
        return this.dis;
    }

    /**
     * Return id and name of all users in the chat room.
     * @return a hash map that maps from user id to user name for all users
     */
    public Map<Integer, String> getUsers() {
        return this.userNameFromUserId;
    }

    /**
     * Check if user satisfy the age, location and school restriction.
     * @param user the user to which the filer is applied
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
     * If user satisfy all restrictions and has the room in his available room list, make a user joined notification and then add user into the observer list.
     * @param user the user that requests to join the room
     * @return boolean value indicating whether or not join is successful
     */
    public boolean addUser(User user) {
        if (this.applyFilter(user) && user.getAvailableRoomIds().contains(this.id)) {
            // Make notification.
            String note = "User " + user.getName() + " joined.\n";
            this.notifications.add(note);

            // Add user to the room obs list.
            this.addObserver(user);
            this.userNameFromUserId.put(user.getId(), user.getName());

            IUserCmd cmd = new JoinRoomCmd(this, user);
            this.setChanged();
            this.notifyObservers(cmd);
            return true;

        } else {
            return false;
        }
    }

    /**
     * Remove user from the chat room, set notification indicating the user left reason, delete user from observer list.
     * @param user the user that is about to leave the room
     * @param reason the reason why the user leaves
     * @return boolean value indicating whether or not leave is successful
     */
    public boolean removeUser(User user, String reason) {
        if (user.getJoinedRoomIds().contains(this.id)) {
            // Remove user from the room obs list.
            if (user == this.getOwner()) { // When room owner leaves, unload the room.
                this.deleteObservers();
                this.userNameFromUserId.clear();
                this.dis.unloadRoom(this.id);
            } else { // otherwise, remove the user from obs.
                // Make notification.
                String note = "User " + user.getName() + " left: " + reason + '\n';
                this.notifications.add(note);

                IUserCmd cmd = new LeaveRoomCmd(this, user);
                this.setChanged();
                this.notifyObservers(cmd);

                this.deleteObserver(user);
                this.userNameFromUserId.remove(user.getId());
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Append chat message into chat history list, map the single message body with key value (smallId&largeId).
     * @param sender the user that sends the message
     * @param receiver the user that receives the message
     * @param message the message being sent
     */
    public void storeMessage(User sender, User receiver, Message message) {
        int userAId = sender.getId();
        int userBId = receiver.getId();

        // Ensure userIdA < userIdB.
        if (userAId > userBId) {
            int temp = userBId;
            userBId = userAId;
            userAId = temp;
        }

        String key = Integer.toString(userAId) + "&" + Integer.toString(userBId);
        if (!this.chatHistory.containsKey(key)) {
            this.chatHistory.put(key, new LinkedList<>());
        }

        List<Message> history = this.chatHistory.get(key);
        history.add(message);
        this.chatHistory.put(key, history);
    }
}
