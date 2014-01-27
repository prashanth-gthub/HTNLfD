package edu.wpi.htnlfd.tireRotation;

import edu.wpi.htnlfd.domain.*;

public class Stud extends PhysObj {

	public Stud(String name, Location location) {
		super(name, location);
	}

	private Nut nut = new Nut(this.name + "_Nut", this.getLocation());

	public Nut getNut() {
		return nut;
	}

	public void setNut(Nut nut) {
		this.nut = nut;
	}
	public void reset(){
		nut = new Nut(this.name + "_Nut", this.getLocation());
	}
}