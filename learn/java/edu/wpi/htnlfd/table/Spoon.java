package edu.wpi.htnlfd.table;

import edu.wpi.htnlfd.Location;

public class Spoon extends Silverware {

   public Spoon (String name, Location location) {
      super(name, location);
   }
 
   public static final Spoon S1 = new Spoon("S1", null);
}
