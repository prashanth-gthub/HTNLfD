package edu.wpi.htnlfd.table;

import edu.wpi.htnlfd.domain.Location;

public class WaterGlass extends Glass {

	public WaterGlass(String name, Location location) {
		super(name, location);
	}

	public static final WaterGlass WaG1 = new WaterGlass("WaG1", null);
}
