package edu.wpi.htnlfd;

import java.io.PrintStream;
import java.util.*;
import javax.xml.transform.*;

import edu.wpi.cetask.*;
import edu.wpi.disco.*;

import edu.wpi.htnlfd.model.TaskModel;

public class Init {

   private static TaskModel learnedTaskmodel = null;
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
         learnedTaskmodel = demonstration.buildTaskModel(disco, taskName,
            DemonstratedTasks, "input1");
      DOM.writeDOM(fileName, learnedTaskmodel);
      demonstration.readDOM(disco, fileName);
      }
      catch(Exception e){
         e.printStackTrace();
      }

   }
   
   public void print (PrintStream stream) throws TransformerException {
      DOM.writeDOM (stream, learnedTaskmodel);
   }
   
   public void load (TaskEngine engine, Properties properties) { 
      //engine.load(learnedTaskmodel.toNode(), null); 
   }


}
