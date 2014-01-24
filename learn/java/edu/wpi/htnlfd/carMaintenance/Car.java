package edu.wpi.htnlfd.carMaintenance;

public class Car extends PhysObj {
   public Car (String name, Location location) {
      super(name, location);
   }

   public static final PhysObj Car = new Car("MyCar", new Location(0, 0, 0));

   public PhysObj OC = new OilContainer("OilContainer", Location.plus(
         Car.location, new Location(5, 5, 0)));

   public PhysObj WWC = new WindshieldWiperContainer(
         "WindshieldWiperContainer", Location.plus(Car.location, new Location(
               4, 4, 0)));

   public PhysObj R = new Radiator("Radiator", Location.plus(Car.location,
         new Location(3, 3, 0)));

   public Tire T1 = new Tire("T1", Location.plus(Car.location, new Location(10,
         10, 0)));

   public Tire T2 = new Tire("T2", Location.plus(Car.location, new Location(
         -10, -10, 0)));

   public Tire T3 = new Tire("T3", Location.plus(Car.location, new Location(10,
         -10, 0)));

   public Tire T4 = new Tire("T4", Location.plus(Car.location, new Location(
         -10, 10, 0)));

}
