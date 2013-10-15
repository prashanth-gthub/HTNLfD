package edu.wpi.htnlfd;

import java.io.PrintStream;
import java.util.*;
import javax.xml.transform.*;
import edu.wpi.cetask.*;
import edu.wpi.disco.*;
import edu.wpi.htnlfd.model.TaskModel;

// TODO: Auto-generated Javadoc
/**
 * The Class Init.
 */
public class Init {

   /** The learned taskmodel. */
   private static TaskModel learnedTaskmodel = null;

   /** The demonstration. */
   private static Demonstration demonstration = null;

   /** The dom. */
   private static DomManipulation DOM = null;

   /**
    * The main method.
    *
    * @param args the arguments
    */
   public static void main (String[] args) {
      System.out.println("YUHU");
   }

   /**
    * Learn.
    *
    * @param disco the disco
    * @param taskName the task name
    * @throws Exception the exception
    */
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
    * Prints the.
    *
    * @param stream the stream
    * @throws TransformerException the transformer exception
    */
   public void print (PrintStream stream) throws TransformerException {
      DOM.writeDOM(stream, learnedTaskmodel);
   }

   /**
    * Load.
    *
    * @param engine the engine
    * @param properties the properties
    */
   public void load (TaskEngine engine, Properties properties) {
      // engine.load(learnedTaskmodel.toNode(), null);
   }

}
