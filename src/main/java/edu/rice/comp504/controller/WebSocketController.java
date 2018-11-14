package edu.rice.comp504.controller;

import edu.rice.comp504.model.DispatcherAdapter;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

/**
 * Create a web socket for the server
 */
@WebSocket
public class WebSocketController {

    /**
     * Open user's session.
     * @param user The user whose session is opened.
     */
    @OnWebSocketConnect
    public void onConnect(Session user) {
        DispatcherAdapter dis = ChatAppController.getDispatcher();
        dis.newSession(user);
    }

    /**
     * Close the user's session.
     * @param user The use whose session is closed.
     */
    @OnWebSocketClose
    public void onClose(Session user, int statusCode, String reason) {
        DispatcherAdapter dis = ChatAppController.getDispatcher();
        int userId = dis.getUserIdFromSession(user);
        dis.unloadUser(userId);
    }

    /**
     * Send a message.
     * @param user  The session user sending the message.
     * @param message The message to be sent.
     */
    @OnWebSocketMessage
    public void onMessage(Session user, String message) {
        DispatcherAdapter dis = ChatAppController.getDispatcher();

        // Assume message will be like "edu.rice.comp504.model.edu.rice.comp504.model.cmd msg"
        String[] tokens = message.split(" ", 2);
        String cmd = tokens[0];
        String body = tokens[1];

        switch (cmd) {
            case "login":
                dis.loadUser(user, body);
                break;
            case "create":
                dis.loadRoom(user, body);
                break;
            case "join":
                dis.joinRoom(user, body);
                break;
            case "leave":
                dis.leaveRoom(user, body);
                break;
            case "send":
                // e.g. chat freedom[chatroom id] #All/username Say hi[chat message]
                dis.sendMessage(user, body);
                break;
            default:
                break;
        }
    }
}
