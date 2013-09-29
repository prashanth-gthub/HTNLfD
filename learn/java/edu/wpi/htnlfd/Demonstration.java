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

   private edu.wpi.cetask.TaskClass recipeTaskClass = null;

   class TempClass {
      String name;

      ArrayList<ArrayList<edu.wpi.cetask.Task>> steps = new ArrayList<ArrayList<edu.wpi.cetask.Task>>();

      ArrayList<String> inputs = new ArrayList<String>();

      ArrayList<ArrayList<String>> stepNames = new ArrayList<ArrayList<String>>();

      public TempClass (String name, ArrayList<edu.wpi.cetask.Task> step, String input,
            ArrayList<String> stepStrValue) {

         this.name = name;
         this.steps.add(step);
         this.inputs.add(input);
         this.stepNames.add(stepStrValue);
      }

   }

   public Demonstration () {

   }

   public List<edu.wpi.cetask.Task> findDemonstration (Disco disco, String taskName) {
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

   public List<TaskClass> build (Disco disco, String taskName,
         List<edu.wpi.cetask.Task> steps, String input) throws NoSuchMethodException,
         ScriptException {
      
      if ( this.externalTaskModel != null ) {
         learnedTaskmodel();
      }
      TaskClass newTask = demonstratedTask(disco, taskName, steps);
      if(!checkRecipe(newTask, input)){
         this.taskModel.add(newTask);
      }
      
      removeBindingsFromChildren(newTask);
      
      return this.taskModel.getTaskClasses();
   }
   
   public boolean checkRecipe(TaskClass newTask, String input){
      
      Iterator<TaskClass> tasksIterator = this.taskModel.getTaskClasses()
            .iterator();

      while (tasksIterator.hasNext()) {

         TaskClass task = tasksIterator.next();
         
         if ( task.getId().equals(newTask.getId()) ) {

            TaskClass.Input inputC = task.new Input(input, "boolean",null);
            task.addInput(inputC);
            task.getDecompositions().get(0).setApplicable("$this." + input);
            newTask.getDecompositions().get(0).setApplicable("!$this." + input);
            newTask.getDecompositions().get(0).setId(task.getDecompositions().get(0).getId()+"1");
            task.getDecompositions().add(newTask.getDecompositions().get(0));
            return true;
         }
      }
      return false;
      //addRecipe(task, input, subtask, inputs, recipe);
     
   }

   public TaskClass demonstratedTask (Disco disco, String taskName,
         List<edu.wpi.cetask.Task> steps)
         throws NoSuchMethodException, ScriptException {
      TaskClass task = null;
         task = new TaskClass(taskModel, taskName);
         

      int countSubtask = task.getDecompositions().size() + 1;
      DecompositionClass subtask = new DecompositionClass(taskModel, Character.toLowerCase(taskName.charAt(0))
         + (taskName.length() > 1 ? taskName.substring(1) : "") + countSubtask, true);

      task.addDecompositionClass(subtask);
      Map<String, Integer> StepNames = new HashMap<String, Integer>();
      Map<String, String> bindingsInputs = new HashMap<String, String>();
      Map<String, String> bindingsOutputs = new HashMap<String, String>();

      ArrayList<TempClass> inputs = new ArrayList<TempClass>();

      List<String> inputsNumbers = new ArrayList<String>();
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
         // finding step's name
         // step type problem
         DecompositionClass.Step stp =  subtask.new Step (new TaskClass(taskModel, step.getType().getId()), 1, 1, null);
         String stepNameR = Character.toLowerCase(stepName.charAt(0))
               + (stepName.length() > 1 ? stepName.substring(1) : "") + count;
         stp.setNamespace(step.getType().getNamespace());
         subtask.addStep(stepNameR,stp);
         // ///////////////////////////////////////////////

         for (String inputName : step.getType().getDeclaredInputNames()) {

            String bindingSlotvalue = "$" + stepNameR + "." + inputName;

            Object inputBinding = (((Invocable) disco.getScriptEngine())
                  .invokeFunction("find", step.getSlotValue(inputName)));

            String inputBindingValue = (String) inputBinding;
            TempClass findInput = null;
            for (TempClass in : inputs) {
               if ( in.name.equals(inputBindingValue) ) {
                  findInput = in;
                  break;
               }
            }
            if ( findInput != null ) {
               boolean contain = false;

               for (int x = 0; x < findInput.inputs.size(); x++) {
                  String str = findInput.inputs.get(x);
                  if ( str.contains(inputName) ) {

                     findInput.steps.get(x).add(step);
                     findInput.stepNames.get(x).add(stepNameR);
                     contain = true;
                     break;
                  }
               }
               if ( !contain ) {

                  findInput.inputs.add(inputName);
                  ArrayList<edu.wpi.cetask.Task> task1 = new ArrayList<edu.wpi.cetask.Task>();
                  task1.add(step);
                  ArrayList<String> taskVal = new ArrayList<String>();
                  taskVal.add(stepNameR);
                  findInput.steps.add(task1);
                  findInput.stepNames.add(taskVal);
               }

            } else {

               ArrayList<edu.wpi.cetask.Task> task1 = new ArrayList<edu.wpi.cetask.Task>();
               task1.add(step);
               ArrayList<String> taskVal = new ArrayList<String>();
               taskVal.add(stepNameR);
               inputs.add(new TempClass(inputBindingValue, task1, inputName,
                     taskVal));
            }

            bindingsInputs.put(bindingSlotvalue, inputBindingValue);
         }
System.out.println("");
         
         
         for (String outputName : step.getType().getDeclaredOutputNames()) {

            String bindingSlot = "$" + stepNameR + "." + outputName;
            boolean contain = false;
            String bindingSlotValue = null;
            for (int i = outputNumbers.size() - 1; i >= 0; i--) {
               if ( outputNumbers.get(i).contains(outputName) ) {
                  int start = outputNumbers.get(i).lastIndexOf(outputName)
                     + outputName.length();
                  int end = outputNumbers.get(i).length();
                  String number = outputNumbers.get(i).substring(start, end);
                  int num = Integer.parseInt(number);
                  num++;
                  outputNumbers.add(outputName + num);
                  bindingSlotValue = outputName + num;
                  contain = true;
                  break;

               }
            }
            if ( !contain ) {
               outputNumbers.add(outputName + "1");
               bindingSlotValue = outputName + "1";
            }
            outputs.put(bindingSlotValue, step);
            bindingsOutputs.put("$this." + bindingSlotValue, bindingSlot);
         }

      }

         
      
      addNotRecipe(inputs, task, inputsNumbers, outputs, subtask);

      
      // ordering
      addOrdering(inputs, subtask);

      // add $this bindings
      for (Entry<String, String> binding : bindingsInputs.entrySet()) {

         for (TempClass inputEntry : inputs) {
            for (int m = 0; m < inputEntry.inputs.size(); m++) {
               String inputListName = inputEntry.inputs.get(m);

               if ( inputEntry.name.equals(binding.getValue())
                  && binding.getKey().endsWith(
                        inputListName.replaceAll("[0-9]$", "")) ) {

                  subtask.getBindings().put(binding.getKey(),
                        subtask.new Binding(binding.getKey(),"$this." + inputListName));

               }
            }
         }

      }

      // add bindings
      for (Entry<String, String> bind : bindingsOutputs.entrySet()) {
         subtask.getBindings().put(bind.getKey(), subtask.new Binding(bind.getKey(),bind.getValue()));
      }
      
      // remove input bindings from children if their parent has that binding
      //removeBindingsFromChildren(task);
      
      return task;

   }

   private void addOrdering (ArrayList<TempClass> inputs, DecompositionClass subtask) {
      boolean subtaskOrdered = true;
      for (TempClass inp : inputs) {
         for (int h = 0; h < inp.inputs.size(); h++) {
            String inputRef = inp.inputs.get(h);

            if ( inputRef.contains(ReferenceFrame) ) {
               for (int s = 0; s < inp.inputs.size(); s++) {
                  String inputDep = inp.inputs.get(s);

                  if ( !inputDep.contains(ReferenceFrame) ) {
                     for (int l = 0; l < inp.steps.get(h).size(); l++) {

                        String modified = inp.steps.get(h).get(l).getType()
                              .getModified(inputDep.replaceAll("[0-9]$", ""));

                        if ( modified != null ) {
                           // System.out.println(inp.steps.get(s).getType().getId()+" "+inp.steps.get(h).getType().getId());

                           String stepRef = inp.stepNames.get(s).get(0);
                           String stepDep = inp.stepNames.get(h).get(l);
                           
                           DecompositionClass.Step orderStep = subtask.getStep(stepDep);
                           if ( !stepRef.equals(stepDep) ) {
                              if (orderStep.getRequired().size() == 0 ) {
                                 orderStep.getRequired().add(stepRef);

                                 subtaskOrdered = false;
                              } else {

                                 boolean contain = false;

                                 for (String order : orderStep.getRequired()) {
                                    if ( order.equals(stepRef) )
                                       contain = true;
                                 }

                                 if ( !contain ) {

                                    orderStep.getRequired().add(stepRef);
                                    subtaskOrdered = false;
                                 }
                              }
                           }
                        }
                     }

                  }
               }
            }
         }
      }
      if ( !subtaskOrdered ) {

         subtask.setOrdered(false);

      } else
         subtask.setOrdered(true);
      
   }

   private void addNotRecipe (ArrayList<TempClass> inputs, TaskClass task,
         List<String> inputsNumbers, Map<String, edu.wpi.cetask.Task> outputs,
         DecompositionClass subtasks) {
      for (TempClass inputEntry : inputs) {
         for (int m = 0; m < inputEntry.inputs.size(); m++) {
            String inputListName = inputEntry.inputs.get(m);
            TaskClass.Input inp = task.new Input();
            task.addInput(inp);
            boolean contain = false;
            for (String str : inputsNumbers) {
               if ( str.contains(inputListName + "1") ) {
                  contain = true;
                  break;
               }
            }
            if ( !contain ) {

               inp.setName(inputListName + "1");
               inputsNumbers.add(inputListName + "1");
               inputEntry.inputs.set(m, inputListName + "1");

            } else {
               for (int i = inputsNumbers.size() - 1; i >= 0; i--) {
                  if ( inputsNumbers.get(i).contains(inputListName) ) {
                     int start = inputsNumbers.get(i).lastIndexOf(
                           inputsNumbers.get(i))
                        + inputsNumbers.get(i).length() - 1;
                     int end = inputsNumbers.get(i).length();
                     String number = inputsNumbers.get(i).substring(start, end);
                     int num = Integer.parseInt(number);
                     num++;
                     inputsNumbers.add(inputListName + num);
                     inp.setName(inputListName + num);
                     inputEntry.inputs.set(m, inputListName + num);
                     break;
                  }
               }
            }

            inp.setType(inputEntry.steps.get(m).get(0).getType()
                  .getSlotType(inputListName));

            String modified = inputEntry.steps.get(m).get(0).getType()
                  .getModified(inputListName);
            if ( modified != null && modified != "" ) {
               for (Entry<String, edu.wpi.cetask.Task> out : outputs.entrySet()) {
                  if ( out.getValue().equals(inputEntry.steps.get(m).get(0)) && out.getKey().replaceAll("[0-9]$", "").equals(modified) ) {
                     TaskClass.Output output = task.new Output(out.getKey(),out.getValue().getType()
                           .getSlotType(out.getKey().replaceAll("[0-9]$", "")));
                     task.addOutput(output);
                     inp.setModified( output);        

                  }
               }
            }
         }

      }

      for (Entry<String, edu.wpi.cetask.Task> out : outputs.entrySet()) {
         boolean contain = false;
         for (TaskClass.Output ou:task.getDeclaredOutputs()){
            if(out.getKey().equals(ou.getName())){
               contain = true;
               break;
            }
         }
         if(!contain){
            TaskClass.Output output = task.new Output(out.getKey(), out.getValue().getType()
                  .getSlotType(out.getKey().replaceAll("[0-9]$", "")));
            task.addOutput(output);
            
         }
      }


      for (TempClass inputEntry : inputs) {
         for (int m = 0; m < inputEntry.inputs.size(); m++) {
            String inputListName = inputEntry.inputs.get(m);
            subtasks.getBindings().put("$this." + inputListName,subtasks.new Binding("this",inputEntry.name)
                  );
         }
      }

   }

   private void addRecipe (TaskClass task, String input, DecompositionClass subtasks,
         ArrayList<TempClass> inputs, TaskClass recipe) {
      task = recipe;

      subtasks.setApplicable("!$this." + input);

      for (TempClass inputEntry : inputs) {
         for (int m = 0; m < inputEntry.inputs.size(); m++) {
            String inputListName = inputEntry.inputs.get(m);
            for (String ins : recipeTaskClass.getDeclaredInputNames()) {
               boolean change = false;
               if ( recipeTaskClass.getSlotType(ins).equals(
                     inputEntry.steps.get(m).get(0).getType()
                           .getSlotType(inputEntry.inputs.get(m)))
                  && ins.contains(inputListName) ) {
                  String findParent = findValueOfInput(ins,
                        recipeTaskClass.getId());
                  if ( findParent != null && findParent.equals(inputEntry.name) ) {
                     inputEntry.inputs.set(m, ins);
                     change = true;
                     // System.out.println("---" + inputEntry.getKey() + " "
                     // + ins);
                  }

                  // if we cannot find the value in it's parents, it may be
                  // in it's siblings
                  if ( !change ) {

                     List<edu.wpi.cetask.DecompositionClass> decompositions = recipeTaskClass
                           .getDecompositions();
                     for (edu.wpi.cetask.DecompositionClass subtaskDecomposition : decompositions) {

                        boolean breaking = false;

                        Collection<Entry<String, edu.wpi.cetask.DecompositionClass.Binding>> bindingsSubtask = subtaskDecomposition
                              .getBindings().entrySet();
                        for (Entry<String, edu.wpi.cetask.DecompositionClass.Binding> binding : bindingsSubtask) {

                           if ( binding.getKey().equals("$this." + ins) ) {

                              if ( binding.getValue().value
                                    .equals(inputEntry.name) ) {
                                 inputEntry.inputs.set(m, ins);
                                 // System.out
                                 // .println("---" + inputListName[0]);

                              }

                           }

                        }
                     }
                  }
               }
            }
         }
      }

   }

   private String findValueOfInput (String in, String parent) {

      Iterator<edu.wpi.cetask.TaskClass> tasksIterator = this.externalTaskModel.getTaskClasses()
            .iterator();
      while (tasksIterator.hasNext()) {
         edu.wpi.cetask.TaskClass taskclass = tasksIterator.next();
         List<edu.wpi.cetask.DecompositionClass> decompositions = taskclass
               .getDecompositions();
         for (edu.wpi.cetask.DecompositionClass subtaskDecomposition : decompositions) {

            for (String stepName : subtaskDecomposition.getStepNames()) {

               if ( subtaskDecomposition.getStepType(stepName).getId()
                     .equals(parent) ) { // I should add namespace
                  Collection<Entry<String, edu.wpi.cetask.DecompositionClass.Binding>> bindingsSubtask = subtaskDecomposition
                        .getBindings().entrySet();
                  for (Entry<String, edu.wpi.cetask.DecompositionClass.Binding> binding : bindingsSubtask) {

                     if ( binding.getKey().contains(stepName)
                        && binding.getKey().contains(in) ) {
                        Collection<Entry<String, edu.wpi.cetask.DecompositionClass.Binding>> bindingsSubtask2 = subtaskDecomposition
                              .getBindings().entrySet();
                        String val = binding.getValue().value;
                        for (Entry<String, edu.wpi.cetask.DecompositionClass.Binding> binding2 : bindingsSubtask2) {

                           if ( binding2.getKey().contains(val) ) {
                              if ( !binding2.getValue().value.contains("$this") ) {
                                 return binding2.getValue().value;
                              } else {
                                 return findValueOfInput(val.substring(6),
                                       subtaskDecomposition.getGoal().getId());
                              }
                           }

                        }
                        return findValueOfInput(val.substring(6),
                              subtaskDecomposition.getGoal().getId());
                     }

                  }
               }
            }

         }
      }
      return null;
   }

   public void learnedTaskmodel () {

      
      
      Iterator<edu.wpi.cetask.TaskClass> tasksIterator = this.externalTaskModel.getTaskClasses()
            .iterator();

      while (tasksIterator.hasNext()) {

         edu.wpi.cetask.TaskClass task = tasksIterator.next();

         TaskClass domTask = new TaskClass(taskModel, task.getId());

         

         for (String outputName : task.getDeclaredOutputNames()) {

            TaskClass.Output outputTask = domTask.new Output(outputName, task.getSlotType(outputName));
            domTask.addOutput(outputTask);

         }
         
         for (String inputName : task.getDeclaredInputNames()) {

            TaskClass.Input inputTask = null;
            for (TaskClass.Output out:domTask.getDeclaredOutputs()){
               if(out.getName().equals(task.getModified(inputName))){
                  inputTask =  domTask.new Input(inputName, task.getSlotType(inputName),
                        out);
                  break;
               }
            }
            if(inputTask == null){
               inputTask = domTask.new Input(inputName, task.getSlotType(inputName),
                     null);
            }
            domTask.addInput(inputTask);
         }

         

         List<edu.wpi.cetask.DecompositionClass> decompositions = task.getDecompositions();
         for (edu.wpi.cetask.DecompositionClass subtaskDecomposition : decompositions) {


            // subtaskDecomposition.getQName()
            DecompositionClass subtask = new DecompositionClass(taskModel, subtaskDecomposition.getId(),null,
                  subtaskDecomposition.isOrdered());

            String name = subtaskDecomposition.getId();


            for (String stepName : subtaskDecomposition.getStepNames()) {

               String namespaceDec = subtaskDecomposition.getStepType(stepName)
                     .getNamespace();
               
               DecompositionClass.Step step =  subtask.new Step (domTask, 1, 1, subtaskDecomposition.getRequiredStepNames(stepName));
               subtask.addStep(subtaskDecomposition.getStepType(
                     stepName).getId(), step);
               
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

               subtask.getBindings().put(binding.getKey(),
                      subtask.new Binding(binding.getValue().slot,binding.getValue().step,binding.getValue().value,binding.getValue().inputInput)); // Error

            }

         }
         
      }
   }

  /* public Step findStep (Subtasks subtask, String value) {
      for (Step step : subtask.getSteps()) {

         if ( step.getName().equals(value) ) {

            return step;

         }
      }
      return null;
   }
*/
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