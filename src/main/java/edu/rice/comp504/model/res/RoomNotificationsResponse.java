package edu.rice.comp504.model.res;

import java.util.List;

public class RoomNotificationsResponse extends AResponse {

    private int roomId;
    private List<String> notifications;

    public RoomNotificationsResponse(int roomId, List<String> notifications) {
        super("RoomNotifications");
        this.roomId = roomId;
        this.notifications = notifications;
    }
}
