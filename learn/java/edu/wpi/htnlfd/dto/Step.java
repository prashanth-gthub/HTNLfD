package edu.wpi.htnlfd.dto;

import java.util.List;

public class Step {
   private String name;
   private String namespace;
   private String task;
   private List<String> requires;
   public String getName () {
      return name;
   }
   public void setName (String name) {
      this.name = name;
   }
   public String getTask () {
      return task;
   }
   public void setTask (String task) {
      this.task = task;
   }
   public Step (String name, String task, String namespace) {
      super();
      this.name = name;
      this.task = task;
      this.namespace = namespace;
   }
   
   public void addRequires(String require){
      requires.add(require);
   }
   public String getNamespace () {
      return namespace;
   }
   public void setNamespace (String namespace) {
      this.namespace = namespace;
   }
   public List<String> getRequires () {
      return requires;
   }
   public void setRequires (List<String> requires) {
      this.requires = requires;
   }
   
   
}
