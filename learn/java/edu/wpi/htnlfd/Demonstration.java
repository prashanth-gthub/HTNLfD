package edu.wpi.htnlfd;

import edu.wpi.cetask.*;
import edu.wpi.cetask.DecompositionClass.Binding;
import edu.wpi.disco.*;
import edu.wpi.disco.lang.Utterance;
import edu.wpi.htnlfd.dto.*;
import java.util.*;
import java.util.Map.Entry;
import javax.script.*;

public class Demonstration {


   
   

   private List<TaskBlock> tasks = new ArrayList<TaskBlock>();

   TaskModel taskModel = null;

   private final String ReferenceFrame = "referenceFrame";



   private TaskClass recipeTaskClass = null;

   class TempClass {
      String name;

      ArrayList<ArrayList<Task>> steps = new ArrayList<ArrayList<Task>>();

      ArrayList<String> inputs = new ArrayList<String>();

      ArrayList<ArrayList<String>> stepNames = new ArrayList<ArrayList<String>>();

      public TempClass (String name, ArrayList<Task> step, String input,
            ArrayList<String> stepStrValue) {

         this.name = name;
         this.steps.add(step);
         this.inputs.add(input);
         this.stepNames.add(stepStrValue);
      }

   }

   public Demonstration () {
      
   }

   public List<Task> findDemonstration (Disco disco, String taskName) {
      List<Task> demonstratedTasks = new ArrayList<Task>();
      List<Task> demonstratedTasksReversed = new ArrayList<Task>();
      Object parent = (disco.getStack().get(0).getChildren().get(disco
            .getStack().get(0).getChildren().size() - 1));
      if ( parent instanceof Segment ) {

         for (int i = ((Segment) parent).getChildren().size() - 1; i >= 0; i--) {
            Object child = ((Segment) parent).getChildren().get(i);
            if ( (child instanceof Task) ) {
               Task task = (Task) child;
               if ( !(task instanceof Utterance) )
                  ;// demonstratedTasks.add(task);
            } else if ( child instanceof Segment ) {
               Segment segment = (Segment) child;
               demonstratedTasks.add(segment.getPurpose());
            }
         }

         for (int i = demonstratedTasks.size() - 1; i >= 0; i--) {
            Task myTask = demonstratedTasks.get(i);
            demonstratedTasksReversed.add(myTask);
         }
         return demonstratedTasksReversed;
      } else {
         return null;
      }
   }

   

   
   public List<TaskBlock> build(Disco disco, String taskName, List<Task> steps,
         String input) throws NoSuchMethodException, ScriptException {
      TaskBlock recipe = null;
      TaskBlock task = null;
      if ( this.taskModel != null ) {
         recipe  = learnedTaskmodelToDom(taskName, steps,input);
      }

     demonstratedTaskToDom(disco,taskName, steps,
            recipe, input);
     return tasks;
   }
   public void demonstratedTaskToDom (Disco disco, 
         String taskName, List<Task> steps,
         TaskBlock recipe, String input) throws NoSuchMethodException,
         ScriptException {
      TaskBlock task = null;
      if(recipe!=null)
         task = recipe;
      else{
         task = new TaskBlock(taskName);
         this.tasks.add(task);
      }
      int countSubtask = task.getSubtasks().size()+1;            
      Subtasks subtask = new Subtasks(Character.toLowerCase(taskName.charAt(0))
            + (taskName.length() > 1 ? taskName.substring(1) : "") + countSubtask);
      
      task.addSubtask(subtask);
      Map<String, Integer> StepNames = new HashMap<String, Integer>();
      Map<String, String> bindingsInputs = new HashMap<String, String>();
      Map<String, String> bindingsOutputs = new HashMap<String, String>();

      ArrayList<TempClass> inputs = new ArrayList<TempClass>();

      List<String> inputsNumbers = new ArrayList<String>();
      Map<String, Task> outputs = new HashMap<String, Task>();
      List<String> outputNumbers = new ArrayList<String>();
      for (Task step : steps) {

         String stepName = step.getType().getId();
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

         Step stp = new Step(Character.toLowerCase(stepName.charAt(0))
               + (stepName.length() > 1 ? stepName.substring(1) : "") + count,stepName,step.getType().getNamespace());
         subtask.getSteps().add(stp);
         // ///////////////////////////////////////////////

         for (String inputName : step.getType().getDeclaredInputNames()) {

            String bindingSlotvalue = "$" + stp.getName() + "." + inputName;

            // String temp2 = step.getSlotValueToString(inputName);

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
                     findInput.stepNames.get(x).add(stp.getName());
                     contain = true;
                     break;
                  }
               }
               if ( !contain ) {

                  findInput.inputs.add(inputName);
                  ArrayList<Task> task1 = new ArrayList<Task>();
                  task1.add(step);
                  ArrayList<String> taskVal = new ArrayList<String>();
                  taskVal.add(stp.getName());
                  findInput.steps.add(task1);
                  findInput.stepNames.add(taskVal);
                  // System.out.println(inputBindingValue + " " + nameType[0]);
               }

            } else {

               ArrayList<Task> task1 = new ArrayList<Task>();
               task1.add(step);
               ArrayList<String> taskVal = new ArrayList<String>();
               taskVal.add(stp.getName());
               inputs.add(new TempClass(inputBindingValue, task1, inputName,
                     taskVal));
               // System.out.println(inputBindingValue + " " + nameType[0]);
            }

