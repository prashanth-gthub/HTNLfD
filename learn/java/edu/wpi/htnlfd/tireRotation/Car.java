package edu.wpi.htnlfd.tireRotation;

import edu.wpi.htnlfd.domain.*;

public class Car extends PhysObj {
   public Car (String name, Location location) {
      super(name, location);
   }

   public static final PhysObj Car = new Car("MyCar", new Location(0, 0, 0));


   public static Tire RFTire = new Tire("RFTire", Location.plus(Car.location, new Location(10,
         10, 0)));

   public static Tire LFTire = new Tire("LFTire", Location.plus(Car.location, new Location(
         -10, -10, 0)));

   public static Tire RRTire = new Tire("RRTire", Location.plus(Car.location, new Location(10,
         -10, 0)));

   public static Tire LRTire = new Tire("LRTire", Location.plus(Car.location, new Location(
         -10, 10, 0)));
   
   
   public static Wheel RFWheel = new Wheel("RFWheel", Location.plus(Car.location, new Location(10,
         10, 0)));

   public static Wheel LFWheel = new Wheel("LFWheel", Location.plus(Car.location, new Location(
         -10, -10, 0)));

   public static Wheel RRWheel = new Wheel("RRWheel", Location.plus(Car.location, new Location(10,
         -10, 0)));

   public static Wheel LRWheel = new Wheel("LRWheel", Location.plus(Car.location, new Location(
         -10, 10, 0)));

}
