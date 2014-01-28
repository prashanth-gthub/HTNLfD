package edu.wpi.htnlfd.tireRotation;

import java.io.PrintStream;
import edu.wpi.htnlfd.domain.*;

public class Stud extends PhysObj {

   public Stud (String name, Location location) {
      super(name, location);
   }

   private Nut nut = new Nut(this.name + "_Nut", this.getLocation());

   public Nut getNut () {
      return nut;
   }

   public void setNut (Nut nut) {
      this.nut = nut;
   }

   public void reset () {
      nut = new Nut(this.name + "_Nut", this.getLocation());
   }

   public void print (PrintStream stream, String indent) {
      indent = "\t" + indent;
      stream.append(this.name + this.getLocation().toString() + "\n");
      stream.append(indent + "getNut() = ");
      nut.print(stream, indent);
   }
}