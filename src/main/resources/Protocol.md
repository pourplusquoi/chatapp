# Socket Protocol and Grammar

## 1. Server to Client Socket

`void ChatAppController::notify(Session user, AResponse response);`

### 1.1. Class `NewUserResponse` => Environment event: login user

Newly loaded user. Locates in `model.DispatcherAdapter`

- `type`: `String("NewUser")`
- `userId`: `int` The id of the new user
- `userName`: `int` The name of the new user

### 1.2. Class `NewRoomResponse` => Environment event: create room

Newly loaded chat room. Locates in `model.DispatcherAdapter`

- `type`: `String("NewRoom")`
- `roomId`: `int` The id of the new room
- `roomName`: `int` The name of the new room
- `ownerId`: `int` The id of the room owner

### 1.3. Class `UserRoomsResponse` => User event: room list change

Chat room list of single user. Locates in `model.obj.User`

- `type`: `String("UserRooms")`
- `userId`: `int` The id of the current user
- `joinedIds`: `List<Integer>` List of ids of room that user has joined
- `availableIds`: `List<Integer>` List of ids of available room that user didn't join

### 1.4. Class `UserChatHistoryResponse` => User event: reveive msg/ack

Chat history in single room between two specific users. Locates in `model.DispatcherAdapter`

- `type`: `String("UserChatHistory")`
- `chatHistory`: `List<Message>` Chat history between two users at chat room

### 1.5. Class `RoomNotificationsResponse` => Room event: new notifications

Notifications in single room. Locates in `model.obj.ChatRoom` & `model.DispatcherAdapter`

- `type`: `String("RoomNotifications")`
- `roomId`: `int` The id of the current room
- `notifications`: `List<String>` List of notifications at chat room

### 1.6. Class `RoomUsersResponse` => Room event: user list change

Users in single room. Locates in `model.obj.ChatRoom` & `model.DispatcherAdapter`

- `type`: `String("RoomUsers")`
- `roomId`: `int` The id of the current room
- `users`: `Map<Integer, String>` All users at chat room, maps from user id to user name

## 2. Client to Server Socket

`void WebSocketController::onMessage(Session user, String message);`

**Fields are sepearated by a white space**

### 2.1. Command `login`

Login a user.

**Grammar**: `login [userName] [age] [location] [school]`

**Example**: `login Shakeshack 30 USA Rice`

### 2.2. Command `create`

Create a user

**Grammar**: `create [roomName] [ageLower] [ageUpper] {[location],}*{[location]} {[school],}*{[school]}`

**Example**: `create Freedom 20 80 USA,Canada Rice,Harvard`

### 2.3. Command `modify`

Modify the chat room restriction

**Grammar**: `modify [roomId] [ageLower] [ageUpper] {[location],}*{[location]} {[school],}*{[school]}`

**Example**: `modify 0 30 50 USA Harvard,MIT `

### 2.4. Command `join`

Make a user join a chat room

**Grammar**: `join [roomId]`

**Example**: `join 1`

### 2.5. Command `leave`

Make a user leave a chat room

**Grammar**: `leave [roomId]`

**Example**: `leave 1`

### 2.6. Command `send`

Sender sends a message to reveiver at a chat room

**Grammar**: `send [roomId] [receiverId] [message]`

**Example**: `send 2 1 Hello, how are you?`

### 2.7. Command `ack`

Receiver ack a message from sender at a chat room

**Grammar**: `ack [msgId]`

**Example**: `ack 10`

### 2.8. Command `query`

Query information (e.g. chat history, notifications, user list, etc.) from chat room

**Grammar**: `query [keyword] {[param]}+`

#### 2.8.1. Keyword `RoomUsers`

Query for all users at a chat room

**Grammar**: `query RoomUsers [roomId]`

**Example**: `query RoomUsers 3`

#### 2.8.2. Keyword `RoomNotifications`

Query for all notifications at a chat room

**Grammar**: `query RoomNotifications [roomId]`

**Example**: `query RoomNotifications 3`

#### 2.8.3. Keyword `UserChatHistory`

Query for chat history between client itself and anther user at a char room

**Grammar**: `query UserChatHistory [roomId] [anotherUserId]`

**Example**: `query UserChatHistory 2 10`

