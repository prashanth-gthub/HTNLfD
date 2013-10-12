package edu.wpi.htnlfd.model;

import edu.wpi.htnlfd.DomManipulation;
import edu.wpi.htnlfd.model.TaskClass.Input;
import org.w3c.dom.*;
import java.util.*;
import java.util.Map.Entry;
import javax.xml.namespace.QName;

public class DecompositionClass extends TaskModel.Member {

   public DecompositionClass (TaskModel taskModel, String id, QName qname,
         boolean ordered2, TaskClass goal) {
      taskModel.super(id, qname);
      this.setId(id);
      this.ordered = ordered2;
      this.goal = goal;
   }

   public DecompositionClass (TaskModel taskModel, String id, boolean ordered2,
         TaskClass goal) {
      taskModel.super(id, null);
      this.setId(id);
      this.ordered = ordered2;
      this.goal = goal;
   }

   private TaskClass goal;

   private String applicable;

   private List<String> stepNames; // in order of definition

   public List<String> getStepNames () {
      return Collections.unmodifiableList(stepNames);
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
      if ( steps == null ) {
         steps = new HashMap<String, Step>();
      }
      return Collections.unmodifiableMap(steps);
   }

   public void addStep (String name, Step step) {

      if ( steps == null ) {
         steps = new HashMap<String, Step>();
      }
      if ( stepNames == null ) {
         stepNames = new ArrayList<String>();
      }
      steps.put(name, step);
      stepNames.add(name);
   }

   public Step getStep (String name) {
      if ( steps != null )
         return steps.get(name);
      return null;
   }

   public Node toNode (Document document, Set<String> namespaces) {
      Element subtasks = document.createElementNS(DomManipulation.xmlnsValue, "subtasks");

      Attr idSubtask = document.createAttribute("id");

      subtasks.setAttributeNode(idSubtask);
      idSubtask.setValue(this.getId());

      if ( !this.isOrdered() ) {
         Attr stepOrder = document.createAttribute("ordered");
         stepOrder.setValue("false");
         subtasks.setAttributeNode(stepOrder);
      }

      for (String step : this.getStepNames()) {

         subtasks.appendChild(this.getStep(step).toNode(document, step,
               namespaces));

      }

      if ( this.getApplicable() != null && this.getApplicable() != "" ) {
         Element applicable = document
               .createElementNS(DomManipulation.xmlnsValue, "applicable");
         applicable.setTextContent(this.getApplicable());
         subtasks.appendChild(applicable);
      }

      for (Entry<String, DecompositionClass.Binding> bind : this.getBindings()
            .entrySet()) {

         subtasks.appendChild(bind.getValue().toNode(document,
               bind.getKey()));
      }
      return subtasks;
   }

   public class Step {
      private TaskClass type;

      private int minOccurs, maxOccurs;

      private List<String> required = new ArrayList<String>();

      public Step (TaskClass type, int minOccurs, int maxOccurs,
            List<String> required) {
         this.type = type;
         this.minOccurs = minOccurs;
         this.maxOccurs = maxOccurs;
         this.required = required;
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
         if ( required == null )
            required = new ArrayList<String>();
         return Collections.unmodifiableList(required);
      }

      public void addRequired (String require) {
         if ( required == null )
            required = new ArrayList<String>();
         boolean contain = false;
         for (String req : this.required) {
            if ( req.equals(require) ) {
               contain = true;
               break;
            }
         }
         if ( !contain )
            this.required.add(require);
      }

      public Node toNode (Document document, String stepName,
            Set<String> namespaces) {
         Element subtaskStep = document.createElementNS(DomManipulation.xmlnsValue, "step");

         Attr nameSubtaskStep = document.createAttribute("name");

         nameSubtaskStep.setValue(stepName);

         subtaskStep.setAttributeNode(nameSubtaskStep);
         Attr valueSubtaskStep = document.createAttribute("task");
         // String namespaceDec = subtask.getStep(stepName).getNamespace();

         String namespaceDec = getType().getQname().getNamespaceURI();
         String[] dNSNameArrayDec = namespaceDec.split(":");
         String dNSNameDec = dNSNameArrayDec[dNSNameArrayDec.length - 1];

         if ( dNSNameDec.compareTo(DomManipulation.namespacePrefix) != 0 )
            valueSubtaskStep.setValue(dNSNameDec + ":" + getType().getId());
         else
            valueSubtaskStep.setValue(getType().getId());
         subtaskStep.setAttributeNode(valueSubtaskStep);

         String requireStr = null;
         if ( required == null )
            required = new ArrayList<String>();
         for (String require : this.required) {
            if ( this.required.size() == 1 ) {
               requireStr = require;
               break;
            } else {
               requireStr = require + " " + requireStr;
            }

         }
         if ( requireStr != null ) {
            Attr stepReq = document.createAttribute("requires");
            subtaskStep.setAttributeNode(stepReq);
            stepReq.setValue(requireStr);
         }

         namespaces.add(getType().getQname().getNamespaceURI());
         return subtaskStep;
      }

