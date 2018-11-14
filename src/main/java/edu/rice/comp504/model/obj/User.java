package edu.rice.comp504.model.obj;

import java.util.*;
import org.eclipse.jetty.websocket.api.Session;
import edu.rice.comp504.model.cmd.IUserCmd;

public class User implements Observer {

    private int id;
    private transient Session session;

    private String name;
    private int age;
    private String[] locations;
    private String[] schools;

    private transient List<ChatRoom> joined;
    private transient List<ChatRoom> available;

    /**
     * Constructor
     */
    public User(int id, Session session, String name, int age,
                String[] locations, String[] schools, ChatRoom[] rooms) {
        this.id = id;
        this.session = session;

        this.name = name;
        this.age = age;
        this.locations = locations;
        this.schools = schools;

        this.joined = new LinkedList<>();
        this.available = new LinkedList<>(Arrays.asList(rooms));
    }

    public int getId() {
        return this.id;
    }

    public Session getSession() {
        return this.session;
    }

    public String getName() {
        return this.name;
    }

    public int getAge() {
        return this.age;
    }

    public String[] getLocations() {
        return this.locations;
    }

    public String[] getSchools() {
        return this.schools;
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

    public boolean joinRoom(ChatRoom room) {
        if (this.available.contains(room) && room.applyFilter(this)) {
            room.addUser(this);
            this.joined.add(room);
            this.available.remove(room);
            return true;
        }
        else return false;
    }

    public boolean leaveRoom(ChatRoom room) {
        if (this.joined.contains(room)) {
            room.removeUser(this, "Volunteered to leave.");
            return true;
        }
        else return false;
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
    }
}
