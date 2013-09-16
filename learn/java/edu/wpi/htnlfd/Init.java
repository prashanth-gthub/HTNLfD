package edu.wpi.htnlfd;

import java.io.IOException;
import java.util.*;
import edu.wpi.cetask.*;
import edu.wpi.disco.*;

public class Init {

   private static Demonstration demonstration = null;

   public static void main (String[] args) {
      System.out.println("YUHU");
   }

   public static void learn (Disco disco, String taskName) throws IOException {

      String separator = System.getProperty("file.separator");
      String filename = "SetTable1";

      if ( demonstration == null ) {
         demonstration = new Demonstration();
         demonstration.addDependentLibaries(System.getProperty("user.dir")
            + separator + "SetTableDemonstration.xml");
      }
      
      String fileName = System.getProperty("user.dir") + separator + filename
            + ".xml";
      List<Task> tasks = demonstration.findDemonstration(disco, taskName);
      demonstration.writeDOM(fileName, taskName, tasks, "isFancy");
      //demonstration.resetDisco(disco);
      demonstration.readDOM(disco, fileName);
   }
   
}
