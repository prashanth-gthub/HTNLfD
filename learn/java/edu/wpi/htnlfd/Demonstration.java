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

// TODO: Auto-generated Javadoc
/**
 * The Class Demonstration.
 */
public class Demonstration {

   /** The task model. */
   private TaskModel taskModel;

   /** The external task model. */
   private edu.wpi.cetask.TaskModel externalTaskModel = null;

   /** The transformation. */
   private InputTransformation transformation = new InputTransformation();

   /**
    * Instantiates a new demonstration.
    */
   public Demonstration () {

   }

   /**
    * Find demonstration.
    *
    *This function searches in disco's stack and finds the last demonstrations.
      Algorithm: Since we know that all of our demonstrated tasks are in "Demonstration" 
      segment, we can just search for all of segments in that segment.
    *
    * @param disco the disco
    * @param taskName the task name
    * @return the list
    */
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

   /**
    * Builds the task model.
    *
    *This function uses the demonstrated tasks and learned tasks, and adds them to the taskmodel.
      Algorithm: Calling all "demonstratedTask" and "learnedTaskmodel" and "addAlternativeRecipe" 
      functions.
    *
    * @param disco the disco
    * @param taskName the task name
    * @param steps the steps
    * @param input the input
    * @return the task model
    * @throws NoSuchMethodException the no such method exception
    * @throws ScriptException the script exception
    */
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

   /**
    * Checks if is alternative recipe.
    *
    *This function checks whether the new demonstrated task is an alternative recipe for previous tasks or not. 
      Algorithm: This function just checks the name of the new task to checks whether it is an alternative recipe or new task.
      It also checks that the new task is not equivalent to the previous ones.
    *
    * @param newTask the new task
    * @return the task class
    */
   public TaskClass isAlternativeRecipe (TaskClass newTask) {
      Iterator<TaskClass> tasksIterator = this.taskModel.getTaskClasses()
            .iterator();
      TaskClass task = null;

      while (tasksIterator.hasNext()) {

         task = tasksIterator.next();

         if ( task.isEquivalent(newTask, taskModel) )
            return null;

         if ( task.getId().equals(newTask.getId()) ) {
            return task;

         }
      }
      return null;
   }

   /**
    * Adds the alternative recipe.
    *
    *This function first checks whether a new demonstrated task is alternative recipe or not. If it is an alternative recipe, it will add 
      it to the it's task.
      Algorithm: This fucntion checks all of the inputs' binding values to find out whether it should add another input to this task or not.
      (It doesn't add inputs to parents.)
    *
    *
    * @param newTask the new task
    * @param input the input
    * @return true, if successful
    */
   @SuppressWarnings("unchecked")
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
         Map<String, String> bindedInput = new HashMap<String, String>();
         Map<String, String> bindedOutput = new HashMap<String, String>();
         for (TaskClass.Input in1 : task.getDeclaredInputs()) {
            for (TaskClass.Input in2 : newTask.getDeclaredInputs()) {
               if ( in1.getType().equals(in2.getType())
                  && ((in1.getModified() == null && in2.getModified() == null) || (in1
                        .getModified() != null && in2.getModified() != null)) ) {
                  Entry<String, Binding> bindValue = task.getDecompositions()
                        .get(0).getBindingStep("this", in1.getName());

                  String value = null;
                  if ( bindValue != null ) {
                     value = bindValue.getValue().getValue();
                  }
                  if ( value == null )
                     value = newTask
                           .getDecompositions()
                           .get(0)
                           .findValueInParents(taskModel, null, task,
                                 task.getDecompositions().get(0), in1.getName());
                  if ( newTask.getDecompositions().get(0).getBindings()
                        .get("$this." + in2.getName()).getValue().equals(value) ) {
                     if ( bindValue == null ) {
                        removed1.put("$this." + in2.getName(),
                              newTask.getDecompositions().get(0).getBindings()
                                    .get("$this" + in2.getName()));
                        bindedInput.put("$this." + in2.getName(), "$this."
                           + in1.getName());
                        if ( in2.getModified() != null ) {
                           if ( !in2.getModified().getName()
                                 .equals(in1.getModified().getName()) )
                              bindedOutput.put("$this."
                                 + in2.getModified().getName(), "$this."
                                 + in1.getModified().getName());
                        }
                     }
                  }
               }
            }
         }

         Map<String, Binding> bindedOutputADD = new HashMap<String, Binding>();
         Map<String, Binding> bindedOutputRemove = new HashMap<String, Binding>();

         for (Entry<String, Binding> rem : removed1.entrySet()) {
            newTask.getDecompositions().get(0).removeBinding(rem.getKey());

            for (Entry<String, Binding> binding : newTask.getDecompositions()
                  .get(0).getBindings().entrySet()) {
               if ( binding.getValue().getValue().equals(rem.getKey()) ) {
                  binding.getValue().setValue(bindedInput.get(rem.getKey()));
               }
               String valOut = bindedOutput.get(binding.getKey());
               if ( valOut != null ) {
                  bindedOutputADD.put(valOut, binding.getValue());
                  bindedOutputRemove.put(binding.getKey(), binding.getValue());
               }
            }

            for (TaskClass.Input in : newTask.getDeclaredInputs()) {
               if ( in.getName().equals(rem.getKey().substring(6)) ) {
                  newTask.getDeclaredInputs().remove(in);
                  if ( in.getModified() != null )
                     newTask.getDeclaredOutputs().remove(in.getModified());
                  break;
               }
            }
         }

