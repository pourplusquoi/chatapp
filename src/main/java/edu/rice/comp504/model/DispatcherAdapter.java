package edu.rice.comp504.model;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;

import edu.rice.comp504.model.cmd.AppendRoomCmd;
import edu.rice.comp504.model.cmd.IUserCmd;
import edu.rice.comp504.model.obj.ChatRoom;
import edu.rice.comp504.model.obj.Message;
import edu.rice.comp504.model.obj.User;
import edu.rice.comp504.controller.ChatAppController;

public class DispatcherAdapter extends Observable {

    private int nextUserId;
    private int nextRoomId;
    private int nextMessageId;

    // Maps user id to the user
    private Map<Integer, User> users;

    // Maps room id to the chat room
    private Map<Integer, ChatRoom> rooms;

    // Maps message id to the message
    private Map<Integer, Message> messages;

    // Maps session to user id
    private Map<Session, Integer> userIdFromSession;

    /**
     * Constructor
     */
    public DispatcherAdapter() {
        this.nextRoomId = 0;
        this.nextUserId = 0;
        this.nextMessageId = 0;
        this.users = new ConcurrentHashMap<>();
        this.rooms = new ConcurrentHashMap<>();
        this.messages = new ConcurrentHashMap<>();
        this.userIdFromSession = new ConcurrentHashMap<>();
    }

    public void newSession(Session session) {
        this.userIdFromSession.put(session, this.nextUserId);
        this.nextUserId++;
    }

    public int getUserIdFromSession(Session session) {
        return this.userIdFromSession.get(session);
    }

    /**
     * Load a user into the environment
     * @param session
     * @param body of format "name + age + (location:USA)* + (school:Rice)*"
     * @return the user loaded
     */
    public User loadUser(Session session, String body) {
        String[] tokens = body.split(" ");
        String name = tokens[0];
        int age = Integer.parseInt(tokens[1]);

        List<String> locationList = new LinkedList<>();
        List<String> schoolList = new LinkedList<>();
        for (int i = 2; i < tokens.length; i++) {
            String str = tokens[i].substring(tokens[i].indexOf(':') + 1);
            if (tokens[i].charAt(0) == 'l')
                locationList.add(str);
            else schoolList.add(str);
        }
        String[] locations = locationList.toArray(new String[0]);
        String[] schools = schoolList.toArray(new String[0]);

        int userId = this.userIdFromSession.get(session);
        ChatRoom[] allRooms = this.rooms.values().toArray(new ChatRoom[0]);

        User user = new User(userId, session, name, age, locations, schools, allRooms);
        this.users.put(userId, user);

        // Put a message for login
        Map<String, String> info = new HashMap<>();
        info.put("action", "login");
        info.put("content", "Hi " + user.getName() + "! You successfully logged in.");
        ChatAppController.notify(session, info);

        this.addObserver(user);
        return user;
    }

    /**
     * Load a room into the environment
     * @param session
     * @param body of format "ownerId + name + ageLower + ageUpper + (location:USA)* + (school:Rice)*"
     * @return
     */
    public ChatRoom loadRoom(Session session, String body) {
         String[] tokens = body.split(" ");
         String name = tokens[0];
         int lower = Integer.parseInt(tokens[1]);
         int upper = Integer.parseInt(tokens[2]);

         List<String> locationList = new LinkedList<>();
         List<String> schoolList = new LinkedList<>();
         for (int i = 3; i < tokens.length; i++) {
             String str = tokens[i].substring(tokens[i].indexOf(':') + 1);
             if (tokens[i].charAt(0) == 'l')
                 locationList.add(str);
             else schoolList.add(str);
         }
         String[] locations = locationList.toArray(new String[0]);
         String[] schools = schoolList.toArray(new String[0]);

        int ownerId = this.userIdFromSession.get(session);
        User owner = this.users.get(ownerId);

        // First create the room
         ChatRoom room = new ChatRoom(this.nextRoomId, name,
                owner, lower, upper, locations, schools, this);
        this.rooms.put(this.nextRoomId, room);
        this.nextRoomId++;

        // Put a message for create room
        Map<String, String> info = new HashMap<>();
        info.put("action", "create");
        info.put("content", "You created room " + room.getName() + ".");
        ChatAppController.notify(session, info);

        // Add the room to users' available list
        IUserCmd cmd = AppendRoomCmd.makeAppendCmd(room);
        this.setChanged();
        this.notifyObservers(cmd);

        // Make owner join the room
        owner.joinRoom(room);
        return room;
    }

