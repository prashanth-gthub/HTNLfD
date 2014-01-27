package edu.wpi.htnlfd.tireRotation;

import edu.wpi.htnlfd.domain.*;

public class Wheel extends PhysObj {

	public Wheel(String name, Location location) {
		super(name, location);
	}

	public Wheel(String name, Location location, Tire tire) {
		super(name, location);
		this.tire = tire;
	}

	public final Stud studA = new Stud(name + "_StudA", Location.plus(this.getLocation(), new Location(0,0,1)));

	public final Stud studB = new Stud(name + "_StudB", Location.plus(this.getLocation(), new Location(0,0,-1)));

	public final Stud studC = new Stud(name + "_StudC", Location.plus(this.getLocation(), new Location(0,0,2)));

	private Tire tire;

	public Tire getTire() {
		return tire;
	}

	public void setTire(Tire tire) {
		this.tire = tire;
	}
	
	public void reset(){
		studA.reset();
		studB.reset();
		studC.reset();
	}
}
