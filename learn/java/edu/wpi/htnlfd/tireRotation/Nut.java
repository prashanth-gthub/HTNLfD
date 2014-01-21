package edu.wpi.htnlfd.tireRotation;

import edu.wpi.htnlfd.domain.*;

public class Nut extends PhysObj {

   public Nut (String name, Location location) {
      super(name, location);
   }
   
   public static Nut Nut1 = new Nut("Nut1",null);
}