package edu.rice.comp504.model.res;

/**
 * Message covers the information that a user is created.
 */
public class NewUserResponse extends AResponse {

    private int userId;
    private String userName;

    /**
     * Constructor.
     * @param userId the id of the created user
     * @param userName the name of the created user
     */
    public NewUserResponse(int userId, String userName) {
        super("NewUser");
        this.userId = userId;
        this.userName = userName;
    }
}
