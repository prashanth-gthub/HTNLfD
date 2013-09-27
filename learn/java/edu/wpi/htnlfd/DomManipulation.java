package edu.wpi.htnlfd;

import edu.wpi.htnlfd.dto.*;
import org.w3c.dom.*;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class DomManipulation {
   
   private DocumentBuilderFactory factory;

   private DocumentBuilder builder;
   
   private Document document;
   
   private final String xmlnsValue = "http://www.cs.wpi.edu/~rich/cetask/cea-2018-ext";

   private final String namespace = "urn:disco.wpi.edu:htnlfd:setTable1";

   private final String namespacePrefix;
   
   
   
   public DomManipulation () {
      factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);

      String[] dNSNameArray = namespace.split(":");
      namespacePrefix = dNSNameArray[dNSNameArray.length - 1];
      try {
         builder = factory.newDocumentBuilder();
         // document = builder.newDocument();
      } catch (ParserConfigurationException e) {
         throw new RuntimeException(
               "An error occured while creating the document builder", e);
      }
   }

   
   
   private void buildDOM (List<TaskBlock> tasks) {

      Element taskModelElement = null;

      taskModelElement = document.createElementNS(xmlnsValue, "taskModel");
      document.appendChild(taskModelElement);

      Attr about = document.createAttribute("about");
      about.setValue(namespace);
      taskModelElement.setAttributeNode(about);
      Attr xmlns = document.createAttribute("xmlns");
      xmlns.setValue(xmlnsValue);
      taskModelElement.setAttributeNode(xmlns);

      Set<String> namespaces = new HashSet<String>();

      for (TaskBlock task:tasks) {    
      
         taskModelElement.appendChild(write(task, namespaces));
      }

      for (String namespaceOfTasks : namespaces) {

         String[] namespaceOfTaskArray = namespaceOfTasks.split(":");
         String namespaceOfTask = namespaceOfTaskArray[namespaceOfTaskArray.length - 1];

         Attr xmlnsReference = document.createAttribute("xmlns:"
            + namespaceOfTask);
         xmlnsReference.setValue(namespaceOfTasks);
         taskModelElement.setAttributeNode(xmlnsReference);
      }

   }
   
   public void writeDOM (String fileName, List<TaskBlock> tasks) throws Exception {

      // Writing document into xml file
      document = builder.newDocument();
      DOMSource domSource = new DOMSource(document);
      File demonstrationFile = new File(fileName);
      if ( !demonstrationFile.exists() )
         demonstrationFile.createNewFile();

      try (FileOutputStream fileOutputStream = new FileOutputStream(
            demonstrationFile, false)) {

         StreamResult streamResult = new StreamResult(fileOutputStream);
         TransformerFactory tf = TransformerFactory.newInstance();
         Transformer transformer = tf.newTransformer();

         buildDOM(tasks);

         // Adding indentation and omitting xml declaration
         transformer.setOutputProperty(OutputKeys.INDENT, "yes");
         transformer.setOutputProperty(
               "{http://xml.apache.org/xslt}indent-amount", "2");
         transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
         transformer.transform(domSource, streamResult);

      } catch (Exception e) {

         throw e;
      }

   }

   
   
   public Element write(TaskBlock task, Set<String> namespaces){
      
           

         Element taskElement = document.createElementNS(xmlnsValue, "task");
         
         Attr idTask = document.createAttribute("id");
         idTask.setValue(task.getId());
         taskElement.setAttributeNode(idTask);
         

         for (Input input : task.getInputs()) {          
            
            
            Element inputTask = document.createElementNS(xmlnsValue, "input");
            taskElement.appendChild(inputTask);
            Attr inputNameAttr = document.createAttribute("name");
            inputNameAttr.setValue(input.getName());
            inputTask.setAttributeNode(inputNameAttr);

            Attr inputType = document.createAttribute("type");
            inputType.setValue(input.getType());
            inputTask.setAttributeNode(inputType);

            if ( input.getModified() != null && input.getModified() != "" ) {
               Attr modifiedAttr = document.createAttribute("modified");
               modifiedAttr.setValue(input.getModified());
               inputTask.setAttributeNode(modifiedAttr);
            }
           
         }

         for (Output output : task.getOutputs()) {
            Element inputTask = document.createElementNS(xmlnsValue, "output");
            taskElement.appendChild(inputTask);
            Attr inputNameAttr = document.createAttribute("name");
            inputNameAttr.setValue(output.getName());
            inputTask.setAttributeNode(inputNameAttr);
            Attr inputType = document.createAttribute("type");
            inputType.setValue(output.getType());
            inputTask.setAttributeNode(inputType);            
         }

       
         for (Subtasks subtask:task.getSubtasks()) {

            Element subtasks = document.createElementNS(xmlnsValue, "subtasks");
            taskElement.appendChild(subtasks);
            Attr idSubtask = document.createAttribute("id");
            
            subtasks.setAttributeNode(idSubtask);
            idSubtask.setValue(subtask.getId());
            
            for (Step step:subtask.getSteps()) {

               Element subtaskStep = document.createElementNS(xmlnsValue,
                     "step");
               subtasks.appendChild(subtaskStep);
               Attr nameSubtaskStep = document.createAttribute("name");

               nameSubtaskStep.setValue(step.getName());               
               
               subtaskStep.setAttributeNode(nameSubtaskStep);
               Attr valueSubtaskStep = document.createAttribute("task");
               String namespaceDec = step.getNamespace();
               String[] dNSNameArrayDec = namespaceDec.split(":");
               String dNSNameDec = dNSNameArrayDec[dNSNameArrayDec.length - 1];

               if ( dNSNameDec.compareTo(namespacePrefix) != 0 )
                  valueSubtaskStep.setValue(dNSNameDec + ":"
                     + step.getTask());
               else
                  valueSubtaskStep.setValue(step.getTask());
               subtaskStep.setAttributeNode(valueSubtaskStep);
               
               namespaces.add(step.getNamespace());
               
              
            }

            

            if ( subtask.getApplicable()!=null && subtask.getApplicable()!="" ) {
               Element applicable = document.createElementNS(xmlnsValue,
                     "applicable");
               applicable.setTextContent(subtask.getApplicable());
               subtasks.appendChild(applicable);
            }


            for (Entry<String, String> bind:subtask.getBindings().entrySet()) {
               Element subtaskBinding = document.createElementNS(xmlnsValue,
                     "binding");
               
               Attr bindingSlot = document.createAttribute("slot");

               bindingSlot.setValue(bind.getKey());

               subtaskBinding.setAttributeNode(bindingSlot);

               Attr bindingValue = document.createAttribute("value");

               bindingValue.setValue(bind.getValue());

               subtaskBinding.setAttributeNode(bindingValue);
               subtasks.appendChild(subtaskBinding);
            }

            

         }
         return taskElement;
       
      
      
   }
}
