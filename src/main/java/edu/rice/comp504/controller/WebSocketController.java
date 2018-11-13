package edu.rice.comp504.controller;

import edu.rice.comp504.model.DispatcherAdapter;
import edu.rice.comp504.model.cmd.*;
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
        DispatcherAdapter dis = ChatAppController.getDis();
        String username = "User" + dis.getNextUserId();
        dis.setNextUserId(dis.getNextUserId()+1);
        dis.getUserNameMap().put(user, username);
    }

    /**
     * Close the user's session.
     * @param user The use whose session is closed.
     */
    @OnWebSocketClose
    public void onClose(Session user, int statusCode, String reason) {
        DispatcherAdapter dis = ChatAppController.getDis();
        String username = dis.getUserNameMap().get(user);
        dis.getUserNameMap().remove(user);
    }

    /**
     * Send a message.
     * @param user  The session user sending the message.
     * @param message The message to be sent.
     */
    @OnWebSocketMessage
    public void onMessage(Session user, String message) {
        DispatcherAdapter dis = ChatAppController.getDis();

        //assume message will be like cmd type + real message
        String[] tokens = message.split(" ",2);
        String cmd = tokens[0];
//        System.out.println(cmd);
        String body = tokens[1];//message.split(" ",1)[1];
//        System.out.println(body);
        switch(cmd){
            case "login":
                dis.loadUser(user, body);
                break;
            case "createroom":
                dis.loadRoom(user, body);
                break;
            case "joinroom":
                dis.joinRoom(user, body);
                break;
//            case "exitroom":
//                //exit room freedom/#all
//                dis.exitRoom(user, body);
//                break;
            case "chat":
                //message chat freedom[chatroom id] #All/username Say hi[chat message]
                dis.chat(user, body);
                break;
        }

//        ChatAppController.update(icmd);

//        ChatAppController.broadcastMessage(ChatAppController.userNameMap.get(user), message);
        // TODO broadcast the message to all clients
    }
}
