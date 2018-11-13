package edu.rice.comp504.model.obj;

import edu.rice.comp504.model.DispatcherAdapter;
import edu.rice.comp504.model.cmd.IUserCmd;

import java.util.*;

public class User implements Observer {

    private int id;

    private String name;

    private int age;
    private String location;
    private String school;

    private List<ChatRoom> joined;
    private List<ChatRoom> available;

    /**
     * Constructor
     */
    public User(int id, String name, int age, String location, String school, ChatRoom[] rooms) {
        this.id = id;
        this.name = name;

        this.age = age;
        this.location = location;
        this.school = school;

        this.joined = new LinkedList<>();
        this.available = new LinkedList<>(Arrays.asList(rooms));
    }

    public User(int id, String name, int age, String location, String school) {
        this.id = id;
        this.name = name;

        this.age = age;
        this.location = location;
        this.school = school;

        this.joined = new LinkedList<>();
        this.available = new LinkedList<>();
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public int getAge() {
        return this.age;
    }

    public String getLocation() {
        return this.location;
    }

    public String getSchool() {
        return this.school;
    }

    public List<ChatRoom> getJoined() {
        return this.joined;
    }

    public List<ChatRoom> getAvailable() {
        return this.available;
    }

    public void addAvailable(ChatRoom room) {
        this.available.add(room);
    }

    public ChatRoom createRoom(int roomId, String name, int lower, int upper, String[] locations, String[] schools, DispatcherAdapter dis) {
        ChatRoom room = new ChatRoom(roomId, name,this, lower, upper, locations, schools, dis);
        this.joined.add(room);
        return room;
    }

    public boolean joinRoom(ChatRoom room) {
//        System.out.println("available rooms " + this.available.toArray().toString());
//        System.out.println("before");
//        for (int i=0; i < available.size(); i++){
//            System.out.println(available.get(i).getName());
//        }
//        System.out.println("after");
//        System.out.println("apply room rule " + room.applyFilter(this));
        if (this.available.contains(room) && room.applyFilter(this)) {
//            System.out.println("Inside room rule");
            room.addUser(this);
            this.joined.add(room);
            this.available.remove(room);
            return true;
        }
        else return false;
    }

    public boolean leaveRoom(ChatRoom room) {
        if (this.joined.contains(room)) {
            room.removeUser(this);
            return true;
        }
        else return false;
    }

    public void sendMsg(ChatRoom room, User target, String msg){
        room.sendMsg(target, msg + " sent by " + this.getName() + " in room " + room.getName());
    }

    /**
     * Modify available chat room list, to be called by evict command
     * @param room the room where some user leave
     * @param victim the user that leaves
     */
    public void unjoinRoom(ChatRoom room, User victim) {
        this.joined.remove(room);
        if (room.getOwner() != victim)
            this.available.add(room);
    }

    @Override
    public void update(Observable o, Object arg) {
        ((IUserCmd)arg).execute(this);
        // TODO
    }
}
