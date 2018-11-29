'use strict';
const webSocket = new WebSocket("ws://" + location.hostname + ":" + location.port + "/chatapp");

var g_curUserId = -1;
var g_curRoomId = -1;
var g_curReceiverId = -1;
var g_curRoomName = "";


// window.onbeforeunload = function() {
//     return "Dude, are you sure you want to leave? Think of the kittens!";
// }

/**
 * Entry point into chat room
 */
var heartbeatTimer;
function heartBeat(){
    var data = {
        type : "heartbeat",
    }
    sendMessage(JSON.stringify(data));
    console.log(data);
}

window.onload = function() {
    // console.log(webSocket);

    heartbeatTimer = setInterval(heartBeat, 30000);

    var loginDiv = document.getElementById("login-space");
    var mainDiv = document.getElementById("whole-page");
    // 设置login为可视 main为隐藏
    loginDiv.style.display = "block";
    mainDiv.style.display = "none";

    //set "exit all joined room button" hidden
    // var exitRoomBtn = document.getElementById("exit-all-btn");
    // exitRoomBtn.style.display = "none";

    webSocket.onclose = () => {
        clearInterval(heartbeatTimer);
        alert("WebSocket connection closed");//app-status-textarea
    }

    //call the updateChatRoom every time a message is received from the server web socket.
    webSocket.onmessage = (event) => {
        console.log(g_curUserId, g_curRoomId, g_curRoomName,g_curReceiverId)
        updateChatRoom(event.data);
    };
};

function exitTest(roomid){
    var data = {
        type : "leave",
        roomId: roomid
    }
    sendMessage(JSON.stringify(data));

    // if(roomid == $("#chat-interface-title-span").attr("roomid")){
    //     clearChatAndUsers();
    //     console.log("====================================")
    // }
}

//update Status Area when you cannot join room/create room due to restriction, or cannot send message yet because haven't selected the correct user
//and when the owner exit room/disconnected(disconnected)
function updateAppStatusArea(message, isErr, isAlert){
    var statusArea = document.getElementById('app-status-textarea');
    if (isErr){
        statusArea.innerHTML = "[ERROR] - " + message +"\n";
        statusArea.style.color = "yellow";
    } else if (isAlert) {
        statusArea.innerHTML = "[ALERT] - " + message +"\n";
        statusArea.style.color = "yellow";
    }else {
        statusArea.innerHTML = "[SYSTEM] - " + message +"\n";
        statusArea.style.color = "white";
    }
}

// function updateRoomStatusArea(message){
//     console.log(message);
//     var roomStatusArea = document.getElementById('room-status-textarea');
//     roomStatusArea.innerHTML += message +"\n";
// }


function clearChatOnly(){
    $("#chat-interface-textarea ul").empty();
    $("#chat-interface-title-span").attr("userid",-1);
    g_curReceiverId = -1;
}

function clearChatAndUsers(){
    $("#user-list ul").empty();
    $("#chat-interface-textarea ul").empty();
    $("#chat-interface-title-span").attr("roomid",-1);
    $("#chat-interface-title-span").attr("userid",-1);
    $("#room-status-textarea").val("");
    g_curReceiverId = -1;
    g_curRoomId = -1;
    g_curRoomName = "";
}

function joinTest(roomid){
    var data = {
        type : "join",
        roomId: roomid
    }
    sendMessage(JSON.stringify(data));
}


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
    var type = JSON.parse(message).type;
    updateAppStatusArea("Received response from Server - type [" + type + "]", false, false);
    console.log(type, message);
    switch(type){
        case "NewUser":
            newUserHandler(message);
            break;
        case "UserRooms":
            userRoomsHandler(message);
            break;
        case "RoomUsers":
            roomUsersHandler(message);
            break;
        case "UserChatHistory":
            userChatHistoryHandler(message, true);
            break;
        case "Null":
            nullHandler(message);
            break;
        case "RoomNotifications":
            roomNotificationHandler(message);
            break;
        case "GroupMessage":
            userChatHistoryHandler(message, false);
            break;
    }
}

function roomNotificationHandler(message){
    var roomId = JSON.parse(message).roomId;
    //only update if roomnotification is for current openning room
    console.log(roomId, g_curRoomId, 'isequal', roomId == g_curRoomId)
    if (roomId == g_curRoomId){
        // console.log(JSON.parse(message).notifications);
        $("#room-status-textarea").val((JSON.parse(message).notifications).toString().replace(/,/g,""));
        // var roomStatusArea = document.getElementById('room-status-textarea');
        // roomStatusArea.innerHTML = ""; //message +"\n";
        //
        // var notifications = JSON.parse(message).notifications;
        // notifications.forEach(function (notification){
        //     console.log("inside updating");
        //     roomStatusArea.innerHTML += notification +"\n";
        // });
    }
}

