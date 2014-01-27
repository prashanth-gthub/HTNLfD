package edu.wpi.htnlfd.tireRotation;

import edu.wpi.htnlfd.domain.*;

public class Car extends PhysObj {
	public Car(String name, Location location) {
		super(name, location);
		reset();
	}
	
	public final Tire RFtire = new Tire("RFtire", Location.plus(this.getLocation(), new Location(
			10, 10, 0)));

	public final Tire LFtire = new Tire("LFtire", Location.plus(this.getLocation(), new Location(
			-10, -10, 0)));

	public final Tire RRtire = new Tire("RRtire", Location.plus(this.getLocation(), new Location(
			10, -10, 0)));

	public final Tire LRtire = new Tire("LRtire", Location.plus(this.getLocation(), new Location(
			-10, 10, 0)));

	public final Wheel RFwheel = new Wheel("RFwheel", Location.plus(this.getLocation(),
			new Location(10, 10, 0)), RFtire);

	public final Wheel LFwheel = new Wheel("LFwheel", Location.plus(this.getLocation(),
			new Location(-10, -10, 0)), LFtire);

	public final Wheel RRwheel = new Wheel("RRwheel", Location.plus(this.getLocation(),
			new Location(10, -10, 0)), RRtire);

	public final Wheel LRwheel = new Wheel("LRwheel", Location.plus(this.getLocation(),
			new Location(-10, 10, 0)), LRtire);

	public void reset() {
		RFtire.setLocation(Location.plus(this.getLocation(), new Location(
			10, 10, 0)));
		LFtire.setLocation(Location.plus(this.getLocation(), new Location(
				-10, -10, 0)));
		RRtire.setLocation(Location.plus(this.getLocation(), new Location(
				10, -10, 0)));
		LRtire.setLocation(Location.plus(this.getLocation(), new Location(
				-10, 10, 0)));
		
		RFwheel.reset();
		LFwheel.reset();
		RRwheel.reset();
		LRwheel.reset();		
	}
}