'use strict';

const webSocket = new WebSocket("ws://" + location.hostname + ":" + location.port + "/chatapp");

/**
 * Entry point into chat room
 */
window.onload = function() {

    webSocket.onclose = () => alert("WebSocket connection closed");

    // TODO add an event handler to the Send Message button to send the message to the server.
    $("#btn-msg").click(()=> {
        // console.log("test");
        // var msg = $("#message").val();
        //document.getElementById('message').value;
        // console.log(msg);
        sendMessage($("#message").val());
    });
    // TODO call the updateChatRoom every time a message is received from the server web socket.
    webSocket.onmessage = (event) => updateChatRoom(event.data);

};

/**
 * Send a message to the server.
 * @param msg  The message to send to the server.
 */
function sendMessage(msg) {
    if (msg !== "") {
        webSocket.send(msg);
        $("#message").val("");
    }
}

/**
 * Update the chat room with a message.
 * @param message  The message to update the chat room with.
 */
function updateChatRoom(message) {
    // TODO convert the data to JSON and append the message to the chat area
    // var action = JSON.parse(message).action;
    // var content = JSON.parse(message).content;
    // console.log(json);
    var div = document.getElementById('chatArea');
    div.innerHTML += message + "<br>";
    // div.html(div.html + msg + "<br>");
}
