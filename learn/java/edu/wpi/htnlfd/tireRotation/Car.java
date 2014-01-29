package edu.wpi.htnlfd.tireRotation;

import java.io.PrintStream;

import edu.wpi.htnlfd.domain.*;

public class Car extends PhysObj {
	public Car(String name, Location location) {
		super(name, location);
		reset();
	}

	public final Tire RFtire = new Tire("RFtire", null);

	public final Tire LFtire = new Tire("LFtire", null);

	public final Tire RRtire = new Tire("RRtire", null);

	public final Tire LRtire = new Tire("LRtire", null);

	public final Wheel RFwheel = new Wheel("RFwheel", null, RFtire);

	public final Wheel LFwheel = new Wheel("LFwheel", null, LFtire);

	public final Wheel RRwheel = new Wheel("RRwheel", null, RRtire);

	public final Wheel LRwheel = new Wheel("LRwheel", null, LRtire);

	public void reset() {
		RFtire.setLocation(Location.plus(this.getLocation(), new Location(10,
				10, 0)));
		LFtire.setLocation(Location.plus(this.getLocation(), new Location(-10,
				-10, 0)));
		RRtire.setLocation(Location.plus(this.getLocation(), new Location(10,
				-10, 0)));
		LRtire.setLocation(Location.plus(this.getLocation(), new Location(-10,
				10, 0)));

		RFwheel.reset();
		LFwheel.reset();
		RRwheel.reset();
		LRwheel.reset();
	}

	public void print(PrintStream stream, String indent) {
		indent = "  " + indent;
		stream.append("." + this.name + this.getLocation() + "\n");
		stream.append(indent + ".RFwheel = ");
		RFwheel.print(stream, indent);
		stream.append(indent + ".LFwheel = ");
		LFwheel.print(stream, indent);
		stream.append(indent + ".RRwheel = ");
		RRwheel.print(stream, indent);
		stream.append(indent + ".LRwheel = ");
		LRwheel.print(stream, indent);
	}

	public String find() {
		return "$world.MyCar";
	}
}