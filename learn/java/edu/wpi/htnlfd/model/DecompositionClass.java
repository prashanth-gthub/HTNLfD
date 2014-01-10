package edu.wpi.htnlfd.model;

import edu.wpi.htnlfd.model.TaskClass.Input;
import edu.wpi.htnlfd.model.TaskClass.*;
import org.w3c.dom.*;
import java.util.*;
import java.util.Map.Entry;
import javax.xml.namespace.QName;

public class DecompositionClass extends TaskModel.Member {

   public enum Type {
      Constant, InputInput, InputOutput, OutputOutput
   };

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

   /**
    * Returns a name that is not used for any decomposition. Check every
    * decomposition in all of the tasks???
    */
   public String findDecompositionName (String DecName) {
      int count = 'A';

      String decNameFind = Character.toLowerCase(DecName.charAt(0))
         + (DecName.length() > 1 ? DecName.substring(1) : "");

      while (true) {
         
         if ( goal.getDecomposition(decNameFind + (char)count) != null )
            count++;
         else
            return decNameFind + (char)count;
      }
   }

   private TaskClass goal;

   private String applicable;

   private List<String> stepNames;

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

   /**
    * Adds the step. Also adds step's name to stepNames
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
    * Creates and adds the step after another step in the demopositionClass.
    */
   public String addStep (Step which, String stepName, Step step,
         String nameWhich, TaskModel taskModel) {
      DecompositionClass.Step stp = which.getDecompositionClass().new Step(
            step.getType(), step.getMinOccurs(), step.getMaxOccurs(), null);
      String newName = which.findStepName(step.getType().getId());
      DecompositionClass currentDec = which.getDecompositionClass();
      DecompositionClass prevDec = step.getDecompositionClass();
      TaskClass prevTask = prevDec.getGoal();
      TaskClass currentTask = currentDec.getGoal();

      currentDec.addStep(newName, stp, nameWhich);

      for (Entry<String, Binding> bind : prevDec.getBindings().entrySet()) {
         if ( bind.getValue().getStep().equals(stepName) ) {
            Entry<String, Binding> bind2 = prevDec.getConstantBinding(bind, prevDec);
            String value = bind2.getValue().getValue();

            TaskClass.Input inputT = null;

            for (Input in : prevTask.getDeclaredInputs()) {
               if ( in.getName()
                     .equals(bind2.getKey().substring(6)) ) {
                  inputT = in;
                  break;
               }
            }

            int adding = currentTask.getDeclaredInputs().size();

            String modified = (inputT.getModified() == null) ? null : inputT
                  .getModified().getName();

            String inName = currentTask.addInput(taskModel, currentTask,
                  inputT.getName(), inputT.getType(), modified, value,
                  currentDec, newName);

            boolean add = (currentTask.getDeclaredInputs().size() != adding);
            if ( !add ) {
               currentDec.addBinding("$" + newName + "."
                  + bind.getValue().getSlot(), currentDec.new Binding(bind
                     .getValue().getSlot(), newName, "$this." + inName,
                     DecompositionClass.Type.InputInput));

               
               if ( modified != null ) {
                  String outName = null;
                  outName = currentTask.getInput(inputT.getName())
                        .getModified().getName();

                  String outputTaskName = prevDec.getBindings()
                        .get("$this." + modified).getValue()
                        .substring(2 + stepName.length());

                  /*currentDec.addBinding("$this." + outName,
                        currentDec.new Binding(outName, "this", "$" + newName
                           + "." + outputTaskName,
                              DecompositionClass.Type.OutputOutput));*/
                  
                  /*TaskClass.Output output = currentTask.new Output(
                        currentTask.findOutputName(newName, outputTaskName),
                        prevTask.getOutput(outName).getType());
                  currentTask.addOutput(output);

                  currentDec.addBinding("$this." + output.getName(),
                        currentDec.new Binding(output.getName(), "this", "$"
                           + newName + "." + outputTaskName,
                              DecompositionClass.Type.OutputOutput));*/
               }

            } else {
               currentDec.addBinding("$" + newName + "."
                  + bind.getValue().getSlot(), currentDec.new Binding(bind
                     .getValue().getSlot(), newName, "$this." + inName,
                     DecompositionClass.Type.InputInput));
               currentDec.addBinding("$this." + inName, currentDec.new Binding(
                     inName, "this", value, DecompositionClass.Type.Constant));

               if ( modified != null ) {
                  String outName = null;
                  outName = modified;

                  String outputTaskName = prevDec.getBindings()
                        .get("$this." + modified).getValue()
                        .substring(2 + stepName.length());
                  TaskClass.Output output = currentTask.new Output(
                        currentTask.findOutputName(newName, outputTaskName),
                        prevTask.getOutput(outName).getType());
                  currentTask.addOutput(output);
                  currentTask.getInput(inName).setModified(output);

                  currentDec.addBinding("$this." + output.getName(),
                        currentDec.new Binding(output.getName(), "this", "$"
                           + newName + "." + outputTaskName,
                              DecompositionClass.Type.OutputOutput));
               }

            }

         }
      }

      // Add Ordering
      currentDec.addOrdering(taskModel);

      return newName;
   }

