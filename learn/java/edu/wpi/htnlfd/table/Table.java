package edu.wpi.htnlfd.table;

import edu.wpi.htnlfd.*;
import edu.wpi.htnlfd.domain.*;

public class Table extends PhysObj {

	public Table(String name, Location location) {
		super(name, location);
	}

	public static final Table T1 = new Table("T1", null);
}
