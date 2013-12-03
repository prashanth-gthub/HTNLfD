package edu.wpi.htnlfd.carMaintenance;

public class Fluid extends PhysObj {
   public Level level;
   public Fluid (String name, Location location, Level level) {
      super(name, location);
      this.level = level;
   }

   
   public static final PhysObj Oil = new Fluid("Oil", null, null);

   public static final PhysObj RadiatorFluid = new Fluid("RadiatorFluid", null, null);

}
