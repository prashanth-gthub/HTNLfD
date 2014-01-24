package edu.wpi.htnlfd.tireRotation;

import edu.wpi.htnlfd.domain.*;

public class Wheel extends PhysObj {

	public Wheel(String name, Location location) {
		super(name, location);
	}

	private Stud studA = new Stud(name + "_StudA", null);

	private Stud studB = new Stud(name + "_StudB", null);

	private Stud studC = new Stud(name + "_StudC", null);

	public Stud getStudA() {
		return studA;
	}

	public Stud getStudB() {
		return studB;
	}

	public Stud getStudC() {
		return studC;
	}

}
