package edu.wpi.htnlfd;

import edu.wpi.disco.*;
import edu.wpi.disco.lang.Utterance;
import edu.wpi.htnlfd.model.*;
import edu.wpi.htnlfd.model.DecompositionClass.Binding;
import edu.wpi.htnlfd.model.DecompositionClass.Step;
import java.util.*;
import java.util.Map.Entry;
import javax.script.*;

public class Demonstration {

   TaskModel taskModel = new TaskModel();

   edu.wpi.cetask.TaskModel externalTaskModel = null;

   private final String ReferenceFrame = "referenceFrame";

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

   public List<TaskClass> buildTaskModel (Disco disco, String taskName,
         List<edu.wpi.cetask.Task> steps, String input)
         throws NoSuchMethodException, ScriptException {

      if ( this.externalTaskModel != null ) {
         learnedTaskmodel();
      }
      TaskClass newTask = demonstratedTask(disco, taskName, steps);
      if ( !checkRecipe(newTask, input) ) {
         this.taskModel.add(newTask);
      }

      removeBindingsFromChildren(newTask);

      inputGeneralization();

      return this.taskModel.getTaskClasses();
   }

   private void inputGeneralization () {
      Iterator<TaskClass> tasksIterator = this.taskModel.getTaskClasses()
            .iterator();
   }

