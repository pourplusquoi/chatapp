package edu.rice.comp504.controller;

import com.google.gson.JsonObject;
import edu.rice.comp504.model.DispatcherAdapter;
import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;

import static spark.Spark.*;

/**
 * The chat app controller communicates with all the clients on the web socket.
 */
public class ChatAppController {

    private static DispatcherAdapter dis = new DispatcherAdapter();
    public static DispatcherAdapter getDis() {
        return dis;
    }

    /**
     * Chat App entry point.
     * @param args The command line arguments
     */
    public static void main(String[] args) {
        port(getHerokuAssignedPort());
        staticFiles.location("/public");

        webSocket("/chatapp", WebSocketController.class);
        init();
    }

//    /**
//     * Broadcast message to all users.
//     * @param sender  The message sender.
//     * @param message The message.
//     */
//     static void broadcastMessage(String sender, String message) {
//        userNameMap.keySet().forEach(session -> {
//            try {
//                JsonObject jo = new JsonObject();
//                // TODO add a JSON object property that has a key (userMessage) and a j2html paragraph value
//                jo.addProperty("userMessage", sender + " said: " + message);
//                session.getRemote().sendString(String.valueOf(jo));
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//        });
//    }

    public static void notify(Session user, String message){
        try {
            JsonObject jo = new JsonObject();
            jo.addProperty("type","login");
            jo.addProperty("message", message);
            user.getRemote().sendString(String.valueOf(jo));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Get the heroku assigned port number.
     * @return The heroku assigned port number
     */
    private static int getHerokuAssignedPort() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (processBuilder.environment().get("PORT") != null) {
            return Integer.parseInt(processBuilder.environment().get("PORT"));
        }
        return 4567; // return default port if heroku-port isn't set.
    }

}
