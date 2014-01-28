package edu.wpi.htnlfd.world;

import edu.wpi.htnlfd.domain.*;
import edu.wpi.htnlfd.tireRotation.*;
import java.io.PrintStream;
import java.util.*;

public class CarWorld extends World {

   public CarWorld () {

   }

   public final List<Nut> LOOSE_NUTS = new ArrayList<Nut>();

   public final Car MyCar = new Car("MyCar", new Location(0, 0, 0));
 
   public void reset () {
      MyCar.reset();
   }
   

   public void print (PrintStream stream) {
      stream = System.out;
      stream.append("LOOSE_NUTS:");
      for(Nut nut:LOOSE_NUTS){
         stream.append(nut.name+",");
      }      
      if(LOOSE_NUTS.isEmpty())
         stream.append("Empty");

      stream.append("\n");
      MyCar.print(stream);
   }

}
