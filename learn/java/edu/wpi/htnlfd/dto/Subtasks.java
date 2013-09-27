package edu.wpi.htnlfd.dto;

import java.util.*;

public class Subtasks {
   String id;
   boolean ordered;
   String applicable;
   ArrayList<Step> steps = new ArrayList<Step>();
   Map<String,String> bindings = new HashMap<String,String>();
  
   public Subtasks (String id, boolean ordered) {
      super();
      this.id = id;
      this.ordered = ordered;
   }   
   public Subtasks (String id, boolean ordered, String applicable) {
      super();
      this.id = id;
      this.ordered = ordered;
      this.applicable = applicable;
   }
   
   public Subtasks (String id) {
      this.id = id;
   }
   public String getId () {
      return id;
   }
   public void setId (String id) {
      this.id = id;
   }
   public boolean isOrdered () {
      return ordered;
   }
   public void setOrdered (boolean ordered) {
      this.ordered = ordered;
   }

   public String getApplicable () {
      return applicable;
   }

   public void setApplicable (String applicable) {
      this.applicable = applicable;
   }
   public ArrayList<Step> getSteps () {
      return steps;
   }
   public void setSteps (ArrayList<Step> steps) {
      this.steps = steps;
   }
   public Map<String, String> getBindings () {
      return bindings;
   }
   public void setBindings (Map<String, String> bindings) {
      this.bindings = bindings;
   }
   

   
   
   
}