    /**
     * Remove a user with given userId from the environment
     * @param userId the id of the user to be removed
     */
    public void unloadUser(int userId) {
        User user = this.users.get(userId);
        this.userIdFromSession.remove(user.getSession());
        // TODO: remove user from the environment, and automatically leave all joined chat rooms
    }

    /**
     * Make a user join a chat room
     * @param session
     * @param body of format "roomId"
     */
    public void joinRoom(Session session, String body) {
        int roomId = Integer.parseInt(body);
        int userId = this.userIdFromSession.get(session);

        User user = this.users.get(userId);
        ChatRoom room = this.rooms.get(roomId);
        user.joinRoom(room);
    }

    /**
     * Make a user leave a chat room
     * @param session
     * @param body of format "roomId"
     */
    public void leaveRoom(Session session, String body) {
        int roomId = Integer.parseInt(body);
        int userId = this.userIdFromSession.get(session);
        // TODO: make user leave room
    }

    public void modifyRoomFilter(Session session, int roomId, int ownerId,
                                 int lower, int upper, String[] locations, String[] schools) {
        // TODO: modify the room filter
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
     * @param session
     * @param body of format "roomId + receiverId + rawMessage"
     */
    public void sendMessage(Session session, String body) {

        String[] tokens = body.split(" ", 3);

        int roomId = Integer.valueOf(tokens[0]);
        int senderId = this.userIdFromSession.get(session);
        int receiverId = Integer.valueOf(tokens[1]);
        String raw = tokens[2]; // The raw message

        User sender = this.users.get(senderId);

        // When user sends "hate", kick him/her out of all rooms
        if (raw.contains("hate")) {
            for (ChatRoom joinedRoom : sender.getJoined())
                joinedRoom.removeUser(sender, "Forced to leave due to illegal speech.");
        }

        else {
            Message message = new Message(this.nextMessageId, roomId, senderId, receiverId, raw);
            this.messages.put(this.nextMessageId, message);
            this.nextMessageId++;

            User receiver = this.users.get(receiverId);
            ChatRoom room = this.rooms.get(roomId);

            // Store the message to history
            room.storeMessage(sender, receiver, message);

            // Notify the client
            Gson gson = new Gson();
            Map<String, String> info = new ConcurrentHashMap<>();
            info.put("action", "receive message");
            info.put("content", gson.toJson(message));
            this.notifyClient(receiver, info);
        }
    }

    /**
     * Acknowledge the message from the receiver
     * @param session
     * @param body of format "msgId"
     */
    public void ackMessage(Session session, String body) {
        int msgId = Integer.valueOf(body);
        Message message = this.messages.get(msgId);
        if (!message.isReceived()) {
            message.setReceived(true);

            Gson gson = new Gson();
            Map<String, String> info = new HashMap<>();
            info.put("action", "ack");
            info.put("content", gson.toJson(message));

            // Refresh whether or not received
            int senderId = message.getSenderId();
            this.notifyClient(this.users.get(senderId), info);
        }
    }

    /**
     * Notify the client for refreshing
     * @param receiver
     * @param info the information for notifying
     */
    public void notifyClient(User receiver, Map<String, String> info) {
        Session session = receiver.getSession();
        ChatAppController.notify(session, info);
    }

    /**
     * Get the name of the chat room owner
     * @param session
     * @param body of format "roomId"
     * @return name of the chat room owner
     */
    public String getOwnerName(Session session, String body) {
        int roomId = Integer.valueOf(body);
        ChatRoom room = this.rooms.get(roomId);
        User user = room.getOwner();
        return user.getName() + "(" + user.getId() + ")";
    }

    /**
     * Get the names of all chat room members
     * @param session
     * @param body of format "roomId"
     * @return names of all chat room members
     */
    public String[] getUserNameList(Session session, String body) {
        int roomId = Integer.valueOf(body);
        ChatRoom room = this.rooms.get(roomId);
        List<String> nameList = room.getUserNames();
        return nameList.toArray(new String[0]);
    }

    /**
     * Get notifications in the chat room
     * @param session
     * @param body of format "roomId"
     * @return notifications of the chat room
     */
    public String[] getNotifications(Session session, String body) {
        int roomId = Integer.valueOf(body);
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
    public Message[] getChatHistory(Session session, int roomId, int userIdA, int userIdB) {
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
