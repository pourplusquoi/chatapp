package dispatcher;

import cmd.AppendCmd;
import cmd.IUserCmd;
import obj.ChatRoom;
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
                age, locations, schools, (ChatRoom[]) this.rooms.values().toArray());
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

    public List<String> getChatHistory(int roomId, int userIdA, int userIdB) {
        // Ensure userIdA < userIdB
        if (userIdA > userIdB) {
            int temp = userIdB;
            userIdB = userIdA;
            userIdA = temp;
        }

        // TODO: get chat history that stored in chat room
        return new LinkedList<>();
    }
}
