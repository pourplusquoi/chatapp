package edu.rice.comp504.model.res;

import java.util.List;

/**
 * Message covers the information when some notifications need to be broadcast in the chat room.
 */
public class RoomNotificationsResponse extends AResponse {

    private int roomId;
    private List<String> notifications;

    /**
     * Constructor.
     * @param roomId the id of the chat room
     * @param notifications all the notifications
     */
    public RoomNotificationsResponse(int roomId, List<String> notifications) {
        super("RoomNotifications");
        this.roomId = roomId;
        this.notifications = notifications;
    }
}
