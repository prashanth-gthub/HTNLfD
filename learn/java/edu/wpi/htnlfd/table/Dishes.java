package edu.wpi.htnlfd.table;

public class Dishes extends PhysObj{

   public Dishes (String name, Location location) {
      super(name, location);
   }
   
   public Dishes clone(){
      return new Dishes(this.getName(),new Location(this.getLocation().x,this.getLocation().y,this.getLocation().z));         
   }
   
   

}
