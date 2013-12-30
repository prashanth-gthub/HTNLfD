package edu.wpi.htnlfd;

import edu.wpi.cetask.Task;
import edu.wpi.disco.*;
import edu.wpi.disco.lang.*;
import edu.wpi.htnlfd.graph.Graph;
import edu.wpi.htnlfd.model.*;
import edu.wpi.htnlfd.model.DecompositionClass.Step;
import edu.wpi.htnlfd.model.TaskClass.Input;
import edu.wpi.htnlfd.model.TaskClass.Output;
import edu.wpi.htnlfd.model.DecompositionClass.*;
import edu.wpi.htnlfd.question.AskQuestion;
//import edu.wpi.htnlfd.table.TableKnowledgeBase;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import javax.script.*;
import javax.xml.namespace.QName;

public class Demonstration {

   private TaskModel taskModel;

   /** The external task model from disco. */
   private edu.wpi.cetask.TaskModel externalTaskModel = null;

   private InputTransformation inputTransformation = new InputTransformation();

   // private KnowledgeBase KB = new TableKnowledgeBase();

   private String defaultInputName = "input1";

   AskQuestion askQuestion = new AskQuestion();

   List<Pair> demonstrations = new ArrayList<Pair>();
   
   LearnAgent LA = new LearnAgent("Anahita");

   static {

   }

   public Demonstration () {

   }

   public class Pair {
      public Object left;

      public Object right;

