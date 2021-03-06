package edu.wpi.htnlfd.carMaintenance;

import edu.wpi.htnlfd.ApplicationSpecificClass;

public class Location extends ApplicationSpecificClass {

   public final int x, y, z;

   public Location (int x, int y, int z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public static Location plus (Location loc1, Location loc2) {
      return new Location(loc1.x + loc2.x, loc1.y + loc2.y, loc1.z + loc2.z);
   }

   public String find () {
      // assumes importPackage("Packages.edu.wpi.htnlfd.table")
      return "new Location(" + x + "," + y + "," + z + ")";
   }

   @Override
   public String toString () {
      return "[" + x + "," + y + "," + z + "]";
   }

   @Override
   public boolean equals (Object obj) {
      if ( this == obj )
         return true;
      if ( obj == null )
         return false;
      if ( getClass() != obj.getClass() )
         return false;
      Location other = (Location) obj;
      if ( x != other.x )
         return false;
      if ( y != other.y )
         return false;
      if ( z != other.z )
         return false;
      return true;
   }

   @Override
   public int hashCode () {
      final int prime = 31;
      int result = 1;
      result = prime * result + x;
      result = prime * result + y;
      result = prime * result + z;
      return result;
   }

}
