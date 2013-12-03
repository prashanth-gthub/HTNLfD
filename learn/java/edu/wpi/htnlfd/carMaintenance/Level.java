package edu.wpi.htnlfd.carMaintenance;

import edu.wpi.htnlfd.ApplicationSpecificClass;

public class Level extends ApplicationSpecificClass {

   public final int level;

   public Level (int level) {
      super();
      this.level = level;
   }

   @Override
   public String toString () {
      return level + "";
   }

   @Override
   public int hashCode () {
      final int prime = 31;
      int result = 1;
      result = prime * result + level;
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
      Level other = (Level) obj;
      if ( level != other.level )
         return false;
      return true;
   }

   @Override
   public String find () {
      return "new Level(" + level + ")";
   }
   
   public static Level minus (Level l1, Level l2) {
      return new Level(l1.level-l2.level);
   }

}
