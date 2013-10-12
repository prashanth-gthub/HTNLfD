package edu.wpi.htnlfd;

import edu.wpi.disco.*;
import edu.wpi.disco.lang.Utterance;
import edu.wpi.htnlfd.model.*;
import edu.wpi.htnlfd.model.DecompositionClass.Binding;
import edu.wpi.htnlfd.model.DecompositionClass.Step;
import edu.wpi.htnlfd.model.TaskClass.Input;
import java.util.*;
import java.util.Map.Entry;
import javax.script.*;
import javax.xml.namespace.QName;


public class Demonstration {

   private TaskModel taskModel;

   private edu.wpi.cetask.TaskModel externalTaskModel = null;

   private InputTransformation transformation = new InputTransformation();

   public Demonstration () {

   }

   public List<edu.wpi.cetask.Task> findDemonstration (Disco disco,
         String taskName) {
      List<edu.wpi.cetask.Task> demonstratedTasks = new ArrayList<edu.wpi.cetask.Task>();
      List<edu.wpi.cetask.Task> demonstratedTasksReversed = new ArrayList<edu.wpi.cetask.Task>();
      Object parent = (disco.getStack().get(0).getChildren().get(disco
            .getStack().get(0).getChildren().size() - 1));
      if ( parent instanceof Segment ) {

         for (int i = ((Segment) parent).getChildren().size() - 1; i >= 0; i--) {
            Object child = ((Segment) parent).getChildren().get(i);
            if ( (child instanceof edu.wpi.cetask.Task) ) {
               edu.wpi.cetask.Task task = (edu.wpi.cetask.Task) child;
               if ( !(task instanceof Utterance) )
                  ;// demonstratedTasks.add(task);
            } else if ( child instanceof Segment ) {
               Segment segment = (Segment) child;
               demonstratedTasks.add(segment.getPurpose());
            }
         }

         for (int i = demonstratedTasks.size() - 1; i >= 0; i--) {
            edu.wpi.cetask.Task myTask = demonstratedTasks.get(i);
            demonstratedTasksReversed.add(myTask);
         }
         return demonstratedTasksReversed;
      } else {
         return null;
      }
   }

   public TaskModel buildTaskModel (Disco disco, String taskName,
         List<edu.wpi.cetask.Task> steps, String input)
         throws NoSuchMethodException, ScriptException {
      taskModel = new TaskModel();
      if ( this.externalTaskModel != null ) {
         learnedTaskmodel();
      }

      TaskClass newTask = demonstratedTask(disco, taskName, steps);
      if ( !addAlternativeRecipe(newTask, input) ) {
         this.taskModel.add(newTask);
      }

 
      transformation.transform(this.taskModel);

      this.taskModel.isEquivalent();

      return this.taskModel;
   }

   public TaskClass isAlternativeRecipe (TaskClass newTask) {
      Iterator<TaskClass> tasksIterator = this.taskModel.getTaskClasses()
            .iterator();
      TaskClass task = null;

      while (tasksIterator.hasNext()) {

         task = tasksIterator.next();

         if ( task.getId().equals(newTask.getId()) ) {
            return task;

         }
      }
      return null;
   }

   


   public boolean addAlternativeRecipe (TaskClass newTask, String input) {

      TaskClass task = isAlternativeRecipe(newTask);

      if ( task != null ) {

         TaskClass.Input inputC = task.new Input(input, "boolean", null);
         task.addInput(inputC);
         task.getDecompositions().get(0).setApplicable("$this." + input);
         newTask.getDecompositions().get(0).setApplicable("!$this." + input);
         newTask.getDecompositions().get(0)
               .setId(task.getDecompositions().get(0).getId() + "1");
         task.getDecompositions().add(newTask.getDecompositions().get(0));

         Map<String, Binding> removed1 = new HashMap<String, Binding>();
         
         return true;
      }
      
      return false;
   }

