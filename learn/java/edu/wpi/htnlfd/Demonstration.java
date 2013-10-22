package edu.wpi.htnlfd;

import edu.wpi.cetask.Task;
import edu.wpi.disco.*;
import edu.wpi.disco.lang.Utterance;
import edu.wpi.htnlfd.model.*;
import edu.wpi.htnlfd.model.DecompositionClass.*;
import java.util.*;
import java.util.Map.Entry;
import javax.script.*;
import javax.xml.namespace.QName;

public class Demonstration {

   private TaskModel taskModel;

   /** The external task model from disco. */
   private edu.wpi.cetask.TaskModel externalTaskModel = null;

   private InputTransformation inputTransformation = new InputTransformation();

   public Demonstration () {

   }

   /**
    * Find demonstration. This function searches in disco's stack and finds the
    * last demonstrations. Algorithm: Since we know that all of our demonstrated
    * tasks are in "Demonstration" segment, we can just search for all of
    * segments in that segment.
    */
   public List<edu.wpi.cetask.Task> findDemonstration (Disco disco) {
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
    * Builds the task model. This function uses the demonstrated tasks and
    * learned tasks, and adds them to the taskmodel. Algorithm: Calling all
    * "demonstratedTask" and "learnedTaskmodel" and "addAlternativeRecipe"
    * functions.
    */
   public TaskModel buildTaskModel (Disco disco, String taskName,
         List<edu.wpi.cetask.Task> steps, String input)
         throws NoSuchMethodException, ScriptException {
      taskModel = new TaskModel();
      if ( this.externalTaskModel != null ) {
         learnedTaskmodel();
      }

      TaskClass newTask = demonstratedTask(disco, taskName, steps);
      TaskClass task = isAlternativeRecipe(newTask);
      if ( task != null ) {
         TaskClass.Input inputC = task.new Input(input, "boolean", null);
         task.addInput(inputC);
         task.getDecompositions().get(0).setApplicable("$this." + input);

         addAlternativeRecipe(newTask, "!$this." + input, task);

      } else
         this.taskModel.add(newTask);

      inputTransformation.transform(this.taskModel);

      this.taskModel.isEquivalent();

      return this.taskModel;
   }

   /**
    * Checks if is alternative recipe. This function checks whether the new
    * demonstrated task is an alternative recipe for previous tasks or not.
    * Algorithm: This function just checks the name of the new task to checks
    * whether it is an alternative recipe or new task. It also checks that the
    * new task is not equivalent to the previous ones.
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
    * Adds the alternative recipe. This function first checks whether a new
    * demonstrated task is alternative recipe or not. If it is an alternative
    * recipe, it will add it to the it's task. Algorithm: This function checks
    * all of the inputs' binding values to find out whether it should add
    * another input to this task or not. (It also adds inputs to parents.)
    */
   public boolean addAlternativeRecipe (TaskClass newTask, String applicable,
         TaskClass task) {

      if ( task != null ) {

         if ( applicable != null && applicable != "" )
            newTask.getDecompositions().get(0).setApplicable(applicable);

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
                        .get(0).getBindingValue("this", in1.getName());

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
                     } else {
                        // removed1.put("$this." + in2.getName(),null);
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
                  if ( bindedInput.get(rem.getKey()) != null )
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

         Map<String, Binding> bindChange = new HashMap<String, Binding>();
         Map<String, String> changed = new HashMap<String, String>();

         for (TaskClass.Output out : newTask.getDeclaredOutputs()) {
            String outputName = task.getDecompositions().get(1).getId() + "_"
               + out.getName();

            for (Entry<String, Binding> binding : task.getDecompositions()
                  .get(1).getBindings().entrySet()) {
               if ( binding.getKey().equals("$this." + out.getName()) ) {
                  bindChange.put(binding.getKey(), binding.getValue());
                  changed.put(binding.getKey(), "$this." + outputName);
               }
            }
            TaskClass.Output outCC = task.new Output(outputName, out.getType());
            task.addOutput(outCC);

            // output
            addSlotToParents(task, task.getDecompositions().get(1), taskModel,
                  false, outputName, out.getType(), null, null);

         }
         for (TaskClass.Input in : newTask.getDeclaredInputs()) {
            String inputName = task.getDecompositions().get(1).getId() + "_"
               + in.getName();

            for (Entry<String, Binding> binding : task.getDecompositions()
                  .get(1).getBindings().entrySet()) {
               if ( binding.getKey().equals("$this." + in.getName()) ) {
                  bindChange.put(binding.getKey(), binding.getValue());
                  changed.put(binding.getKey(), "$this." + inputName);
               } else if ( binding.getValue().getValue()
                     .equals("$this." + in.getName()) ) {
                  binding.getValue().setValue("$this." + inputName);
               }
            }

            String modified = null;
            TaskClass.Output outputModified = null;
            if ( in.getModified() != null ) {
               modified = task.getDecompositions().get(1).getId() + "_"
                  + in.getModified().getName();
               outputModified = task.getOutput(modified);
            }
            TaskClass.Input inputCC = task.new Input(inputName, in.getType(),
                  outputModified);
            task.addInput(inputCC);
            String binding = task.getDecompositions().get(1).getBindings()
                  .get("$this." + in.getName()).getValue();

            // input
            boolean binded = addSlotToParents(task, task.getDecompositions()
                  .get(1), taskModel, true, inputName, in.getType(), binding,
                  modified);

            if ( binded ) {
               task.getDecompositions().get(1)
                     .removeBinding("$this." + in.getName());
            }
         }

         for (Entry<String, Binding> binding : bindChange.entrySet()) {

            if ( newTask.getDecompositions().get(0)
                  .removeBinding(binding.getKey()) )
               newTask
                     .getDecompositions()
                     .get(0)
                     .addBinding(changed.get(binding.getKey()),
                           binding.getValue());
         }

         return true;
      }
      return false;

   }

   /**
    * Adds the slot to parents. If a subtask is added to one of the child tasks,
    * this function adds it's input or output to it's parents
    */
   public boolean addSlotToParents (TaskClass parentTask,
         DecompositionClass parentSubtask, TaskModel taskModel, boolean type,
         String slotName, String slotType, String binding, String modified) {
      List<Object[]> parents = parentSubtask.findParents(parentTask, null,
            parentSubtask, taskModel);

      for (Object[] parent : parents) {
         TaskClass task = (TaskClass) (parent[0]);
         DecompositionClass subtask = (DecompositionClass) parent[1];
         Entry<String, Step> step = (Entry<String, Step>) parent[2];

         this.inputTransformation.transform(parents, slotName, slotType,
               modified, type);
      }

      if ( type && parents != null && parents.size() != 0 ) {

         DecompositionClass subtask = (DecompositionClass) parents.get(parents
               .size() - 1)[1];

         Entry<String, Step> step = (Entry<String, Step>) parents.get(parents
               .size() - 1)[2];

         subtask.addBinding("$this." + step.getKey() + "_" + slotName,
               subtask.new Binding(step.getKey() + "_" + slotName, "this",
                     binding, true));

         return true;

      }
      return false;

   }

   /**
    * Demonstrated task. This function uses the new demonstrated tasks and
    * returns the generated TaskClass. (with subtask and inputs and outputs and
    * bindings) Algorithm: This function converts all of the disco's data into
    * our classes. It also calls addOrdering function to add ordering
    * constraints. It adds all of the subtask's steps' inputs and bindings to
    * the new task.
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

      for (edu.wpi.cetask.Task step : steps) {

         String stepName = step.getType().getId();
         // finding step's name

         DecompositionClass.Step stp = subtask.new Step(null, 1, 1, null);
         String stepNameR = stp.findStepName(stepName);

         stp.setType(stp.findGoal(taskModel, step));

         // stp.setNamespace(step.getType().getNamespace());
         subtask.addStep(stepNameR, stp);
         subtask.setGoal(task);

         task.addOutputsBindings(step, stepNameR, subtask);

         List<String> inputs = task.addInputsBindings(taskModel, step,
               stepNameR, subtask, disco);

         for (String inputName : inputs) {

            for (DecompositionClass sub : subtask.getStep(stepNameR).getType()
                  .getDecompositions()) {
               sub.removeBindingInput(inputName);
            }

         }
      }

      // ordering
      subtask.addOrdering(taskModel);

      return task;

   }

   /**
    * Learned taskmodel. This function add the learned tasks(finds them from
    * disco's loaded taskmodel) to current taskmodel. Algorithm: This function
    * converts all of the disco's data into our classes.
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

         domTask.setPrecondition(task.getPrecondition());
         domTask.setPostcondition(task.getPostcondition());
         domTask.setSufficient(task.isSufficient());
         domTask.setPrimitive(task.isPrimitive());
         
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

               step.setMinOccurs(subtaskDecomposition
                     .getMinOccursStep(stepName));
               step.setMaxOccurs(subtaskDecomposition
                     .getMaxOccursStep(stepName));

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
    * Read dom. This function load the new taskModel into disco.(By calling
    * "load" function)
    */
   public void readDOM (Disco disco, String fileName) {
      this.externalTaskModel = disco.getInteraction().load(fileName);
   }

   /**
    * Adds the step. This function adds the specified steps the end of a subtask
    * of a task.
    */
   public TaskModel addSteps (Disco disco, String taskName, String subtaskId,
         String afterStep) throws NoSuchMethodException, ScriptException {
      List<Task> steps = findDemonstration(disco);
      TaskClass task = this.taskModel.getTaskClass(taskName);
      DecompositionClass subtask = task.getDecomposition(subtaskId);

      for (edu.wpi.cetask.Task step : steps) {

         String stepName = step.getType().getId();

         DecompositionClass.Step stp = subtask.new Step(null, 1, 1, null);
         String stepNameR = stp.findStepName(stepName);

         stp.setType(stp.findGoal(taskModel, step));

         if ( afterStep == null || afterStep == "" )
            subtask.addStep(stepNameR, stp);
         else
            subtask.addStep(stepNameR, stp, afterStep);
         subtask.setGoal(task);

         task.addOutputsBindings(step, stepNameR, subtask);

         List<String> inputs = task.addInputsBindings(taskModel, step,
               stepNameR, subtask, disco);

         for (String inputName : inputs) {

            for (DecompositionClass sub : subtask.getStep(stepNameR).getType()
                  .getDecompositions()) {
               sub.removeBindingInput(inputName);
            }

         }
      }

      // ordering
      subtask.addOrdering(taskModel);

      return taskModel;

   }

   /**
    * Adds the optional step.
    */
   public TaskModel addOptionalStep (String taskName, String subtaskId,
         String stepName) {

      TaskClass task = this.taskModel.getTaskClass(taskName);
      DecompositionClass subtask = task.getDecomposition(subtaskId);
      DecompositionClass.Step step = subtask.getStep(stepName);
      step.setMinOccurs(0);

      return taskModel;
   }

   /**
    * Makes the step to be repeated
    */
   public TaskModel addMaxOccurs (String taskName, String subtaskId,
         String stepName, int maxOccurs) {

      TaskClass task = this.taskModel.getTaskClass(taskName);
      DecompositionClass subtask = task.getDecomposition(subtaskId);
      DecompositionClass.Step step = subtask.getStep(stepName);
      step.setMaxOccurs(maxOccurs);

      return taskModel;
   }

   /**
    * Adds the alternative recipe.
    */
   public TaskModel addAlternativeRecipe (Disco disco, String taskName,
         String applicable) throws NoSuchMethodException, ScriptException {

      List<Task> steps = findDemonstration(disco);
      TaskClass task = this.taskModel.getTaskClass(taskName);
      TaskClass newTask = demonstratedTask(disco, taskName, steps);
      addAlternativeRecipe(newTask, applicable, task);

      return taskModel;
   }

   /**
    * Adds an ordering constraint to a step.
    */
   public TaskModel addOrderStep (String taskName, String subtaskId,
         String stepNameDep, String stepNameRef) {

      TaskClass task = this.taskModel.getTaskClass(taskName);
      DecompositionClass subtask = task.getDecomposition(subtaskId);
      subtask.setOrdered(false);
      DecompositionClass.Step step = subtask.getStep(stepNameDep);
      step.addRequired(stepNameRef);

      return taskModel;
   }

   /**
    * Makes the steps of a subtask completely ordered
    */
   public TaskModel setOrdered (String taskName, String subtaskId) {

      TaskClass task = this.taskModel.getTaskClass(taskName);
      DecompositionClass subtask = task.getDecomposition(subtaskId);
      subtask.setOrdered(true);

      for (Entry<String, Step> step : subtask.getSteps().entrySet()) {
         step.getValue().removeRequired();
      }

      return taskModel;
   }

   /**
    * Adds the applicable condition to a subtask.
    */
   public TaskModel addApplicable (String taskName, String subtaskId,
         String condition) {

      TaskClass task = this.taskModel.getTaskClass(taskName);
      DecompositionClass subtask = task.getDecomposition(subtaskId);
      subtask.setApplicable(condition);

      return taskModel;
   }

   /**
    * Adds the precondition to a task.
    */
   public TaskModel addPrecondition (String taskName, String precondition) {

      TaskClass task = this.taskModel.getTaskClass(taskName);
      task.setPrecondition(precondition);

      return taskModel;
   }

   /**
    * Adds the postcondition to a task.
    */
   public TaskModel addPostcondition (String taskName, String postcondition,
         boolean sufficient) {

      TaskClass task = this.taskModel.getTaskClass(taskName);
      task.setPostcondition(postcondition);
      task.setSufficient(sufficient);

      return taskModel;
   }

   /**
    * Adds the output to a task.
    */
   public TaskModel addOutput (String taskName, String outputName,
         String outputType) {

      TaskClass task = this.taskModel.getTaskClass(taskName);
      TaskClass.Output outputC = null;
      outputC = task.new Output(outputName, outputType);
      task.addOutput(outputC);

      return taskModel;
   }

   /**
    * Adds the input to a task.
    */
   public TaskModel addInput (String taskName, String inputName, String type,
         String modified) {

      TaskClass task = this.taskModel.getTaskClass(taskName);
      TaskClass.Output outputC = null;
      if ( modified != null && modified != "" )
         outputC = task.getOutput(modified);

      TaskClass.Input inputC = task.new Input(inputName, type, outputC);
      task.addInput(inputC);

      return taskModel;
   }

}