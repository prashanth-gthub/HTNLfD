package edu.wpi.htnlfd.tireRotation;

import edu.wpi.htnlfd.domain.*;

public class Wheel extends PhysObj {

   public Wheel (String name, Location location) {
      super(name, location);
   }
   
   public Wheel (String name, Location location, Tire tire) {
      super(name, location);
      this.tire = tire;
   }

   public Stud studA = new Stud(name + "_StudA", null);

   public Stud studB = new Stud(name + "_StudB", null);

   public Stud studC = new Stud(name + "_StudC", null);
   
   private Tire tire;

   public Tire getTire () {
      return tire;
   }

   public void setTire (Tire tire) {
      this.tire = tire;
   }
}