            bindingsInputs.put(bindingSlotvalue, inputBindingValue);
         }

         for (String outputName : step.getType().getDeclaredOutputNames()) {

            String bindingSlot = "$" + stp.getName() + "." + outputName;
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

      if ( recipe != null ) {
         addRecipe(task, input, subtask, inputs, recipe);
      }

      // taskElement.appendChild();
      if ( recipe == null ) {
         addNotRecipe(inputs, task, inputsNumbers, outputs, subtask);

      }
      // ordering
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
                           Step orderStep = findStep(subtask,stepDep);
                           if ( !stepRef.equals(stepDep) ) {
                              if (orderStep.getRequires().size()==0) {
                                 orderStep.getRequires().add(stepRef);
   
                                 subtaskOrdered = false;
                              } else {
                                 
                                 boolean contain = false;
                                 
                                    for (String order : orderStep.getRequires()) {
                                       if ( order.equals(stepRef) )
                                          contain = true;
                                    }
                                 
                                 if ( !contain ) {

                                    orderStep.getRequires().add(stepRef);
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

      }

      for (Entry<String, String> binding : bindingsInputs.entrySet()) {

         for (TempClass inputEntry : inputs) {
            for (int m = 0; m < inputEntry.inputs.size(); m++) {
               String inputListName = inputEntry.inputs.get(m);


               if ( inputEntry.name.equals(binding.getValue())
                  && binding.getKey().endsWith(
                        inputListName.replaceAll("[0-9]$", "")) ) {
                  
                  subtask.getBindings().put(binding.getKey(), "$this." + inputListName);

               }
            }
         }

      }

      for (Entry<String, String> bind : bindingsOutputs.entrySet()) {
         subtask.getBindings().put(bind.getKey(), bind.getValue());
      }

   }

   private void addNotRecipe (ArrayList<TempClass> inputs, TaskBlock task,
         List<String> inputsNumbers, Map<String, Task> outputs, Subtasks subtasks) {
      for (TempClass inputEntry : inputs) {
         for (int m = 0; m < inputEntry.inputs.size(); m++) {
            String inputListName = inputEntry.inputs.get(m);
            Input inp = new Input();
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
               for (Entry<String, Task> out : outputs.entrySet()) {
                  if ( out.getValue().equals(inputEntry.steps.get(m).get(0)) ) {

                     inp.setModified(out.getKey());

                  }
               }
            }
         }

      }

      for (Entry<String, Task> out : outputs.entrySet()) {
         Output output = new Output(out.getKey(),out.getValue().getType()
               .getSlotType(out.getKey().replaceAll("[0-9]$", "")));
         task.addOutput(output);
      }

      for (TempClass inputEntry : inputs) {
         for (int m = 0; m < inputEntry.inputs.size(); m++) {
            String inputListName = inputEntry.inputs.get(m);
            subtasks.getBindings().put("$this." + inputListName, inputEntry.name);
         }
      }

   }

   private void addRecipe (TaskBlock task, String input, Subtasks subtasks,
         ArrayList<TempClass> inputs, TaskBlock recipe) {
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

                     List<DecompositionClass> decompositions = recipeTaskClass
                           .getDecompositions();
                     for (DecompositionClass subtaskDecomposition : decompositions) {

                        boolean breaking = false;

                        Collection<Entry<String, Binding>> bindingsSubtask = subtaskDecomposition
                              .getBindings().entrySet();
                        for (Entry<String, Binding> binding : bindingsSubtask) {

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

      Iterator<TaskClass> tasksIterator = this.taskModel.getTaskClasses()
            .iterator();
      while (tasksIterator.hasNext()) {
         TaskClass taskclass = tasksIterator.next();
         List<DecompositionClass> decompositions = taskclass
               .getDecompositions();
         for (DecompositionClass subtaskDecomposition : decompositions) {

            for (String stepName : subtaskDecomposition.getStepNames()) {

               if ( subtaskDecomposition.getStepType(stepName).getId()
                     .equals(parent) ) { // I should add namespace
                  Collection<Entry<String, Binding>> bindingsSubtask = subtaskDecomposition
                        .getBindings().entrySet();
                  for (Entry<String, Binding> binding : bindingsSubtask) {

                     if ( binding.getKey().contains(stepName)
                        && binding.getKey().contains(in) ) {
                        Collection<Entry<String, Binding>> bindingsSubtask2 = subtaskDecomposition
                              .getBindings().entrySet();
                        String val = binding.getValue().value;
                        for (Entry<String, Binding> binding2 : bindingsSubtask2) {

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

   public TaskBlock learnedTaskmodelToDom (
         String taskName, List<Task> steps,
         String input) {

      TaskBlock recipe = null;
      boolean recipeOccured = true;
      Iterator<TaskClass> tasksIterator = this.taskModel.getTaskClasses()
            .iterator();
  

      while (tasksIterator.hasNext()) {

         TaskClass task = tasksIterator.next();
         
         TaskBlock domTask = new TaskBlock(task.getId());

         if ( task.getId().equals(taskName) ) {
            recipe = domTask;
            recipeTaskClass = task;
            Input inputC = new Input(input,"boolean");
            recipe.addInput(inputC);
         }

         boolean changedTask = false;
         for (Task step : steps) {
            String stepType = step.getType().getId();
            if ( task.getId().equals(stepType) ) {
           
               changedTask = true;
               break;
            }
         }

         for (String inputName : task.getDeclaredInputNames()) {

            Input inputTask = new Input(inputName,task.getSlotType(inputName),task.getModified(inputName));
            domTask.addInput(inputTask);
         }

         for (String outputName : task.getDeclaredOutputNames()) {

            
            Output outputTask = new Output(outputName,task.getSlotType(task.getSlotType(outputName)));
            domTask.addOutput(outputTask);
            
         }

         List<DecompositionClass> decompositions = task.getDecompositions();
         for (DecompositionClass subtaskDecomposition : decompositions) {


            Subtasks subtask = new Subtasks(subtaskDecomposition.getId(),subtaskDecomposition.isOrdered());
            
            String name = subtaskDecomposition.getId();
            
            subtask.setId(name);
            
            for (String stepName : subtaskDecomposition.getStepNames()) {

               String namespaceDec = subtaskDecomposition.getStepType(stepName)
                     .getNamespace();
               

               Step step = new Step(stepName,subtaskDecomposition.getStepType(
                     stepName).getId(),namespaceDec);
               
               subtask.getSteps().add(step);
               
            }

            Collection<Entry<String, Binding>> bindingsSubtask = subtaskDecomposition
                  .getBindings().entrySet();

            if ( recipe != null && recipeOccured ) {

               subtask.setApplicable("$this." + input);
               
               recipeOccured = false;
            }

            if ( subtaskDecomposition.getApplicable() != null
               && subtaskDecomposition.getApplicable() != "" ) {

               subtask.setApplicable(subtaskDecomposition.getApplicable());
            }

            for (Entry<String, Binding> binding : bindingsSubtask) {
          
               if ( changedTask == false || !binding.getKey().contains("this") )
                  subtask.getBindings().put(binding.getKey(), binding.getValue().value); //Error
               
            }


         }
         this.tasks.add(domTask);
      }
      return recipe;
   }

   public Step findStep (Subtasks subtask,
         String value) {
      for (Step step:subtask.getSteps()) {
         
         if ( step.getName().equals(value) ) {
            
               return step;
            
         }
      }
      return null;
   }
   public TaskModel readDOM (Disco disco, String fileName) {
      return disco.getInteraction().load(fileName);
   }

}
