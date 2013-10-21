package edu.wpi.htnlfd;

import java.io.PrintStream;
import java.util.*;
import javax.xml.transform.*;
import edu.wpi.cetask.*;
import edu.wpi.disco.*;
import edu.wpi.htnlfd.model.TaskModel;

public class Init {

   /** The learned taskmodel. */
   private static TaskModel learnedTaskmodel = null;

   private static Demonstration demonstration = null;

   private static DomManipulation DOM = null;

   private static String filename = "SetTable1";

   private static String separator = System.getProperty("file.separator");

   private static String fileName = System.getProperty("user.dir") + separator
      + filename + ".xml";

   /**
    * The main method.(Never called)
    */
   public static void main (String[] args) {
      System.out.println("YUHU");
   }

   /**
    * Learns demonstrations.
    */
   public static void learn (Disco disco, String taskName) throws Exception {

      if ( demonstration == null ) {
         demonstration = new Demonstration();
         DOM = new DomManipulation();
      }

      List<Task> DemonstratedTasks = demonstration.findDemonstration(disco);
      try {
         learnedTaskmodel = demonstration.buildTaskModel(disco, taskName,
               DemonstratedTasks, "input1");
         DOM.writeDOM(fileName, learnedTaskmodel);
         demonstration.readDOM(disco, fileName);
      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   /**
    * Adds the steps to the specified task and subtask.
    */
   public static void addSteps (Disco disco, String taskName, String subtask)
         throws Exception {

      learnedTaskmodel = demonstration.addSteps(disco, taskName, subtask);
      DOM.writeDOM(fileName, learnedTaskmodel);
      demonstration.readDOM(disco, fileName);
   }

   /**
    * Makes one step to be optional.
    */
   public static void addOptionalStep (Disco disco, String taskName,
         String subtask, String stepName) throws Exception {

      learnedTaskmodel = demonstration.addOptionalStep(taskName, subtask,
            stepName);
      DOM.writeDOM(fileName, learnedTaskmodel);
      demonstration.readDOM(disco, fileName);
   }

   /**
    * Prints the learned taskmodel.
    */
   public static void print (PrintStream stream) throws TransformerException {
      DOM.writeDOM(stream, learnedTaskmodel);
   }

   public static void print () throws TransformerException {
      PrintStream stream = new PrintStream(System.out);
      print(stream);
   }

   /**
    * Load learned taskmodel into disco.
    */
   public void load (TaskEngine engine, Properties properties) {
      // engine.load(learnedTaskmodel.toNode(), null);
   }

}