   public TaskClass demonstratedTask (Disco disco, String taskName,
         List<edu.wpi.cetask.Task> steps) throws NoSuchMethodException,
         ScriptException {
      TaskClass task = null;
      task = new TaskClass(taskModel, taskName);

      int countSubtask = task.getDecompositions().size() + 1;
      DecompositionClass subtask = new DecompositionClass(taskModel,
            Character.toLowerCase(taskName.charAt(0))
               + (taskName.length() > 1 ? taskName.substring(1) : "")
               + countSubtask, true, task);

      task.addDecompositionClass(subtask);
      Map<String, Integer> StepNames = new HashMap<String, Integer>();

      Map<String, edu.wpi.cetask.Task> outputs = new HashMap<String, edu.wpi.cetask.Task>();

      for (edu.wpi.cetask.Task step : steps) {

         String stepName = step.getType().getId();
         // finding step's name
         int count = 1;
         Map.Entry<String, Integer> stepEntry = null;
         for (Map.Entry<String, Integer> entry : StepNames.entrySet()) {
            if ( stepName.equals(entry.getKey()) ) {
               entry.setValue(entry.getValue() + 1);
               stepEntry = entry;
               count = stepEntry.getValue();
               break; // breaking because its one to one map
            }
         }

         if ( stepEntry == null ) {
            StepNames.put(stepName, count);
         }
         TaskClass goal = null;
         for(TaskClass goalI :this.taskModel.getTaskClasses()){
            if(goalI.getId().equals(step.getType().getId())){
               goal = goalI;
               
               //goal.setQname(step.getType().getQName());
               break;
            }
         }
         if(goal == null){
            goal = new TaskClass(
                  taskModel, step.getType().getId(), step.getType().getQName());
            
            for(String out:step.getType().getDeclaredOutputNames()){

               TaskClass.Output outputTask = goal.new Output(out,
                     step.getType().getSlotType(out));
               goal.addOutput(outputTask);

         }
         
      for(String in:step.getType().getDeclaredInputNames()){
         
         TaskClass.Input inputC = null;
         for (TaskClass.Output out : goal.getDeclaredOutputs()) {
            if (step.getType().getModified(in)!=null && out.getName().equals(step.getType().getModified(in)) ) {
               inputC = goal.new Input(in,step.getType().getSlotType(in),null);
               break;
            }
         }
         if ( inputC == null ) {
            inputC = goal.new Input(in,
                  step.getType().getSlotType(in), null);
         }
         goal.addInput(inputC);
      }
         }
         DecompositionClass.Step stp = subtask.new Step(goal,
               1, 1, null);
         String stepNameR = Character.toLowerCase(stepName.charAt(0))
            + (stepName.length() > 1 ? stepName.substring(1) : "") + count;
         // stp.setNamespace(step.getType().getNamespace());
         subtask.addStep(stepNameR, stp);
         subtask.setGoal(task);

         for (String outputName : step.getType().getDeclaredOutputNames()) {

            String bindingSlot = "$" + stepNameR + "." + outputName;

            String bindingSlotValue = null;
            bindingSlotValue = stepNameR + "_" + outputName;
            outputs.put(bindingSlotValue, step);

            subtask.addBinding("$this." + bindingSlotValue,
                  subtask.new Binding(bindingSlotValue, "this", bindingSlot,
                        false));
            task.addOutput(task.new Output(bindingSlotValue, step.getType()
                  .getSlotType(outputName)));
         }

         List<String> inputs = new ArrayList<String>();
         for (String inputName : step.getType().getDeclaredInputNames()) {

            String bindingSlotvalue = "$" + stepNameR + "." + inputName;
            
            
            inputs.add(inputName);
            
            Object inputBinding = (((Invocable) disco.getScriptEngine())
                  .invokeFunction("find", step.getSlotValue(inputName)));

            String inputBindingValue = (String) inputBinding;
            int inputNum1 = task.getDeclaredInputs().size();
            String changedName = task.addInput(task, inputName, step.getType()
                  .getSlotType(inputName), step.getType()
                  .getModified(inputName), inputBindingValue, subtask,
                  stepNameR);
            int inputNum2 = task.getDeclaredInputs().size();
            subtask.addBinding(bindingSlotvalue, subtask.new Binding(inputName,
                  stepNameR, "$this." + changedName, true));

            if ( inputNum1 != inputNum2 ) {
               subtask.addBinding("$this." + changedName, subtask.new Binding(
                     changedName, "this", inputBindingValue, true));

               for (int i = task.getDeclaredOutputs().size() - 1; i >= (task
                     .getDeclaredOutputs().size() - step.getType()
                     .getDeclaredOutputNames().size()); i--) {
                  if ( step.getType().getModified(inputName) != null
                     && task.getDeclaredOutputs().get(i).getName()
                           .contains(step.getType().getModified(inputName)) ) {
                     task.getDeclaredInputs()
                           .get(task.getDeclaredInputs().size() - 1)
                           .setModified(task.getDeclaredOutputs().get(i));
                     break;
                  }
               }
            }

         }
         
         for(String inputName:inputs){           
               
               for (DecompositionClass sub : subtask.getStep(stepNameR).getType().getDecompositions()) {
                     sub.removeBindingInput(inputName);
               }
            
         }
      }

      // ordering
      subtask.addOrdering();
      
      

      return task;

   }

