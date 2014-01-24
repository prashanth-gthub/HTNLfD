package edu.wpi.htnlfd.tireRotation;

import edu.wpi.htnlfd.domain.*;

public class Car extends PhysObj {
   public Car (String name, Location location) {
      super(name, location);
   }

   public Tire RFTire = new Tire("RFTire", Location.plus(this.location, new Location(10,
         10, 0)));

   public Tire LFTire = new Tire("LFTire", Location.plus(this.location, new Location(
         -10, -10, 0)));

   public Tire RRTire = new Tire("RRTire", Location.plus(this.location, new Location(10,
         -10, 0)));

   public Tire LRTire = new Tire("LRTire", Location.plus(this.location, new Location(
         -10, 10, 0)));
   
   
   public Wheel RFWheel = new Wheel("RFWheel", Location.plus(this.location, new Location(10,
         10, 0)));

   public Wheel LFWheel = new Wheel("LFWheel", Location.plus(this.location, new Location(
         -10, -10, 0)));

   public Wheel RRWheel = new Wheel("RRWheel", Location.plus(this.location, new Location(10,
         -10, 0)));

   public Wheel LRWheel = new Wheel("LRWheel", Location.plus(this.location, new Location(
         -10, 10, 0)));

   public Tire getRFTire () {
      return RFTire;
   }

   public Tire getLFTire () {
      return LFTire;
   }

   public Tire getRRTire () {
      return RRTire;
   }

   public Tire getLRTire () {
      return LRTire;
   }

   public Wheel getRFWheel () {
      return RFWheel;
   }

   public Wheel getLFWheel () {
      return LFWheel;
   }

   public Wheel getRRWheel () {
      return RRWheel;
   }

   public Wheel getLRWheel () {
      return LRWheel;
   }
   
   
}
