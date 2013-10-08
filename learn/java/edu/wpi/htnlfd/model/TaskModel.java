package edu.wpi.htnlfd.model;


import java.util.*;
import javax.xml.namespace.QName;


public class TaskModel {

   public TaskModel () {
      
   }

   private List<TaskClass> tasks = new ArrayList<TaskClass>();

   public TaskClass getTaskClass (String name) {
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

      public QName getQName () {
         return qname;
      }

      public TaskModel getModel () {
         return TaskModel.this;
      }

   }

}