function nullHandler(message){
    updateAppStatusArea(JSON.parse(message).message, true, false);
    // var statusArea = document.getElementById('status-textarea');
    // statusArea.innerHTML += JSON.parse(message).message +"\n";
}

function userChatHistoryHandler(message, isNeedAck){
    $("#chat-interface-textarea ul").empty();

    var sender = JSON.parse(message).sender;
    var receiver = JSON.parse(message).receiver;
    var roomName = JSON.parse(message).roomName;
    var roomId = JSON.parse(message).roomId;
    var senderId = JSON.parse(message).senderId;
    var receiverId = JSON.parse(message).receiverId;
    var chatHistory = JSON.parse(message).chatHistory;

    var viewcurrentId = $("#current-user").attr("userId");
    var viewreceiverId = $("#chat-interface-title-span").attr("userid");

    // var groupmsgMark = ""
    // if (isNeedAck == false)
    // {
    //     groupmsgMark = "[G]"
    // }

    //this is to acknowledge the message received.
    if (chatHistory != null && isNeedAck == true) {
        //send acknowledge to server
        if (viewcurrentId == receiverId) {
            chatHistory.forEach(function (singlemsg) {
                if (singlemsg["receiverId"] == viewcurrentId && singlemsg['isReceived'] == false) {
                    var data = {
                        type: "ack",
                        msgId: singlemsg["id"]
                    }
                    sendMessage(JSON.stringify(data));
                }
            })
        }
    }
        //check if message is between currentId and receiverId
    if (roomId == $("#chat-interface-title-span").attr("roomid") && ((senderId == viewcurrentId && receiverId == viewreceiverId)||(senderId == viewreceiverId && receiverId == viewcurrentId))){
        if (chatHistory != null){
            //update chat history message box
            chatHistory.forEach(function (singlemsg){
                //if message is the other
                if (singlemsg["senderId"] == viewreceiverId || singlemsg["receiverId"] == viewcurrentId){
                    $("#chat-interface-textarea ul").append('<li><img class="imgleft" src="static/img/user.png"><span class="spanleft">' +  singlemsg["message"] + '</span></li>');
                }
                if (singlemsg["senderId"] == viewcurrentId || singlemsg["receiverId"] == viewreceiverId){
                    // $("#chat-interface-textarea ul").append('<li><img class="imgright" src="static/img/user.png"><span class="spanright">' +  singlemsg["message"] + " " + singlemsg["isReceived"] + '</span></li>');
                    if (singlemsg["isReceived"]) {
                        $("#chat-interface-textarea ul").append('<li><img class="imgright" src="static/img/user.png"><span class="glyphicon glyphicon-ok sentsymbol"></span><span class="spanright">' + singlemsg["message"] + '</span></li>');
                    }
                    else {
                        $("#chat-interface-textarea ul").append('<li><img class="imgright" src="static/img/user.png"><span class="spanright">' + singlemsg["message"] + '</span></li>');
                    }
                }
            })
        }
    }
}

function roomUsersHandler(message){
    var users = JSON.parse(message).users;
    var ownerId = JSON.parse(message).ownerId;
    var roomName = JSON.parse(message).roomName;
    var roomId = JSON.parse(message).roomId;


    var curRoomId = $("#chat-interface-title-span").attr("roomid");
    //if the chatroom id is the same as the roomId received, then in the same room, need to update
    if (roomId == curRoomId){

        //if the current receiver user left, refresh userId and empty the textbox
        var curUserId = $("#chat-interface-title-span").attr("userid");
        if(!users.hasOwnProperty(curUserId) && g_curReceiverId != -1){
            clearChatOnly();
            console.log("#####################################")
        }

        $("#user-list ul").empty();

        for (var key in users){
            var username = users[key];
            var userId = key;//JSON.parse(message).roomId;
            // var initial = roomname[0].toUpperCase();
            if (ownerId == key){
                if (userId == g_curReceiverId){
                    $("#user-list ul").append('<li userid = "' + userId + '" class="list-group-item js_open_user active"><img src="static/img/crown.png" style="width:12px; height:12px;"> &nbsp;' + username +'</li>');
                } else {
                    $("#user-list ul").append('<li userid = "' + userId + '" class="list-group-item js_open_user"><img src="static/img/crown.png" style="width:12px; height:12px;"> &nbsp;' + username +'</li>');
                }
            } else {
                if (userId == g_curReceiverId) {
                    $("#user-list ul").append('<li userid = "' + userId + '" class="list-group-item js_open_user active"><img src="static/img/user.png" style="width:12px; height:12px;"> &nbsp;' + username +'</li>');
                }
                else{
                    $("#user-list ul").append('<li userid = "' + userId + '" class="list-group-item js_open_user"><img src="static/img/user.png" style="width:12px; height:12px;"> &nbsp;' + username +'</li>');
                }
            }
        }
    }
}

