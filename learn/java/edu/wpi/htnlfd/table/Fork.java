package edu.wpi.htnlfd.table;

import edu.wpi.htnlfd.domain.Location;

public class Fork extends Silverware {

   public Fork (String name, Location location) {
      super(name, location);
   }
 
   public static final Fork F1 = new Fork("F1", null);
   public static final Fork F2 = new Fork("F2", null);
   public static final Fork F3 = new Fork("F3", null);
}
