package edu.wpi.htnlfd.world;

import edu.wpi.htnlfd.tireRotation.Nut;

import java.util.*;

public class CarWorld extends World{

   public static List<Nut> LOOSE_NUTS = new ArrayList<Nut>();
   static{
	   LOOSE_NUTS.add(new Nut("NutTemp",null));
   }
   
}