function userRoomsHandler(message){
    $("#available-room-list ul").empty();
    $("#joined-room-list ul").empty();

    var availableRooms = JSON.parse(message).availableRooms;
    for (var key in availableRooms){
        var room = availableRooms[key];
        var roomId = key;//JSON.parse(message).roomId;
        var initial = room["name"][0].toUpperCase();
        $("#available-room-list ul").append('<li action="join" data-toggle="popover" value="'+room["ageRange"]+','+room["locations"] +',' + room["schools"]  +'" class="list-group-item" roomid = "' + roomId + '"><img src="static/img/Letter-' + initial + '.jpg" style="width:12px; height:12px;"> &nbsp;' + room["name"] +'</li>'); //<button class = "js_join">JOIN</button>
    }
    var joinedRooms = JSON.parse(message).joinedRooms;

    //if previously joined room no longer exists, the room deleted by owner. clear all chat and users
    if(!joinedRooms.hasOwnProperty(g_curRoomId) && g_curRoomId != -1){
        updateAppStatusArea("You are no longer in room " + g_curRoomName + "!",false, true);
        console.log("====================================" + g_curRoomName + " has been closed");
        clearChatAndUsers();
    }

    for (var key in joinedRooms){
        var room = joinedRooms[key];
        var roomId = key;//JSON.parse(message).roomId;
        var initial = room["name"][0].toUpperCase();
        if (roomId == g_curRoomId){
            $("#joined-room-list ul").append('<li action="exit" data-toggle="popover" value="'+room["ageRange"]+','+room["locations"] +',' + room["schools"]  +'" class="list-group-item js_open_room active" roomid = "' + roomId + '"><img src="static/img/Letter-' + initial + '.jpg" style="width:12px; height:12px;"> &nbsp;' + room["name"] +'</li>');//<button class = "js_exit">EXIT</button>
        }
        else {
            $("#joined-room-list ul").append('<li action="exit" data-toggle="popover" value="'+room["ageRange"]+','+room["locations"] +',' + room["schools"]  +'" class="list-group-item js_open_room" roomid = "' + roomId + '"><img src="static/img/Letter-' + initial + '.jpg" style="width:12px; height:12px;"> &nbsp;' + room["name"] +'</li>');//<button class = "js_exit">EXIT</button>
        }
    }

    popover();
}

$(document).on('click','#send-btn', function() {

    var roomid = $("#chat-interface-title-span").attr("roomid");
    var userid = $("#chat-interface-title-span").attr("userid");
    // if (roomid == -1 || userid == -1){
    if (g_curRoomId == -1 || g_curReceiverId == -1) {
        updateAppStatusArea("You are not allowed to start chat before you selecting room and user", true, false);
    }
    else {
        var content = $("#user-input-textarea").val();
        var data = {
            type : "send",
            roomId: roomid,
            receiverId: userid,
            content: content
        }
        sendMessage(JSON.stringify(data));
        $("#user-input-textarea").val('');
    }
});

//when user click the joined room. highlight the room clicked and send to server to show all users in the room
$(document).on('click','.js_open_room', function(){

    //clear chat history and reset user selected if click on room
    $("#chat-interface-textarea ul").empty();
    g_curReceiverId = -1;

    var roomid = $(this).attr("roomid");


    g_curRoomId = roomid;
    g_curRoomName = $(this).text();

    $("#chat")
    // console.log(g_curRoomId, g_curRoomName);

    $("#chat-interface-title-span").attr("roomid", roomid);

    $(this).siblings('li').removeClass('active');
    $(this).addClass('active');

    var data = {
        type : "openroom",
        roomId: roomid
    }
    sendMessage(JSON.stringify(data));
})

