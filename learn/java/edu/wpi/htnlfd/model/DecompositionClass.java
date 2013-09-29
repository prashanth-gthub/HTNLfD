package edu.wpi.htnlfd.model;

import edu.wpi.cetask.DecompositionClass.Binding;
import java.util.*;
import javax.xml.namespace.QName;

public class DecompositionClass extends TaskModel.Member {

   public DecompositionClass (TaskModel taskModel, String id, QName qname,
         boolean ordered2) {
      taskModel.super(id, qname);
      this.setId(id);
      this.ordered = ordered2;
   }

   public DecompositionClass (TaskModel taskModel, String id, boolean ordered2) {
      taskModel.super(id, null);
      this.setId(id);
      this.ordered = ordered2;
   }

   private TaskClass goal;

   private String applicable;

   private List<String> stepNames; // in order of definition
   
   public List<String> getStepNames () {
      return stepNames;
   }

   public void setStepNames (List<String> stepNames) {
      this.stepNames = stepNames;
   }

   public String getApplicable () {
      return applicable;
   }

   private boolean ordered;

   public boolean isOrdered () {
      return ordered;
   }

   private Map<String, Step> steps;

   public Map<String, Step> getSteps () {
      if ( steps == null ){
         steps = new HashMap<String, Step>();
      }
      return steps;
   }
   public void addStep(String name,Step step){
      
      if ( steps == null ){
         steps = new HashMap<String, Step>();
      }
      if(stepNames==null){
         stepNames = new ArrayList<String>();
      }
      steps.put(name, step);
      stepNames.add(name);      
   }

   public Step getStep(String name){
      if(steps!=null)
         return steps.get(name);
      return null;      
   }
   
   // added namespace to Step
   public class Step {
      private TaskClass type;
      private String namespace;
      private int minOccurs, maxOccurs;

      private List<String> required;

      public Step (TaskClass type, int minOccurs, int maxOccurs,
            List<String> required) {
         this.type = type;
         this.minOccurs = minOccurs;
         this.maxOccurs = maxOccurs;
         this.required = required;
      }

      
      public String getNamespace () {
         return namespace;
      }


      public void setNamespace (String namespace) {
         this.namespace = namespace;
      }


      public TaskClass getType () {
         return type;
      }

      public void setType (TaskClass type) {
         this.type = type;
      }

      public int getMinOccurs () {
         return minOccurs;
      }

      public void setMinOccurs (int minOccurs) {
         this.minOccurs = minOccurs;
      }

      public int getMaxOccurs () {
         return maxOccurs;
      }

      public void setMaxOccurs (int maxOccurs) {
         this.maxOccurs = maxOccurs;
      }

      public List<String> getRequired () {
         if(required == null)
            required = new ArrayList<String>();
         return required;
      }

      public void setRequired (List<String> required) {
         this.required = required;
      }

   }

   public TaskClass getStepType (String name) {
      return steps.get(name).type;
   }

   public void setOrdered (boolean ordered) {
      this.ordered = ordered;
   }

   public void setGoal (TaskClass goal) {
      this.goal = goal;
   }

   public void setApplicable (String applicable) {
      this.applicable = applicable;
   }

   public boolean isOptionalStep (String name) {
      // note this pertains to the _first_ step of repeated steps only
      return steps.get(name).minOccurs < 1;
   }

   private final Map<String, Binding> bindings = new HashMap<String, Binding>();

   public Map<String, Binding> getBindings () {
      return bindings;
   }

   public void addBinding (String key, Binding value) {
      bindings.put(key, value);
   }

   public class Binding {

      // since these are final, ok to make public
      public String value, step, slot;

      private boolean inputInput;

      private List<Binding> depends = new ArrayList<Binding>();

      public Binding (String slot, String value) {
         super();
         this.value = value;
         this.slot = slot;
      }

      public Binding (String slot, String step, String value, boolean inputInput) {
         this.slot = slot;
         this.step = step;
         this.value = value;
         this.inputInput = inputInput;
      }

      public List<Binding> getDepends () {
         return Collections.unmodifiableList(depends);
      }

      public String getValue () {
         return value;
      }

      public void setValue (String value) {
         this.value = value;
      }

   }

   public TaskClass getGoal () {
      return goal;
   }

}
