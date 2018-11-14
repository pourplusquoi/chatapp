package edu.rice.comp504.model.obj;

import java.util.*;

import com.google.gson.Gson;
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
        this.refresh(room);
    }

    public void removeAvailable(ChatRoom room) {
        this.available.remove(room);
        this.refresh(room);
    }

    public boolean joinRoom(ChatRoom room) {
        if (this.available.contains(room) && room.applyFilter(this)) {
            room.addUser(this);
            this.joined.add(room);
            this.available.remove(room);
            this.refresh(room);
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
        this.refresh(room);
    }

    @Override
    public void update(Observable o, Object arg) {
        ((IUserCmd)arg).execute(this);
    }

    private void refresh(ChatRoom room) {
        Gson gson = new Gson();
        Map<String, String> info = new HashMap<>();
        info.put("type", "roomList");
        info.put("userId", Integer.toString(this.id));

        int[] joinedIds = new int[this.joined.size()];
        for (int i = 0; i < this.joined.size(); i++)
            joinedIds[i] = this.joined.get(i).getId();
        info.put("joinedIds", gson.toJson(joinedIds));

        int[] availableIds = new int[this.available.size()];
        for (int i = 0; i < this.available.size(); i++)
            availableIds[i] = this.available.get(i).getId();
        info.put("availableIds", gson.toJson(availableIds));

        room.getDispatcher().notifyClient(this, info);
    }
}
