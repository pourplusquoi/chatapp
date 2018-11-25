$(function() {
    $("[data-toggle='popover']").each(function(){
        // Get the popover element
        var element = $(this);
        // Define the behavior of element
        console.log(element.attr('id'));
        var id = element.attr('id');
        
        // Show the current user tag
        if(id == "current-user"){
            element.popover({
                trigger: 'manul',
                html: true,
                title: element.attr('name'), // User name
                placement: 'bottom',
                content: function(){
                    return userDetail();
                }       
            // When user mouse enter, show the details
            }).on("mouseenter", function(){
                var _this = this;
                $(this).popover("show");
                $(this).siblings(".popover").on("mouseleave", function(){
                    $(_this).popover('hide');
                });
            // When user mouse leave, hide the details
            }).on("mouseleave", function(){ 
                var _this = this;
                setTimeout(function(){
                    if(!$(".popover:hover").length){
                        $(_this).popover("hide")
                    }
                }, 100);
            });
        }
        // Show chat room tag
        else{
            element.popover({
                trigger: 'manul',
                html: true,
                title: element.attr('name'), // Chat room name
                placement: 'right',
                content: function(){
                    return chatRoomDetail();
                }            
            // When user mouse enter, show the details
            }).on("mouseenter", function(){
                var _this = this;
                $(this).popover("show");
                $(this).siblings(".popover").on("mouseleave", function(){
                    $(_this).popover('hide');
                });
            // When user mouse leave, hide the details
            }).on("mouseleave", function(){ 
                var _this = this;
                setTimeout(function(){
                    if(!$(".popover:hover").length){
                        $(_this).popover("hide")
                    }
                }, 100);
            });
        }
    });
});  

// The chat room detail interface
function chatRoomDetail(){      
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
function userDetail(){      
    var data = $(
        "<div class='chat-room-details'>" + 
            "<ul>" + 
                "<li><span><img src='static/img/age.png'>Age</span></li>" +                
                "<li><span><img src='static/img/location.png'>North America</span></li>" +
                "<li><span><img src='static/img/university.png'>Rice University</span></li>" +
            "</ul>" +
        "</div>"
    );            
    return data;  
}  
// Exist the chat room  
function exit(){      
    alert('老铁别走啊');  
}