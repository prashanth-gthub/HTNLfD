package edu.wpi.htnlfd.table;

import edu.wpi.htnlfd.domain.Location;

public class Fork extends Silverware {

   public Fork (String name, Location location) {
      super(name, location);
   }
 
   public static final Fork F1 = new Fork("F1", null);
}
