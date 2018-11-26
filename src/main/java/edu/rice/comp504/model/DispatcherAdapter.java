package edu.rice.comp504.model;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.eclipse.jetty.websocket.api.Session;

import edu.rice.comp504.model.obj.ChatRoom;
import edu.rice.comp504.model.obj.Message;
import edu.rice.comp504.model.obj.User;
import edu.rice.comp504.model.cmd.*;
import edu.rice.comp504.model.res.*;

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
     * Constructor, initializing all private fields.
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

    /**
     * Allocate a user id for a new session.
     * @param session the new session
     */
    public void newSession(Session session) {
        this.userIdFromSession.put(session, this.nextUserId);
        this.nextUserId++;
    }

    /**
     * Get the user if from a session.
     * @param session the session
     * @return the user id binding with session
     */
    public int getUserIdFromSession(Session session) {
        return this.userIdFromSession.get(session);
    }

    /**
     * Determine whether the session exists.
     * @param session the session
     * @return whether the session is still connected or not
     */
    public boolean containsSession(Session session) {
        return this.userIdFromSession.containsKey(session);
    }

    //private helper function to get string from JSON
    private static String getFromJSON(JsonObject jsonObject, String key){
        String ret = jsonObject.get(key).toString();
        return ret.substring(1, ret.length() - 1);
    }

    /**
     * Load a user into the environment.
     * @param session the session that requests to called the method
     * @param body of format "name age location school"
     * @return the new user that has been loaded
     */
    public User loadUser(Session session, String body) {
//        String[] tokens = body.split(" ");
//        String name = tokens[0];
//        int age = Integer.parseInt(tokens[1]);
//        String region = tokens[2];
//        String school = tokens[3];
//        System.out.println(name+age+region+school);

        JsonParser parser = new JsonParser();
        JsonObject o = parser.parse(body).getAsJsonObject();
        String name = getFromJSON(o, "username");
        int age  = Integer.parseInt(getFromJSON(o,"age"));
        String region = getFromJSON(o, "region");
        String school = getFromJSON(o, "school");

        System.out.println(name+age+region+school);

        int userId = this.userIdFromSession.get(session);

        ChatRoom[] allRooms = this.rooms.values().toArray(new ChatRoom[0]);
        User user = new User(userId, session, name, age, region, school, allRooms);
        this.users.put(userId, user);

        // Put a message for creating new user
        AResponse res = new NewUserResponse(userId, name, age, region, school);
        notifyClient(session, res);

        // Put a message for chat rooms that new user have
        List<Integer> joinedRoomIds = user.getJoinedRoomIds();
        List<Integer> availableRoomIds = user.getAvailableRoomIds();
        res = new UserRoomsResponse(userId, joinedRoomIds, availableRoomIds);
        notifyClient(session, res);

        this.addObserver(user);
        return user;
    }

    /**
     * Load a room into the environment.
     * @param session the session that requests to called the method
     * @param body of format "name ageLower ageUpper {[location],}*{[location]} {[school],}*{[school]}"
     * @return the new room that has been loaded
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

        if (room.applyFilter(owner)) {
            this.rooms.put(this.nextRoomId, room);
            this.nextRoomId++;

            // Put a message for creating new room
            AResponse res = new NewRoomResponse(room.getId(), ownerId, name);
            notifyClient(session, res);

            // Add the room to users' available list
            IUserCmd cmd = new AddRoomCmd(room);
            this.setChanged();
            this.notifyObservers(cmd);

            // Make owner join the room
            room.addUser(owner);
            return room;

        } else { // When the room owner is not qualified
            return null;
        }
    }

    /**
     * Remove a user with given userId from the environment.
     * @param userId the id of the user to be removed
     */
    public void unloadUser(int userId) {
        User user = this.users.get(userId);
        this.users.remove(userId);
        this.userIdFromSession.remove(user.getSession());

        for (Integer roomId : user.getJoinedRoomIds()) {
            ChatRoom room = this.rooms.get(roomId);
            room.removeUser(user, "Disconnected");
        }
    }

    /**
     * Remove a room with given roomId from the environment.
     * @param roomId the id of the chat room to be removed
     */
    public void unloadRoom(int roomId) {
        ChatRoom room = this.rooms.get(roomId);
        this.rooms.remove(roomId);

        IUserCmd cmd = new RemoveRoomCmd(room);
        this.setChanged();
        this.notifyObservers(cmd);
    }

    /**
     * Make a user join a chat room.
     * @param session the session that requests to called the method
     * @param body of format "roomId"
     */
    public void joinRoom(Session session, String body) {
        int roomId = Integer.parseInt(body);
        int userId = this.userIdFromSession.get(session);

        User user = this.users.get(userId);
        ChatRoom room = this.rooms.get(roomId);

        room.addUser(user);
    }

    /**
     * Make a user volunteer to leave a chat room.
     * @param session the session that requests to called the method
     * @param body of format "roomId"
     */
    public void leaveRoom(Session session, String body) {
        int roomId = Integer.parseInt(body);
        int userId = this.userIdFromSession.get(session);

        User user = this.users.get(userId);
        ChatRoom room = this.rooms.get(roomId);

        room.removeUser(user, "Volunteered to leave.");
    }

    /**
     * Make modification on chat room filer by the owner.
     * @param session the session of the chat room owner
     * @param body of format "roomId lower upper {[location],}*{[location]} {[school],}*{[school]}"
     */
    public void modifyRoom(Session session, String body) {
        String [] tokens = body.split(" ");
        int userId = this.userIdFromSession.get(session);
        int roomId = Integer.parseInt(tokens[0]);
        int lower = Integer.parseInt(tokens[1]);
        int upper = Integer.parseInt(tokens[2]);

        String[] locations = tokens[3].split(",");
        String[] schools = tokens[4].split(",");

        ChatRoom room = this.rooms.get(roomId);
        User user = this.users.get(userId);

        // Modify room filter only when the user is the chat room owner
        if (user == room.getOwner()) {
            room.modifyFilter(lower, upper, locations, schools);
        }
    }

    /**
     * A sender sends a string message to a receiver.
     * @param session the session of the message sender
     * @param body of format "roomId receiverId rawMessage"
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
            String reason = "Forced to leave due to illegal speech.";
            for (int joinedRoomId : sender.getJoinedRoomIds()) {
                ChatRoom joinedRoom = this.rooms.get(joinedRoomId);
                joinedRoom.removeUser(sender, reason);
            }
        } else {
            Message message = new Message(this.nextMessageId, roomId, senderId, receiverId, raw);
            this.messages.put(this.nextMessageId, message);
            this.nextMessageId++;

            User receiver = this.users.get(receiverId);
            ChatRoom room = this.rooms.get(roomId);

            // Store the message to history
            room.storeMessage(sender, receiver, message);

            List<Message> history = this.getChatHistory(roomId, senderId, receiverId);

            // Notify the receiver of the message
            AResponse res = new UserChatHistoryResponse(history);
            notifyClient(receiver.getSession(), res);
        }
    }

    /**
     * Acknowledge the message from the receiver.
     * @param session the session of the message receiver
     * @param body of format "msgId"
     */
    public void ackMessage(Session session, String body) {
        int msgId = Integer.valueOf(body);
        Message message = this.messages.get(msgId);
        if (!message.getIsReceived()) {
            message.setIsReceived(true);

            int senderId = message.getSenderId();
            User sender = this.users.get(senderId);

            List<Message> history = this.getChatHistory(message.getRoomId(),
                    message.getSenderId(), message.getReceiverId());

            // Notify the sender whether or not received
            AResponse res = new UserChatHistoryResponse(history);
            notifyClient(sender.getSession(), res);
        }
    }

    /**
     * Send query result from controller to front end.
     * @param session the session that requests to called the method
     * @param body of format "type roomId [senderId] [receiverId]"
     */
    public void query(Session session, String body) {
        String[] tokens = body.split(" ");
        String type = tokens[0];

        int roomId;
        int userAId;
        int userBId;
        AResponse res;

        switch (type) {
            case "RoomUsers":
                roomId = Integer.parseInt(tokens[1]);
                Map<Integer, String> users = this.getUsers(roomId);
                res = new RoomUsersResponse(roomId, users);
                break;

            case "RoomNotifications":
                roomId = Integer.parseInt(tokens[1]);
                List<String> notifications = this.getNotifications(roomId);
                res = new RoomNotificationsResponse(roomId, notifications);
                break;

            case "UserChatHistory":
                roomId = Integer.parseInt(tokens[1]);
                userAId = this.userIdFromSession.get(session);
                userBId = Integer.parseInt(tokens[2]);
                List<Message> chatHistory = this.getChatHistory(roomId, userAId, userBId);
                res = new UserChatHistoryResponse(chatHistory);
                break;

            default:
                res = new NullResponse();
                break;
        }

        notifyClient(session, res);
    }

    /**
     * Notify the client for refreshing.
     * @param user user expected to receive the notification
     * @param response the information for notifying
     */
    public static void notifyClient(User user, AResponse response) {
        Session session = user.getSession();
        notifyClient(session, response);
    }

    /**
     * Notify session about the message.
     * @param session the session to notify
     * @param response the notification information
     */
    private static void notifyClient(Session session, AResponse response) {
        try {
//            System.out.println("I am here");
            session.getRemote().sendString(response.toJson());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the names of all chat room members.
     * @param roomId the id of the chat room
     * @return all chat room members, mapping from user id to user name
     */
    private Map<Integer, String> getUsers(int roomId) {
        ChatRoom room = this.rooms.get(roomId);
        return room.getUsers();
    }

    /**
     * Get notifications in the chat room.
     * @param roomId the id of the chat room
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
     * @return chat history between user A and user B at a chat room
     */
    private List<Message> getChatHistory(int roomId, int userAId, int userBId) {
        // Ensure userIdA < userIdB
        if (userAId > userBId) {
            int temp = userBId;
            userBId = userAId;
            userAId = temp;
        }

        ChatRoom room = this.rooms.get(roomId);
        String key = Integer.toString(userAId) + "&" + Integer.toString(userBId);
        return room.getChatHistory().get(key);
    }
}
