package edu.wpi.htnlfd.model;

import edu.wpi.htnlfd.DomManipulation;
import edu.wpi.htnlfd.model.TaskClass.Input;
import org.w3c.dom.*;
import java.util.*;
import java.util.Map.Entry;
import javax.xml.namespace.QName;

// TODO: Auto-generated Javadoc
/**
 * The Class DecompositionClass.
 */
public class DecompositionClass extends TaskModel.Member {

   /**
    * Instantiates a new decomposition class.
    *
    * @param taskModel the task model
    * @param id the id
    * @param qname the qname
    * @param ordered2 the ordered2
    * @param goal the goal
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
    *
    * @param taskModel the task model
    * @param id the id
    * @param ordered2 the ordered2
    * @param goal the goal
    */
   public DecompositionClass (TaskModel taskModel, String id, boolean ordered2,
         TaskClass goal) {
      taskModel.super(id, null);
      this.setId(id);
      this.ordered = ordered2;
      this.goal = goal;
   }

   /**
    * Instantiates a new decomposition class.
    *
    * @param taskModel the task model
    * @param oldDecomposition the old decomposition
    * @param goal the goal
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

   /** The goal. */
   private TaskClass goal;

   /** The applicable. */
   private String applicable;

   /** The step names. */
   private List<String> stepNames; // in order of definition

   /**
    * Gets the step names.
    *
    * @return the step names
    */
   public List<String> getStepNames () {
      return Collections.unmodifiableList(stepNames);
   }

   /**
    * Sets the step names.
    *
    * @param stepNames the new step names
    */
   public void setStepNames (List<String> stepNames) {
      this.stepNames = stepNames;
   }

   /**
    * Gets the applicable.
    *
    * @return the applicable
    */
   public String getApplicable () {
      return applicable;
   }

   /** The ordered. */
   private boolean ordered;

   /**
    * Checks if is ordered.
    *
    * @return true, if is ordered
    */
   public boolean isOrdered () {
      return ordered;
   }

   /** The steps. */
   private Map<String, Step> steps;

   /**
    * Gets the steps.
    *
    * @return the steps
    */
   public Map<String, Step> getSteps () {
      if ( steps == null ) {
         steps = new HashMap<String, Step>();
      }
      return Collections.unmodifiableMap(steps);
   }

   /**
    * Adds the step.
    *
    * @param name the name
    * @param step the step
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
    *
    * @param name the name
    * @return the step
    */
   public Step getStep (String name) {
      if ( steps != null )
         return steps.get(name);
      return null;
   }

   /**
    * To node.
    * 
    * This function makes the DecompositionClass's DOM element recursively.
    *
    * @param document the document
    * @param namespaces the namespaces
    * @return the node
    */
   public Node toNode (Document document, Set<String> namespaces) {
      Element subtasks = document.createElementNS(DomManipulation.xmlnsValue,
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
               DomManipulation.xmlnsValue, "applicable");
         applicable.setTextContent(this.getApplicable());
         subtasks.appendChild(applicable);
      }

