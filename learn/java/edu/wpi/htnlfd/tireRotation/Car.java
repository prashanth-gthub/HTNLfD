package edu.wpi.htnlfd.tireRotation;

import edu.wpi.htnlfd.domain.*;

public class Car extends PhysObj {
	public Car(String name, Location location) {
		super(name, location);
	}

	public Tire RFtire = new Tire("RFtire", Location.plus(this.location,
			new Location(10, 10, 0)));

	public Tire LFtire = new Tire("LFtire", Location.plus(this.location,
			new Location(-10, -10, 0)));

	public Tire RRtire = new Tire("RRtire", Location.plus(this.location,
			new Location(10, -10, 0)));

	public Tire LRtire = new Tire("LRtire", Location.plus(this.location,
			new Location(-10, 10, 0)));

	public Wheel RFwheel = new Wheel("RFwheel", Location.plus(this.location,
			new Location(10, 10, 0)));

	public Wheel LFwheel = new Wheel("LFwheel", Location.plus(this.location,
			new Location(-10, -10, 0)));

	public Wheel RRwheel = new Wheel("RRwheel", Location.plus(this.location,
			new Location(10, -10, 0)));

	public Wheel LRwheel = new Wheel("LRwheel", Location.plus(this.location,
			new Location(-10, 10, 0)));

	}
