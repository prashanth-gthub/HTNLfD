package edu.wpi.htnlfd.model;

import edu.wpi.htnlfd.DomManipulation;
import org.w3c.dom.*;
import java.util.*;
import javax.xml.namespace.QName;

// TODO: Auto-generated Javadoc
/**
 * The Class TaskModel.
 */
public class TaskModel {

   /**
    * Instantiates a new task model.
    */
   public TaskModel () {

   }

   /**
    * Instantiates a new task model.
    *
    * @param oldModel the old model
    */
   public TaskModel (TaskModel oldModel) {
      List<TaskClass> tasks = new ArrayList<TaskClass>(oldModel.tasks.size());
      for (TaskClass oldTaskClass : oldModel.getTaskClasses()) {
         tasks.add(new TaskClass(this, oldTaskClass));
      }
   }

   /** The tasks. */
   private List<TaskClass> tasks = new ArrayList<TaskClass>();

   /**
    * Gets the task class.
    *
    * @param id the id
    * @return the task class
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
    *
    * @return the task classes
    */
   public List<TaskClass> getTaskClasses () {
      return tasks;// return unmodifiableList

   }

   /**
    * Adds the.
    *
    * @param task the task
    */
   public void add (TaskClass task) {
      tasks.add(task);
   }

   /**
    * Removes the.
    *
    * @param task the task
    * @return true, if successful
    */
   public boolean remove (TaskClass task) {
      return tasks.remove(task);
   }

   /**
    * Checks if is equivalent.
    *
    *This function checks for equivalent TaskClass classes recursively.
    *
    * @return true, if is equivalent
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

   /**
    * The Class Member.
    */
   abstract class Member {

      /**
       * Instantiates a new member.
       *
       * @param id the id
       * @param qname the qname
       */
      public Member (String id, QName qname) {
         super();
         this.id = id;
         this.qname = qname;
      }

      /** The id. */
      private String id;

      /** The qname. */
      private QName qname;

      /**
       * Sets the id.
       *
       * @param id the new id
       */
      public void setId (String id) {
         this.id = id;
      }

      /**
       * Sets the qname.
       *
       * @param qname the new qname
       */
      public void setQname (QName qname) {
         this.qname = qname;
      }

      /**
       * Gets the id.
       *
       * @return the id
       */
      public String getId () {
         return id;
      }

      /**
       * Gets the qname.
       *
       * @return the qname
       */
      public QName getQname () {
         return qname;
      }

      /**
       * Gets the model.
       *
       * @return the model
       */
      public TaskModel getModel () {
         return TaskModel.this;
      }

   }

   /**
    * To node.
    *
    *This function makes the TaslModel's DOM element recursively.
    *
    * @param document the document
    * @return the node
    */
   public Node toNode (Document document) {
      Element taskModelElement = null;

      taskModelElement = document.createElementNS(DomManipulation.xmlnsValue,
            "taskModel");
      document.appendChild(taskModelElement);

      Attr about = document.createAttribute("about");
      about.setValue(DomManipulation.namespace);
      taskModelElement.setAttributeNode(about);
      Attr xmlns = document.createAttribute("xmlns");
      xmlns.setValue(DomManipulation.xmlnsValue);
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
