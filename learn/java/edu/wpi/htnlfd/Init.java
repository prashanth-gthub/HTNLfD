package edu.wpi.htnlfd;

import java.io.PrintStream;
import java.util.*;
import javax.script.ScriptException;
import javax.xml.transform.*;
import edu.wpi.cetask.*;
import edu.wpi.disco.*;
import edu.wpi.htnlfd.model.DecompositionClass;
import edu.wpi.htnlfd.model.TaskClass;
import edu.wpi.htnlfd.model.TaskModel;
import edu.wpi.htnlfd.model.TaskClass.*;

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
         load(disco);
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
      load(disco);
   }

   /**
    * Makes one step to be optional.
    */
   public static void addOptionalStep (Disco disco, String taskName,
         String subtask, String stepName) throws Exception {

      learnedTaskmodel = demonstration.addOptionalStep(taskName, subtask,
            stepName);
      load(disco);
   }
   
   /**
    * Adds the alternative recipe.
    */
   public static void addAlternativeRecipe (Disco disco, String taskName,
         String inputName) throws Exception {

      learnedTaskmodel = demonstration.addAlternativeRecipe(disco, taskName, inputName);
      load(disco);
   }

   public static void addOrderStep (Disco disco, String taskName, String subtaskId,
         String stepNameDep, String stepNameRef) throws Exception {

      learnedTaskmodel = demonstration.addOrderStep(taskName, subtaskId, stepNameDep, stepNameRef);
      load(disco);
   }

   /**
    * Makes the steps of a subtask completely ordered
    */
   public static void setOrdered (Disco disco, String taskName, String subtaskId) throws Exception {
      learnedTaskmodel = demonstration.setOrdered(taskName, subtaskId);
      load(disco);
      
   }

   /**
    * Adds the applicable condition to a subtask.
    */
   public static void addApplicable (Disco disco, String taskName, String subtaskId,
         String condition) throws Exception {

      learnedTaskmodel = demonstration.addApplicable(taskName, subtaskId, condition);
      load(disco);
   }

   /**
    * Adds the precondition to a task.
    */
   public static void addPrecondition (Disco disco, String taskName, String precondition) throws Exception {
      learnedTaskmodel = demonstration.addPrecondition(taskName, precondition);
      load(disco);
      
   }

   /**
    * Adds the postcondition to a task.
    */
   public static void addPostcondition (Disco disco, String taskName, String postcondition) throws Exception {

      learnedTaskmodel = demonstration.addPostcondition(taskName, postcondition);
      load(disco);
   }

   /**
    * Adds the output to a task.
    */
   public static void addOutput (Disco disco, String taskName, String outputName,
         String outputType) throws Exception {
      learnedTaskmodel = demonstration.addOutput(taskName, outputName, outputType);
      load(disco);
      
   }

   /**
    * Adds the input to a task.
    */
   public static void addInput (Disco disco, String taskName, String inputName, String type,
         String modified) throws Exception {
      learnedTaskmodel = demonstration.addInput(taskName, inputName, type, modified);
      load(disco);
      
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
    * @throws Exception 
    */
   public static void load (Disco disco) throws Exception {
       //engine.load(learnedTaskmodel.toNode(), null);
      DOM.writeDOM(fileName, learnedTaskmodel);
      demonstration.readDOM(disco, fileName);
   }

}
