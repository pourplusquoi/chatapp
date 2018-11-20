package edu.rice.comp504.model.res;

import java.util.List;

/**
 * Message covers the information when some notifications need to be broadcasted in the chatroom
 */
public class RoomNotificationsResponse extends AResponse {

    private int roomId;     // The id of the chatroom
    private List<String> notifications; // The notifications
    
    /**
     * Constructor.
     */
    public RoomNotificationsResponse(int roomId, List<String> notifications) {
        super("RoomNotifications");
        this.roomId = roomId;
        this.notifications = notifications;
    }
}
