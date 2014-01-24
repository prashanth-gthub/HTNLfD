package edu.wpi.htnlfd.tireRotation;

import edu.wpi.htnlfd.domain.*;

public class Wheel extends PhysObj {

   public Wheel (String name, Location location) {
      super(name, location);
   }

   public Stud stud1 = new Stud(name + "_Stud1", null);

   public Stud stud2 = new Stud(name + "_Stud2", null);

   public Stud stud3 = new Stud(name + "_Stud3", null);

   public Stud getStud1 () {
      return stud1;
   }

   public Stud getStud2 () {
      return stud2;
   }

   public Stud getStud3 () {
      return stud3;
   }

}
