// function validateInput() {
//
//             /* Add validator function with each input */
//             $('#login-form').bootstrapValidator({
//                 message: 'This value is not vaid',
//                 feedbackIcons: {
//                     valid: 'glyphicon glyphicon-ok',
//                     invalid: 'glyphicon glyphicon-remove',
//                     validating: 'glyphicon glyphicon-refresh'
//                 },
//                 fields: {
//                     username: {
//                         message: 'Username is not valid',
//                         validators: {
//                             notEmpty: {
//                                 message: 'Username cannot be null'
//                             },
//                             regexp: {
//                                 regexp: /^[a-zA-Z0-9_]+$/,
//                                 message: 'Username must contain only lowercase or uppercase letters, numbers, and underscores'
//                             }
//                         }
//                     },
//                     age: {
//                         message: 'Age is not valid',
//                         validators: {
//                             notEmpty: {
//                                 message: 'Age cannot be null'
//                             },
//                             regexp: {
//                                 regexp: /^[0-9_]+$/,
//                                 message: 'Age must be numbers'
//                             },
//                             between: {
//                                 min: 1,
//                                 max: 140,
//                                 message: 'Invalid age'
//                             }
//                         }
//                     },
//                     region: {
//                         message: 'Region is not valid',
//                         validators: {
//                             notEmpty: {
//                                 message: 'Must select one region'
//                             },
//                             callback: { //Custom validate rules
//                                 message: 'Invalid Select',
//                                 callback: function(value, validator) {
//                                     var selectedRegion = $('#region-select option:selected').val();
//                                     if(selectedRegion == "Invalid") { return false; }
//                                     else { return true; }
//                                 }
//                             }
//                         }
//                     },
//                     school: {
//                         message: 'School is not valid',
//                         validators: {
//                             notEmpty: {
//                                 message: 'Must select one school'
//                             },
//                             callback: { //Custom validate rules
//                                 message: 'Selected school is not in selected region',
//                                 callback: function(value, validator) {
//                                     var thisSchoolRegionSymbol = value.split("-")[0];
//                                     var selectedRegion = $('#region-select option:selected').val();
//                                     if(thisSchoolRegionSymbol=="Invalid" || !selectedRegion.startsWith(thisSchoolRegionSymbol)) { return false; }
//                                     else { return true; }
//                                 }
//                             }
//                         }
//                     }
//                 }
//             })
//             .on('success.form.bv', function(e) {
//                 // Prevent submit form
//                 e.preventDefault();
//
//                 var $form     = $(e.target),
//                     validator = $form.data('bootstrapValidator');
//                 //$form.submit();
//                 //alert("Login success");
//
//                 var username = $('#login-form').find('input[name="username"]').val();
//                 var age = $('#login-form').find('input[name="age"]').val();
//                 var region = $('#login-form').find('select[name="region"]').val();
//                 var school = $('#login-form').find('select[name="school"]').val();
//
//                 var data = {
//                     type : "login",
//                     username : username,
//                     age : age,
//                     region : region,
//                     school : school
//                 }
//                 // console.log(data);
//                 sendMessage(data);
//                 /**
//                 当用户验证成功时
//                 设置login界面为hidden
//                 设置main界面为viewable
//                 **/
//                 console.log("success");
//                 var loginDiv = document.getElementById("login-space");
//                 var mainDiv = document.getElementById("whole-page");
//                 loginDiv.style.display = "none";
//                 mainDiv.style.display = "block";
//
//
//
//
//             });
//         }