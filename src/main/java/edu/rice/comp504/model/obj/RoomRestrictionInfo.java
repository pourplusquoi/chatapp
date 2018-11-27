package edu.rice.comp504.model.obj;

import java.util.Arrays;

public class RoomRestrictionInfo {
        String name;
        String ageRange;
        String locations;
        String schools;
        public RoomRestrictionInfo(String name, int ageLB, int ageUB, String[] locations, String[] schools){
            this.name = name;
            this.ageRange = ageLB +" - " + ageUB;
            this.locations = Arrays.toString(locations);
            this.schools = Arrays.toString(schools);
        }
}