//when user click the user name. highlight the user clicked and send to server to show the chat history with certain body.
$(document).on('click','.js_open_user', function(){
    var userid = $(this).attr("userid");

    g_curReceiverId = userid;

    $("#chat-interface-title-span").attr("userid", userid);

    // https://stackoverflow.com/questions/8558979/how-to-set-class-active-in-jquery
    $(this).siblings('li').removeClass('active');
    $(this).addClass('active');


    var roomid = $("#chat-interface-title-span").attr("roomid");

    var data = {
        type : "openuser",
        roomId: roomid,
        userId: userid
    }
    sendMessage(JSON.stringify(data)); //send to server to get chat history
})


// function newRoomHandler(message){
//     var roomname = JSON.parse(message).roomName;
//     var roomId = JSON.parse(message).roomId;
//     var initial = roomname[0].toUpperCase();
//
//     $("#joined-room-list ul").append('<li class="list-group-item"><img src="static/img/Letter-' + initial + '.jpg" style="width:12px; height:12px;"><span>'+ roomId +'</span>&nbsp;' + roomname +'</li>');
// }

function newUserHandler(message){
    var username = JSON.parse(message).userName;
    var age = JSON.parse(message).age;
    var region = JSON.parse(message).region;
    var school = JSON.parse(message).school;
    var userId = JSON.parse(message).userId;

    var userSpan = document.getElementById('current-user');
    userSpan.innerHTML = username;
    $("#current-user").attr("value", age+","+region+","+school);
    $("#current-user").attr("name", username);
    $("#current-user").attr("userId", userId);

    g_curUserId = userId;
    // console.log(g_curUserId, $("#current-user").attr("userId"));//0 "0"
    popover();
}



function validateInput() {

    /* Add validator function with each input */
    $('#login-form').bootstrapValidator({
        message: 'This value is not vaid',
        feedbackIcons: {
            valid: 'glyphicon glyphicon-ok',
            invalid: 'glyphicon glyphicon-remove',
            validating: 'glyphicon glyphicon-refresh'
        },
        fields: {
            username: {
                message: 'Username is not valid',
                validators: {
                    notEmpty: {
                        message: 'Username cannot be null'
                    },
                    regexp: {
                        regexp: /^[a-zA-Z0-9_]+$/,
                        message: 'Username must contain only lowercase or uppercase letters, numbers, and underscores'
                    }
                }
            },
            age: {
                message: 'Age is not valid',
                validators: {
                    notEmpty: {
                        message: 'Age cannot be null'
                    },
                    regexp: {
                        regexp: /^[0-9_]+$/,
                        message: 'Age must be numbers'
                    },
                    between: {
                        min: 1,
                        max: 140,
                        message: 'Invalid age'
                    }
                }
            }
            /*region: {
                message: 'Region is not valid',
                validators: {
                    notEmpty: {
                        message: 'Must select one region'
                    },
                    callback: { //Custom validate rules
                        message: 'Invalid Select',
                        callback: function(value, validator) {
                            var selectedRegion = $('#region-select option:selected').val();
                            if(selectedRegion == "Invalid") { return false; }
                            else { return true; }
                        }
                    }
                }
            },
            school: {
                message: 'School is not valid',
                validators: {
                    notEmpty: {
                        message: 'Must select one school'
                    },
                    callback: { //Custom validate rules
                        message: 'Selected school is not in selected region',
                        callback: function(value, validator) {
                            var thisSchoolRegionSymbol = value.split("-")[0];
                            var selectedRegion = $('#region-select option:selected').val();
                            if(thisSchoolRegionSymbol=="Invalid" || !selectedRegion.startsWith(thisSchoolRegionSymbol)) { return false; }
                            else { return true; }
                        }
                    }
                }
            }*/
        }
    })
        .on('success.form.bv', function(e) {
            // Prevent submit form
            e.preventDefault();

            var $form     = $(e.target),
                validator = $form.data('bootstrapValidator');
            //$form.submit();
            //alert("Login success");

            var username = $('#login-form').find('input[name="username"]').val();
            var age = $('#login-form').find('input[name="age"]').val();
            var region = $('#login-form').find('select[name="region"]').val();
            var school = $('#login-form').find('select[name="school"]').val();

            var data = {
                type : "login",
                username : username,
                age : age,
                region : region,
                school : school
            }
            sendMessage(JSON.stringify(data));
            /**
             当用户验证成功时
             设置login界面为hidden
             设置main界面为viewable
             **/
            var loginDiv = document.getElementById("login-space");
            var mainDiv = document.getElementById("whole-page");
            loginDiv.style.display = "none";
            mainDiv.style.display = "block";
        });
}

$('#create-room-form').submit(function(){
    createRoom();
    return false;
})

