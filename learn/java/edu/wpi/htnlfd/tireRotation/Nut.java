package edu.wpi.htnlfd.tireRotation;

import java.io.PrintStream;
import edu.wpi.htnlfd.domain.*;

public class Nut extends PhysObj {

   public Nut (String name, Location location) {
      super(name, location);
   }

   public void print (PrintStream stream, String indent) {
      stream.append(this.name + this.getLocation() + "\n");
   }
}