package edu.wpi.htnlfd.table;

import edu.wpi.htnlfd.domain.*;

public class Cover extends PhysObj{
   public Cover (String name, Location location) {
      super(name, location);
   }
 
   public static final Cover C1 = new Cover("C1", null);
}
