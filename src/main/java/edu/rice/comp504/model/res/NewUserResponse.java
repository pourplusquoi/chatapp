package edu.rice.comp504.model.res;

/**
 * Message covers the information that a user is created.
 */
public class NewUserResponse extends AResponse {

    private int userId;
    private String userName;
    private int age;
    private String region;
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
    }
}
