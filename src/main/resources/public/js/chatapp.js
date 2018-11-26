'use strict';

const webSocket = new WebSocket("ws://" + location.hostname + ":" + location.port + "/chatapp");


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

    // TODO add an event handler to the Send Message button to send the message to the server.
    // $("#btn-msg").click(()=> {
    //     // console.log("test");
    //     // var msg = $("#message").val();
    //     //document.getElementById('message').value;
    //     // console.log(msg);
    //     sendMessage($("#message").val());
    // });
    // TODO call the updateChatRoom every time a message is received from the server web socket.
    webSocket.onmessage = (event) => {
        // console.log("returned");
        // console.log(event.data);
        updateChatRoom(event.data);
    };

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
    // var div = document.getElementById('chatArea');
    // div.innerHTML += message + "<br>";
    // div.html(div.html + msg + "<br>");
    // console.log("here");
    console.log(message);
    var type = JSON.parse(message).type;
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
            userChatHistoryHandler(message);
            break;
    }
}

function userChatHistoryHandler(message){
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
    console.log('current user',viewcurrentId,'receiver user',viewreceiverId);

    // console.log("in chat handler", message);
    //check if message is between currentId and receiverId
    if (roomId == $("#chat-interface-title-span").attr("roomid") && ((senderId == viewcurrentId && receiverId == viewreceiverId)||(senderId == viewreceiverId && receiverId == viewcurrentId))){
        var titleSpan = document.getElementById('chat-interface-title-span');
        titleSpan.innerHTML = "Chatroom " + roomName + " with " + receiver;

        // console.log("in the correct message box");
        chatHistory.forEach(function (singlemsg){
            console.log(singlemsg["senderId"], singlemsg["message"]);
            //if message is the other
            if (singlemsg["senderId"] == viewreceiverId || singlemsg["receiverId"] == viewcurrentId){
                $("#chat-interface-textarea ul").append('<li><img class="imgleft" src="static/img/user.png"><span class="spanleft">' +  singlemsg["message"] +'</span></li>');
            }
            if (singlemsg["senderId"] == viewcurrentId || singlemsg["receiverId"] == viewreceiverId){
                $("#chat-interface-textarea ul").append('<li><img class="imgright" src="static/img/user.png"><span class="spanright">' +  singlemsg["message"] +'</span></li>');
            }
        })
    }

}

function roomUsersHandler(message){
    var users = JSON.parse(message).users;
    var ownerId = JSON.parse(message).ownerId;
    var roomName = JSON.parse(message).roomName;
    var roomId = JSON.parse(message).roomId;
    // console.log(roomId, $("#chat-interface-title-span").attr("roomid"), "should not equal");
    //if the chatroom id is the same as the roomId received, then in the same room, need to update
    if (roomId == $("#chat-interface-title-span").attr("roomid")){
        // console.log("in the same room");
        $("#user-list ul").empty();

        var titleSpan = document.getElementById('chat-interface-title-span');
        titleSpan.innerHTML = "Chatroom " + roomName + " with NULL";

        // $("#chat-interface-title-span").attr("roomid", roomId);
        // console.log("after open room",$("#chat-interface-title-span").attr("roomid"));


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
        var roomname = availableRooms[key];
        var roomId = key;//JSON.parse(message).roomId;
        var initial = roomname[0].toUpperCase();
        $("#available-room-list ul").append('<li class="list-group-item" roomid = "' + roomId + '"><img src="static/img/Letter-' + initial + '.jpg" style="width:12px; height:12px;"> &nbsp;' + roomname +'<button class = "js_join">JOIN</button></li>');
    }
    // console.log(availableRooms);
    var joinedRooms = JSON.parse(message).joinedRooms;
    for (var key in joinedRooms){
        var roomname = joinedRooms[key];
        var roomId = key;//JSON.parse(message).roomId;
        var initial = roomname[0].toUpperCase();
        $("#joined-room-list ul").append('<li class="list-group-item js_open_room" roomid = "' + roomId + '"><img src="static/img/Letter-' + initial + '.jpg" style="width:12px; height:12px;"> &nbsp;' + roomname +'<button class = "js_exit">EXIT</button></li>');
    }
    // console.log(availableRooms);
}

