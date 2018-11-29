import edu.rice.comp504.controller.ChatAppController;
import edu.rice.comp504.model.DispatcherAdapter;
import edu.rice.comp504.model.obj.ChatRoom;
import edu.rice.comp504.model.obj.User;
import junit.framework.TestCase;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.jetty.websocket.common.WebSocketSession;

import static org.mockito.Mockito.mock;

public class DispatchAdapterTest extends TestCase {
    private String url = "ws://localhost:4567/chatapp";

    /** test: no room at the beginning **/
    @org.junit.Test
    public void testInit() {
        DispatcherAdapter adapter = new DispatcherAdapter();
        assertEquals("No room at the start", adapter.getRooms().isEmpty(), true);
    }

    /** test: create a new session **/
    @org.junit.Test
    public void testNewSession() {
        DispatcherAdapter adapter = new DispatcherAdapter();
        Session session = mock(Session.class);
        adapter.newSession(session);
        assertEquals("should get a user id which is 0", adapter.getUserIdFromSession(session), 0);
        assertEquals("should contain session", adapter.containsSession(session), true);
    }


    /** test: login and create a user **/
    @org.junit.Test
    public void testLoadUser() {
        DispatcherAdapter adapter = new DispatcherAdapter();
        Session session = mock(Session.class);
        adapter.newSession(session);

        User user = adapter.loadUser(session, "{\"type\":\"login\",\"username\":\"user\",\"age\":\"25\",\"region\":\"USA\",\"school\":\"Rice\"}");
        assertEquals("name", user.getName(), "user");
        assertEquals("age", user.getAge(), 25);
        assertEquals("location", user.getLocation(), "USA");
        assertEquals("school", user.getSchool(), "Rice");
    }

    /** test: user can create a chat room **/
    @org.junit.Test
    public void testCreateRoom() {
        DispatcherAdapter adapter = new DispatcherAdapter();
        Session session = mock(Session.class);
        adapter.newSession(session);

        User user = adapter.loadUser(session, "{\"type\":\"login\",\"username\":\"user\",\"age\":\"25\",\"region\":\"USA\",\"school\":\"Rice\"}");
        ChatRoom room = adapter.loadRoom(session, "{\"type\":\"create\",\"roomname\":\"Freedom\",\"agelb\":\"20\",\"ageub\":\"40\",\"regions\":[\"USA\"],\"schools\":[\"Rice\"]}");

        assertEquals("name", room.getName(), "Freedom");
        assertEquals("owner", room.getOwner().getName(), user.getName());
        assertEquals("id", room.getId(), 0);
    }

    /** test: delete user while unload user **/
    @org.junit.Test
    public void testUnloadUser() {
        DispatcherAdapter adapter = new DispatcherAdapter();
        Session session = mock(Session.class);
        adapter.newSession(session);
        User user = adapter.loadUser(session, "{\"type\":\"login\",\"username\":\"user\",\"age\":\"25\",\"region\":\"USA\",\"school\":\"Rice\"}");

        assertEquals("before unloading there should be one user", adapter.countObservers(), 1);
        adapter.unloadUser(user.getId());
        assertEquals("after unloading, user should be deleted", adapter.countObservers(), 0);
    }

    /** test: delete room while unload room **/
    @org.junit.Test
    public void testUnloadRoom() {
        DispatcherAdapter adapter = new DispatcherAdapter();
        Session session = mock(Session.class);
        adapter.newSession(session);

        User user = adapter.loadUser(session, "{\"type\":\"login\",\"username\":\"user\",\"age\":\"25\",\"region\":\"USA\",\"school\":\"Rice\"}");
        ChatRoom room = adapter.loadRoom(session, "{\"type\":\"create\",\"roomname\":\"Freedom\",\"agelb\":\"20\",\"ageub\":\"40\",\"regions\":[\"USA\"],\"schools\":[\"Rice\"]}");

        assertEquals("before unloading, there should be one room", adapter.getRooms().size(), 1);
        adapter.unloadRoom(room.getId());
        assertEquals("after unloading, there should be no room", adapter.getRooms().size(), 0);
    }

