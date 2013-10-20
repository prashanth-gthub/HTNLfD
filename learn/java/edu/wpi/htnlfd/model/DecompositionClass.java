package edu.wpi.htnlfd.model;

import edu.wpi.htnlfd.model.TaskClass.*;
import org.w3c.dom.*;
import java.util.*;
import java.util.Map.Entry;
import javax.xml.namespace.QName;

public class DecompositionClass extends TaskModel.Member {

   /**
    * Instantiates a new decomposition class.
    */
   public DecompositionClass (TaskModel taskModel, String id, QName qname,
         boolean ordered2, TaskClass goal) {
      taskModel.super(id, qname);
      this.setId(id);
      this.ordered = ordered2;
      this.goal = goal;
   }

   /**
    * Instantiates a new decomposition class.
    */
   public DecompositionClass (TaskModel taskModel, String id, boolean ordered2,
         TaskClass goal) {
      taskModel.super(id, null);
      this.setId(id);
      this.ordered = ordered2;
      this.goal = goal;
   }

   /**
    * Copy constructor for decomposition class.
    */
   public DecompositionClass (TaskModel taskModel,
         DecompositionClass oldDecomposition, TaskClass goal) {
      taskModel.super(oldDecomposition.getId(), oldDecomposition.getQname());
      this.applicable = oldDecomposition.getApplicable();
      this.ordered = oldDecomposition.isOrdered();
      this.goal = goal;
      this.stepNames = new ArrayList<String>();
      for (String stepName : oldDecomposition.getStepNames())
         this.stepNames.add(stepName);
      this.steps = new HashMap<String, Step>();
      for (Entry<String, Step> step : oldDecomposition.getSteps().entrySet()) {
         this.steps.put(step.getKey(), new Step(step.getValue()));
      }
      this.bindings = new HashMap<String, Binding>();
      for (Entry<String, Binding> bind : oldDecomposition.getBindings()
            .entrySet()) {
         this.bindings.put(bind.getKey(), new Binding(bind.getValue()));
      }

   }

   private TaskClass goal;

   private String applicable;

   private List<String> stepNames;

   /**
    * Gets the step names.
    */
   public List<String> getStepNames () {
      return Collections.unmodifiableList(stepNames);
   }

   /**
    * Sets the step names.
    */
   public void setStepNames (List<String> stepNames) {
      this.stepNames = stepNames;
   }

   /**
    * Gets the applicable.
    */
   public String getApplicable () {
      return applicable;
   }

   private boolean ordered;

   /**
    * Checks if is ordered.
    */
   public boolean isOrdered () {
      return ordered;
   }

   private Map<String, Step> steps;

   /**
    * Gets the steps.
    */
   public Map<String, Step> getSteps () {
      if ( steps == null ) {
         steps = new HashMap<String, Step>();
      }
      return Collections.unmodifiableMap(steps);
   }

   /**
    * Adds the step.
    */
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

   /**
    * Gets the step.
    */
   public Step getStep (String name) {
      if ( steps != null )
         return steps.get(name);
      return null;
   }

   /**
    * This function makes the DecompositionClass's DOM element recursively.
    */
   public Node toNode (Document document, Set<String> namespaces) {
      Element subtasks = document.createElementNS(TaskModel.xmlnsValue,
            "subtasks");

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
         Element applicable = document.createElementNS(
               TaskModel.xmlnsValue, "applicable");
         applicable.setTextContent(this.getApplicable());
         subtasks.appendChild(applicable);
      }