      for (Entry<String, DecompositionClass.Binding> bind : this.getBindings()
            .entrySet()) {

         subtasks.appendChild(bind.getValue().toNode(document, bind.getKey()));
      }
      return subtasks;
   }

   /**
    * The Class Step.
    */
   public class Step {
      
      /** The type. */
      private TaskClass type;

      /** The max occurs. */
      private int minOccurs, maxOccurs;

      /** The required. */
      private List<String> required = new ArrayList<String>();

      /**
       * Instantiates a new step.
       *
       * @param type the type
       * @param minOccurs the min occurs
       * @param maxOccurs the max occurs
       * @param required the required
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
       *
       * @param oldStep the old step
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
       * Gets the type.
       *
       * @return the type
       */
      public TaskClass getType () {
         return type;
      }

      /**
       * Sets the type.
       *
       * @param type the new type
       */
      public void setType (TaskClass type) {
         this.type = type;
      }

      /**
       * Gets the min occurs.
       *
       * @return the min occurs
       */
      public int getMinOccurs () {
         return minOccurs;
      }

      /**
       * Sets the min occurs.
       *
       * @param minOccurs the new min occurs
       */
      public void setMinOccurs (int minOccurs) {
         this.minOccurs = minOccurs;
      }

      /**
       * Gets the max occurs.
       *
       * @return the max occurs
       */
      public int getMaxOccurs () {
         return maxOccurs;
      }

      /**
       * Sets the max occurs.
       *
       * @param maxOccurs the new max occurs
       */
      public void setMaxOccurs (int maxOccurs) {
         this.maxOccurs = maxOccurs;
      }

      /**
       * Gets the required.
       *
       * @return the required
       */
      public List<String> getRequired () {
         if ( required == null )
            required = new ArrayList<String>();
         return Collections.unmodifiableList(required);
      }

      /**
       * Adds the required.
       *
       * @param require the require
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
       * To node.
       *
       *This function makes the step's DOM element.
       *
       * @param document the document
       * @param stepName the step name
       * @param namespaces the namespaces
       * @return the node
       */
      public Node toNode (Document document, String stepName,
            Set<String> namespaces) {
         Element subtaskStep = document.createElementNS(
               DomManipulation.xmlnsValue, "step");

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

      /**
       * Checks if is equivalent.
       *
       *This function checks for equivalent steps.
       *
       * @param step the step
       * @param dec1 the dec1
       * @param dec2 the dec2
       * @param taskModel the task model
       * @return true, if is equivalent
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

   }

   /**
    * Check inputs.
    * 
    * This function checks whether two inputs have the same value in their parents.(This function is called by isEquivalent function)
    *
    * @param stp1 the stp1
    * @param type1 the type1
    * @param stp2 the stp2
    * @param type2 the type2
    * @param dec1 the dec1
    * @param dec2 the dec2
    * @param taskModel the task model
    * @return true, if successful
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
    * Find value in parents.
    *
    *This fucntion finds value of an input in it's parents.(from each subtasks' bindings)
      Assumption: The value of our input is in it's oldest parent.
    *
    * @param taskModel the task model
    * @param stp the stp
    * @param task the task
    * @param dec the dec
    * @param inputName the input name
    * @return the string
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
               && bind1.getValue().getSlot().contains(inputName) ) {
               return bind1.getValue().getValue();
            }
         }
      }

      List<Object> temp = findRootParent(parentTask, parentStep, parentSubtask,
            taskModel);
      parentTask = (TaskClass) temp.get(0);
      parentSubtask = (DecompositionClass) temp.get(1);
      parentStep = (Entry<String, Step>) temp.get(2);

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
    * Find root parent.
    *
    *This function finds the last parent(oldest parent) of a TaskClass.(By calling the same fucntion recursively on it's parents.)
    *
    * @param parentTask the parent task
    * @param parentStep the parent step
    * @param parentSubtask the parent subtask
    * @param taskModel the task model
    * @return the list
    */
   public List<Object> findRootParent (TaskClass parentTask,
         Entry<String, Step> parentStep, DecompositionClass parentSubtask,
         TaskModel taskModel) {
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

   /**
    * Gets the step type.
    *
    * @param name the name
    * @return the step type
    */
   public TaskClass getStepType (String name) {
      return steps.get(name).type;
   }

   /**
    * Sets the ordered.
    *
    * @param ordered the new ordered
    */
   public void setOrdered (boolean ordered) {
      this.ordered = ordered;
   }

   /**
    * Sets the goal.
    *
    * @param goal the new goal
    */
   public void setGoal (TaskClass goal) {
      this.goal = goal;
   }

   /**
    * Sets the applicable.
    *
    * @param applicable the new applicable
    */
   public void setApplicable (String applicable) {
      this.applicable = applicable;
   }

   /**
    * Checks if is optional step.
    *
    * @param name the name
    * @return true, if is optional step
    */
   public boolean isOptionalStep (String name) {
      // note this pertains to the _first_ step of repeated steps only
      return steps.get(name).minOccurs < 1;
   }

   /** The bindings. */
   private Map<String, Binding> bindings = new HashMap<String, Binding>();

   /**
    * Gets the bindings.
    *
    * @return the bindings
    */
   public Map<String, Binding> getBindings () {
      return Collections.unmodifiableMap(bindings); // Collections.unmodifiableMap(
   }

   /**
    * Adds the binding.
    *
    * @param key the key
    * @param value the value
    */
   public void addBinding (String key, Binding value) {
      bindings.put(key, value);
   }

   /**
    * Removes the binding.
    *
    * @param key the key
    */
   public void removeBinding (String key) {
      bindings.remove(key);
   }

   /**
    * The Class Binding.
    */
   public class Binding {

      /** The slot. */
      private String value, step, slot;

      /** The input input. */
      private boolean inputInput;

      /** The depends. */
      private List<Binding> depends = new ArrayList<Binding>();

      /**
       * Instantiates a new binding.
       *
       * @param slot the slot
       * @param value the value
       */
      public Binding (String slot, String value) {
         super();
         this.value = value;
         this.slot = slot;
      }

      /**
       * Instantiates a new binding.
       *
       * @param slot the slot
       * @param step the step
       * @param value the value
       * @param inputInput the input input
       */
      public Binding (String slot, String step, String value, boolean inputInput) {
         this.slot = slot;
         this.step = step;
         this.value = value;
         this.inputInput = inputInput;
      }

      /**
       * Instantiates a new binding.
       *
       * @param oldBind the old bind
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
       *
       * @return the depends
       */
      public List<Binding> getDepends () {
         return Collections.unmodifiableList(depends);
      }

      /**
       * Gets the value.
       *
       * @return the value
       */
      public String getValue () {
         return value;
      }

      /**
       * Sets the value.
       *
       * @param value the new value
       */
      public void setValue (String value) {
         this.value = value;
      }

      /**
       * Gets the step.
       *
       * @return the step
       */
      public String getStep () {
         return step;
      }

      /**
       * Sets the step.
       *
       * @param step the new step
       */
      public void setStep (String step) {
         this.step = step;
      }

      /**
       * Gets the slot.
       *
       * @return the slot
       */
      public String getSlot () {
         return slot;
      }

      /**
       * Sets the slot.
       *
       * @param slot the new slot
       */
      public void setSlot (String slot) {
         this.slot = slot;
      }

      /**
       * Checks if is input input.
       *
       * @return true, if is input input
       */
      public boolean isInputInput () {
         return inputInput;
      }

      /**
       * Sets the input input.
       *
       * @param inputInput the new input input
       */
      public void setInputInput (boolean inputInput) {
         this.inputInput = inputInput;
      }

      /**
       * To node.
       *
       *This function makes the binding's DOM element.
       *
       * @param document the document
       * @param name the name
       * @return the node
       */
      Node toNode (Document document, String name) {
         Element subtaskBinding = document.createElementNS(
               DomManipulation.xmlnsValue, "binding");

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
    *
    * @return the goal
    */
   public TaskClass getGoal () {
      return goal;
   }

   /**
    * Gets the binding value.
    *
    * @param bindingRef the binding ref
    * @param dec the dec
    * @return the binding value
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
    * Adds the ordering.
    * 
    * This function adds the ordering constraints according to the flow of inputs and outputs.
    * 
    */
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

                  String valueRef = getBindingValue(bindingRef, this);
                  String inputRef = null;
                  String valueDep = getBindingValue(bindingDep, this);

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

   /**
    * Removes the ordering.
    * 
    * This function removes ordering constraints.
    * 
    */
   public void removeOrdering () {
      this.setOrdered(true);
      for (Entry<String, Step> step : getSteps().entrySet()) {
         step.getValue().required.clear();
      }
   }

   /**
    * Checks if is equivalent.
    *
    *This function checks for equivalent steps recursively.
    *
    * @param dec the dec
    * @param taskModel the task model
    * @return true, if is equivalent
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
    * Removes the binding input.
    *
    *This function removes the binding that is related to an input.
    *
    * @param input the input
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
    * Gets the binding step.
    *
    *This fucntion returns the binding that is related to a step and an input.
    *
    * @param stepName the step name
    * @param inputName the input name
    * @return the binding step
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