    /** test: user can join room if the user satisfy the qualification and leave room any tine they want **/
    @org.junit.Test
    public void testJoinAndLeaveRoom() {
        DispatcherAdapter adapter = new DispatcherAdapter();

        Session session1 = mock(Session.class);
        adapter.newSession(session1);
        User user1 = adapter.loadUser(session1, "{\"type\":\"login\",\"username\":\"user1\",\"age\":\"25\",\"region\":\"USA\",\"school\":\"Rice\"}");
        ChatRoom room = adapter.loadRoom(session1, "{\"type\":\"create\",\"roomname\":\"Freedom\",\"agelb\":\"20\",\"ageub\":\"40\",\"regions\":[\"USA\"],\"schools\":[\"Rice\"]}");

        Session session2 = mock(Session.class);
        adapter.newSession(session2);
        User user2 = adapter.loadUser(session2, "{\"type\":\"login\",\"username\":\"user2\",\"age\":\"25\",\"region\":\"USA\",\"school\":\"Rice\"}");

        assertEquals("before user2 joining, there should be one user in chat room", room.getUsers().size(), 1);
        adapter.joinRoom(session2, "{\"type\":\"join\",\"roomId\":0}");
        assertEquals("after user2 joining, there should be two user in chat room", room.getUsers().size(), 2);

        adapter.leaveRoom(session2, "{\"type\":\"leave\",\"roomId\":0}");
        assertEquals("after user2 leaving, there should be one user in chat room", room.getUsers().size(), 1);

        adapter.joinRoom(session2, "{\"type\":\"join\",\"roomId\":0}");
        adapter.leaveRoom(session1, "{\"type\":\"leave\",\"roomId\":0}");
        assertEquals("after owner's (user1) leaving, there should be no room", adapter.getRooms().size(), 0);
    }

    /** test: user can open a joined chat room and see the room's info **/
    @org.junit.Test
    public void testOpenRoom() {
        DispatcherAdapter adapter = new DispatcherAdapter();
        Session session = mock(Session.class);
        adapter.newSession(session);

        User user = adapter.loadUser(session, "{\"type\":\"login\",\"username\":\"user\",\"age\":\"25\",\"region\":\"USA\",\"school\":\"Rice\"}");
        ChatRoom room = adapter.loadRoom(session, "{\"type\":\"create\",\"roomname\":\"Freedom\",\"agelb\":\"20\",\"ageub\":\"40\",\"regions\":[\"USA\"],\"schools\":[\"Rice\"]}");

        adapter.openRoom(session, "{\"type\":\"openroom\",\"roomId\":\"0\"}");
    }

    /** test: user can open a chat with certain user by open user function **/
    @org.junit.Test
    public void testOpenUser() {
        DispatcherAdapter adapter = new DispatcherAdapter();

        Session session1 = mock(Session.class);
        adapter.newSession(session1);
        User user1 = adapter.loadUser(session1, "{\"type\":\"login\",\"username\":\"user1\",\"age\":\"25\",\"region\":\"USA\",\"school\":\"Rice\"}");
        ChatRoom room = adapter.loadRoom(session1, "{\"type\":\"create\",\"roomname\":\"Freedom\",\"agelb\":\"20\",\"ageub\":\"40\",\"regions\":[\"USA\"],\"schools\":[\"Rice\"]}");

        Session session2 = mock(Session.class);
        adapter.newSession(session2);
        User user2 = adapter.loadUser(session2, "{\"type\":\"login\",\"username\":\"user2\",\"age\":\"25\",\"region\":\"USA\",\"school\":\"Rice\"}");

        adapter.joinRoom(session2, "{\"type\":\"join\",\"roomId\":0}");

        adapter.openUser(session2, "{\"type\":\"openuser\",\"roomId\":\"0\",\"userId\":\"0\"}");
    }

