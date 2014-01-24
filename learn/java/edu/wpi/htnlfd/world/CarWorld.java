package edu.wpi.htnlfd.world;

import edu.wpi.htnlfd.domain.*;
import edu.wpi.htnlfd.tireRotation.*;
import java.util.*;

public class CarWorld extends World{

   public CarWorld(){
      
   }
   public static List<Nut> LOOSE_NUTS = new ArrayList<Nut>();
   public static final Car MyCar = new Car("MyCar", new Location(0, 0, 0));
   
   static{
	   LOOSE_NUTS.add(new Nut("NutTemp",null));
   }
   
}
