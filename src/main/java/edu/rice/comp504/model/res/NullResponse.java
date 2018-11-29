package edu.rice.comp504.model.res;

/**
 * Message covers no information (a default message).
 */
public class NullResponse extends AResponse {

    //message of null response
    private String message;

    /**
     * Constructor.
     */
    public NullResponse(String message) {
        super("Null");
        this.message = message;
    }
}
