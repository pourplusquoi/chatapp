package edu.rice.comp504.model.obj;

import java.util.*;

import org.eclipse.jetty.websocket.api.Session;

import edu.rice.comp504.model.res.AResponse;
import edu.rice.comp504.model.res.UserRoomsResponse;
import edu.rice.comp504.model.cmd.IUserCmd;

public class User implements Observer {

    private int id;
    private transient Session session;

    private String name;
    private int age;
    private String location;
    private String school;

    private List<Integer> joinedRoomIds;
    private List<Integer> availableRoomIds;

    /**
     * Constructor.
     */
    public User(int id, Session session, String name, int age,
                String location, String school, ChatRoom[] rooms) {
        this.id = id;
        this.session = session;

        this.name = name;
        this.age = age;
        this.location = location;
        this.school = school;

        this.joinedRoomIds = new LinkedList<>();
        this.availableRoomIds = new LinkedList<>();
        for (ChatRoom room : rooms) {
            this.availableRoomIds.add(room.getId());
        }
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

    public String getLocation() {
        return this.location;
    }

    public String getSchool() {
        return this.school;
    }

    public List<Integer> getJoinedRoomIds() {
        return this.joinedRoomIds;
    }

    public List<Integer> getAvailableRoomIds() {
        return this.availableRoomIds;
    }

    public void addRoom(ChatRoom room) {
        Integer roomId = room.getId();
        this.availableRoomIds.add(roomId);
        this.refresh(room);
    }

    public void removeRoom(ChatRoom room) {
        Integer roomId = room.getId();
        this.joinedRoomIds.remove(roomId);
        this.availableRoomIds.remove(roomId);
        this.refresh(room);
    }

    public void moveToJoined(ChatRoom room) {
        Integer roomId = room.getId();
        this.joinedRoomIds.add(roomId);
        this.availableRoomIds.remove(roomId);
        this.refresh(room);
    }

    public void moveToAvailable(ChatRoom room) {
        Integer roomId = room.getId();
        this.availableRoomIds.add(roomId);
        this.joinedRoomIds.remove(roomId);
        this.refresh(room);
    }

    /**
     * Make user join the room
     * @param room the room where some user join
     * @return whether or not successful
     */
    public boolean joinRoom(ChatRoom room) {
        Integer roomId = room.getId();
        if (this.availableRoomIds.contains(roomId) && room.applyFilter(this)) {
            this.moveToJoined(room);
            room.addUser(this);
            this.refresh(room);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Make user volunteer to leave the room
     * @param room the room where some user leave
     * @return whether or not successful
     */
    public boolean leaveRoom(ChatRoom room) {
        Integer roomId = room.getId();
        if (this.joinedRoomIds.contains(roomId)) {
            room.removeUser(this, "Volunteered to leave");
            this.refresh(room);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        ((IUserCmd) arg).execute(this);
    }

    private void refresh(ChatRoom room) {
        AResponse res = new UserRoomsResponse(this.id, this.joinedRoomIds, this.availableRoomIds);
        room.getDispatcher().notifyClient(this, res);
    }
}
