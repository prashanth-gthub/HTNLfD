package edu.wpi.htnlfd.tireRotation;

import edu.wpi.htnlfd.domain.*;

public class Wheel extends PhysObj {

	public Wheel(String name, Location location) {
		super(name, location);
	}

	public Stud studA = new Stud(name + "_StudA", null);

	public Stud studB = new Stud(name + "_StudB", null);

	public Stud studC = new Stud(name + "_StudC", null);

}
