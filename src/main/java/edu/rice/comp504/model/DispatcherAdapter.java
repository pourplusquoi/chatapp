package edu.rice.comp504.model;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jetty.websocket.api.Session;

import edu.rice.comp504.model.obj.ChatRoom;
import edu.rice.comp504.model.obj.Message;
import edu.rice.comp504.model.obj.User;
import edu.rice.comp504.model.cmd.*;
import edu.rice.comp504.model.res.*;
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
        AResponse res = new NewUserResponse(userId, name);
        ChatAppController.notify(session, res);

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
        AResponse res = new NewRoomResponse(room.getId(), ownerId, name);
        ChatAppController.notify(session, res);

        // Add the room to users' available list
        IUserCmd cmd = AddRoomCmd.makeAddRoomCmd(room);
        this.setChanged();
        this.notifyObservers(cmd);

        // Make owner join the room
        room.addUser(owner);
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
        IUserCmd cmd = RemoveRoomCmd.makeRemoveRoomCmd(room);
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

        room.addUser(user);
    }

    /**
     * Make a user leave a chat room.
     * @param session session
     * @param body of format "roomId reason"
     */
    public void leaveRoom(Session session, String body) {
        String [] tokens = body.split(" ", 2);
        int roomId = Integer.parseInt(tokens[0]);
        int userId = this.userIdFromSession.get(session);

        User user = this.users.get(userId);
        ChatRoom room = this.rooms.get(roomId);

        String reason = tokens[1];
        room.removeUser(user, reason);
    }

    /**
     * Make modification on chat room filer by the owner.
     * @param session session
     * @param body of format "roomId lower upper {[location],}*{[location]} {[school],}*{[school]}"
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
            ChatAppController.notify(receiver.getSession(), res);
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
        if (!message.getIsReceived()) {
            message.setIsReceived(true);

            int senderId = message.getSenderId();
            User sender = this.users.get(senderId);

            List<Message> history = this.getChatHistory(message.getRoomId(),
                    message.getSenderId(), message.getReceiverId());

            // Notify the sender whether or not received
            AResponse res = new UserChatHistoryResponse(history);
            ChatAppController.notify(sender.getSession(), res);
        }
    }

    /**
     * Send query result from controller to front end.
     * @param session session
     * @param body of format "type + roomId + [senderId] + [receiverId]"
     */
    public void query(Session session, String body) {
        String[] tokens = body.split(" ");
        String type = tokens[0];

        AResponse res;

        int roomId;
        int userAId;
        int userBId;
        switch (type) {
            case "roomUsers":
                roomId = Integer.parseInt(tokens[1]);
                Map<Integer, String> users = this.getUsers(roomId);
                res = new RoomUsersResponse(roomId, users);
                break;
            case "roomNotifications":
                roomId = Integer.parseInt(tokens[1]);
                List<String> notifications = this.getNotifications(roomId);
                res = new RoomNotificationsResponse(roomId, notifications);
                break;
            case "userChatHistory":
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

        ChatAppController.notify(session, res);
    }

    /**
     * Notify the client for refreshing.
     * @param user user expected to receive the notification
     * @param response the information for notifying
     */
    public void notifyClient(User user, AResponse response) {
        Session session = user.getSession();
        ChatAppController.notify(session, response);
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
