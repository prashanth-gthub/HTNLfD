package edu.wpi.htnlfd.table;

public class Silverware extends PhysObj{

   public Silverware (String name, Location location) {
      super(name, location);
   }
   
   public Silverware clone(){
      return new Silverware(this.getName(),new Location(this.getLocation().x,this.getLocation().y,this.getLocation().z));    
   }

}
