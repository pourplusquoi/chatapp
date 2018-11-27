'use strict';

const webSocket = new WebSocket("ws://" + location.hostname + ":" + location.port + "/chatapp");

var curUserId = -1;
var curRoomId = -1;
var curReceiverId = -1;

// window.onbeforeunload = function() {
//     return "Dude, are you sure you want to leave? Think of the kittens!";
// }

/**
 * Entry point into chat room
 */
window.onload = function() {

    var loginDiv = document.getElementById("login-space");
    var mainDiv = document.getElementById("whole-page");
    // 设置login为可视 main为隐藏
    loginDiv.style.display = "block";
    mainDiv.style.display = "none";

    webSocket.onclose = () => alert("WebSocket connection closed");

    //call the updateChatRoom every time a message is received from the server web socket.
    webSocket.onmessage = (event) => {
        updateChatRoom(event.data);
    };
};

function exitTest(roomid){
    var data = {
        type : "leave",
        roomId: roomid
    }
    sendMessage(JSON.stringify(data));

    if(roomid == $("#chat-interface-title-span").attr("roomid")){
        clearChatAndUsers();
    }
}

//update Status Area when you cannot join room/create room due to restriction, or cannot send message yet because haven't selected the correct user
//and when the owner exit room/disconnected(disconnected)
function updateAppStatusArea(message){
    var statusArea = document.getElementById('app-status-textarea');
    statusArea.innerHTML += message +"\n";
}

function updateRoomStatusArea(message){
    console.log(message);
    var roomStatusArea = document.getElementById('room-status-textarea');
    roomStatusArea.innerHTML += message +"\n";
}


function clearChatOnly(){
    $("#chat-interface-textarea ul").empty();
    $("#chat-interface-title-span").attr("userid",-1);
}

function clearChatAndUsers(){
    $("#user-list ul").empty();
    $("#chat-interface-textarea ul").empty();
    $("#chat-interface-title-span").attr("roomid",-1);
    $("#chat-interface-title-span").attr("userid",-1);
    $("#app-status-textarea").val("");
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
    console.log(type, message);
    switch(type){
        case "NewUser":
            newUserHandler(message);
            break;
        // case "NewRoom":
        //     newRoomHandler(message);
        //     break;
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
    // $("#room-status-textarea").val("");
    var roomStatusArea = document.getElementById('room-status-textarea');
    roomStatusArea.innerHTML = ""; //message +"\n";

    var notifications = JSON.parse(message).notifications;
    // console.log(notifications);
    notifications.forEach(function (notification){
        roomStatusArea.innerHTML += notification +"\n";
        // updateRoomStatusArea(notification);
        // console.log(notification);
    });
    // updateRoomStatusArea()
    // console.log(message);
}

function nullHandler(message){
    updateAppStatusArea(JSON.parse(message).message);
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
                    $("#chat-interface-textarea ul").append('<li><img class="imgright" src="static/img/user.png"><span class="spanright">' +  singlemsg["message"] + " " + singlemsg["isReceived"] + '</span></li>');
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
        if(!users.hasOwnProperty(curUserId)){
            clearChatOnly();
            console.log("#####################################")
        }

        $("#user-list ul").empty();

        for (var key in users){
            var username = users[key];
            var userId = key;//JSON.parse(message).roomId;
            // var initial = roomname[0].toUpperCase();
            if (ownerId == key){
                $("#user-list ul").append('<li userid = "' + userId + '" class="list-group-item js_open_user"><img src="static/img/crown.png" style="width:12px; height:12px;"> &nbsp;' + username +'</li>');
            } else {
                $("#user-list ul").append('<li userid = "' + userId + '" class="list-group-item js_open_user"><img src="static/img/user.png" style="width:12px; height:12px;"> &nbsp;' + username +'</li>');
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
    for (var key in joinedRooms){
        var room = joinedRooms[key];
        var roomId = key;//JSON.parse(message).roomId;
        var initial = room["name"][0].toUpperCase();
        $("#joined-room-list ul").append('<li action="exit" data-toggle="popover" value="'+room["ageRange"]+','+room["locations"] +',' + room["schools"]  +'" class="list-group-item js_open_room" roomid = "' + roomId + '"><img src="static/img/Letter-' + initial + '.jpg" style="width:12px; height:12px;"> &nbsp;' + room["name"] +'</li>');//<button class = "js_exit">EXIT</button>
    }

    //if current room no longer in the joined room list but there is a curRoomId, which means it has been removed by the owner, and needs to update the chatroom
    var curRoomId = $("#chat-interface-title-span").attr("roomid");
    if(!joinedRooms.hasOwnProperty(curRoomId) && curRoomId != -1){
        clearChatAndUsers();
        updateAppStatusArea("Room " + currentRoomName + " no longer exists");
    }

    popover();
}

$(document).on('click','#send-btn', function() {

    var roomid = $("#chat-interface-title-span").attr("roomid");
    var userid = $("#chat-interface-title-span").attr("userid");
    if (roomid == -1 || userid == -1){
        updateAppStatusArea("You are not allowed to start chat before you selecting room and user");
        // var statusArea = document.getElementById('status-textarea');
        // statusArea.innerHTML += "You are not allowed to start chat before you selecting room and user" +"\n";
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

var currentRoomName;
//when user click the joined room. highlight the room clicked and send to server to show all users in the room
$(document).on('click','.js_open_room', function(){
    var roomid = $(this).attr("roomid");
    $("#chat-interface-title-span").attr("roomid", roomid);

    $(this).siblings('li').removeClass('active');
    $(this).addClass('active');
    currentRoomName =  $(this).text();
    // clearChatAndUsers();

    var data = {
        type : "openroom",
        roomId: roomid
    }
    sendMessage(JSON.stringify(data));
})

//when user click the user name. highlight the user clicked and send to server to show the chat history with certain body.
$(document).on('click','.js_open_user', function(){
    var userid = $(this).attr("userid");
    $("#chat-interface-title-span").attr("userid", userid);

    // https://stackoverflow.com/questions/8558979/how-to-set-class-active-in-jquery
    $(this).siblings('li').removeClass('active');
    $(this).addClass('active');
    // clearChatOnly();


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
            },
            region: {
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
            }
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

// Exist the chat room
    function exit() {
        alert('老铁别走啊');
    }
}