package edu.wpi.htnlfd.tireRotation;

import java.util.*;
import edu.wpi.htnlfd.domain.*;

public class Wheel extends PhysObj {

   public Wheel (String name, Location location) {
      super(name, location);
      
      studs.add(new Stud(name+"_Stud1",null));
      studs.add(new Stud(name+"_Stud2",null));
      studs.add(new Stud(name+"_Stud3",null));
   }

   public List<Stud> studs = new ArrayList<Stud>();

	public List<Stud> getStuds() {
		return studs;
	}
	   
}
