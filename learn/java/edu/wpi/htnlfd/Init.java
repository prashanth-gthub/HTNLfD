package edu.wpi.htnlfd;

import java.util.*;
import edu.wpi.cetask.*;
import edu.wpi.disco.*;

public class Init {

   public static void main (String[] args) {
      System.out.println("YUHU");
   }

   public static void demonstration (Disco disco, String taskName) {
      Demonstration demonstration = new Demonstration();
      String separator = System.getProperty("file.separator");
      String fileName = System.getProperty("user.dir") + separator + taskName
         + ".xml";
      Vector<Task> tasks = demonstration.findDemonstration(disco, taskName);
      demonstration.writeDOM(tasks, taskName, fileName);
      demonstration.readDOM(disco, fileName);
   }
}
