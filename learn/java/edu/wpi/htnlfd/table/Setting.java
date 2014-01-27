package edu.wpi.htnlfd.table;

import edu.wpi.htnlfd.domain.*;

public final class Setting extends PhysObj {

	public final DinnerPlate plate;
	public final Fork fork;
	public final Knife knife;

	public Setting(DinnerPlate plate, Fork fork, Knife knife) {
		super("Setting1", new Location(0, 0, 0));
		this.plate = plate;
		this.fork = fork;
		this.knife = knife;
	}

}
