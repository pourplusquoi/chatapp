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
     * @param body of format "name + age + location + school"
     * @return the user loaded
     */
    public User loadUser(Session session, String body) {
        String[] tokens = body.split(" ");
        String name = tokens[0];
        int age = Integer.parseInt(tokens[1]);
        String location = tokens[2];
        String school = tokens[3];

        int userId = this.userIdFromSession.get(session);

        // Refresh for a new login user
        Map<String, String> info = new HashMap<>();
        info.put("type", "newUser");
        info.put("userId", Integer.toString(userId));
        info.put("userName", name);
        ChatAppController.notify(session, info);

        ChatRoom[] allRooms = this.rooms.values().toArray(new ChatRoom[0]);
        User user = new User(userId, session, name, age, location, school, allRooms);
        this.users.put(userId, user);

        this.addObserver(user);
        return user;
    }

    /**
     * Load a room into the environment.
     * @param session session
     * @param body of format "name + ageLower + ageUpper + {[location],}*{[location]} + {[school],}*{[school]}"
     * @return return
     */
    public ChatRoom loadRoom(Session session, String body) {
        String [] tokens = body.split(" ");
        String name = tokens[0];
        int lower = Integer.parseInt(tokens[1]);
        int upper = Integer.parseInt(tokens[2]);

        String[] locations = tokens[3].split(",");
        String[] schools = tokens[4].split(",");

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
        info.put("ownerId", Integer.toString(room.getOwner().getId()));
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
     * Remove a room with given roomId from the environment.
     * @param roomId the id of the room to be removed
     */
    public void unloadRoom(int roomId) {
        ChatRoom room = this.rooms.get(roomId);
        IUserCmd cmd = DeleteRoomCmd.makeDeteleCmd(room);
        this.setChanged();
        this.notifyObservers(cmd);
        this.rooms.remove(room.getId());
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
     * @param body of format "roomId + lower + upper + {[location],}*{[location]} + {[school],}*{[school]}"
     */
    public void modifyRoom(Session session, String body) {
        String [] tokens = body.split(" ");
        int roomId = Integer.parseInt(tokens[0]);
        int lower = Integer.parseInt(tokens[1]);
        int upper = Integer.parseInt(tokens[2]);

        String[] locations = tokens[3].split(",");
        String[] schools = tokens[4].split(",");

        ChatRoom room = this.rooms.get(roomId);
        room.modifyFilter(lower, upper, locations, schools);
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
            info.put("type", "userChatHistory");
            List<Message> history = this.getChatHistory(roomId, senderId, receiverId);
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
            info.put("type", "userChatHistory");
            List<Message> history = this.getChatHistory(message.getRoomId(),
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
        int userAId;
        int userBId;
        switch (type) {
            case "roomUsers":
                roomId = Integer.parseInt(tokens[1]);
                info.put("roomId", Integer.toString(roomId));
                info.put("content", gson.toJson(this.getUsers(roomId)));
                break;
            case "roomNotifications":
                roomId = Integer.parseInt(tokens[1]);
                info.put("roomId", Integer.toString(roomId));
                info.put("content", gson.toJson(this.getNotifications(roomId)));
                break;
            case "userChatHistory":
                roomId = Integer.parseInt(tokens[1]);
                userAId = this.userIdFromSession.get(session);
                userBId = Integer.parseInt(tokens[2]);
                info.put("content", gson.toJson(this.getChatHistory(roomId, userAId, userBId)));
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
    private List<String> getNotifications(int roomId) {
        ChatRoom room = this.rooms.get(roomId);
        return room.getNotifications();
    }

    /**
     * Get chat history between user A and user B (commutative).
     * @param roomId the id of the chat room
     * @param userAId the id of user A
     * @param userBId the id of user B
     * @return chat history between user A and user B
     */
    private List<Message> getChatHistory(int roomId, int userAId, int userBId) {
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
