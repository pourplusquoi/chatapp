package edu.rice.comp504.model;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import edu.rice.comp504.model.cmd.DeleteRoomCmd;
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
     * Constructor.
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
     * Load a user into the environment.
     * @param session session
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
            if (tokens[i].charAt(0) == 'l') {
                locationList.add(str);
            } else {
                schoolList.add(str);
            }
        }
        String[] locations = locationList.toArray(new String[0]);
        String[] schools = schoolList.toArray(new String[0]);

        int userId = this.userIdFromSession.get(session);
        ChatRoom[] allRooms = this.rooms.values().toArray(new ChatRoom[0]);

        User user = new User(userId, session, name, age, locations, schools, allRooms);
        this.users.put(userId, user);

        // Put a message for login
        Map<String, String> info = new HashMap<>();
        info.put("type", "newUser");
        info.put("userId", Integer.toString(user.getId()));
        info.put("userName", user.getName());
        ChatAppController.notify(session, info);

        this.addObserver(user);
        return user;
    }

    /**
     * Load a room into the environment.
     * @param session session
     * @param body of format "ownerId + name + ageLower + ageUpper + (location:USA)* + (school:Rice)*"
     * @return return
     */
    public ChatRoom loadRoom(Session session, String body) {
        String [] tokens = body.split(" ");
        String name = tokens[0];
        int lower = Integer.parseInt(tokens[1]);
        int upper = Integer.parseInt(tokens[2]);

        List<String> locationList = new LinkedList<>();
        List<String> schoolList = new LinkedList<>();
        for (int i = 3; i < tokens.length; i++) {
            String str = tokens[i].substring(tokens[i].indexOf(':') + 1);
            if (tokens[i].charAt(0) == 'l') {
                locationList.add(str);
            } else {
                schoolList.add(str);
            }
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
        info.put("type", "newRoom");
        info.put("roomId", Integer.toString(room.getId()));
        info.put("roomName", room.getName());
        info.put("roomOwnerId", Integer.toString(room.getOwner().getId()));
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
     * Remove a user with given userId from the environment.
     * @param userId the id of the user to be removed
     */
    public void unloadUser(int userId) {
        User user = this.users.get(userId);
        this.userIdFromSession.remove(user.getSession());
        // TODO: remove user from the environment, and automatically leave all joined chat rooms
    }

    /**
     * Make a user join a chat room.
     * @param session session
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
     * Make a user leave a chat room.
     * @param session session
     * @param body of format "roomId"
     */
    public void leaveRoom(Session session, String body) {
        int roomId = Integer.parseInt(body);
        int userId = this.userIdFromSession.get(session);
        // TODO: make user leave room
    }

    /**
     * Make modification on chat room filer by the owner.
     * @param session session
     * @param body body
     */
    public void modifyRoomFilter(Session session, String body) {
        // TODO: parse body and modify the room filter
    }
    // public void modifyRoomFilter(Session session, int roomId, int ownerId,
    //                              int lower, int upper, String[] locations, String[] schools) {}

    /**
     * Recycle rooms with no users.
     */
    public void freeEmptyRooms() {
        List<ChatRoom> empty = new LinkedList<>();
        for (Integer roomId : this.rooms.keySet()) {
            ChatRoom room = this.rooms.get(roomId);
            if (room.countObservers() == 0) {
                empty.add(room);
            }
        }
        for (ChatRoom room : empty) {
            IUserCmd cmd = DeleteRoomCmd.makeDeteleCmd(room);
            this.setChanged();
            this.notifyObservers(cmd);
            this.rooms.remove(room.getId());
        }
    }

    /**
     * A sender sends a string message to a receiver.
     * @param session session
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
            for (ChatRoom joinedRoom : sender.getJoined()) {
                joinedRoom.removeUser(sender, "Forced to leave due to illegal speech.");
            }
        } else {
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
            info.put("type", "chatHistory");
            Message[] history = this.getChatHistory(roomId, senderId, receiverId);
            info.put("content", gson.toJson(history));
            this.notifyClient(receiver, info);
        }
    }

    /**
     * Acknowledge the message from the receiver.
     * @param session session
     * @param body of format "msgId"
     */
    public void ackMessage(Session session, String body) {
        int msgId = Integer.valueOf(body);
        Message message = this.messages.get(msgId);
        if (!message.isReceived()) {
            message.setReceived(true);

            Gson gson = new Gson();
            Map<String, String> info = new HashMap<>();
            info.put("type", "chatHistory");
            Message[] history = this.getChatHistory(message.getRoomId(),
                    message.getSenderId(), message.getReceiverId());
            info.put("content", gson.toJson(history));

            // Refresh whether or not received
            int senderId = message.getSenderId();
            this.notifyClient(this.users.get(senderId), info);
        }
    }

    /**
     * Notify the client for refreshing.
     * @param receiver user
     * @param info the information for notifying
     */
    public void notifyClient(User receiver, Map<String, String> info) {
        Session session = receiver.getSession();
        ChatAppController.notify(session, info);
    }

    /**
     * Send query result from controller to front end.
     * @param session session
     * @param body of format "type + roomId + [senderId] + [receiverId]"
     */
    public void query(Session session, String body) {
        String[] tokens = body.split(" ");
        String type = tokens[0];

        User receiver = this.users.get(this.userIdFromSession.get(session));
        Map<String, String> info = new HashMap<>();
        info.put("type", type);

        Gson gson = new Gson();
        int roomId;
        int senderId;
        int receiverId;
        switch (type) {
            case "users":
                roomId = Integer.parseInt(tokens[1]);
                info.put("roomId", Integer.toString(roomId));
                info.put("content", gson.toJson(this.getUsers(roomId)));
                break;
            case "notifications":
                roomId = Integer.parseInt(tokens[1]);
                info.put("roomId", Integer.toString(roomId));
                info.put("content", gson.toJson(this.getNotifications(roomId)));
                break;
            case "chatHistory":
                roomId = Integer.parseInt(tokens[1]);
                senderId = Integer.parseInt(tokens[2]);
                receiverId = Integer.parseInt(tokens[3]);
                info.put("content", gson.toJson(this.getChatHistory(roomId, senderId, receiverId)));
                break;
            default: break;
        }

        this.notifyClient(receiver, info);
    }

    /**
     * Get the names of all chat room members.
     * @param roomId the id of the room
     * @return names of all chat room members
     */
    private Map<Integer, String> getUsers(int roomId) {
        ChatRoom room = this.rooms.get(roomId);
        return room.getUsers();
    }

    /**
     * Get notifications in the chat room.
     * @param roomId the id of the room
     * @return notifications of the chat room
     */
    private String[] getNotifications(int roomId) {
        ChatRoom room = this.rooms.get(roomId);
        return room.getNotifications().toArray(new String[0]);
    }

    /**
     * Get chat history between user A and user B (commutative).
     * @param roomId the id of the chat room
     * @param userAId the id of user A
     * @param userBId the id of user B
     * @return chat history between user A and user B
     */
    private Message[] getChatHistory(int roomId, int userAId, int userBId) {
        // Ensure userIdA < userIdB
        if (userAId > userBId) {
            int temp = userBId;
            userBId = userAId;
            userAId = temp;
        }

        // TODO: get chat history that stored in chat room
        return null;
    }
}