      for (Entry<String, DecompositionClass.Binding> bind : this.getBindings()
            .entrySet()) {

         subtasks.appendChild(bind.getValue().toNode(document, bind.getKey()));
      }
      return subtasks;
   }

   public class Step {

      private TaskClass type;

      private int minOccurs, maxOccurs;

      private List<String> required = new ArrayList<String>();

      /**
       * Instantiates a new step.
       */
      public Step (TaskClass type, int minOccurs, int maxOccurs,
            List<String> required) {
         this.type = type;
         this.minOccurs = minOccurs;
         this.maxOccurs = maxOccurs;
         this.required = required;
      }

      /**
       * Instantiates a new step.
       */
      public Step (Step oldStep) {
         this.type = oldStep.getType();
         this.minOccurs = oldStep.getMinOccurs();
         this.maxOccurs = oldStep.getMaxOccurs();
         this.required = new ArrayList<String>();
         for (String require : oldStep.getRequired())
            this.required.add(require);
      }

      /**
       * Gets the type of taskclass.
       */
      public TaskClass getType () {
         return type;
      }

      /**
       * Sets the type of taskclass.
       */
      public void setType (TaskClass type) {
         this.type = type;
      }

      /**
       * Gets the min occurs.
       */
      public int getMinOccurs () {
         return minOccurs;
      }

      /**
       * Sets the min occurs.
       */
      public void setMinOccurs (int minOccurs) {
         this.minOccurs = minOccurs;
      }

      /**
       * Gets the max occurs.
       */
      public int getMaxOccurs () {
         return maxOccurs;
      }

      /**
       * Sets the max occurs.
       */
      public void setMaxOccurs (int maxOccurs) {
         this.maxOccurs = maxOccurs;
      }

      /**
       * Gets the required.
       */
      public List<String> getRequired () {
         if ( required == null )
            required = new ArrayList<String>();
         return Collections.unmodifiableList(required);
      }

      /**
       * Adds the required.
       */
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

      /**
       * This function makes the step's DOM element.
       */
      public Node toNode (Document document, String stepName,
            Set<String> namespaces) {
         Element subtaskStep = document.createElementNS(
               TaskModel.xmlnsValue, "step");

         Attr nameSubtaskStep = document.createAttribute("name");

         nameSubtaskStep.setValue(stepName);

         subtaskStep.setAttributeNode(nameSubtaskStep);
         Attr valueSubtaskStep = document.createAttribute("task");
         // String namespaceDec = subtask.getStep(stepName).getNamespace();

         String namespaceDec = getType().getQname().getNamespaceURI();
         String[] dNSNameArrayDec = namespaceDec.split(":");
         String dNSNameDec = dNSNameArrayDec[dNSNameArrayDec.length - 1];

         if ( dNSNameDec.compareTo(TaskModel.namespacePrefix) != 0 )
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
         
         if(this.minOccurs!=1){
            Attr minOccurs = document.createAttribute("minOccurs");
   
            minOccurs.setValue(new Integer(this.minOccurs).toString());
   
            subtaskStep.setAttributeNode(minOccurs);
         }
         
         if(this.maxOccurs!=1){
            Attr maxOccurs = document.createAttribute("maxOccurs");
   
            maxOccurs.setValue(new Integer(this.maxOccurs).toString());
   
            subtaskStep.setAttributeNode(maxOccurs);
         }

         namespaces.add(getType().getQname().getNamespaceURI());
         return subtaskStep;
      }

      /**
       * Checks if is equivalent. This function checks for equivalent steps.
       */
      public boolean isEquivalent (Step step, DecompositionClass dec1,
            DecompositionClass dec2, TaskModel taskModel) {
         boolean sameOrder = false;
         if ( this.getType().getId().equals(step.getType().getId())
            && this.getType().getQname().getNamespaceURI()
                  .equals(step.getType().getQname().getNamespaceURI()) ) {
            if ( (this.required == null && step.required == null)
               || (this.required != null && this.required.size() == 0 && step.required == null)
               || (step.required != null && step.required.size() == 0 && this.required == null) ) {
               sameOrder = true;
            }
            if ( this.required != null && step.required != null
               && this.required.size() == step.required.size() ) {
               // Assuming that order of required list doesn't matter
               Collections.sort(this.required);
               Collections.sort(step.required);
               if ( (this.required == null && step.required == null)
                  || (this.required != null && step.required != null && this.required
                        .equals(step.required)) ) {
                  sameOrder = true;
               }
            }
         }

         return sameOrder;
      }

      public TaskClass findGoal (TaskModel taskModel, edu.wpi.cetask.Task step) {
         TaskClass goal = null;
         for (TaskClass goalI : taskModel.getTaskClasses()) {
            if ( goalI.getId().equals(step.getType().getId()) ) {
               goal = goalI;

               // goal.setQname(step.getType().getQName());
               break;
            }
         }
         if ( goal == null ) {
            goal = new TaskClass(taskModel, step.getType().getId(), step
                  .getType().getQName());

            for (String out : step.getType().getDeclaredOutputNames()) {

               TaskClass.Output outputTask = goal.new Output(out, step
                     .getType().getSlotType(out));
               goal.addOutput(outputTask);

            }

            for (String in : step.getType().getDeclaredInputNames()) {

               TaskClass.Input inputC = null;
               for (TaskClass.Output out : goal.getDeclaredOutputs()) {
                  if ( step.getType().getModified(in) != null
                     && out.getName().equals(step.getType().getModified(in)) ) {
                     inputC = goal.new Input(in,
                           step.getType().getSlotType(in), null);
                     break;
                  }
               }
               if ( inputC == null ) {
                  inputC = goal.new Input(in, step.getType().getSlotType(in),
                        null);
               }
               goal.addInput(inputC);
            }
         }
         
         return goal;
        
      }
      
      public String findStepName(String stepName){
         int count = 1;
         
         String stepNameFind = Character.toLowerCase(stepName.charAt(0))
               + (stepName.length() > 1 ? stepName.substring(1) : "");
         
         while(true){
            if(getStep(stepNameFind+count)!=null)
               count++;
            else
               return stepNameFind+count;            
         }
      }

   }

   /**
    * Check inputs. This function checks whether two inputs have the same value
    * in their parents.(This function is called by isEquivalent function)
    */
   public boolean checkInputs (String stp1, TaskClass type1, String stp2,
         TaskClass type2, DecompositionClass dec1, DecompositionClass dec2,
         TaskModel taskModel) {
      for (Input in1 : type1.getDeclaredInputs()) {
         for (Input in2 : type2.getDeclaredInputs()) {
            if ( in1.getName().equals(in2.getName()) ) {
               String value1 = findValueInParents(taskModel, stp1, type1, dec1,
                     in1.getName());
               String value2 = findValueInParents(taskModel, stp2, type2, dec2,
                     in2.getName());
               if ( value1 != null && value2 != null && !value1.equals(value2) ) {
                  return false;

               } else if ( (value1 != null && value2 == null)
                  || (value1 == null && value2 != null) ) {
                  return false;
               }

            }
         }
      }
      return true;
   }

   /**
    * Find value in parents. This function finds value of an input in it's
    * parents.(from each subtasks' bindings) Assumption: The value of our input
    * is in it's oldest parent.
    */
   @SuppressWarnings("unchecked")
   public String findValueInParents (TaskModel taskModel, String stp,
         TaskClass task, DecompositionClass dec, String inputName) {

      if ( dec != null && stp != null ) {
         for (Entry<String, Binding> bind1 : dec.getBindings().entrySet()) {
            if ( bind1.getValue().getStep().equals(stp)
               && bind1.getValue().getSlot().contains(inputName) ) {
               String tem = getBindingValue(bind1, dec);
               if ( tem != null )
                  return tem;
               break;
            }
         }
      }

      TaskClass parentTask = task;
      Entry<String, Step> parentStep = null;
      DecompositionClass parentSubtask = null;

      if ( dec != null && stp == null ) {
         for (Entry<String, Binding> bind1 : dec.getBindings().entrySet()) {
            if ( bind1.getValue().getStep().equals("this")
               && bind1.getValue().getSlot().equals(inputName) ) { // contains
               return bind1.getValue().getValue();
            }
         }
      }

      List<Object[]> temp = findParents(parentTask, parentStep, parentSubtask,
            taskModel);
      parentTask = (TaskClass) temp.get(temp.size()-1)[0];
      parentSubtask = (DecompositionClass) temp.get(temp.size()-1)[1];
      parentStep = (Entry<String, Step>) temp.get(temp.size()-1)[2];

      if ( parentSubtask != null ) {
         for (Entry<String, Binding> bind1 : parentSubtask.getBindings()
               .entrySet()) {
            if ( bind1.getValue().getStep().equals(parentStep.getKey())
               && bind1.getValue().getSlot().equals(inputName) ) {
               String tem = getBindingValue(bind1, parentSubtask);
               if ( tem != null )
                  return tem;
               break;
            }
         }
      }

      return null;
   }

   /**
    * Find root parent. This function finds the last parent(oldest parent) of a
    * TaskClass. (By calling the same function recursively on it's parents.)
    */
   public List<Object[]> findParents (TaskClass parentTask,
         Entry<String, Step> parentStep, DecompositionClass parentSubtask,
         TaskModel taskModel) {
      
      List<Object[]> parents = new ArrayList<Object[]>();
      
      boolean contain = false;
      while (true) {
         contain = false;
         for (TaskClass temptask : taskModel.getTaskClasses()) {
            for (DecompositionClass subtask : temptask.getDecompositions()) {

               for (Entry<String, Step> step : subtask.getSteps().entrySet()) {
                  if ( step.getValue().getType().getId()
                        .equals(parentTask.getId()) ) {
                     
                     parentTask = temptask;
                     parentSubtask = subtask;
                     parentStep = step;
                     
                     Object[] parent= new Object[3];
                     
                     parent[0] = temptask;
                     parent[1] = subtask;
                     parent[2] = step;                  
                                         
                     parents.add(parent);                     
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

            return parents;
         }
      }
   }

   /**
    * Gets the step type.
    */
   public TaskClass getStepType (String name) {
      return steps.get(name).type;
   }

   /**
    * Sets the ordered.
    */
   public void setOrdered (boolean ordered) {
      this.ordered = ordered;
   }

   /**
    * Sets the goal(TaskClass).
    */
   public void setGoal (TaskClass goal) {
      this.goal = goal;
   }

   /**
    * Sets the applicable.
    */
   public void setApplicable (String applicable) {
      this.applicable = applicable;
   }

   /**
    * Checks if is optional step.
    */
   public boolean isOptionalStep (String name) {
      // note this pertains to the _first_ step of repeated steps only
      return steps.get(name).minOccurs < 1;
   }

   private Map<String, Binding> bindings = new HashMap<String, Binding>();

   /**
    * Gets the bindings.
    */
   public Map<String, Binding> getBindings () {
      return Collections.unmodifiableMap(bindings); // Collections.unmodifiableMap(
   }

   /**
    * Adds the binding.
    */
   public void addBinding (String key, Binding value) {
      bindings.put(key, value);
   }

   /**
    * Removes the binding.
    */
   public void removeBinding (String key) {
      bindings.remove(key);
   }

   public class Binding {

      private String value, step, slot;

      private boolean inputInput;

      private List<Binding> depends = new ArrayList<Binding>();

      /**
       * Instantiates a new binding.
       */
      public Binding (String slot, String value) {
         super();
         this.value = value;
         this.slot = slot;
      }

      /**
       * Instantiates a new binding.
       */
      public Binding (String slot, String step, String value, boolean inputInput) {
         this.slot = slot;
         this.step = step;
         this.value = value;
         this.inputInput = inputInput;
      }

      /**
       * Copy constructor for binding.
       */
      public Binding (Binding oldBind) {
         this.slot = oldBind.getSlot();
         this.step = oldBind.getStep();
         this.value = oldBind.getValue();
         this.inputInput = oldBind.isInputInput();
         this.depends = new ArrayList<Binding>();
         // depends ????
      }

      /**
       * Gets the depends.
       */
      public List<Binding> getDepends () {
         return Collections.unmodifiableList(depends);
      }

      /**
       * Gets the value.
       */
      public String getValue () {
         return value;
      }

      /**
       * Sets the value.
       */
      public void setValue (String value) {
         this.value = value;
      }

      /**
       * Gets the step.
       */
      public String getStep () {
         return step;
      }

      /**
       * Sets the step.
       */
      public void setStep (String step) {
         this.step = step;
      }

      /**
       * Gets the slot.
       */
      public String getSlot () {
         return slot;
      }

      /**
       * Sets the slot.
       */
      public void setSlot (String slot) {
         this.slot = slot;
      }

      /**
       * Checks if is input input.
       */
      public boolean isInputInput () {
         return inputInput;
      }

      /**
       * Sets the input input.
       */
      public void setInputInput (boolean inputInput) {
         this.inputInput = inputInput;
      }

      /**
       * This function makes the binding's DOM element.
       */
      Node toNode (Document document, String name) {
         Element subtaskBinding = document.createElementNS(
               TaskModel.xmlnsValue, "binding");

         Attr bindingSlot = document.createAttribute("slot");

         bindingSlot.setValue(name);

         subtaskBinding.setAttributeNode(bindingSlot);

         Attr bindingValue = document.createAttribute("value");

         bindingValue.setValue(this.getValue());

         subtaskBinding.setAttributeNode(bindingValue);
         return subtaskBinding;
      }

   }

   /**
    * Gets the goal.
    */
   public TaskClass getGoal () {
      return goal;
   }

   /**
    * Gets the binding value.
    */
   public String getBindingValue (Entry<String, Binding> bindingRef,
         DecompositionClass dec) {
      for (Entry<String, Binding> binding : dec.getBindings().entrySet()) {
         if ( binding.getKey().equals(bindingRef.getValue().getValue()) ) {
            return binding.getValue().getValue();

         }
      }
      return null;
   }

   /**
    * Adds the ordering. This function adds the ordering constraints according
    * to the flow of inputs and outputs.
    */
   public void addOrdering (TaskModel taskModel) {
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

                  String valueRef = findValueInParents (taskModel, null,
                         task, this, bindingRef.getValue().getValue().substring(6));                        
                        //getBindingValue(bindingRef, this);
                  String inputRef = null;
                  String valueDep = findValueInParents (taskModel, null,
                        task, this, bindingDep.getValue().getValue().substring(6));
                        //getBindingValue(bindingDep, this);

                  if (valueDep!=null && valueRef!=null && valueDep.equals(valueRef) ) {
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

   /**
    * Removes the ordering. This function removes ordering constraints.
    */
   public void removeOrdering () {
      this.setOrdered(true);
      for (Entry<String, Step> step : getSteps().entrySet()) {
         if(step.getValue().required!=null && step.getValue().required.size()!=0)
            step.getValue().required.clear();
      }
   }

   /**
    * Checks if is equivalent. This function checks for equivalent steps
    * recursively.
    */
   public boolean isEquivalent (DecompositionClass dec, TaskModel taskModel) {

      ArrayList<String> temp1 = new ArrayList<String>(this.getStepNames());
      ArrayList<String> temp2 = new ArrayList<String>(dec.getStepNames());

      if ( temp1.size() == temp2.size() ) {
         for (int i = 0; i < temp1.size(); i++) {
            Step step1 = this.getStep(temp1.get(i));
            int where = -1;
            boolean contain = false;
            for (int j = 0; j < temp2.size(); j++) {
               Step step2 = dec.getStep(temp2.get(j));
               if ( step1.isEquivalent(step2, this, dec, taskModel) ) {

                  if ( checkInputs(temp1.get(i), step1.getType(), temp2.get(j),
                        step2.getType(), this, dec, taskModel) ) {

                     where = j;
                     contain = true;
                     break;
                  } else
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

   /**
    * Removes the binding input. This function removes the binding that is
    * related to an input.
    */
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

   /**
    * Gets the binding step. This fucntion returns the binding that is related
    * to a step and an input.
    */
   public Entry<String, Binding> getBindingStep (String stepName,
         String inputName) {
      for (Entry<String, Binding> binding : this.getBindings().entrySet()) {
         if ( binding.getValue().getStep().equals(stepName)
            && binding.getValue().isInputInput()
            && binding.getValue().getValue().contains(inputName) ) {
            return binding;
         }
      }
      return null;
   }

}
