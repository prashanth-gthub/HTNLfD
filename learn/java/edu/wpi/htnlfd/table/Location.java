package edu.wpi.htnlfd.table;

import edu.wpi.htnlfd.ApplicationSpecificClass;

public class Location extends ApplicationSpecificClass{

   public final int x;
   public final int y;
   public final int z;
   public Location (int x, int y, int z) {
      super();
      this.x = x;
      this.y = y;
      this.z = z;
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
   public String toString () {
      return "[" + x + "," + y + "," + z + "]";
   }
   public String find () {
      return "location("+x+","+y+","+z+")";
   }
   
   
}
