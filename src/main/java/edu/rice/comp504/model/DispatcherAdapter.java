package edu.rice.comp504.model;

import edu.rice.comp504.controller.ChatAppController;
import edu.rice.comp504.model.cmd.AppendRoomCmd;
import edu.rice.comp504.model.cmd.IUserCmd;
import edu.rice.comp504.model.obj.ChatRoom;
import edu.rice.comp504.model.obj.User;
import org.eclipse.jetty.websocket.api.Session;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DispatcherAdapter extends Observable {

    private Map<String, ChatRoom> chatRoomFromNameMap = new ConcurrentHashMap<>();
    private Map<User, Session> sessionFromProfileMap = new ConcurrentHashMap<>();
    private Map<Session, User> profileFromSessionMap = new ConcurrentHashMap<>();
    private Map<Session,String> userFromSessionMap = new ConcurrentHashMap<>();
    private Map<String,Session> sessionFromUserMap = new ConcurrentHashMap<>();
    private int nextUserId = 1;


    public Map<Session, String> getUserNameMap() {
        return userFromSessionMap;
    }
    public Map<User, Session> getSessionFromProfileMap() {
        return sessionFromProfileMap;
    }
    public Map<Session, User> getProfileFromSessionMap() {
        return profileFromSessionMap;
    }
    public int getNextUserId() {
        return nextUserId;
    }
    public void setNextUserId(int nextUserId) {
        this.nextUserId = nextUserId;
    }
    public Map<String,ChatRoom> getchatRoomFromNameMap() {
        return chatRoomFromNameMap;
    }

    public int getNextRoomId() {
        return nextRoomId;
    }

    public Map<Integer, User> getUsers() {
        return users;
    }

    public Map<Integer, ChatRoom> getRooms() {
        return rooms;
    }

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

    public User loadUser(Session session, String body){
        //message: login jialei 26 USA Rice
        String[] tokens = body.split(" ");
        String username = tokens[0];
        int age = Integer.parseInt(tokens[1]);
        String location = tokens[2];
        String school = tokens[3];

        User user;
        if (this.chatRoomFromNameMap.values().isEmpty()){
            user = new User(this.nextUserId, username, age, location, school);
        }
        else {
            user = new User(this.nextUserId, username, age, location, school, /*(ChatRoom[])*/ this.chatRoomFromNameMap.values().toArray(new ChatRoom[0]));
        }
        this.nextUserId++;
        this.sessionFromProfileMap.put(user, session);
        this.profileFromSessionMap.put(session, user);
        this.sessionFromUserMap.put(username, session);
        this.userFromSessionMap.put(session, username);
        ChatAppController.notify(session, "Hi " + user.getName() + "! you logged successfully");

        this.addObserver(user);
//        System.out.println("observer count is " + this.countObservers());

        return user;
    }

//    public User loadUser(int age, String locations, String schools) {
//        User user = new User(this.nextUserId,
//                age, locations, schools, (ChatRoom[]) this.rooms.values().toArray());
//        this.users.put(this.nextRoomId, user);
//        this.nextUserId++;
//
//        this.addObserver(user);
//        return user;
//    }

    public ChatRoom loadRoom(Session session, String body) {
        //createroom freedom test
        //message createroom freedom-room[chatroom id] age 20[GTE] 80[LTE] location-USA location-Non-USA school-Rice school-NonRice
        String[] tokens = body.split(" ",2);
        String roomName = tokens[0];
//        String rule = tokens[1];
        User user = this.getProfileFromSessionMap().get(session);

        //hardcoded room rule
        ChatRoom room = user.createRoom(this.nextRoomId, roomName, 20,80, new String[] {"US"}, new String []{"Rice"}, this);
        this.chatRoomFromNameMap.put(roomName,room);

        ChatAppController.notify(session, "you created room " + roomName);

        //to notify all user that a new room has been created
        IUserCmd cmd = AppendRoomCmd.makeAppendCmd(room, this);
        this.setChanged();
        this.notifyObservers(cmd);

        return room;

    }

//    public ChatRoom loadRoom(int ownerId, int lower, int upper,
//                             String[] locations, String[] schools) {
//        ChatRoom room = new ChatRoom(this.nextRoomId,
//                this.users.get(ownerId), lower, upper, locations, schools);
//        this.rooms.put(this.nextRoomId, room);
//        this.nextRoomId++;
//
//        IUserCmd cmd = AppendRoomCmd.makeAppendCmd(room);
//        this.setChanged();
//        this.notifyObservers(cmd);
//
//        return room;
//    }

    public void unloadUser(int userId) {
        // TODO: remove user from the environment, and automatically leave all joined chat rooms
    }

    //joinroom freedom
    //message: freedom
    public void joinRoom(Session session, String body){
//        String[] tokens = body.split(" ",2);
        String roomName = body;
//        String rule = tokens[1];
        ChatRoom room = this.chatRoomFromNameMap.get(roomName);
        User user = this.profileFromSessionMap.get(session);
//        System.out.println(room.getName());
//        System.out.println(user.getName());
        user.joinRoom(room);
    }

//    public void joinRoom(int roomId, int userId) {
//        // TODO: make user join room
//    }

    public void leaveRoom(int roomId, int userId) {
        // TODO: make user leave room
    }

    //Let's consider one to one only, no send to all from owner yet
    public void chat(Session session, String body){
        //message chat freedom[chatroom id] #All/username Say hi[chat message]
        String[] tokens = body.split(" ",3);
        String roomName = tokens[0];
        ChatRoom room = this.chatRoomFromNameMap.get(roomName);
        String userName = tokens[1];
        User receiver = this.profileFromSessionMap.get(this.sessionFromUserMap.get(userName));
        String msg = tokens[2];
        User sender = this.profileFromSessionMap.get(session);

        sender.sendMsg(room, receiver, msg);

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

    public void notifyClient(User user, String message){
        Session session = this.sessionFromProfileMap.get(user);
        ChatAppController.notify(session, message);
    }
}