      public Pair (Object left, Object right) {
         super();
         this.left = left;
         this.right = right;
      }

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
    * 
    * @throws IOException
    */
   public TaskModel buildTaskModel (Disco disco, String taskName,
         List<edu.wpi.cetask.Task> steps) throws NoSuchMethodException,
         ScriptException, IOException {
      taskModel = new TaskModel();

      if ( this.externalTaskModel != null ) {
         learnedTaskmodel();
      }

      TaskClass newTask = demonstratedTask(disco, taskName, steps);
      //findLoop(newTask); 

      demonstrations.add(new Pair(taskName, newTask)); // saving demonstrations

      TaskClass task = isAlternativeRecipe(newTask);

      // Graph graph = Graph.getGraph(taskName);

      // graph.addGraph(this,task,taskModel, newTask);
      // graph.printGraph();
      if ( task != null ) {

         Graph graph = new Graph();
         newTask.setId("Temp");
         graph.buildTree(task, newTask, taskModel, this);

         // String applicable = KB.getApplicable(task, newTask);

         /*
          * String input = defaultInputName; String applicable = "!this." +
          * input; if ( task.getDecompositions().get(0).getApplicable() == null
          * ) { task.getDecompositions().get(0).setApplicable("this." + input);
          * } TaskClass.Input inputC = task.new Input(input, "boolean", null);
          * task.addInput(inputC); addAlternativeRecipe(newTask, applicable,
          * task); // askQuestion(disco);
          */
         task.changeNameSpace(newTask);

      } else {
         this.taskModel.add(newTask);
         // optionals(this.taskModel);
      }

      // inputTransformation.generalizeInput(this.taskModel);

      this.taskModel.isEquivalent();

      //internalTaskQ();

      LA.question = askQuestion.Ask(taskModel);

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

         newTask
               .getDecompositions()
               .get(0)
               .setId(
                     task.getDecompositions().get(0)
                           .findDecompositionName(task.getId()));
         task.getDecompositions().add(newTask.getDecompositions().get(0));

         Map<String, Binding> removed1 = new HashMap<String, Binding>();
         Map<String, String> bindedInput = new HashMap<String, String>();
         Map<String, String> bindedOutput = new HashMap<String, String>();
         for (TaskClass.Input in1 : task.getDeclaredInputs()) {
            boolean contain = false;
            for (TaskClass.Input in2 : newTask.getDeclaredInputs()) {
               if ( in1.getType().equals(in2.getType())
                  && ((in1.getModified() == null && in2.getModified() == null) || (in1
                        .getModified() != null && in2.getModified() != null)) ) {
                  Entry<String, Binding> bindValue = null;
                  for(int tg=0;tg<task.getDecompositions().size()-1;tg++){
                     bindValue = task.getDecompositions()
                           .get(tg).getBindingValue("this", in1.getName()); ////
                     if(bindValue!=null)
                        break;
                  }
                  String value = null;
                  if ( bindValue != null ) {
                     value = bindValue.getValue().getValue();
                  }
                  if ( value == null ){
                     for(int tg=0;tg<task.getDecompositions().size()-1;tg++){
                        value = newTask
                              .getDecompositions()
                              .get(0)
                              .findValueInParents(taskModel, null, task,
                                    task.getDecompositions().get(tg), in1.getName()); ////
                        if(value!=null)
                           break;
                     }
                  }
                  if ( newTask.getDecompositions().get(0).getBindings()
                        .get("$this." + in2.getName()) != null
                     && newTask.getDecompositions().get(0).getBindings()
                           .get("$this." + in2.getName()).getValue()
                           .equals(value) ) {
                     contain = true;
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
            if ( !contain ) {
               // if they are not the same, the input is optional
               in1.setOptional(true);
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
            String outputName = newTask.getDecompositions().get(0).getId() + "_"
               + out.getName();

            for (Entry<String, Binding> binding : newTask.getDecompositions().get(0).getBindings().entrySet()) {
               if ( binding.getKey().equals("$this." + out.getName()) ) {
                  bindChange.put(binding.getKey(), binding.getValue());
                  changed.put(binding.getKey(), "$this." + outputName);
               }
            }
            TaskClass.Output outCC = task.new Output(outputName, out.getType());
            task.addOutput(outCC);

            // output
            addSlotToParents(task, newTask.getDecompositions().get(0), taskModel,
                  false, outputName, out.getType(), null, null);

         }
         for (TaskClass.Input in : newTask.getDeclaredInputs()) {
            String inputName = newTask.getDecompositions().get(0).getId() + "_"
               + in.getName();//

            for (Entry<String, Binding> binding : newTask.getDecompositions().get(0).getBindings().entrySet()) {
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
               modified = newTask.getDecompositions().get(0).getId() + "_"
                  + in.getModified().getName();
               outputModified = task.getOutput(modified);
            }
            TaskClass.Input inputCC = task.new Input(inputName, in.getType(),
                  outputModified);
            inputCC.setOptional(true);
            task.addInput(inputCC);
            String binding = newTask.getDecompositions().get(0).getBindings()
                  .get("$this." + in.getName()).getValue();

            // input
            boolean binded = addSlotToParents(task, newTask.getDecompositions().get(0), taskModel, true, inputName, in.getType(), binding,
                  modified);

            if ( binded ) {
               newTask.getDecompositions().get(0)
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

         task.changeNameSpace(newTask);
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

      String lastInputName = this.inputTransformation.transferBottomUp(parents,
            slotName, slotType, modified, type);

      if ( type && parents != null && parents.size() != 0 ) {

         DecompositionClass subtask = (DecompositionClass) parents.get(parents
               .size() - 1)[1];

//         @SuppressWarnings("unchecked")
//         Entry<String, Step> step = (Entry<String, Step>) parents.get(parents
//               .size() - 1)[2];

         subtask.addBinding("$this." + lastInputName,
               subtask.new Binding(lastInputName, "this", binding,
                     DecompositionClass.Type.Constant));

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
      task = new TaskClass(taskModel, taskName, new QName(taskModel.namespace,
            taskName));

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

      subtask.addOrderingByDataflow ();
      
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
         domTask.setPrimitive(task.isPrimitive());
         domTask.setInternal(task.isInternal());
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

            String optional = task.getProperty(task.getId() + "." + inputName
               + "@optional");
            if ( Boolean.parseBoolean(optional) )
               inputTask.setOptional(true);

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
                                 .getStepType(stepName).getSlotType(in), out);

                           break;
                        }
                     }
                     if ( inputC == null ) {
                        inputC = taskType.new Input(in, subtaskDecomposition
                              .getStepType(stepName).getSlotType(in), null);
                     }

                     String optional = task.getProperty(task.getId() + "." + in
                        + "@optional");
                     if ( Boolean.parseBoolean(optional) )
                        inputC.setOptional(true);
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

               DecompositionClass.Type type = null;

               if ( binding.getValue().inputInput )
                  type = DecompositionClass.Type.InputInput;
               else if ( binding.getValue().outputInput )
                  type = DecompositionClass.Type.OutputOutput;
               subtask.addBinding(
                     binding.getKey(),
                     subtask.new Binding(binding.getValue().slot, binding
                           .getValue().step, binding.getValue().value, type)); // Error

            }

         }

      }
      for (TaskClass mytask : taskModel.getTaskClasses()) {
         for (DecompositionClass dec : mytask.getDecompositions()) {
            for (String stepName : dec.getStepNames()) {
               Step step = dec.getStep(stepName);
               if ( step.getType().getDecompositions() == null
                  || step.getType().getDecompositions().size() == 0 ) {
                  TaskClass taskCl = taskModel.getTaskClass(step.getType()
                        .getId());
                  if ( taskCl != null )
                     step.setType(taskCl);
               }
            }
         }
      }

   }

   /**
    * Read dom. This function load the new taskModel into disco.(By calling
    * "load" function)
    */
   public void readDOM (Disco disco, String fileName) {
      this.externalTaskModel = disco.getInteraction().load(fileName + ".xml");
      disco.getInteraction().load("Tell.xml"); ///???
   }

   /**
    * Adds the step. This function adds the specified steps the end of a subtask
    * of a task.
    */
   public TaskModel addSteps (Disco disco, String subtaskId,
         String afterStep) throws NoSuchMethodException, ScriptException {

      getNewTaskModel();

      List<Task> steps = findDemonstration(disco);
      DecompositionClass subtask = this.taskModel.getDecompositionClass(subtaskId);
      TaskClass task = subtask.getGoal();
      

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
   public TaskModel addOptionalStep (String subtaskId,
         String stepName) {

      getNewTaskModel();

      DecompositionClass subtask = this.taskModel.getDecompositionClass(subtaskId);

      DecompositionClass.Step step = subtask.getStep(stepName);
      step.setMinOccurs(0);

      return taskModel;
   }

   /**
    * Makes the step to be repeated
    */
   public TaskModel addMaxOccurs (String subtaskId,
         String stepName, int maxOccurs) {

      getNewTaskModel();

      DecompositionClass subtask = this.taskModel.getDecompositionClass(subtaskId);

      DecompositionClass.Step step = subtask.getStep(stepName);
      step.setMaxOccurs(maxOccurs);
      int i=0;
      for(i=0;i<subtask.getStepNames().size();i++){
         if(subtask.getStepNames().get(i).equals(stepName))
         break;
      }
      List<String> rem = new ArrayList<String>();
      for(int j = i+1;j<subtask.getStepNames().size() && j<i+maxOccurs;j++){
         
         DecompositionClass.Step stepF = subtask.getStep(subtask.getStepNames().get(j));
         DecompositionClass.Step copyStepF = subtask.new Step(stepF);
         copyStepF.removeRequired(stepName);
         if(step.isEquivalent(copyStepF, taskModel)){
            if ( subtask.checkInputs(stepName,
                  step.getType(), subtask.getStepNames().get(j),
                  copyStepF.getType(), subtask, subtask, taskModel) ) {
               rem.add(subtask.getStepNames().get(j));
            }
         }
      }
      for(String re:rem){
         subtask.removeStepB(re);
      }
      
      LA.question = null; ///???
      return taskModel;
   }

   /**
    * Adds the alternative recipe.
    */
   public TaskModel addAlternativeRecipe (Disco disco, String taskName,
         String applicable) throws NoSuchMethodException, ScriptException {

      getNewTaskModel();

      List<Task> steps = findDemonstration(disco);
      TaskClass task = this.taskModel.getTaskClass(taskName);
      TaskClass newTask = demonstratedTask(disco, taskName, steps);
      addAlternativeRecipe(newTask, applicable, task);

      return taskModel;
   }

   public TaskModel answerQuestion (String taskName, String input) {

      getNewTaskModel();

      TaskClass task = taskModel.getTaskClass(taskName);
      Input in = task.getInput(defaultInputName);
      in.setName(input);
      for (DecompositionClass dec : task.getDecompositions()) {
         dec.setApplicable(dec.getApplicable().replace(defaultInputName, input));
      }
      defaultInputName = input;

      return taskModel;
   }

   /**
    * Adds an ordering constraint to a step.
    */
   public TaskModel addOrderStep (String subtaskId,
         String stepNameDep, String stepNameRef) {

      getNewTaskModel();

      DecompositionClass subtask = this.taskModel.getDecompositionClass(subtaskId);

      subtask.setOrdered(false);
      DecompositionClass.Step step = subtask.getStep(stepNameDep);
      step.addRequired(stepNameRef);

      return taskModel;
   }

   /**
    * Makes the steps of a subtask totally ordered
    */
   public TaskModel setOrdered (String subtaskId) {

      getNewTaskModel();

      DecompositionClass subtask = this.taskModel.getDecompositionClass(subtaskId);
  
      subtask.setOrdered(true);

      for (Entry<String, Step> step : subtask.getSteps().entrySet()) {
         step.getValue().removeRequireds();
      }
      
      

      return taskModel;
   }

   /**
    * Adds the applicable condition to a subtask.
    */
   public TaskModel addApplicable (String subtaskId,
         String condition) {

      getNewTaskModel();

      DecompositionClass subtask = this.taskModel.getDecompositionClass(subtaskId);

      subtask.setApplicable(condition);

      LA.question = askQuestion.Ask(taskModel);
      
      return taskModel;
   }

   /**
    * Adds the precondition to a task.
    */
   public TaskModel addPrecondition (String taskName, String precondition) {

      getNewTaskModel();

      TaskClass task = this.taskModel.getTaskClass(taskName);
      task.setPrecondition(precondition);

      LA.question = askQuestion.Ask(taskModel);
      
      return taskModel;
   }

   /**
    * Adds the postcondition to a task.
    */
   public TaskModel addPostcondition (String taskName, String postcondition,
         boolean sufficient) {

      getNewTaskModel();

      TaskClass task = this.taskModel.getTaskClass(taskName);
      task.setPostcondition(postcondition);
      task.setSufficient(sufficient);

      LA.question = askQuestion.Ask(taskModel);
      
      return taskModel;
   }

   /**
    * Adds the output to a task.
    */
   public TaskModel addOutput (String taskName, String outputName,
         String outputType) {

      getNewTaskModel();

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

      getNewTaskModel();

      TaskClass task = this.taskModel.getTaskClass(taskName);
      TaskClass.Output outputC = null;
      if ( modified != null && modified != "" )
         outputC = task.getOutput(modified);

      TaskClass.Input inputC = task.new Input(inputName, type, outputC);
      task.addInput(inputC);

      return taskModel;
   }

   /**
    * Finds whether there is a loop in the steps of the task or not by searching
    * for more than one consecutive same step.
    */
   public void findLoop (TaskClass task) {

      for (DecompositionClass dec : task.getDecompositions()) {
         if(dec.getStepNames().size() <=1)
            continue;
         Map<Step, List<Step>> steps = new HashMap<Step, List<Step>>();
         Map<Step, List<String>> names = new HashMap<Step, List<String>>();

         {
            int i = dec.getStepNames().size() - 1;
            // Finding more than one consecutive same steps
            while (i > 0) {
               int j = i - 1;
               while (j >= 0) {
                  Step stepO1 = dec.getStep(dec.getStepNames().get(i));
                  Step stepO2 = dec.getStep(dec.getStepNames().get(j));

                  DecompositionClass.Step step1 = dec.new Step(stepO1);
                  DecompositionClass.Step step2 = dec.new Step(stepO2);
                  step1.removeRequired(dec.getStepNames().get(j));

                  boolean contain = false;
                  if ( step1.isEquivalent(step2, taskModel) ) {
                     if ( dec.checkInputs(dec.getStepNames().get(i),
                           step1.getType(), dec.getStepNames().get(j),
                           step2.getType(), dec, dec, taskModel) ) {

                        for (Entry<Step, List<Step>> step : steps.entrySet()) {
                           if ( step.getKey().equals(stepO1) ) {
                              steps.get(stepO1).add(stepO2);
                              names.get(stepO1).add(dec.getStepNames().get(j));
                              contain = true;
                              break;
                           } else {
                              for (Step st : step.getValue()) {
                                 if ( st.equals(stepO1) ) {
                                    step.getValue().add(stepO2);
                                    names.get(step.getKey()).add(
                                          dec.getStepNames().get(j));
                                    contain = true;
                                    break;
                                 }
                              }
                              if ( contain ) {
                                 break;
                              }
                           }
                        }

                        if ( contain ) {
                           ;
                        } else {
                           List<Step> listStep = new ArrayList<Step>();
                           listStep.add(stepO2);
                           steps.put(stepO1, listStep);

                           List<String> listName = new ArrayList<String>();
                           listName.add(dec.getStepNames().get(i));
                           listName.add(dec.getStepNames().get(j));
                           names.put(stepO1, listName);

                        }

                        i = j;
                        j = i - 1;

                     } else {
                        i = i - 1;
                        j = j - 1;
                        break;
                     }

                  } else {
                     i = i - 1;
                     j = j - 1;
                     break;
                  }

               }
            }

         }
         // removing extra steps and their bindings and inputs/outputs
         for (Entry<Step, List<Step>> step : steps.entrySet()) {
            List<String> list = names.get(step.getKey());
            int maxOccurs = list.size();
            step.getValue().get(maxOccurs - 2).setMaxOccurs(maxOccurs);
            for (int i = 0; i < list.size() - 1; i++) {
               String stepName = list.get(i);
               Step removeStep = dec.getStep(stepName);
               for (Input in : removeStep.getType().getDeclaredInputs()) {
                  dec.removeBinding("$" + stepName + "." + in.getName());
               }
               for (Output out : removeStep.getType().getDeclaredOutputs()) {
                  Entry<String, Binding> binding = dec.removeBindingValue("$"
                     + stepName + "." + out.getName());
                  task.removeOutput(binding.getValue().getSlot());
               }

               dec.removeStep(stepName);

            }
         }
      }

   }

   /**
    * Checks for optional steps between two DecompositionClass. Checks whether
    * one of the DecompositionClass Classes is the subset of the other
    * DecompositionClass.
    */
   public DecompositionClass checkOptional (DecompositionClass task1,
         DecompositionClass task2) {
      // Checking subset

      if ( task1 != null && task2 != null ) {
         DecompositionClass subtask = null;
         DecompositionClass supertask = null;
         if ( task1.getStepNames().size() > task2.getStepNames().size() ) {
            supertask = task1;
            subtask = task2;
         } else {
            // return null; // just one direction
            supertask = task2;
            subtask = task1;
         }

         ArrayList<String> temp1 = new ArrayList<String>(subtask.getStepNames());
         ArrayList<String> temp2 = new ArrayList<String>(
               supertask.getStepNames());
         ArrayList<String> optionals = new ArrayList<String>(
               supertask.getStepNames());

         for (int i = 0; i < temp1.size(); i++) {
            Step step1 = subtask.getStep(temp1.get(i));
            int where = -1;
            boolean contain = false;
            for (int j = 0; j < temp2.size(); j++) {
               Step step2 = supertask.getStep(temp2.get(j));
               if ( step1.isEquivalent(step2, taskModel) ) {

                  if ( subtask.checkInputs(temp1.get(i), step1.getType(),
                        temp2.get(j), step2.getType(), subtask, supertask,
                        taskModel) ) {

                     where = j;
                     contain = true;
                     break;
                  }
               }
            }

            if ( contain ) {
               temp2.remove(where);
               optionals.remove(where);
            } else
               return null;
         }
         System.out.println(optionals);
         for (String opt : optionals) {
            supertask.getStep(opt).setMinOccurs(0);
         }

         return subtask;
      }
      return null;
   }

   /**
    * Checks for optional steps between two DecompositionClass
    */
   public void optionals (TaskModel taskModel) {
      Iterator<TaskClass> iterator1 = taskModel.getTaskClasses().iterator();
      Iterator<TaskClass> iterator2 = taskModel.getTaskClasses().iterator();
      TaskClass remove = null;
      DecompositionClass decRemove = null;
      while (iterator1.hasNext()) {
         TaskClass next1 = iterator1.next();
         while (iterator2.hasNext()) {
            TaskClass next2 = iterator2.next();
            if ( !next1.equals(next2) ) {
               for (DecompositionClass dec1 : next1.getDecompositions()) {
                  for (DecompositionClass dec2 : next2.getDecompositions()) {

                     decRemove = checkOptional(dec1, dec2);
                     if ( decRemove != null )
                        remove = decRemove.getGoal();
                  }
               }
            }
         }
      }

      if ( remove != null && decRemove != null ) {
         if ( remove.getDecompositions().size() == 1 )
            taskModel.remove(remove);
         else {
            remove.removeDecompositionClass(decRemove);
            // removing from parents
         }
      }

   }

   /**
    * Checks equality of two DecompositionClass without considering their
    * bindings' value.
    */
   public boolean equalityDec (DecompositionClass dec1, DecompositionClass dec2) {

      ArrayList<String> dec1Steps = new ArrayList<String>(dec1.getStepNames());
      ArrayList<String> dec2Steps = new ArrayList<String>(dec2.getStepNames());

      if ( dec1Steps.size() == dec2Steps.size() ) {
         for (int i = 0; i < dec1Steps.size(); i++) {
            Step step1 = dec1.getStep(dec1Steps.get(i));
            int where = -1;
            boolean contain = false;
            for (int j = 0; j < dec2Steps.size(); j++) {
               Step step2 = dec2.getStep(dec2Steps.get(j));
               if ( step1.isEquivalent(step2, taskModel) ) {
                  where = j;
                  contain = true;
                  break;
               }
            }

            if ( !contain )
               return false;
            else
               dec2Steps.remove(where);
         }
      } else
         return false;
      return true;
   }

   /**
    * Checks equality of two tasks and their DecompositionClasses. It will do
    * nothing if they are equal.
    */
   public void equality () {
      Iterator<TaskClass> iterator1 = taskModel.getTaskClasses().iterator();
      Iterator<TaskClass> iterator2 = taskModel.getTaskClasses().iterator();
      TaskClass remove = null;
      DecompositionClass decRemove = null;
      while (iterator1.hasNext()) {
         TaskClass next1 = iterator1.next();
         while (iterator2.hasNext()) {
            TaskClass next2 = iterator2.next();
            if ( !next1.equals(next2) ) {
               for (DecompositionClass dec1 : next1.getDecompositions()) {
                  for (DecompositionClass dec2 : next2.getDecompositions()) {
                     if ( equalityDec(dec1, dec2) ) {
                        decRemove = dec2;
                        System.out.println("----------" + dec1.getId());
                        System.out.println(dec2.getId());
                        if ( decRemove != null )
                           remove = decRemove.getGoal();
                     }
                  }
               }
            }
         }
      }

      if ( remove != null && decRemove != null ) {
         if ( remove.getDecompositions().size() == 1 )
            ;
         // taskModel.remove(remove);
         else {
            ;
            // remove.removeDecompositionClass(decRemove);
            // remove bindings or add binding to the upper class
         }
      }

   }

   /**
    * Gets the new taskmodel.
    */
   void getNewTaskModel () {
      taskModel = new TaskModel();
      if ( this.externalTaskModel != null ) {
         learnedTaskmodel();
      }
   }

   /**
    * Creates questions for all internal tasks.
    */
   void internalTaskQ () {
     /* for (TaskClass task : taskModel.getTaskClasses()) {
         if ( task.getDecompositions().size() > 1 )
            task.getQuestion(taskModel);
      }*/
   }
   
   


}
