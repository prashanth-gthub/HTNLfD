package edu.wpi.htnlfd.model;

import org.w3c.dom.*;
import java.util.*;
import javax.xml.namespace.QName;

public class TaskModel {

   public TaskModel () {

   }

   private List<TaskClass> tasks = new ArrayList<TaskClass>();

   public TaskClass getTaskClass (String id) {
      for(TaskClass task:tasks){
         if(task.getId().equals(id))
            return task;
      }
      return null;      
   }

   public List<TaskClass> getTaskClasses () {
      return tasks;// return unmodifiableList

   }

   public void add (TaskClass task) {
      tasks.add(task);
   }

   public boolean remove (TaskClass task) {
      return tasks.remove(task);
   }

   public boolean isEquivalent () {
      Iterator<TaskClass> iterator = tasks.iterator();
      for (TaskClass c : getTaskClasses()) {
         while (iterator.hasNext()) {
            TaskClass next = iterator.next();
            if ( !next.equals(c) ) {
               if ( c.isEquivalent(next) ) {
                  iterator.remove();
                  System.out.println(c.getId() + " and " + next.getId());
               } else if ( next.getDecompositions().isEmpty() ) {
                  iterator.remove();
               }
            }
         }
      }
      return tasks.isEmpty();
   }

   abstract class Member {

      public Member (String id, QName qname) {
         super();
         this.id = id;
         this.qname = qname;
      }

      private String id;

      private QName qname;

      public void setId (String id) {
         this.id = id;
      }

      public void setQname (QName qname) {
         this.qname = qname;
      }

      public String getId () {
         return id;
      }

      public QName getQname () {
         return qname;
      }


      public TaskModel getModel () {
         return TaskModel.this;
      }

   }

   public Node toNode (Document document, String xmlnsValue, String namespace,
         String namespacePrefix) {
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
      // namespaces.add("urn:disco.wpi.edu:htnlfd:std");
      for (TaskClass task : tasks) {

         taskModelElement.appendChild(task.toNode(document, xmlnsValue,
               namespacePrefix, namespaces));
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
