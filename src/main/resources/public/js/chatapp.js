'use strict';

const webSocket = new WebSocket("ws://" + location.hostname + ":" + location.port + "/chatapp");

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
    $("#btn-msg").click(()=> {
        // console.log("test");
        // var msg = $("#message").val();
        //document.getElementById('message').value;
        // console.log(msg);
        sendMessage($("#message").val());
    });
    // TODO call the updateChatRoom every time a message is received from the server web socket.
    webSocket.onmessage = (event) => {
        console.log("returned");
        updateChatRoom(event.data)
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
    console.log("here");
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
    console.log('after',$("#current-user").attr("value"))
    // userSpan.val(age+","+region+","+school);
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