package edu.wpi.htnlfd.model;

import org.w3c.dom.*;
import java.util.*;
import javax.xml.namespace.QName;

public class TaskModel {

   public static final String xmlnsValue = "http://www.cs.wpi.edu/~rich/cetask/cea-2018-ext";

   public static final String namespace = "urn:disco.wpi.edu:htnlfd:setTable1";

   public static final String namespacePrefix;
   
   static {
      String[] dNSNameArray = namespace.split(":");
      namespacePrefix = dNSNameArray[dNSNameArray.length - 1];
   }
   

   /**
    * Instantiates a new task model.
    */
   public TaskModel () {

   }

   /**
    * Copy constructor for taskmodel.
    */
   public TaskModel (TaskModel oldModel) {
      List<TaskClass> tasks = new ArrayList<TaskClass>(oldModel.tasks.size());
      for (TaskClass oldTaskClass : oldModel.getTaskClasses()) {
         tasks.add(new TaskClass(this, oldTaskClass));
      }
   }

   private List<TaskClass> tasks = new ArrayList<TaskClass>();

   /**
    * Gets the task class.
    */
   public TaskClass getTaskClass (String id) {
      for (TaskClass task : tasks) {
         if ( task.getId().equals(id) )
            return task;
      }
      return null;
   }

   /**
    * Gets the task classes.
    */
   public List<TaskClass> getTaskClasses () {
      return tasks;// return unmodifiableList

   }

   /**
    * Adds task to our taskmodel.
    */
   public void add (TaskClass task) {
      tasks.add(task);
   }

   /**
    * Removes task from our taskmodel.
    */
   public boolean remove (TaskClass task) {
      return tasks.remove(task);
   }

   /**
    * This function checks for equivalent TaskClass classes recursively.
    */
   public boolean isEquivalent () {
      Iterator<TaskClass> iterator = tasks.iterator();
      for (TaskClass c : getTaskClasses()) {
         while (iterator.hasNext()) {
            TaskClass next = iterator.next();
            if ( !next.equals(c) ) {
               if ( c.isEquivalent(next, this) ) {
                  iterator.remove();
                  System.out.println(c.getId() + " and " + next.getId());
               } else if ( next.getDecompositions().isEmpty() ) {
                  // not just remove, change their parents ?????????????????
                  iterator.remove();
               }
            }
         }
      }
      return tasks.isEmpty();
   }

   abstract class Member {

      /**
       * Instantiates a new member.
       */
      public Member (String id, QName qname) {
         super();
         this.id = id;
         this.qname = qname;
      }

      private String id;

      private QName qname;

      /**
       * Sets the id.
       */
      public void setId (String id) {
         this.id = id;
      }

      /**
       * Sets the qname.
       */
      public void setQname (QName qname) {
         this.qname = qname;
      }

      /**
       * Gets the id.
       */
      public String getId () {
         return id;
      }

      /**
       * Gets the qname.
       */
      public QName getQname () {
         return qname;
      }

      /**
       * Gets the model.
       */
      public TaskModel getModel () {
         return TaskModel.this;
      }

   }

   /**
    * This function makes the TaslModel's DOM element recursively.
    */
   public Node toNode (Document document) {
      Element taskModelElement = null;

      taskModelElement = document.createElementNS(TaskModel.xmlnsValue,
            "taskModel");
      document.appendChild(taskModelElement);

      Attr about = document.createAttribute("about");
      about.setValue(TaskModel.namespace);
      taskModelElement.setAttributeNode(about);
      Attr xmlns = document.createAttribute("xmlns");
      xmlns.setValue(TaskModel.xmlnsValue);
      taskModelElement.setAttributeNode(xmlns);

      Set<String> namespaces = new HashSet<String>();
      // namespaces.add("urn:disco.wpi.edu:htnlfd:std");
      for (TaskClass task : tasks) {

         taskModelElement.appendChild(task.toNode(document, namespaces));
      }

      for (String namespaceOfTasks : namespaces) {

         String[] namespaceOfTaskArray = namespaceOfTasks.split(":");
         String namespaceOfTask = namespaceOfTaskArray[namespaceOfTaskArray.length - 1];

         Attr xmlnsReference = document.createAttribute("xmlns:"
            + namespaceOfTask);
         xmlnsReference.setValue(namespaceOfTasks);
         taskModelElement.setAttributeNode(xmlnsReference);
      }
      return taskModelElement;

   }

}