      public boolean isEquivalent (Step step,DecompositionClass dec1,DecompositionClass dec2,TaskModel taskModel) {
         boolean sameOrder = false;
         if ( this.getType().getId().equals(step.getType().getId())
            && this.getType().getQname().getNamespaceURI()
                  .equals(step.getType().getQname().getNamespaceURI()) ) {
            if ( (this.required == null && step.required == null)
               || (this.required != null && this.required.size() == 0 && step.required == null)
               || (step.required != null && step.required.size() == 0 && this.required == null) ) {
               sameOrder =  true;
            }
            if ( this.required != null && step.required != null
               && this.required.size() == step.required.size() ) {
               // Assuming that order of required list doesn't matter
               Collections.sort(this.required);
               Collections.sort(step.required);
               if ( (this.required == null && step.required == null)
                  || (this.required != null && step.required != null && this.required
                        .equals(step.required)) ) {
                  sameOrder =  true;
               }
            }
         }
         
         return sameOrder;
      }
   
   
   }
   public boolean checkInputs (String stp1, TaskClass type1,String stp2, TaskClass type2,DecompositionClass dec1, DecompositionClass dec2,TaskModel taskModel) {
      for (Input in1:type1.getDeclaredInputs()){
         for (Input in2:type2.getDeclaredInputs()){
            if(in1.getName().equals(in2.getName())){
            String value1 = findValueInParents(taskModel, stp1,type1,dec1, in1.getName());
            String value2 = findValueInParents(taskModel, stp2,type2,dec2, in2.getName());
            if(value1!=null && value2!= null && !value1.equals(value2)){
               return false;
               
            }
            else if ((value1!=null && value2== null) || (value1==null && value2 != null)){
               return false;
            }
            
         }
      }
      }
      return true;      
   }
   @SuppressWarnings("unchecked")
   public String findValueInParents(TaskModel taskModel, String stp, TaskClass task,DecompositionClass dec, String inputName){
      
      
      for(Entry<String, Binding> bind1:dec.getBindings().entrySet()){
         if(bind1.getValue().getStep().equals(stp) && bind1.getValue().getSlot().contains(inputName)){
            String tem = getBindingValue(bind1,dec);
            if(tem!=null)
               return tem;
            break;
         }
      }
      
      TaskClass parentTask = task;
      Entry<String, Step> parentStep = null;
      DecompositionClass parentSubtask = null;

      List<Object> temp = findRootParent(parentTask, parentStep, parentSubtask,taskModel);
      parentTask = (TaskClass) temp.get(0);
      parentSubtask = (DecompositionClass) temp.get(1);
      parentStep = (Entry<String, Step>) temp.get(2);

      if(parentSubtask != null){
         for(Entry<String, Binding> bind1:parentSubtask.getBindings().entrySet()){
            if(bind1.getValue().getStep().equals(parentStep.getKey()) && bind1.getValue().getSlot().equals(inputName)){
               String tem = getBindingValue(bind1,dec);
               if(tem!=null)
                  return tem;
               break;
            }
         }
      }

      
      
      return null;
   }

