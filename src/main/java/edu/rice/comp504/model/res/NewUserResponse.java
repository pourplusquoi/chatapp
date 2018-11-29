package edu.rice.comp504.model.res;

/**
 * Message covers the information that a user is created.
 */
public class NewUserResponse extends AResponse {

    //userId
    private int userId;

    //username
    private String userName;

    //age
    private int age;

    //region
    private String region;

    //school
    private String school;

    /**
     * Constructor.
     * @param userId the id of the created user
     * @param userName the name of the created user
     */
    public NewUserResponse(int userId, String userName, int age, String region, String school) {
        super("NewUser");
        this.userId = userId;
        this.userName = userName;
        this.age = age;
        this.region = region;
        this.school = school;
        System.out.println(this.userId + " " + this.userName + " " + this.age + " " + this.region + " " + this.school);
    }
}