   public void learnedTaskmodel () {

      
      Iterator<edu.wpi.cetask.TaskClass> tasksIterator = this.externalTaskModel
            .getTaskClasses().iterator();

      while (tasksIterator.hasNext()) {

         edu.wpi.cetask.TaskClass task = tasksIterator.next();

         TaskClass domTask = new TaskClass(taskModel, task.getId(),new QName(task.getQName().getNamespaceURI(),task.getQName().getLocalPart()));
         this.taskModel.add(domTask);
         for (String outputName : task.getDeclaredOutputNames()) {

            TaskClass.Output outputTask = domTask.new Output(outputName,
                  task.getSlotType(outputName));
            domTask.addOutput(outputTask);

         }

         for (String inputName : task.getDeclaredInputNames()) {

            TaskClass.Input inputTask = null;
            for (TaskClass.Output out : domTask.getDeclaredOutputs()) {
               if ( out.getName().equals(task.getModified(inputName)) ) {
                  inputTask = domTask.new Input(inputName,
                        task.getSlotType(inputName), out);
                  break;
               }
            }
            if ( inputTask == null ) {
               inputTask = domTask.new Input(inputName,
                     task.getSlotType(inputName), null);
            }
            domTask.addInput(inputTask);
         }

         List<edu.wpi.cetask.DecompositionClass> decompositions = task
               .getDecompositions();
         for (edu.wpi.cetask.DecompositionClass subtaskDecomposition : decompositions) {

            // subtaskDecomposition.getQName()
            DecompositionClass subtask = new DecompositionClass(taskModel,
                  subtaskDecomposition.getId(),
                  subtaskDecomposition.getQName(),
                  subtaskDecomposition.isOrdered(), domTask);
            subtask.setQname(subtaskDecomposition.getQName());
            domTask.addDecompositionClass(subtask);
            
            for (String stepName : subtaskDecomposition.getStepNames()) {
              
               TaskClass taskType = this.taskModel.getTaskClass(subtaskDecomposition.getStepType(stepName).getId());
               if(taskType==null){
                  taskType = new TaskClass(
                        taskModel, subtaskDecomposition.getStepType(stepName).getId(), subtaskDecomposition.getStepType(stepName).getQName());
                  
                     for(String out:subtaskDecomposition.getStepType(stepName).getDeclaredOutputNames()){

                           TaskClass.Output outputTask = taskType.new Output(out,
                                 subtaskDecomposition.getStepType(stepName).getSlotType(out));
                           taskType.addOutput(outputTask);

                     }
                     
                  for(String in:subtaskDecomposition.getStepType(stepName).getDeclaredInputNames()){
                     
                     TaskClass.Input inputC = null;
                     for (TaskClass.Output out : taskType.getDeclaredOutputs()) {
                        if (subtaskDecomposition.getStepType(stepName).getModified(in)!=null && out.getName().equals(subtaskDecomposition.getStepType(stepName).getModified(in)) ) {
                           inputC = taskType.new Input(in,subtaskDecomposition.getStepType(stepName).getSlotType(in),null);
                           break;
                        }
                     }
                     if ( inputC == null ) {
                        inputC = taskType.new Input(in,
                              subtaskDecomposition.getStepType(stepName).getSlotType(in), null);
                     }
                     taskType.addInput(inputC);
                  }
                  
                  
                  
               }
               DecompositionClass.Step step = subtask.new Step(taskType, 1, 1,
                     null);
               // since isOrdered is true so this function getRequiredStepNames(stepName) will return all of the required steps
               if(!subtaskDecomposition.isOrdered()){
                  for(String require:subtaskDecomposition.getRequiredStepNames(stepName)){
                     step.addRequired(require);
                  }
               }
               subtask.addStep(stepName, step);
               
               // step.setNamespace(subtaskDecomposition.getStepType(stepName)
               // .getNamespace());

            }

            Collection<Entry<String, edu.wpi.cetask.DecompositionClass.Binding>> bindingsSubtask = subtaskDecomposition
                  .getBindings().entrySet();

            if ( subtaskDecomposition.getApplicable() != null
               && subtaskDecomposition.getApplicable() != "" ) {

               subtask.setApplicable(subtaskDecomposition.getApplicable());
            }

            for (Entry<String, edu.wpi.cetask.DecompositionClass.Binding> binding : bindingsSubtask) {

               subtask.addBinding(
                     binding.getKey(),
                     subtask.new Binding(binding.getValue().slot, binding
                           .getValue().step, binding.getValue().value, binding
                           .getValue().inputInput)); // Error

            }

         }

      }
   }

   public void readDOM (Disco disco, String fileName) {
      this.externalTaskModel = disco.getInteraction().load(fileName);
   }
   
   

}