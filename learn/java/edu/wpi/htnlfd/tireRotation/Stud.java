package edu.wpi.htnlfd.tireRotation;

import edu.wpi.htnlfd.domain.*;

public class Stud extends PhysObj {

   public Stud (String name, Location location) {
      super(name, location);
      nut = new Nut(this.name+"_Nut",null);
   }
   
   private Nut nut;

	public Nut getNut() {
		return nut;
	}
	
	public void setNut(Nut nut) {
		this.nut = nut;
	}
   
}