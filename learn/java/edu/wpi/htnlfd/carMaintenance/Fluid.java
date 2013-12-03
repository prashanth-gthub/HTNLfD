package edu.wpi.htnlfd.carMaintenance;

public class Fluid extends PhysObj {
   public Fluid (String name, Location location) {
      super(name, location);
   }

   public static final PhysObj Oil = new Fluid("Oil", null);

   public static final PhysObj RadiatorFluid = new Fluid("RadiatorFluid", null);

}
