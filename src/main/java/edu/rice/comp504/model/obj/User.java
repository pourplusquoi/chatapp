package edu.rice.comp504.model.obj;

import java.util.*;

import com.google.gson.Gson;
import edu.rice.comp504.model.DispatcherAdapter;
import org.eclipse.jetty.websocket.api.Session;

import edu.rice.comp504.model.cmd.IUserCmd;

/**
 * The User class defines a user object and private fields of a user.
 */
public class User implements Observer {

    //user id
    private int id;

    //user session
    private transient Session session;

    //user name
    private String name;

    //user age
    private int age;

    //user location
    private String location;

    //user school
    private String school;

    //joined rooms id list
    private List<Integer> joinedRoomIds;

    //available rooms id list
    private List<Integer> availableRoomIds;

    //the dispatcher
    private DispatcherAdapter dis;

    /**
     * Constructor.
     * @param id the user id
     * @param session the user session
     * @param name the user name when register
     * @param age the user age when register
     * @param location the user location when register
     * @param school the user school when register
     * @param rooms the ChatRoom array contains all rooms
     */
    public User(int id, Session session, String name, int age,
                String location, String school, ChatRoom[] rooms, DispatcherAdapter dis) {
        this.id = id;
        this.session = session;

        this.name = name;
        this.age = age;
        this.location = location;
        this.school = school;

        this.joinedRoomIds = new LinkedList<>();
        this.availableRoomIds = new LinkedList<>();

        this.dis = dis;

        // load all room id into availableRoomIds list
        for (ChatRoom room : rooms) {
            this.availableRoomIds.add(room.getId());
        }
    }

    /**
     * Get current user id.
     * @return the user id
     */
    public int getId() {
        return this.id;
    }

    /**
     * Get current user session.
     * @return the user session
     */
    public Session getSession() {
        return this.session;
    }

    /**
     * Get current user name.
     * @return the user name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get current user age.
     * @return the user age
     */
    public int getAge() {
        return this.age;
    }

    /**
     * Get the user location.
     * @return the user register location in String
     */
    public String getLocation() {
        return this.location;
    }

    /**
     * Get the user school.
     * @return the user register school in String
     */
    public String getSchool() {
        return this.school;
    }

    /**
     * Get a list of user joined chat rooms.
     * @return joined rooms ids
     */
    public List<Integer> getJoinedRoomIds() {
        return this.joinedRoomIds;
    }


    //private helper function to get id room name map
    private HashMap<Integer, RoomRestrictionInfo> getRoomMap(List<Integer> roomIds) {
        HashMap<Integer,RoomRestrictionInfo> retRooms = new HashMap<>();
        for (Integer roomId : roomIds) {
            ChatRoom room = dis.getRooms().get(roomId);
            RoomRestrictionInfo roomInfo = new RoomRestrictionInfo(room.getName(), room.getAgeLowerBound(), room.getAgeUpperBound(), room.getLocations(), room.getSchools());
            retRooms.put(roomId,roomInfo);//room.getName());
        }
        return retRooms;
    }

    //get joinedrooms map
    public HashMap<Integer,RoomRestrictionInfo> getJoinedRooms() {
        return getRoomMap(this.joinedRoomIds);//new HashMap<>();
    }

    //get availablerooms map
    public HashMap<Integer,RoomRestrictionInfo> getAvailableRooms() {
        return getRoomMap(this.availableRoomIds);//new HashMap<>();
    }

    /**
     * Get a list of user available chat rooms.
     * @return available chat rooms ids
     */
    public List<Integer> getAvailableRoomIds() {
        return this.availableRoomIds;
    }

    /**
     * Get a chat room id then store into available chat room list.
     * @param room the chat room object
     */
    public void addRoom(ChatRoom room) {
        Integer roomId = room.getId();
        this.availableRoomIds.add(roomId);
    }

    /**
     * Get a chat room id then remove it from both user joined rooms list and available rooms list.
     * @param room the chat room object
     */
    public void removeRoom(ChatRoom room) {
        Integer roomId = room.getId();
        this.joinedRoomIds.remove(roomId);
        this.availableRoomIds.remove(roomId);
    }

    /**
     * Move a chat room from available room list to joined room list.
     * @param room the chat room object
     */
    public void moveToJoined(ChatRoom room) {
        Integer roomId = room.getId();
        this.joinedRoomIds.add(roomId);
        this.availableRoomIds.remove(roomId);
    }

    /**
     * Move a chat room from joined room list to available room list.
     * @param room the chat room object
     */
    public void moveToAvailable(ChatRoom room) {
        Integer roomId = room.getId();
        this.availableRoomIds.add(roomId);
        this.joinedRoomIds.remove(roomId);
    }

    /**
     * User update when observable has changed.
     */
    @Override
    public void update(Observable o, Object arg) {
        ((IUserCmd) arg).execute(this);
    }
}
