package edu.rice.comp504.controller;

import java.io.IOException;
import java.util.Map;

import com.google.gson.JsonObject;
import org.eclipse.jetty.websocket.api.Session;

import edu.rice.comp504.model.DispatcherAdapter;

import static spark.Spark.*;

/**
 * The chat app controller communicates with all the clients on the web socket.
 */
public class ChatAppController {

    private static DispatcherAdapter dispatcher = new DispatcherAdapter();

    /**
     * Get the chat app dispatcher
     * @return the dispatcher of chat app
     */
    public static DispatcherAdapter getDispatcher() {
        return dispatcher;
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

    /**
     Notify session about the message
     * @param user the session to notify
     * @param info the notification information
     */
    public static void notify(Session user, Map<String, String> info) {
        try {
            JsonObject jo = new JsonObject();
            // jo.addProperty("type", "login");
            for (String key : info.keySet())
                jo.addProperty(key, info.get(key));
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
