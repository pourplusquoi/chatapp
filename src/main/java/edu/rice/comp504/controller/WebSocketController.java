package edu.rice.comp504.controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import edu.rice.comp504.model.DispatcherAdapter;

/**
 * Create a web socket for the server.
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

        JsonParser parser = new JsonParser();
        JsonObject o = parser.parse(message).getAsJsonObject();
//        System.out.println(o);
        String type = o.get("type").toString();
        //to remove the double quotation mark
        type = type.substring(1, type.length() - 1);


//        // Assume message will be like "[command] [body]"
//        String[] tokens = message.split(" ", 2);
//        String command = tokens[0];
//        String body = tokens[1];

        String body = message;
        switch (type) {
//            switch (command) {
            case "login":
                dis.loadUser(user, body);
                break;

            case "create":
                dis.loadRoom(user, body);
                break;

            case "modify":
                dis.modifyRoom(user, body);
                break;

            case "join":
                dis.joinRoom(user, body);
                break;

            case "leave":
                dis.leaveRoom(user, body);
                break;

            case "send":
                dis.sendMessage(user, body);
                break;

            case "ack":
                dis.ackMessage(user, body);
                break;

            case "query":
                dis.query(user, body);
                break;

            default: break;
        }
    }
}
