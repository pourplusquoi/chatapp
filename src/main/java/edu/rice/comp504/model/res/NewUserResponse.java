package edu.rice.comp504.model.res;

/**
 * Message covers the information that a user is created
 */
public class NewUserResponse extends AResponse {

    private int userId;         // The id of the created user
    private String userName;    // The name of the created user

    /**
     * Constructor.
     */
    public NewUserResponse(int userId, String userName) {
        super("NewUser");
        this.userId = userId;
        this.userName = userName;
    }
}
