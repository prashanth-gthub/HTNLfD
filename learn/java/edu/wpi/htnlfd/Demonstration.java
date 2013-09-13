package edu.wpi.htnlfd;

import edu.wpi.cetask.*;
import edu.wpi.disco.*;
import edu.wpi.disco.lang.Utterance;
import org.w3c.dom.*;
import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class Demonstration {

   private DocumentBuilderFactory factory;

   private DocumentBuilder builder;

   private Document document;

   private List<String> dependentLibraries = new ArrayList<String>();

   public Demonstration () {
      factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      try {
         builder = factory.newDocumentBuilder();
         document = builder.newDocument();
      } catch (ParserConfigurationException e) {
         e.printStackTrace();
         throw new RuntimeException(
               "An error occured while creating the document builder", e);
      }
   }

   public void addDependentLibaries (String dependentLibrary) {
      dependentLibraries.add(dependentLibrary);
   }

   public List<Task> findDemonstration (Disco disco, String taskName) {
      List<Task> demonstratedTasks = new ArrayList<Task>();
      Segment parent = disco.getSegment();

      int start = 0;
      int end = parent.getChildren().size() - 1;
      for (int i = parent.getChildren().size() - 1; i >= 0; i--) {
         Object child = parent.getChildren().get(i);
         if ( (child instanceof Task) ) {
            Task task = (Task) child;
            if ( task.getType().getId().compareTo("Demonstration") == 0 ) {
               start = i + 1;
               break;
            }
         }
      }
      for (int i = start; i < end; i++) {
         Object child = parent.getChildren().get(i);
         if ( (child instanceof Task) ) {
            Task task = (Task) child;
            if ( !(task instanceof Utterance) ) {
               demonstratedTasks.add(task);
            }
         }
      }

      return demonstratedTasks;
   }

   public void writeDOM (String fileName, String taskName, List<Task> steps,
         String input) throws IOException {

      // Writing document into xml file
      DOMSource domSource = new DOMSource(document);
      File demonstrationFile = new File(fileName);
      if ( !demonstrationFile.exists() )
         demonstrationFile.createNewFile();

      try (FileOutputStream fileOutputStream = new FileOutputStream(
            demonstrationFile, false)) {

         StreamResult streamResult = new StreamResult(fileOutputStream);
         TransformerFactory tf = TransformerFactory.newInstance();
         Transformer transformer = tf.newTransformer();

         buildDOM(taskName, steps, input);

         // Adding indentation and omitting xml declaration
         transformer.setOutputProperty(OutputKeys.INDENT, "yes");
         transformer.setOutputProperty(
               "{http://xml.apache.org/xslt}indent-amount", "2");
         transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
         transformer.transform(domSource, streamResult);

      } catch (Exception e) {
         e.printStackTrace();
         throw new RuntimeException(
               "An error occured while writing to the xml file", e);
      }

   }

   public void readDOM (Disco disco, String fileName) {
      disco.getInteraction().load(fileName);
   }

   public void resetDisco (Disco disco) {
      disco.getInteraction().reset();
      Iterator<String> dependentLibrariesIterator = dependentLibraries
            .iterator();
      while (dependentLibrariesIterator.hasNext()) {
         String xmlFile = dependentLibrariesIterator.next();
         disco.getInteraction().load(xmlFile);
      }
   }

   private void buildDOM (String taskName, List<Task> steps, String input) {
      // String namespace = "urn:disco.wpi.edu:htnlfd:" + taskName;
      String namespace = "urn:disco.wpi.edu:htnlfd:setTable1";
      String demonstrationNamespace = steps.get(0).getType().getNamespace();
      String xmlnsValue = "http://www.cs.wpi.edu/~rich/cetask/cea-2018-ext";
      String[] namespaceNameArray = demonstrationNamespace.split(":");
      String namespaceName = namespaceNameArray[namespaceNameArray.length - 1];

      String[] dNSNameArray = namespace.split(":");
      String dNSName = dNSNameArray[dNSNameArray.length - 1];

      Element taskModel = null;

      if ( document.getElementsByTagName("taskModel") == null
         || document.getElementsByTagName("taskModel").getLength() == 0 ) {
         taskModel = document.createElementNS(xmlnsValue, "taskModel");
         document.appendChild(taskModel);
         Attr about = document.createAttribute("about");
         about.setValue(namespace);
         taskModel.setAttributeNode(about);
         Attr xmlns = document.createAttribute("xmlns");
         xmlns.setValue(xmlnsValue);
         taskModel.setAttributeNode(xmlns);
         Attr xmlnsReference = document.createAttribute("xmlns:"
            + namespaceName);
         xmlnsReference.setValue(demonstrationNamespace);
         taskModel.setAttributeNode(xmlnsReference);
      } else {
         NodeList taskModels = document.getElementsByTagName("taskModel");
         taskModel = (Element) taskModels.item(0);
      }

      Element subtasks = null;
      Element applicable = null;
      Element task = null;
      for (int i = 0; i < document.getElementsByTagName("task").getLength(); i++) {
         Element taskTemp = (Element) document.getElementsByTagName("task")
               .item(i);
         if ( taskTemp.getAttribute("id").compareTo(taskName) == 0 ) {
            task = taskTemp;
            break;
         }
      }

      if ( task == null ) {
         task = document.createElementNS(xmlnsValue, "task");
         taskModel.appendChild(task);
         Attr idTask = document.createAttribute("id");
         idTask.setValue(taskName);
         task.setAttributeNode(idTask);
         subtasks = document.createElementNS(xmlnsValue, "subtasks");
         task.appendChild(subtasks);
         Attr idSubtask = document.createAttribute("id");
         idSubtask.setValue(Character.toLowerCase(taskName.charAt(0))
            + (taskName.length() > 1 ? taskName.substring(1) : ""));
         subtasks.setAttributeNode(idSubtask);
      } else {
         Element inputTask = document.createElementNS(xmlnsValue, "input");
         task.insertBefore(inputTask, task.getFirstChild());
         Attr inputName = document.createAttribute("name");
         inputName.setValue(input);
         inputTask.setAttributeNode(inputName);
         Attr inputType = document.createAttribute("type");
         inputType.setValue("boolean");
         inputTask.setAttributeNode(inputType);

         NodeList taskChildren = task.getChildNodes();

         String id = Character.toLowerCase(taskName.charAt(0))
            + (taskName.length() > 1 ? taskName.substring(1) : "");
         taskChildren.item(1).getAttributes().item(0).setTextContent(id + "1");

         applicable = document.createElementNS(xmlnsValue, "applicable");
         taskChildren.item(taskChildren.getLength() - 1)
               .appendChild(applicable);
         applicable.setTextContent("$this." + input);

         subtasks = document.createElementNS(xmlnsValue, "subtasks");
         task.appendChild(subtasks);
         Attr idSubtask = document.createAttribute("id");
         idSubtask.setValue(id + "2");
         subtasks.setAttributeNode(idSubtask);

         applicable = document.createElementNS(xmlnsValue, "applicable");
         applicable.setTextContent("!$this." + input);
      }

      // Adding steps to task
      for (Task step : steps) {
         Element subtaskStep = document.createElementNS(xmlnsValue, "step");
         subtasks.appendChild(subtaskStep);
         Attr nameSubtaskStep = document.createAttribute("name");
         String stepStr = step.getType().getId();
         nameSubtaskStep.setValue(Character.toLowerCase(stepStr.charAt(0))
            + (stepStr.length() > 1 ? stepStr.substring(1) : ""));
         subtaskStep.setAttributeNode(nameSubtaskStep);
         Attr valueSubtaskStep = document.createAttribute("task");
         if ( dNSName.compareTo(namespaceName) != 0 )
            valueSubtaskStep.setValue(namespaceName + ":" + stepStr);
         else
            valueSubtaskStep.setValue(stepStr);
         subtaskStep.setAttributeNode(valueSubtaskStep);

         if ( applicable != null )
            subtasks.appendChild(applicable);
      }
   }

}
