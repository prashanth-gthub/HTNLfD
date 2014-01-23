package edu.wpi.htnlfd.tireRotation;

import java.util.*;
import edu.wpi.htnlfd.domain.*;

public class Wheel extends PhysObj {

   public Wheel (String name, Location location) {
      super(name, location);
      
      studs.add(new Stud("Stud1",null));
      studs.add(new Stud("Stud2",null));
      studs.add(new Stud("Stud3",null));
   }

   public List<Stud> studs = new ArrayList<Stud>();
   
}
