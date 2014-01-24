package edu.wpi.htnlfd.table;

import edu.wpi.htnlfd.domain.Location;

public class SaladPlate extends Dish {

   public SaladPlate (String name, Location location) {
      super(name, location);
   }
   
   public static final SaladPlate SP1 = new SaladPlate("SP1", null);
 
}
