package edu.wpi.htnlfd.tireRotation;

import java.io.PrintStream;

import edu.wpi.htnlfd.domain.*;

public class Wheel extends PhysObj {

	public Wheel(String name, Location location) {
		super(name, location);
	}

	public Wheel(String name, Location location, Tire tire) {
		super(name, location);
		this.tire = tire;
	}

	public final Stud studA = new Stud(name + "_StudA", Location.plus(
			this.getLocation(), new Location(0, 0, 1)));

	public final Stud studB = new Stud(name + "_StudB", Location.plus(
			this.getLocation(), new Location(0, 0, -1)));

	public final Stud studC = new Stud(name + "_StudC", Location.plus(
			this.getLocation(), new Location(0, 0, 2)));

	private Tire tire;

	public Tire getTire() {
		return tire;
	}

	public void setTire(Tire tire) {
		this.tire = tire;
	}

	public void reset() {
		studA.reset();
		studB.reset();
		studC.reset();
	}

	public void print(PrintStream stream, String indent) {
		indent = "  " + indent;
		stream.append(this.name + this.getLocation() + "\n");
		stream.append(indent + ".studA = ");
		studA.print(stream, indent);
		stream.append(indent + ".studB = ");
		studB.print(stream, indent);
		stream.append(indent + ".studC = ");
		studC.print(stream, indent);

		if (getTire() != null) {
			stream.append(indent + "getTire() = ");
			getTire().print(stream, indent);
		}
	}

	public String find() {
		return "$world.MyCar."+this.name;
	}
}
