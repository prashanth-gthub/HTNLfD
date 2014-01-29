package edu.wpi.htnlfd.tireRotation;

import java.io.PrintStream;

import edu.wpi.htnlfd.domain.*;

public class Nut extends PhysObj {

	public Nut(String name, Location location) {
		super(name, location);
	}

	public void print(PrintStream stream, String indent) {
		stream.append(this.name + this.getLocation() + "\n");
	}
	public String find() {
		String[] wheelName = this.name.split("_");
		return "$world.MyCar." + wheelName[0] + ".getNut()";
	}
}