package edu.rice.comp504.model.obj;

import java.util.Arrays;

/**The ChatRoom class defines a chat room restriction info and private fields of a chat room.
 *
 */
public class RoomRestrictionInfo {

    //room name of the restriction
    String name;
    //age range of the restriction
    String ageRange;
    //locations of the restriction
    String locations;
    //schools of the restriction
    String schools;

    /**The constructor.
     *
     */
    public RoomRestrictionInfo(String name, int ageLB, int ageUB, String[] locations, String[] schools) {
        this.name = name;
        this.ageRange = ageLB + " - " + ageUB;
        this.locations = Arrays.toString(locations);
        this.schools = Arrays.toString(schools);
    }
}