    /** test: user can exit all joined room at the same time **/
    @org.junit.Test
    public void testExitAllRooms() {
        DispatcherAdapter adapter = new DispatcherAdapter();

        Session session1 = mock(Session.class);
        adapter.newSession(session1);
        User user1 = adapter.loadUser(session1, "{\"type\":\"login\",\"username\":\"user1\",\"age\":\"25\",\"region\":\"USA\",\"school\":\"Rice\"}");
        ChatRoom room1 = adapter.loadRoom(session1, "{\"type\":\"create\",\"roomname\":\"Freedom1\",\"agelb\":\"20\",\"ageub\":\"40\",\"regions\":[\"USA\"],\"schools\":[\"Rice\"]}");

        Session session2 = mock(Session.class);
        adapter.newSession(session2);
        User user2 = adapter.loadUser(session2, "{\"type\":\"login\",\"username\":\"user2\",\"age\":\"25\",\"region\":\"USA\",\"school\":\"Rice\"}");
        ChatRoom room2 = adapter.loadRoom(session2, "{\"type\":\"create\",\"roomname\":\"Freedom2\",\"agelb\":\"20\",\"ageub\":\"40\",\"regions\":[\"USA\"],\"schools\":[\"Rice\"]}");

        adapter.joinRoom(session2, "{\"type\":\"join\",\"roomId\":0}");

        assertEquals("before exiting user2 should have 2 joined rooms", user2.getJoinedRooms().size(), 2);
        adapter.exitAllRooms(session2, "");
        assertEquals("after exiting user2 should have 0 joined rooms", user2.getJoinedRooms().size(), 0);
    }

    /** test: user can send message to another user in the same chat room **/
    @org.junit.Test
    public void testSendMessage() {
        DispatcherAdapter adapter = new DispatcherAdapter();

        Session session1 = mock(Session.class);
        adapter.newSession(session1);
        User user1 = adapter.loadUser(session1, "{\"type\":\"login\",\"username\":\"user1\",\"age\":\"25\",\"region\":\"USA\",\"school\":\"Rice\"}");
        ChatRoom room = adapter.loadRoom(session1, "{\"type\":\"create\",\"roomname\":\"Freedom1\",\"agelb\":\"20\",\"ageub\":\"40\",\"regions\":[\"USA\"],\"schools\":[\"Rice\"]}");

        Session session2 = mock(Session.class);
        adapter.newSession(session2);
        User user2 = adapter.loadUser(session2, "{\"type\":\"login\",\"username\":\"user2\",\"age\":\"25\",\"region\":\"USA\",\"school\":\"Rice\"}");

        adapter.joinRoom(session2, "{\"type\":\"join\",\"roomId\":0}");

        adapter.sendMessage(session2, "{\"type\":\"send\",\"roomId\":\"0\",\"receiverId\":\"0\",\"content\":\"hi\"}");
        assertEquals("after user2 sending message to user1, there should be a record in chat history", room.getChatHistory().size(), 1);

        assertEquals("before owner sending message to all, there should be only one record of chat history", room.getChatHistory().values().size(), 1);

        adapter.sendMessage(session1, "{\"type\":\"send\",\"roomId\":\"0\",\"receiverId\":\"0\",\"content\":\"hi\"}");
        assertEquals("after owner sending message to all, there should be 2 record of user1's chat history", room.getChatHistory().values().size(), 2);

        assertEquals("before sending hate, there should be 2 users in the chat room", room.getUsers().size(), 2);
        adapter.sendMessage(session2, "{\"type\":\"send\",\"roomId\":\"0\",\"receiverId\":\"0\",\"content\":\"hate you\"}");
        assertEquals("after sending hate, user2 should be removed from chat room", room.getUsers().size(), 1);
    }

    /** test: check if user receive message **/
    @org.junit.Test
    public void testAckMessage() {
        DispatcherAdapter adapter = new DispatcherAdapter();

        Session session1 = mock(Session.class);
        adapter.newSession(session1);
        User user1 = adapter.loadUser(session1, "{\"type\":\"login\",\"username\":\"user1\",\"age\":\"25\",\"region\":\"USA\",\"school\":\"Rice\"}");
        ChatRoom room = adapter.loadRoom(session1, "{\"type\":\"create\",\"roomname\":\"Freedom1\",\"agelb\":\"20\",\"ageub\":\"40\",\"regions\":[\"USA\"],\"schools\":[\"Rice\"]}");

        Session session2 = mock(Session.class);
        adapter.newSession(session2);
        User user2 = adapter.loadUser(session2, "{\"type\":\"login\",\"username\":\"user2\",\"age\":\"25\",\"region\":\"USA\",\"school\":\"Rice\"}");

        adapter.joinRoom(session2, "{\"type\":\"join\",\"roomId\":0}");

        adapter.sendMessage(session2, "{\"type\":\"send\",\"roomId\":\"0\",\"receiverId\":\"0\",\"content\":\"hi\"}");
        adapter.ackMessage(session2, "{\"type\":\"ack\",\"msgId\":0}");
    }

}
