package edu.wpi.htnlfd.table;

import edu.wpi.htnlfd.ApplicationSpecificClass;

// NB: PhysObj must cloneable because it is used in modified inputs

public class PhysObj extends ApplicationSpecificClass implements Cloneable {

   public static PhysObj TABLE = new PhysObj("TABLE", null);
   
   // fields public for convenience in JavaScript
   public final String name;
   public Location location;
 
   @Override
   public Object clone () { 
      // default clone method will copy the two fields above
      try { return super.clone(); }
      catch (CloneNotSupportedException e) { return null; } // cannot happen 
   }
   
   public PhysObj (String name, Location location) {
      if ( name == null )
         throw new IllegalArgumentException("PhysObj must have name");
      this.name = name;
      this.location = location == null ? new Location(0,0,0) : location;
   }

   @Override
   public String find () {
      // assumes importPackage("Packages.edu.wpi.htnlfd.table")
      return getClass().getSimpleName()+'.'+name;
   }

   @Override
   public String toString () { return find(); }

   /** 
    * Two physical objects are equal iff they have the same names
    */
   @Override
   public boolean equals (Object obj) {
      if ( this == obj )
         return true;
      if ( obj == null )
         return false;
      if ( getClass() != obj.getClass() )
         return false;
      PhysObj other = (PhysObj) obj;
      if ( name == null ) {
         if ( other.name != null )
            return false;
      } else if ( !name.equals(other.name) )
         return false;
      return true;
   }
   
   @Override
   public int hashCode () {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      return result;
   }

}
