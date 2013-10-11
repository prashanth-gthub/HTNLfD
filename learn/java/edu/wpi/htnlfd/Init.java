package edu.wpi.htnlfd;

import java.util.*;
import edu.wpi.cetask.Task;
import edu.wpi.disco.*;
import edu.wpi.htnlfd.model.*;

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
      List<Task> DemonstratedTasks = demonstration.findDemonstration(disco,
            taskName);
      try{
      TaskModel taskmodel = demonstration.buildTaskModel(disco, taskName,
            DemonstratedTasks, "input1");
      DOM.writeDOM(fileName, taskmodel);
      demonstration.readDOM(disco, fileName);
      }
      catch(Exception e){
         e.printStackTrace();
      }

   }

}
