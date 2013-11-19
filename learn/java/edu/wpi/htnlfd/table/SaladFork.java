package edu.wpi.htnlfd.table;

public class SaladFork extends Fork {

   public SaladFork (String name, Location location) {
      super(name, location);
   }
        
   // we will need more than one fork
   
   public static final SaladFork
     F1 = new SaladFork("F1", null),
     F2 = new SaladFork("F2", null);
}