         for (Entry<String, Binding> binding : bindedOutputADD.entrySet()) {
            newTask.getDecompositions().get(0)
                  .addBinding(binding.getKey(), binding.getValue());
         }
         for (Entry<String, Binding> binding : bindedOutputRemove.entrySet()) {
            newTask.getDecompositions().get(0).removeBinding(binding.getKey());
         }

         /*
          * for(TaskClass.Output out : newTask.getDeclaredOutputs()){
          * TaskClass.Output outCC = task.new
          * Output(newTask.getDecompositions().get(0).getId()+"_"+out.getName(),
          * out.getType()); task.addOutput(outCC); } for(TaskClass.Input in :
          * newTask.getDeclaredInputs()){ TaskClass.Input inputCC = task.new
          * Input(newTask.getDecompositions().get(0).getId()+"_"+in.getName(),
          * in.getType(), in.getModified()); task.addInput(inputCC); }
          */

         // not added input and outputs.
         // end
         return true;
      }
      return false;

   }

   /**
    * Demonstrated task.
    *
    *This function uses the new demonstrated tasks and returns the generated TaskClass.(with subtask and inputs and outputs and bindings)
      Algorithm: This function converts all of the disco's data into our classes. It also calls addOrdering function to add ordering constraints.
      It adds all of the subtask's steps' inputs and bindings to the new task.
    *
    * @param disco the disco
    * @param taskName the task name
    * @param steps the steps
    * @return the task class
    * @throws NoSuchMethodException the no such method exception
    * @throws ScriptException the script exception
    */
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
         for (TaskClass goalI : this.taskModel.getTaskClasses()) {
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
         DecompositionClass.Step stp = subtask.new Step(goal, 1, 1, null);
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

         for (String inputName : inputs) {

            for (DecompositionClass sub : subtask.getStep(stepNameR).getType()
                  .getDecompositions()) {
               sub.removeBindingInput(inputName);
            }

         }
      }

      // ordering
      subtask.addOrdering();

      return task;

   }

   /**
    * Learned taskmodel.
    * 
    * This function add the learned tasks(finds them from disco's loaded taskmodel) to current taskmodel.
      Algorithm: This function converts all of the disco's data into our classes.
      
    */
   public void learnedTaskmodel () {

      Iterator<edu.wpi.cetask.TaskClass> tasksIterator = this.externalTaskModel
            .getTaskClasses().iterator();

      while (tasksIterator.hasNext()) {

         edu.wpi.cetask.TaskClass task = tasksIterator.next();

         TaskClass domTask = new TaskClass(taskModel, task.getId(), new QName(
               task.getQName().getNamespaceURI(), task.getQName()
                     .getLocalPart()));
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

               TaskClass taskType = this.taskModel
                     .getTaskClass(subtaskDecomposition.getStepType(stepName)
                           .getId());
               if ( taskType == null ) {
                  taskType = new TaskClass(taskModel, subtaskDecomposition
                        .getStepType(stepName).getId(), subtaskDecomposition
                        .getStepType(stepName).getQName());

                  for (String out : subtaskDecomposition.getStepType(stepName)
                        .getDeclaredOutputNames()) {

                     TaskClass.Output outputTask = taskType.new Output(out,
                           subtaskDecomposition.getStepType(stepName)
                                 .getSlotType(out));
                     taskType.addOutput(outputTask);

                  }

                  for (String in : subtaskDecomposition.getStepType(stepName)
                        .getDeclaredInputNames()) {

                     TaskClass.Input inputC = null;
                     for (TaskClass.Output out : taskType.getDeclaredOutputs()) {
                        if ( subtaskDecomposition.getStepType(stepName)
                              .getModified(in) != null
                           && out.getName().equals(
                                 subtaskDecomposition.getStepType(stepName)
                                       .getModified(in)) ) {
                           inputC = taskType.new Input(in, subtaskDecomposition
                                 .getStepType(stepName).getSlotType(in), null);
                           break;
                        }
                     }
                     if ( inputC == null ) {
                        inputC = taskType.new Input(in, subtaskDecomposition
                              .getStepType(stepName).getSlotType(in), null);
                     }
                     taskType.addInput(inputC);
                  }

               }
               DecompositionClass.Step step = subtask.new Step(taskType, 1, 1,
                     null);
               // since isOrdered is true so this function
               // getRequiredStepNames(stepName) will return all of the required
               // steps
               if ( !subtaskDecomposition.isOrdered() ) {
                  for (String require : subtaskDecomposition
                        .getRequiredStepNames(stepName)) {
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

   /**
    * Read dom.
    *
    *This function load the new taskModel into disco.(By calling "load" fucntion)
    *
    * @param disco the disco
    * @param fileName the file name
    */
   public void readDOM (Disco disco, String fileName) {
      this.externalTaskModel = disco.getInteraction().load(fileName);
   }

}