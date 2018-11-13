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

    // Maps key("smallId&largeId") to list of chat history strings
    private Map<String, List<Message>> chatHistory;

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

    public List<String> getNotifications() {
        return this.notifications;
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

    public void removeUser(User user, String reason) {
        IUserCmd cmd = EvictCmd.makeEvictCmd(this, user);
        this.setChanged();
        this.notifyObservers(cmd);

        if (user == this.owner)
            this.deleteObservers();
        else this.deleteObserver(user);

        String note = "User " + Integer.toString(user.getId()) + " left: " + reason;
        this.notifications.add(note);
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
        List<Message> history = this.chatHistory.get(key);
        history.add(message);
        this.chatHistory.put(key, history);
    }

    private void freeChatHistory(User user) {
        // TODO: parse the key and remove chat history related to user
    }
}