   public List<Object> findRootParent (TaskClass parentTask,
         Entry<String, Step> parentStep, DecompositionClass parentSubtask,TaskModel taskModel) {
      boolean contain = false;
      while (true) {
         contain = false;
         for (TaskClass temptask : taskModel.getTaskClasses()) {
            for (DecompositionClass subtask : temptask.getDecompositions()) {

               for (Entry<String, Step> step : subtask.getSteps().entrySet()) {
                  if ( step.getValue().getType().getId()
                        .equals(parentTask.getId()) ) {
                     parentStep = step;
                     parentTask = temptask;
                     parentSubtask = subtask;
                     contain = true;
                     break;
                  }

               }
               if ( contain )
                  break;
            }

            if ( contain )
               break;
         }
         if ( !contain ) {
            ArrayList<Object> temp = new ArrayList<Object>();
            temp.add(parentTask);
            temp.add(parentSubtask);
            temp.add(parentStep);

            return temp;
         }
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
      return Collections.unmodifiableMap(bindings); // Collections.unmodifiableMap(
   }

   public void addBinding (String key, Binding value) {
      bindings.put(key, value);
   }

   public void removeBinding (String key) {
      bindings.remove(key);
   }

   public class Binding {

      private String value, step, slot;

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

      public String getStep () {
         return step;
      }

      public void setStep (String step) {
         this.step = step;
      }

      public String getSlot () {
         return slot;
      }

      public void setSlot (String slot) {
         this.slot = slot;
      }

      public boolean isInputInput () {
         return inputInput;
      }

      public void setInputInput (boolean inputInput) {
         this.inputInput = inputInput;
      }

      Node toNode (Document document, String name) {
         Element subtaskBinding = document.createElementNS(DomManipulation.xmlnsValue,
               "binding");

         Attr bindingSlot = document.createAttribute("slot");

         bindingSlot.setValue(name);

         subtaskBinding.setAttributeNode(bindingSlot);

         Attr bindingValue = document.createAttribute("value");

         bindingValue.setValue(this.getValue());

         subtaskBinding.setAttributeNode(bindingValue);
         return subtaskBinding;
      }

   }

   public TaskClass getGoal () {
      return goal;
   }
   
   public String getBindingValue(Entry<String, Binding> bindingRef, DecompositionClass dec){
      for (Entry<String, Binding> binding : dec.getBindings()
            .entrySet()) {
         if ( binding.getKey().equals(
               bindingRef.getValue().getValue()) ) {
            return binding.getValue().getValue();

         }         
      }
      return null;
   }

   public void addOrdering () {
      TaskClass task = this.goal;
      for (Entry<String, Binding> bindingDep : this.getBindings().entrySet()) {
         if ( !bindingDep.getKey().contains("this") ) { // bindingDep.getKey().contains(ReferenceFrame)
                                                        // &&
            for (Entry<String, Binding> bindingRef : this.getBindings()
                  .entrySet()) {
               if ( !bindingRef.getKey().contains("this") // &&
                                                          // !bindingRef.getKey().contains(ReferenceFrame)
                  && !bindingDep.getValue().getStep()
                        .equals(bindingRef.getValue().getStep()) ) {

                  String valueRef = getBindingValue(bindingRef,this);
                  String inputRef = null;
                  String valueDep = getBindingValue(bindingDep,this);
                  
                  if ( valueDep.equals(valueRef) ) {
                     inputRef = bindingRef.getValue().getValue().substring(6);

                     for (TaskClass.Input inputs : task.getDeclaredInputs()) {
                        if ( inputs.getName().equals(inputRef)
                           && inputs.getModified() != null ) {
                           for (Entry<String, Step> step : this.getSteps()
                                 .entrySet()) {
                              if ( step.getKey().equals(
                                    bindingDep.getValue().getStep()) ) {
                                 step.getValue().addRequired(
                                       bindingRef.getValue().getStep());
                                 this.setOrdered(false);
                                 break;
                              }
                           }
                           break;
                        }
                     }
                  }

               }
            }
         }
      }

   }

   public void removeOrdering () {
      this.setOrdered(true);
      for (Entry<String, Step> step : getSteps().entrySet()) {
         step.getValue().required.clear();
      }
   }

   public boolean isEquivalent (DecompositionClass dec,TaskModel taskModel) {

      ArrayList<String> temp1 = new ArrayList<String>(this.getStepNames());
      ArrayList<String> temp2 = new ArrayList<String>(dec.getStepNames());

      if ( temp1.size() == temp2.size() ) {
         for (int i = 0; i < temp1.size(); i++) {
            Step step1 = this.getStep(temp1.get(i));
            int where = -1;
            boolean contain = false;
            for (int j = 0; j < temp2.size(); j++) {
               Step step2 = dec.getStep(temp2.get(j));
               if ( step1.isEquivalent(step2,this,dec,taskModel) ) {

                  if(checkInputs(temp1.get(i),step1.getType(),temp2.get(j),step2.getType(),this,dec,taskModel)){
                  
                     where = j;
                     contain = true;
                     break;
                  }
                  else
                     return false;
               }
            }
            if ( !contain )
               return false;
            else
               temp2.remove(where);
         }
         
         // Check Bindings if steps are the same

         return true;
      }
      return false;
   }

   public void removeBindingInput (String input) {
      String inputName = input;

      List<Entry<String, Binding>> removed = new ArrayList<Entry<String, Binding>>();

      for (Entry<String, Binding> bind : this.getBindings().entrySet()) {

         if ( bind.getValue().getSlot().contains(inputName)
            && !bind.getValue().getValue().contains("this") ) {
            removed.add(bind);
         }
      }

      for (Entry<String, Binding> rem : removed) {
         // System.out.println(rem.getKey()+" "+rem.getValue());
         this.removeBinding(rem.getKey());

      }

   }
   public Binding getBindingStep(String stepName,String inputName){
      for (Entry<String, Binding> binding : this
            .getBindings().entrySet()) {
         if ( binding.getValue().getStep().equals(stepName) && binding.getValue().isInputInput()
            && binding.getValue().getValue()
                  .contains(inputName) ) {
            return binding.getValue();
         }
      }
      return null;
   }

}
