package edu.wpi.htnlfd.tireRotation;

import edu.wpi.htnlfd.domain.*;

public class Car extends PhysObj {
	public Car(String name, Location location) {
		super(name, location);
		reset();
	}

	public Tire RFtire;

	public Tire LFtire;

	public Tire RRtire;

	public Tire LRtire;

	public Wheel RFwheel;

	public Wheel LFwheel;

	public Wheel RRwheel;

	public Wheel LRwheel;

	public void reset() {
		RFtire = new Tire("RFtire", Location.plus(this.location, new Location(
				10, 10, 0)));

		LFtire = new Tire("LFtire", Location.plus(this.location, new Location(
				-10, -10, 0)));

		RRtire = new Tire("RRtire", Location.plus(this.location, new Location(
				10, -10, 0)));

		LRtire = new Tire("LRtire", Location.plus(this.location, new Location(
				-10, 10, 0)));

		RFwheel = new Wheel("RFwheel", Location.plus(this.location,
				new Location(10, 10, 0)), RFtire);

		LFwheel = new Wheel("LFwheel", Location.plus(this.location,
				new Location(-10, -10, 0)), LFtire);

		RRwheel = new Wheel("RRwheel", Location.plus(this.location,
				new Location(10, -10, 0)), RRtire);

		LRwheel = new Wheel("LRwheel", Location.plus(this.location,
				new Location(-10, 10, 0)), LRtire);
	}
}