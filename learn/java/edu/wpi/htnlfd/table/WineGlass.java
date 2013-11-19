package edu.wpi.htnlfd.table;

public class WineGlass extends Glasses {

   public WineGlass (String name, Location location) {
      super(name, location);
   }
 
   public static WineGlass WG1 = new WineGlass("WG1", null);
}
