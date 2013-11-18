package edu.wpi.htnlfd.table;

public class Glasses extends PhysObj {

   public Glasses (String name, Location location) {
      super(name, location);
   }

   public Glasses clone () {
      return new Glasses(this.getName(), new Location(this.getLocation().x,
            this.getLocation().y, this.getLocation().z));
   }

}
