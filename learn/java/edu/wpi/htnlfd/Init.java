package edu.wpi.htnlfd;


import java.util.*;
import edu.wpi.cetask.*;
import edu.wpi.disco.*;
import edu.wpi.htnlfd.dto.TaskBlock;

public class Init {

   private static Demonstration demonstration = null;
   private static DomManipulation DOM = null;

   public static void main (String[] args) {
      System.out.println("YUHU");
   }

   public static void learn (Disco disco, String taskName) throws Exception {

      String separator = System.getProperty("file.separator");
      String filename = "SetTable1";

      if ( demonstration == null ) {
         demonstration = new Demonstration();
         DOM = new DomManipulation();
      }

      String fileName = System.getProperty("user.dir") + separator + filename
         + ".xml";
      List<Task> DemonstratedTasks = demonstration.findDemonstration(disco, taskName);
      
      List<TaskBlock> tasks = demonstration.build(disco, taskName, DemonstratedTasks, "input1");
      DOM.writeDOM(fileName, tasks);
      demonstration.readDOM(disco, fileName);

   }

}
