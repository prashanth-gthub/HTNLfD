package edu.wpi.htnlfd;

import edu.wpi.cetask.Task;
import edu.wpi.disco.*;
import edu.wpi.disco.lang.Utterance;
import org.w3c.dom.*;
import java.io.*;
import java.util.Vector;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class Demonstration {

   public Vector<Task> findDemonstration (Disco disco, String taskName) {
      Segment parent = disco.getStack().get(0);
      Vector<Task> tasks = new Vector<Task>();
      for (Object child : parent.getChildren()) {
         if ( child instanceof Task ) {
            Task task = (Task) child;
            if ( !(task instanceof Utterance) ) {
               tasks.add(task);
            }
         }
      }
      return tasks;
   }

   public void writeDOM (Vector<Task> steps, String taskName, String fileName) {
      FileOutputStream fileOutputStream = null;
      String namespace = "urn:disco.wpi.edu:htnlfd:" + taskName;
      String demonstrationNamespace = steps.get(0).getType().getNamespace();
      String xmlnsValue = "http://www.cs.wpi.edu/~rich/cetask/cea-2018-ext";
      String[] namespaceNameArray = demonstrationNamespace.split(":");
      String namespaceName = namespaceNameArray[namespaceNameArray.length - 1];
      try {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setNamespaceAware(true);
         DocumentBuilder builder = factory.newDocumentBuilder();
         Document doc = builder.newDocument();

         // Creating the xml file
         Element taskModel = doc.createElementNS(xmlnsValue, "taskModel");
         doc.appendChild(taskModel);
         Attr about = doc.createAttribute("about");
         about.setValue(namespace);
         taskModel.setAttributeNode(about);
         Attr xmlns = doc.createAttribute("xmlns");
         xmlns.setValue(xmlnsValue);
         taskModel.setAttributeNode(xmlns);
         Attr xmlnsReference = doc.createAttribute("xmlns:" + namespaceName);
         xmlnsReference.setValue(demonstrationNamespace);
         taskModel.setAttributeNode(xmlnsReference);
         Element task = doc.createElementNS(xmlnsValue, "task");
         taskModel.appendChild(task);
         Attr idTask = doc.createAttribute("id");
         idTask.setValue(taskName);
         task.setAttributeNode(idTask);
         Element subtasks = doc.createElementNS(xmlnsValue, "subtasks");
         task.appendChild(subtasks);
         Attr idSubtask = doc.createAttribute("id");
         idSubtask.setValue(Character.toLowerCase(taskName.charAt(0))
            + (taskName.length() > 1 ? taskName.substring(1) : ""));
         subtasks.setAttributeNode(idSubtask);

         // Adding steps to task
         for (Task step : steps) {
            Element subtaskStep = doc.createElementNS(xmlnsValue, "step");
            subtasks.appendChild(subtaskStep);
            Attr nameSubtaskStep = doc.createAttribute("name");
            String stepStr = step.getType().getId();
            nameSubtaskStep.setValue(Character.toLowerCase(stepStr.charAt(0))
               + (stepStr.length() > 1 ? stepStr.substring(1) : ""));
            subtaskStep.setAttributeNode(nameSubtaskStep);
            Attr valueSubtaskStep = doc.createAttribute("task");
            valueSubtaskStep.setValue(namespaceName + ":" + stepStr);
            subtaskStep.setAttributeNode(valueSubtaskStep);
         }

         // Writing document into xml file
         DOMSource domSource = new DOMSource(doc);
         File demonstrationFile = new File(fileName);
         if ( !demonstrationFile.exists() )
            demonstrationFile.createNewFile();
         fileOutputStream = new FileOutputStream(demonstrationFile, false);
         StreamResult streamResult = new StreamResult(fileOutputStream);
         TransformerFactory tf = TransformerFactory.newInstance();
         Transformer transformer = tf.newTransformer();

         // Adding indentation and omitting xml declaration
         transformer.setOutputProperty(OutputKeys.INDENT, "yes");
         transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
         transformer.transform(domSource, streamResult);

      } catch (Exception e) {
         e.printStackTrace();
         System.err.println("An error occurs while writing to the xml file");
      } finally {
         try {
            fileOutputStream.close();
         } catch (IOException e) {
            e.printStackTrace();
         }
      }
   }

   public void readDOM (Disco disco, String fileName) {
      disco.load(fileName);
   }
}