   /**
    * Adds the step. Also adds step's name to stepNames by considering that the
    * new step should be after the other step.
    */
   public void addStep (String name, Step step, String afterStep) {

      if ( steps == null ) {
         steps = new HashMap<String, Step>();
      }
      if ( stepNames == null ) {
         stepNames = new ArrayList<String>();
      }
      steps.put(name, step);
      if ( afterStep == null ) {
         stepNames.add(0, name);
         return;
      }
      for (int i = 0; i < stepNames.size(); i++) {
         if ( stepNames.get(i).equals(afterStep) ) {
            stepNames.add(i + 1, name);
            break;
         }
      }

   }

   /**
    * Removes the step.
    */
   public void removeStep (String name) {
      steps.remove(name);
      stepNames.remove(name);
   }

   /**
    * Removes the step and their bindings and their inputs/outputs. remove
    * inputs????
    */
   public void removeStepB (String name) {
      String stepName = name;
      Step removeStep = this.getStep(stepName);
      for (Input in : removeStep.getType().getDeclaredInputs()) {
         this.removeBinding("$" + stepName + "." + in.getName());
      }
      for (Output out : removeStep.getType().getDeclaredOutputs()) {
         Entry<String, Binding> binding = this.removeBindingValue("$"
            + stepName + "." + out.getName());
         this.goal.removeOutput(binding.getValue().getSlot());
      }

      this.removeStep(stepName);

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
    * Makes the DecompositionClass's DOM element recursively.
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
         Element applicable = document.createElementNS(TaskModel.xmlnsValue,
               "applicable");
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
         if ( required == null ) {
            this.required = new ArrayList<String>();
         }
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

      /**
       * Adds the required if it hadn't been added.
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
       * Removes all required steps.
       */
      public void removeRequireds () {
         if ( required != null && !this.required.isEmpty() )
            this.required.clear();
      }

      /**
       * Removes the specified required step.
       */
      public void removeRequired (String require) {
         this.required.remove(require);
      }

      /**
       * Makes the step's DOM element.
       */
      public Node toNode (Document document, String stepName,
            Set<String> namespaces) {
         Element subtaskStep = document.createElementNS(TaskModel.xmlnsValue,
               "step");

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

         String requireStr = "";
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
         if ( requireStr != null && requireStr != "" ) {
            Attr stepReq = document.createAttribute("requires");
            subtaskStep.setAttributeNode(stepReq);
            stepReq.setValue(requireStr);
         }

         if ( this.minOccurs != 1 ) {
            Attr minOccurs = document.createAttribute("minOccurs");

            minOccurs.setValue(new Integer(this.minOccurs).toString());

            subtaskStep.setAttributeNode(minOccurs);
         }

         if ( this.maxOccurs != 1 ) {
            Attr maxOccurs = document.createAttribute("maxOccurs");

            maxOccurs.setValue(new Integer(this.maxOccurs).toString());

            subtaskStep.setAttributeNode(maxOccurs);
         }

         namespaces.add(getType().getQname().getNamespaceURI());
         return subtaskStep;
      }

      /**
       * Checks for equivalent steps.
       * 
       * @param taskModel TODO
       */
      public boolean isEquivalent (Step step, TaskModel taskModel) {
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
               // Collections.sort(this.required);
               // Collections.sort(step.required);
               if ( this.required.size() == 0 && step.required.size() == 0 ) {
                  sameOrder = true;
               } else if ( (this.required != null && step.required != null) ) {
                  // && this.required.equals(step.required))
                  if ( this.isEquivalentSteps(taskModel, this.required,
                        getDecomposition(), step.required,
                        step.getDecompositionClass()) ) {
                     sameOrder = true;
                  }
               }
            }
         }

