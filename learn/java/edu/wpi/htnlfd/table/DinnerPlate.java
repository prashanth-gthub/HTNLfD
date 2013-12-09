package edu.wpi.htnlfd.table;

import edu.wpi.htnlfd.Location;

public class DinnerPlate extends Dish {

   public DinnerPlate (String name, Location location) {
      super(name, location);
   }
 
   public static final DinnerPlate DP1 = new DinnerPlate("DP1", null);
}