$(document).on('click','#send-btn', function() {
    // console.log("send btn clicked");
    // var roomid = $(this).closest('li').attr("roomid");
    var roomid = $("#chat-interface-title-span").attr("roomid");
    var userid = $("#chat-interface-title-span").attr("userid");
    var content = $("#user-input-textarea").val();

    var data = {
        type : "send",
        roomId: roomid,
        receiverId: userid,
        content: content
    }
    // console.log(data);
    sendMessage(JSON.stringify(data));
    // console.log("join room " + roomid);
    console.log(data);

});


// https://stackoverflow.com/questions/41061046/get-ul-attribute-clicking-on-a-li-item
$(document).on('click','.js_join', function(){
    // console.log("clicked");
    var roomid = $(this).closest('li').attr("roomid");
    var data = {
        type : "join",
        roomId: roomid
    }
    // console.log(data);
    sendMessage(JSON.stringify(data));
    console.log("join room " + roomid);
})

$(document).on('click','.js_open_room', function(){
    // console.log("clicked");
    var roomid = $(this).attr("roomid");
    $("#chat-interface-title-span").attr("roomid", roomid);

    // var titleSpan = document.getElementById('chat-interface-title-span');
    // titleSpan.innerHTML = "Chatroom " + $(this).text() + " with NULL";

    // console.log("after open room",$("#chat-interface-title-span").attr("roomid"));

    var data = {
        type : "openroom",
        roomId: roomid
    }
    // console.log(data);
    sendMessage(JSON.stringify(data));
    console.log("open room " + roomid);
})

$(document).on('click','.js_open_user', function(){
    // console.log("clicked");
    var userid = $(this).attr("userid");
    $("#chat-interface-title-span").attr("userid", userid);

    // var titleSpan = document.getElementById('chat-interface-title-span');
    // titleSpan.innerHTML = "Chatroom " + roomName + " with NULL";

    var roomid = $("#chat-interface-title-span").attr("roomid");

    var data = {
        type : "openuser",
        roomId: roomid,
        userId: userid
    }
    // console.log(data);
    sendMessage(JSON.stringify(data)); //send to server to get chat history
    console.log("open user ", roomid, userid);
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
    // console.log('before',$("#current-user").attr("value"))
    $("#current-user").attr("value", age+","+region+","+school);
    $("#current-user").attr("name", username);
    $("#current-user").attr("userId", userId);
    // console.log('after userId is',$("#current-user").attr("userId"))
    // userSpan.val(age+","+region+","+school);
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
            // console.log(data);
            sendMessage(JSON.stringify(data));
            /**
             当用户验证成功时
             设置login界面为hidden
             设置main界面为viewable
             **/
            console.log("success");
            var loginDiv = document.getElementById("login-space");
            var mainDiv = document.getElementById("whole-page");
            loginDiv.style.display = "none";
            mainDiv.style.display = "block";
        });
}

function createRoom(){

    $('#create-room-form').bootstrapValidator().on('success.form.bv', function(e) {
        // Prevent submit form
        e.preventDefault();

        var roomname = $('#create-room-form').find('input[name="roomname"]').val();
        var agelb = $('#create-room-form').find('input[name="agelb"]').val();
        var ageub = $('#create-room-form').find('input[name="ageub"]').val();
        var regions = $('#create-room-form').find('select[name="region"]').val();
        var schools = $('#create-room-form').find('select[name="school"]').val();
        var data = {
            type : "create",
            roomname: roomname,
            agelb:agelb,
            ageub:ageub,
            regions:regions,
            schools:schools
        }

        sendMessage(JSON.stringify(data));
        console.log("created room");
    });


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
            console.log(element.attr('id'));
            var id = element.attr('id');
            console.log(element.attr('value'));
            var restriction = element.attr('value');
            // For those value is not undefined
            if (restriction != null) {
                var age = restriction.split(',')[0];
                var location = restriction.split(',')[1];
                var school = restriction.split(',')[2];
                console.log(age + ";" + location + ";" + "school");
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
                        return chatRoomDetail();
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
    function chatRoomDetail() {
        var data = $(
            "<div class='chat-room-details'>" +
            "<ul>" +
            "<li><span><img src='static/img/age.png'>18+</span></li>" +
            "<li><span><img src='static/img/location.png'>North America</span></li>" +
            "<li><span><img src='static/img/university.png'>Rice University</span></li>" +
            "</ul>" +

            "<input id='btn' type='button' value='EXIT' onclick='exit()' class='btn btn-primary'/>" +
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