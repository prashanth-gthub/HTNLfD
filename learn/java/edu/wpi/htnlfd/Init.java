package edu.wpi.htnlfd;

import org.w3c.dom.Element;
import java.io.IOException;
import java.util.*;
import edu.wpi.cetask.*;
import edu.wpi.disco.*;

public class Init {

   private static Demonstration demonstration = null;

   public static void main (String[] args) {
      System.out.println("YUHU");
   }

   public static void learn (Disco disco, String taskName) throws Exception {

      String separator = System.getProperty("file.separator");
      String filename = "SetTable1";

      if ( demonstration == null ) {
         demonstration = new Demonstration();
      }

      String fileName = System.getProperty("user.dir") + separator + filename
         + ".xml";
      List<Task> tasks = demonstration.findDemonstration(disco, taskName);

      demonstration.writeDOM(disco,fileName, taskName, tasks, "input1");
      // demonstration.resetDisco(disco);
      //demonstration.partialOrderring(tasks, taskName);
      //demonstration.findParentsOfTasks();
      demonstration.readDOM(disco, fileName);

   }

}
