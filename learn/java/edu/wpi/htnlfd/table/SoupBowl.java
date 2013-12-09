package edu.wpi.htnlfd.table;

import edu.wpi.htnlfd.domain.Location;

public class SoupBowl extends Dish {

   public SoupBowl (String name, Location location) {
      super(name, location);
   }
   
   public static final SoupBowl SB1 = new SoupBowl("SB1", null);
 
}