   public boolean checkRecipe (TaskClass newTask, String input) {

      Iterator<TaskClass> tasksIterator = this.taskModel.getTaskClasses()
            .iterator();

      while (tasksIterator.hasNext()) {

         TaskClass task = tasksIterator.next();

         if ( task.getId().equals(newTask.getId()) ) {

            TaskClass.Input inputC = task.new Input(input, "boolean", null);
            task.addInput(inputC);
            task.getDecompositions().get(0).setApplicable("$this." + input);
            newTask.getDecompositions().get(0).setApplicable("!$this." + input);
            newTask.getDecompositions().get(0)
                  .setId(task.getDecompositions().get(0).getId() + "1");
            task.getDecompositions().add(newTask.getDecompositions().get(0));
            return true;
         }
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
               + countSubtask, true);

      task.addDecompositionClass(subtask);
      Map<String, Integer> StepNames = new HashMap<String, Integer>();

      Map<String, edu.wpi.cetask.Task> outputs = new HashMap<String, edu.wpi.cetask.Task>();
      List<String> outputNumbers = new ArrayList<String>();
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

         DecompositionClass.Step stp = subtask.new Step(new TaskClass(
               taskModel, step.getType().getId()), 1, 1, null);
         String stepNameR = Character.toLowerCase(stepName.charAt(0))
            + (stepName.length() > 1 ? stepName.substring(1) : "") + count;
         stp.setNamespace(step.getType().getNamespace());
         subtask.addStep(stepNameR, stp);
         subtask.setGoal(task);

         for (String outputName : step.getType().getDeclaredOutputNames()) {

            String bindingSlot = "$" + stepNameR + "." + outputName;

            String bindingSlotValue = null;
            bindingSlotValue = stepNameR+"_"+outputName;
            outputs.put(bindingSlotValue, step);

            subtask.getBindings().put("$this." + bindingSlotValue,
                  subtask.new Binding("this", stepNameR, bindingSlot, false));
            task.addOutput(task.new Output(bindingSlotValue, step.getType()
                  .getSlotType(outputName)));
         }

         for (String inputName : step.getType().getDeclaredInputNames()) {

            String bindingSlotvalue = "$" + stepNameR + "." + inputName;

            Object inputBinding = (((Invocable) disco.getScriptEngine())
                  .invokeFunction("find", step.getSlotValue(inputName)));

            String inputBindingValue = (String) inputBinding;
            int inputNum1 = task.getDeclaredInputs().size();
            String changedName = addInput(task, inputName, step.getType()
                  .getSlotType(inputName), inputBindingValue, subtask,stepNameR);
            int inputNum2 = task.getDeclaredInputs().size();
            subtask.addBinding(bindingSlotvalue, subtask.new Binding(stepNameR,
                  stepNameR, "$this." + changedName, true));

            if ( inputNum1 != inputNum2 ) {
               subtask.addBinding("$this." + changedName, subtask.new Binding(
                     "this", stepNameR, inputBindingValue, true));

               
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
      }

      // ordering
      addOrdering(subtask, task);

      return task;

   }

   private String addInput (TaskClass task, String inputName, String inputType,
         String inputBindingValue, DecompositionClass subtask, String prefix) {
      boolean contain = false;
      String name = null;
      for (Entry<String, Binding> bind : subtask.getBindings().entrySet()) {

         if ( bind.getKey().contains("this")
            && bind.getValue().isInputInput()
            && ((inputName.contains(ReferenceFrame) && bind.getKey().contains(
                  ReferenceFrame)) || (!inputName.contains(ReferenceFrame) && !bind
                  .getKey().contains(ReferenceFrame))) ) {
            if ( bind.getValue().getValue().equals(inputBindingValue) ) {
               contain = true;
               name = bind.getKey().substring(6);
               break;
            }
         }
      }
      if ( contain ) {
         return name;
      } else {
         if ( name == null )
            name = prefix +"_"+ inputName;
         String prefixName = prefix+name.substring(name.indexOf("_"));
         
         TaskClass.Input inputC = task.new Input(prefixName, inputType, null);
         task.addInput(inputC);
         return prefixName;
      }
   }

   private void addOrdering (DecompositionClass subtask, TaskClass task) {
     
      for (Entry<String, Binding> bindingDep : subtask.getBindings().entrySet()) {
         if ( bindingDep.getKey().contains(ReferenceFrame)
            && !bindingDep.getKey().contains("this") ) {
            for (Entry<String, Binding> bindingRef : subtask.getBindings()
                  .entrySet()) {
               if ( !bindingRef.getKey().contains("this")
                  && !bindingRef.getKey().contains(ReferenceFrame) && !bindingDep.getValue().getStep().equals(bindingRef.getValue().getStep()) ) {

                  String valueRef = null;
                  String inputRef = null;
                  String valueDep = null;
                  for (Entry<String, Binding> binding : subtask.getBindings()
                        .entrySet()) {
                     if ( binding.getKey().equals(
                           bindingRef.getValue().getValue()) ) {
                        valueRef = binding.getValue().getValue();

                        if ( valueDep != null )
                           break;
                     }
                     if ( binding.getKey().equals(
                           bindingDep.getValue().getValue()) ) {
                        valueDep = binding.getValue().getValue();
                        if ( valueRef != null )
                           break;
                     }
                  }
                  if ( valueDep.equals(valueRef) ) {
                     inputRef = bindingRef.getValue().getValue().substring(6);

                     for (TaskClass.Input inputs : task.getDeclaredInputs()) {
                        if ( inputs.getName().equals(inputRef)
                           && inputs.getModified() != null ) {
                           for (Entry<String, Step> step : subtask.getSteps()
                                 .entrySet()) {
                              if ( step.getKey().equals(bindingDep.getValue().getStep()) ) {
                                 step.getValue().addRequired(bindingRef.getValue().getStep());
                                 subtask.setOrdered(false);
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

   public void learnedTaskmodel () {

      Iterator<edu.wpi.cetask.TaskClass> tasksIterator = this.externalTaskModel
            .getTaskClasses().iterator();

      while (tasksIterator.hasNext()) {

         edu.wpi.cetask.TaskClass task = tasksIterator.next();

         TaskClass domTask = new TaskClass(taskModel, task.getId());

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
                  subtaskDecomposition.getId(), null,
                  subtaskDecomposition.isOrdered());

            subtask.setGoal(domTask);

            for (String stepName : subtaskDecomposition.getStepNames()) {

               DecompositionClass.Step step = subtask.new Step(domTask, 1, 1,
                     subtaskDecomposition.getRequiredStepNames(stepName));
               subtask.addStep(subtaskDecomposition.getStepType(stepName)
                     .getId(), step);

               step.setNamespace(subtaskDecomposition.getStepType(stepName)
                     .getNamespace());

            }

            Collection<Entry<String, edu.wpi.cetask.DecompositionClass.Binding>> bindingsSubtask = subtaskDecomposition
                  .getBindings().entrySet();

            if ( subtaskDecomposition.getApplicable() != null
               && subtaskDecomposition.getApplicable() != "" ) {

               subtask.setApplicable(subtaskDecomposition.getApplicable());
            }

            for (Entry<String, edu.wpi.cetask.DecompositionClass.Binding> binding : bindingsSubtask) {

               subtask.getBindings().put(
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

   void removeBindingsFromChildren (TaskClass newTask) {

      for (DecompositionClass subtask : newTask.getDecompositions()) {

         for (Entry<String, Step> step : subtask.getSteps().entrySet()) {
            for (TaskClass task : this.taskModel.getTaskClasses()) {
               if ( task.getId().equals(step.getValue().getType().getId()) ) {
                  for (DecompositionClass sub : task.getDecompositions()) {
                     List<Object> removed = new ArrayList<Object>();
                     for (Entry<String, Binding> bind : sub.getBindings()
                           .entrySet()) {
                        if ( bind.getKey().contains("this")
                           && !bind.getValue().getValue().contains("$") ) {
                           removed.add(bind.getKey());
                        }
                     }

                     for (Object rem : removed) {

                        sub.getBindings().remove(rem);

                     }

                  }
               }
            }
         }

      }

   }

}