         return sameOrder;
      }

      /**
       * Checks two steps are equivalent.
       */
      public boolean isEquivalentSteps (TaskModel taskModel,
            List<String> steps1, DecompositionClass dec1, List<String> steps2,
            DecompositionClass dec2) {
         ArrayList<String> temp1 = new ArrayList<String>(steps1);
         ArrayList<String> temp2 = new ArrayList<String>(steps2);

         if ( temp1.size() == temp2.size() ) {
            for (int i = 0; i < temp1.size(); i++) {
               Step step1 = dec1.getStep(temp1.get(i));
               int where = -1;
               boolean contain = false;
               for (int j = 0; j < temp2.size(); j++) {
                  Step step2 = dec2.getStep(temp2.get(j));
                  if ( step1.isEquivalent(step2, taskModel) ) {
                     Map.Entry<String, Step> entry1 = new AbstractMap.SimpleEntry<String, Step>(
                           temp1.get(i), step1);
                     Map.Entry<String, Step> entry2 = new AbstractMap.SimpleEntry<String, Step>(
                           temp2.get(j), step2);

                     if ( checkStepInputs(entry1, dec1.getGoal(), entry2,
                           dec2.getGoal(), dec1, dec2, taskModel) ) {

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

      public DecompositionClass getDecompositionClass () {
         return getDecomposition();
      }

      /**
       * Finds goal, if the task is primitive it should be created otherwise it
       * will be searched in the existed tasks.
       */
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
            goal.setPrimitive(true);
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
                           step.getType().getSlotType(in), out);
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

      /**
       * Searches for step name. If it exists, a new name will be returned.
       */
      public String findStepName (String stepName) {
         int count = 1;

         String stepNameFind = Character.toLowerCase(stepName.charAt(0))
            + (stepName.length() > 1 ? stepName.substring(1) : "");

         if ( this.getDecompositionClass().stepNames != null ) {
            for (String name : this.getDecompositionClass().stepNames) {
               if ( name.length() > stepNameFind.length()
                  && stepNameFind.equals(name.substring(0,
                        stepNameFind.length())) ) {
                  try {
                     int num = Integer.parseInt(name.substring(stepNameFind
                           .length()));
                     if ( num > count )
                        count = num;
                  } catch (NumberFormatException e) {
                     ;
                  }
               }
            }
         }

         while (true) {
            if ( getStep(stepNameFind + count) != null )
               count++;
            else
               return stepNameFind + count;
         }
      }

   }

   /**
    * Checks whether each two inputs have the same value in their parents.(This
    * function is called by isEquivalent function)
    */
   public boolean checkInputs (String stp1, TaskClass type1, String stp2,
         TaskClass type2, DecompositionClass dec1, DecompositionClass dec2,
         TaskModel taskModel) {
      for (Input in1 : type1.getDeclaredInputs()) {
         boolean contain = false;
         for (Input in2 : type2.getDeclaredInputs()) {

            if ( !((in1.getModified() != null) ^ in2.getModified() != null)
               && (in1.getType().equals(in2.getType())) ) { // in1.getName().equals(in2.getName())

               String value1 = findValueInParents(taskModel, stp1, type1, dec1,
                     in1.getName());
               String value2 = findValueInParents(taskModel, stp2, type2, dec2,
                     in2.getName());
               if ( value1 != null && value2 != null && value1.equals(value2) ) {
                  // ???? removing the input
                  contain = true;
                  break;
               }

            }
         }
         if ( !contain )
            return false;
      }

      return true;
   }

   /**
    * Checks if two steps have the same inputs.
    */
   public boolean checkStepInputs (Entry<String, Step> stp1, TaskClass type1,
         Entry<String, Step> stp2, TaskClass type2, DecompositionClass dec1,
         DecompositionClass dec2, TaskModel taskModel) {
      for (Input in1 : stp1.getValue().getType().getDeclaredInputs()) {
         boolean contain = false;
         for (Input in2 : stp2.getValue().getType().getDeclaredInputs()) {

            if ( !((in1.getModified() != null) ^ in2.getModified() != null)
               && (in1.getType().equals(in2.getType())) ) { // in1.getName().equals(in2.getName())
               String in1Name = null;
               String in2Name = null;
               String value1 = null;
               String value2 = null;
               for (Entry<String, Binding> binding : dec1.getBindings()
                     .entrySet()) {

                  if ( binding.getValue().getStep().equals(stp1.getKey())
                     && binding.getValue().getSlot().equals(in1.getName()) ) {
                     value1 = getConstantValue(binding, stp1.getValue()
                           .getDecompositionClass());
                     if ( value1 == null ) {
                        binding = getConstantBinding(binding, stp1.getValue()
                              .getDecompositionClass());

                        int index = binding.getValue().getValue().indexOf('.');
                        in1Name = binding.getValue().getValue()
                              .substring(index + 1);
                     }
                     break;
                  }
               }
               for (Entry<String, Binding> binding : dec2.getBindings()
                     .entrySet()) {

                  if ( binding.getValue().getStep().equals(stp2.getKey())
                     && binding.getValue().getSlot().equals(in2.getName()) ) {
                     value2 = getConstantValue(binding, stp2.getValue()
                           .getDecompositionClass());
                     if ( value2 == null ) {
                        binding = getConstantBinding(binding, stp2.getValue()
                              .getDecompositionClass());
                        int index = binding.getValue().getValue().indexOf('.');
                        in2Name = binding.getValue().getValue()
                              .substring(index + 1);
                     }
                     break;
                  }
               }

               if ( value1 == null ) {
                  value1 = findValueInParents(taskModel, stp1.getKey(), type1,
                        dec1, in1Name);
               }
               if ( value2 == null ) {
                  value2 = findValueInParents(taskModel, stp2.getKey(), type2,
                        dec2, in2Name);
               }
               if ( value1 != null && value2 != null && value1.equals(value2) ) {
                  // ???? removing the input
                  contain = true;
                  break;
               }
               

            }
         }
         if ( !contain )
            return false;
      }

      return true;
   }

   /**
    * Finds value of an input in it's parents.(from each subtasks' bindings)
    * Assumption: The value of our input is in it's oldest parent.
    */
   @SuppressWarnings("unchecked")
   public String findValueInParents (TaskModel taskModel, String stp,
         TaskClass task, DecompositionClass dec, String inputName) {

      if ( dec != null && stp != null ) {
         for (Entry<String, Binding> bind1 : dec.getBindings().entrySet()) {
            if ( bind1.getValue().getStep().equals(stp)
               && inputName.contains(bind1.getValue().getSlot()) ) { // /????
               String tem = getConstantValue(bind1, dec);
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

      List<Object[]> temp = findParents(parentTask, parentStep, parentSubtask, inputName,
            taskModel);
      if ( temp.size() > 0 ) {
         parentTask = (TaskClass) temp.get(temp.size() - 1)[0];
         parentSubtask = (DecompositionClass) temp.get(temp.size() - 1)[1];
         parentStep = (Entry<String, Step>) temp.get(temp.size() - 1)[2];
         if(temp.size() - 2 >=0){
            inputName = (String) temp.get(temp.size() - 2)[3];
         }
         
      } else {
         return null;
      }

      if ( parentSubtask != null ) {
         for (Entry<String, Binding> bind1 : parentSubtask.getBindings()
               .entrySet()) {
            if ( bind1.getValue().getStep().equals(parentStep.getKey())
               && bind1.getValue().getSlot().equals(inputName) ) {
               String tem = getConstantValue(bind1, parentSubtask);
               if ( tem != null )
                  return tem;
               break;
            }
         }
      }

      return null;
   }

   /**
    * Finds the parents of a TaskClass. (By calling the same function
    * recursively on it's parents.) It returns just one tree.(I should correct
    * it.)
    */
   public List<Object[]> findParents (TaskClass parentTask,
         Entry<String, Step> parentStep, DecompositionClass parentSubtask, String inputName,
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
                     

                     Object[] parent = new Object[4];

                     parent[0] = temptask;
                     parent[1] = subtask;
                     parent[2] = step;

                     if(inputName != null){
                        for( Entry<String, Binding> bind:subtask.getBindings().entrySet()){
                           if(bind.getValue().getSlot().equals(inputName)){
                              Entry<String, Binding> bindF = this.getConstantBinding(bind, subtask);
                              if(bindF.getValue().getType() == Type.Constant){
                                 parent[3] = bindF.getValue().getSlot();
                              }
                              else if(bindF.getValue().getType() == Type.InputInput && bindF.getValue().value.
                                    substring(1, 5).equals("this")){
                                 parent[3] = bindF.getValue().value.
                                       substring(6);
                                 
                              }
                              inputName = (String) parent[3];
                           }
                        }
                     }
                     
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

   DecompositionClass getDecomposition () {
      return this;
   }

   public TaskClass getStepType (String name) {
      return steps.get(name).type;
   }

   public void setOrdered (boolean ordered) {
      this.ordered = ordered;
   }

   /**
    * Sets the goal(TaskClass).
    */
   public void setGoal (TaskClass goal) {
      this.goal = goal;
   }

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
   public boolean removeBinding (String key) {
      if ( bindings.remove(key) == null )
         return false;
      return true;
   }

   /**
    * Sets the type of specified binding.
    */
   public void setType (Entry<String, Binding> bind) {
      Object first = null;
      Object second = null;
      if ( bind == null )
         return;
      // Finds left part of binding's type
      String bindVal = bind.getKey();
      if ( bindVal.contains("$this.") ) {

         String name = bindVal.substring(6);
         first = getType(name, this.goal);

      } else {
         String stepName = bindVal.substring(1, bindVal.indexOf('.'));
         Step step = bind.getValue().getDecompositionClass().getStep(stepName);

         String name = bindVal.substring(bindVal.indexOf('.') + 1);
         first = getType(name, step.getType());
      }

      // Finds right part of binding's type
      bindVal = bind.getValue().value;
      if ( bindVal.contains("$this.") ) {

         String name = bindVal.substring(6);
         second = getType(name, this.goal);

      } else {
         if ( bindVal.indexOf('.') > 1 ) {
            String stepName = bindVal.substring(1, bindVal.indexOf('.'));

            Step step = bind.getValue().getDecompositionClass()
                  .getStep(stepName);

            if ( step != null ) {
               String name = bindVal.substring(bindVal.indexOf('.') + 1);
               second = getType(name, step.getType());
            } else {
               bind.getValue().setType(Type.Constant);
            }
         } else {
            bind.getValue().setType(Type.Constant);
         }
      }

      if ( (first instanceof Input) && (second instanceof Input) ) {
         bind.getValue().setType(Type.InputInput);
      } else if ( (first instanceof Input) && (second instanceof Output) ) {
         bind.getValue().setType(Type.InputOutput);
      } else if ( (first instanceof Output) && (second instanceof Output) ) {
         bind.getValue().setType(Type.OutputOutput);
      }
   }

   /**
    * Gets the type.
    */
   public Slot getType (String name, TaskClass task) {

      Input in = task.getInput(name);
      Output out = task.getOutput(name);
      if ( in != null ) {
         return in;
      } else if ( out != null ) {
         return out;
      }
      return null;
   }

   public class Binding {

      private String value, step, slot;

      private Type type;

      public Type getType () {
         return type;
      }

      public void setType (Type type) {
         this.type = type;
      }

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
      public Binding (String slot, String step, String value, Type inputInput) {
         this.slot = slot;
         this.step = step;
         this.value = value;
         this.type = inputInput;
      }

      /**
       * Copy constructor for binding.
       */
      public Binding (Binding oldBind) {
         this.slot = oldBind.getSlot();
         this.step = oldBind.getStep();
         this.value = oldBind.getValue();
         this.type = oldBind.getType();
         // depends ????
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

      public DecompositionClass getDecompositionClass () {
         return getDecomposition();
      }

      /**
       * Makes the binding's DOM element.
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
    * Gets the constant value of a binding.
    */
   public String getConstantValue (Entry<String, Binding> bindingRef,
         DecompositionClass dec) {

      Entry<String, Binding> binding = getConstantBinding(bindingRef, dec);
      if ( binding != null )
         if(binding.getValue().getType() == Type.Constant)
            return binding.getValue().value;
         else
            return null;
      else
         return null;

   }

   /**
    * Gets the constant binding of a binding by considering inputOutput bindings.
    */
   public Entry<String, Binding> getConstantBinding (
         Entry<String, Binding> bindingRef, DecompositionClass dec) {

      while (true) {
         boolean contain = false;

         for (Entry<String, Binding> binding : dec.getBindings().entrySet()) {
            if ( binding.getKey().equals(bindingRef.getKey()) ) {
               this.setType(binding);
               if ( binding.getValue().getType() == Type.Constant ) {
                  contain = true;
                  return binding;
               } else if ( binding.getValue().getType() == Type.InputInput ) {
                  contain = true;
                  Entry<String, Binding> bindingR = getBinding(binding, dec);
                  if ( bindingR != null )
                     bindingRef = bindingR;
                  else
                     return bindingRef;
                  break;
               } else if ( binding.getValue().getType() == Type.InputOutput ) {
                  contain = true;
                  String stepName = binding.getValue().value.substring(1,
                        binding.getValue().value.indexOf('.'));
                  Step step = binding.getValue().getDecompositionClass()
                        .getStep(stepName);
                  String outputName = binding.getValue().value
                        .substring(binding.getValue().value.indexOf('.') + 1);
                  String inputName = null;
                  for (Input in : step.getType().getDeclaredInputs()) {
                     if ( in.getModified().getName().equals(outputName) ) {
                        inputName = "$" + stepName + "." + in.getName();
                        break;
                     }
                  }
                  for (Entry<String, Binding> binding2 : dec.getBindings()
                        .entrySet()) {
                     if ( binding2.getKey().equals(inputName) ) {
                        bindingRef = binding2;
                     }
                  }
                  break;
               }

            }
         }
         if ( !contain ) {
            break;
         }
      }
      return bindingRef;
   }

   /**
    * Gets the binding entry.
    */
   public Entry<String, Binding> getBinding (Entry<String, Binding> bindingRef,
         DecompositionClass dec) {
      for (Entry<String, Binding> binding : dec.getBindings().entrySet()) {
         if ( binding.getKey().equals(bindingRef.getValue().getValue()) ) {
            return binding;

         }
      }
      return null;
   }

   /**
    * Gets the binding entry.
    */
   public Entry<String, Binding> getBinding (String value) {
      for (Entry<String, Binding> binding : this.getBindings().entrySet()) {
         if ( binding.getValue().getValue().equals(value) ) {
            return binding;
         }
      }
      return null;
   }

   /**
    * Checks whether one step is before another or not.
    */
   public boolean checkStepBefore (String nameDep, String nameRef) {

      boolean contain = false;
      for (String stepName : this.getStepNames()) {
         if ( stepName.equals(nameRef) ) {
            contain = true;
         }

         if ( stepName.equals(nameDep) ) {
            if ( contain )
               return true;
            else
               return false;
         }

      }

      return false;
   }

   /**
    * Adds the ordering constraints according to the flow of inputs and outputs.
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
                        .equals(bindingRef.getValue().getStep())
                  && checkStepBefore(bindingDep.getValue().getStep(),
                        bindingRef.getValue().getStep()) ) {

                  String valueRef = findValueInParents(taskModel, null, task,
                        this, bindingRef.getValue().getValue().substring(6));
                  // getBindingValue(bindingRef, this);
                  String inputRef = null;
                  String valueDep = findValueInParents(taskModel, null, task,
                        this, bindingDep.getValue().getValue().substring(6));
                  // getBindingValue(bindingDep, this);

                  // Better implementation
                  if ( valueDep != null && valueRef != null
                     && valueDep.equals(valueRef) ) {
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
                                 String stepOutputName = bindingRef.getKey()
                                       .substring(1,
                                             bindingRef.getKey().indexOf('.'));
                                 Input stO = this.getStep(stepOutputName).type
                                       .getInput(bindingRef.getKey()
                                             .substring(
                                                   bindingRef.getKey().indexOf(
                                                         '.') + 1));
                                 bindingDep.getValue().setValue(
                                       "$" + stepOutputName + "."
                                          + stO.getModified().getName());

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
    * Removes ordering constraints.
    */
   public void removeOrdering () {
      this.setOrdered(true);
      for (Entry<String, Step> step : getSteps().entrySet()) {
         if ( step.getValue().required != null
            && step.getValue().required.size() != 0 )
            step.getValue().required.clear();
      }
   }

   /**
    * Checks for equivalent steps recursively.
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
               if ( step1.isEquivalent(step2, taskModel) ) {

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
    * Removes the binding that is related to an input.
    */
   public void removeBindingInput (String name) {

      List<Entry<String, Binding>> removed = new ArrayList<Entry<String, Binding>>();

      for (Entry<String, Binding> bind : this.getBindings().entrySet()) {

         if ( bind.getValue().getSlot().contains(name)
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
    * Removes the binding by key.
    */
   public Entry<String, Binding> removeBindingKey (String name) {

      Entry<String, Binding> removed = null;

      for (Entry<String, Binding> bind : this.getBindings().entrySet()) {

         if ( bind.getKey().equals(name) ) {
            removed = bind;
            break;
         }
      }
      if ( removed != null ) {
         this.removeBinding(removed.getKey());
         return removed;
      }
      return null;

   }

   /**
    * Removes the binding by value.
    */
   public Entry<String, Binding> removeBindingValue (String value) {

      Entry<String, Binding> removed = null;

      for (Entry<String, Binding> bind : this.getBindings().entrySet()) {

         if ( bind.getValue().getValue().equals(value) ) {
            removed = bind;
            break;
         }
      }

      if ( removed != null ) {
         this.removeBinding(removed.getKey());
         return removed;
      }
      return null;

   }

   /**
    * Gets the binding that is related to a step and an input.
    */
   public Entry<String, Binding> getBindingStep (String stepName,
         String inputName) {
      for (Entry<String, Binding> binding : this.getBindings().entrySet()) {
         if ( binding.getValue().getStep().equals(stepName)
            && binding.getValue().getType().equals(Type.InputInput)
            && binding.getValue().getValue().contains(inputName) ) {
            return binding;
         }
      }
      return null;
   }

   /**
    * Gets bindings that are related to the specified step.
    */
   public List<Entry<String, Binding>> getBindingStep (String stepName) {
      List<Entry<String, Binding>> bindings = new ArrayList<Entry<String, Binding>>();
      for (Entry<String, Binding> binding : this.getBindings().entrySet()) {
         if ( binding.getValue().getStep().equals(stepName) ) {
            bindings.add(binding);
         }
         if ( binding.getValue().getValue().contains("$" + stepName + ".") ) {
            bindings.add(binding);
         }
      }
      return bindings;
   }

   public Entry<String, Binding> getBindingValue (String stepName,
         String inputName) {
      for (Entry<String, Binding> binding : this.getBindings().entrySet()) {
         if ( binding.getValue().getStep().equals(stepName)
            && binding.getKey().equals("$" + stepName + "." + inputName) ) {
            return binding;
         }
      }
      return null;
   }

   /**
    * Gets the question of this decompositionClass.
    */
   public String getQuestion (TaskModel taskModel) {
      String valueOut = "";

      for (Entry<String, Step> step : this.getSteps().entrySet()) {
         String val = valueOut;
         if ( !step.getValue().getType().isInternal() ) {
            valueOut += step.getValue().getType().getId();
            for (Input in : step.getValue().getType().getDeclaredInputs()) {
               String var = findValueInParents(taskModel, step.getKey(), step
                     .getValue().getDecompositionClass().getGoal(), step
                     .getValue().getDecompositionClass(), in.getName());
               if ( var != null )
                  val += var + " and";
            }

            if ( val != null && val != "" )
               valueOut = val.substring(0, val.length() - 4);
         } else {
            String valTask = "";
            valTask = step.getValue().getType().getQuestion(taskModel);
            if ( valTask != null )
               valueOut += valTask;
         }
      }
      return valueOut;
   }

   /**
    * Adds the ordering by dataflow.
    */
   public void addOrderingByDataflow () {
      for (Entry<String, Binding> bind : this.bindings.entrySet()) {
         String stepIn = bind.getValue().step;
         this.setType(bind);
         if ( !stepIn.equals("this") && bind.getValue().type != Type.Constant
            && this.getStep(stepIn).getType().isInput(bind.getValue().slot) ) {
            int st = bind.getValue().getValue().indexOf('.');
            if ( st == -1 )
               continue;
            String outputName = bind.getValue().getValue().substring(st + 1);
            String stepOut = bind.getValue().getValue().substring(1, st);
            if ( !stepOut.equals("this")
               && this.getStep(stepOut).getType().isOutput(outputName) ) {

               if ( !stepIn.equals(stepOut) ) {
                  this.getStep(stepIn).addRequired(stepOut);
               }
            }
         }
      }
   }

}
