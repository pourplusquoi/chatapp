package dispatcher;

import cmd.AppendCmd;
import cmd.IUserCmd;
import obj.ChatRoom;
import obj.Message;
import obj.User;

import java.util.*;

public class DispatcherAdapter extends Observable {

    private int nextUserId;
    private int nextRoomId;

    // Maps user id to the user
    private Map<Integer, User> users;

    // Maps room id to the chat room
    private Map<Integer, ChatRoom> rooms;

    /**
     * Constructor
     */
    public DispatcherAdapter() {
        this.nextRoomId = 0;
        this.nextUserId = 0;
        this.users = new HashMap<>();
        this.rooms = new HashMap<>();
    }

    public User loadUser(int age, String[] locations, String[] schools) {
        User user = new User(this.nextUserId,
                age, locations, schools, this.rooms.values().toArray(new ChatRoom[0]));
        this.users.put(this.nextRoomId, user);
        this.nextUserId++;

        this.addObserver(user);
        return user;
    }

    public ChatRoom loadRoom(int ownerId, int lower, int upper,
                             String[] locations, String[] schools) {
        ChatRoom room = new ChatRoom(this.nextRoomId,
                this.users.get(ownerId), lower, upper, locations, schools);
        this.rooms.put(this.nextRoomId, room);
        this.nextRoomId++;

        IUserCmd cmd = AppendCmd.makeAppendCmd(room);
        this.setChanged();
        this.notifyObservers(cmd);

        return room;
    }

    public void unloadUser(int userId) {
        // TODO: remove user from the environment, and automatically leave all joined chat rooms
    }

    public void joinRoom(int roomId, int userId) {
        // TODO: make user join room
    }

    public void leaveRoom(int roomId, int userId) {
        // TODO: make user leave room
    }

    public void freeEmptyRooms() {
        List<Integer> empty = new LinkedList<>();
        for (Integer roomId : this.rooms.keySet()) {
            if (this.rooms.get(roomId).countObservers() == 0)
                empty.add(roomId);
        }
        for (Integer roomId : empty)
            this.rooms.remove(roomId);
    }

    /**
     * A sender sends a string message to a receiver
     * @param roomId the if of the chat room
     * @param fromUserId the id of the sender
     * @param toUserId the id of the receiver
     * @param raw string message that sender sends to receiver
     */
    public void sendMessage(int roomId, int fromUserId, int toUserId, String raw) {
        ChatRoom room = this.rooms.get(roomId);
        User sender = this.users.get(fromUserId);
        User receiver = this.users.get(toUserId);

        // When user sends "hate", kick him/her out of all rooms
        if (raw.contains("hate")) {
            for (ChatRoom joinedRoom : sender.getJoined())
                joinedRoom.removeUser(sender, "Forced to leave due to illegal speech.");
        }

        else {
            Message message = new Message(sender, receiver, raw);
            room.storeMessage(sender, receiver, message);
        }
    }

    /**
     * Get notifications in the chat room
     * @param roomId the if of the chat room
     * @return notifications of the chat room
     */
    public String[] getNotifications(int roomId) {
        ChatRoom room = this.rooms.get(roomId);
        return room.getNotifications().toArray(new String[0]);
    }

    /**
     * Get chat history between user A and user B (commutative)
     * @param roomId the id of chat room
     * @param userIdA the id of user A
     * @param userIdB the id of user B
     * @return chat history between user A and user B
     */
    public Message[] getChatHistory(int roomId, int userIdA, int userIdB) {
        // Ensure userIdA < userIdB
        if (userIdA > userIdB) {
            int temp = userIdB;
            userIdB = userIdA;
            userIdA = temp;
        }

        // TODO: get chat history that stored in chat room
        return null;
    }
}