function exitAllRooms(){
    var data = {
        type : "exitall"
    }
    sendMessage(JSON.stringify(data));
}

function cancelCreate() {
    $('#myTab a:first').tab('show'); //click the first tab to show the joined chatroom list
}

function createRoom(){
    var roomname = $('#cr_roomname').val();
    var agelb = $('#cr_agelb').val();
    var ageub = $('#cr_ageub').val();
    var regions = $('#region-restriction-select').val();
    var schools = $('#school-restriction-select').val();
    var data = {
        type : "create",
        roomname: roomname,
        agelb:agelb,
        ageub:ageub,
        regions:regions,
        schools:schools
    }

    sendMessage(JSON.stringify(data));
    cancelCreate();
}

/*********************************************
 * *
 * *Popover js
 */
function popover() {
    $(function () {
        $("[data-toggle='popover']").each(function () {
            // Get the popover element
            var element = $(this);
            // Define the behavior of element
            var id = element.attr('id');
            var restriction = element.attr('value');
            var roomid = element.attr('roomid');
            var action = element.attr('action');

            // For those value is not undefined
            if (restriction != null) {
                var age = restriction.split(',')[0];
                var location = restriction.split(',')[1];
                var school = restriction.split(',')[2];
            }

            // Show the current user tag
            if (id == "current-user") {
                element.popover({
                    trigger: 'manul',
                    html: true,
                    title: element.attr('name'), // User name
                    placement: 'bottom',
                    content: function () {
                        return userDetail(age, location, school);
                    }
                    // When user mouse enter, show the details
                }).on("mouseenter", function () {
                    var _this = this;
                    $(this).popover("show");
                    $(this).siblings(".popover").on("mouseleave", function () {
                        $(_this).popover('hide');
                    });
                    // When user mouse leave, hide the details
                }).on("mouseleave", function () {
                    var _this = this;
                    setTimeout(function () {
                        if (!$(".popover:hover").length) {
                            $(_this).popover("hide")
                        }
                    }, 100);
                });
            }
            // Show chat room tag
            else {
                element.popover({
                    trigger: 'manul',
                    html: true,
                    title: element.attr('name'), // Chat room name
                    placement: 'right',
                    content: function () {
                        if (action == "join"){
                            return joinChatRoomDetail(roomid, age, location, school);
                        } else {
                            return exitChatRoomDetail(roomid, age, location, school);
                        }
                    }
                    // When user mouse enter, show the details
                }).on("mouseenter", function () {
                    var _this = this;
                    $(this).popover("show");
                    $(this).siblings(".popover").on("mouseleave", function () {
                        $(_this).popover('hide');
                    });
                    // When user mouse leave, hide the details
                }).on("mouseleave", function () {
                    var _this = this;
                    setTimeout(function () {
                        if (!$(".popover:hover").length) {
                            $(_this).popover("hide")
                        }
                    }, 100);
                });
            }
        });
    });

// The chat room detail interface
function exitChatRoomDetail(roomid, age, location, school) {
    var data = $(
        "<div class='chat-room-details'>" +
        "<ul>" +
        "<li><span><img src='static/img/age.png'>" + age + "</span></li>" +
        "<li><span><img src='static/img/location.png'>" + location + "</span></li>" +
        "<li><span><img src='static/img/university.png'>" + school + "</span></li>" +
        "</ul>" +

        "<input id='btn' type='button' value='EXIT' onclick='exitTest(" + roomid + ")' class='btn btn-primary'/>" +
        "</div>"
    );
    return data;
}

function joinChatRoomDetail(roomid, age, location, school) {
    var data = $(
        "<div class='chat-room-details'>" +
        "<ul>" +
        "<li><span><img src='static/img/age.png'>" + age + "</span></li>" +
        "<li><span><img src='static/img/location.png'>" + location + "</span></li>" +
        "<li><span><img src='static/img/university.png'>" + school + "</span></li>" +
        "</ul>" +

        "<input id='btn' type='button' value='JOIN' onclick='joinTest(" + roomid + ")' class='btn btn-primary'/>" +
        "</div>"
    );
    return data;
}

// The user tag interface
function userDetail(age, location, school) {
    var data = $(
        "<div class='chat-room-details'>" +
        "<ul>" +
        "<li><span><img src='static/img/age.png'>" + age + "</span></li>" +
        "<li><span><img src='static/img/location.png'>" + location + "</span></li>" +
        "<li><span><img src='static/img/university.png'>" + school + "</span></li>" +
        "</ul>" +
        "</div>"
    );
    return data;
}

}