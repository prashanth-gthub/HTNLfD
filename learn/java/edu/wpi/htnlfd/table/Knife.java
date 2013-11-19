package edu.wpi.htnlfd.table;

public class Knife extends Silverware {

   public Knife (String name, Location location) {
      super(name, location);
   }
 
   public static final Knife K1 = new Knife("K1", null);
}
