package edu.wpi.htnlfd.tireRotation;

import edu.wpi.htnlfd.domain.*;

public class Car extends PhysObj {
   public Car (String name, Location location) {
      super(name, location);
   }

   public static final PhysObj Car = new Car("MyCar", new Location(0, 0, 0));


   public static Tire T1 = new Tire("T1", Location.plus(Car.location, new Location(10,
         10, 0)));

   public static Tire T2 = new Tire("T2", Location.plus(Car.location, new Location(
         -10, -10, 0)));

   public static Tire T3 = new Tire("T3", Location.plus(Car.location, new Location(10,
         -10, 0)));

   public static Tire T4 = new Tire("T4", Location.plus(Car.location, new Location(
         -10, 10, 0)));
   
   public static Location LF = new Location(0,0,0);
   public static Location LR = new Location(0,10,0);
   public static Location RF = new Location(10,0,0);
   public static Location RR = new Location(10,10,0);
   

}
