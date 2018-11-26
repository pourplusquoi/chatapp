'use strict';

const webSocket = new WebSocket("ws://" + location.hostname + ":" + location.port + "/chatapp");


window.onbeforeunload = function() {
    return "Dude, are you sure you want to leave? Think of the kittens!";
}

/**
 * Entry point into chat room
 */
window.onload = function() {

    // var loginDiv = document.getElementById("login-space");
    // var mainDiv = document.getElementById("whole-page");
    // // 设置login为可视 main为隐藏
    // loginDiv.style.display = "block";
    // mainDiv.style.display = "none";

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
        updateChatRoom(event.data
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
    }
}

function newUserHandler(message){
    var username = JSON.parse(message).userName;
    var age = JSON.parse(message).age;
    var region = JSON.parse(message).region;
    var school = JSON.parse(message).school;

    var userSpan = document.getElementById('current-user');
    userSpan.innerHTML = username;
    console.log('before',$("#current-user").attr("value"))
    $("#current-user").attr("value", age+","+region+","+school);
    $("#current-user").attr("name", username);
    console.log('after',$("#current-user").attr("value"))
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
            // var loginDiv = document.getElementById("login-space");
            // var mainDiv = document.getElementById("whole-page");
            // loginDiv.style.display = "none";
            // mainDiv.style.display = "block";
        });
}

function createRoom(){
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