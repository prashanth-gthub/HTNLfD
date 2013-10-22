package edu.wpi.htnlfd;

import java.util.*;
import java.util.Map.Entry;
import edu.wpi.htnlfd.model.*;
import edu.wpi.htnlfd.model.DecompositionClass.*;
import edu.wpi.htnlfd.model.TaskClass.*;

public class InputTransformation {

   /**
    * Adds the specified input to the specified parents
    * 
    * @param parents the parents (TaskClass, DecompositionClass, Step)
    * @param type the type: Input or Output
    */
   void transform (List<Object[]> parents, String slotName, String slotType,
         String modified, boolean type) {
      for (Object[] parent : parents) {
         TaskClass task = (TaskClass) (parent[0]);
         DecompositionClass subtask = (DecompositionClass) parent[1];
         @SuppressWarnings("unchecked")
         Entry<String, Step> step = (Entry<String, Step>) parent[2];

         if ( type ) {
            String inputPar = step.getKey() + "_" + slotName;
            TaskClass.Input inputCC = task.new Input(inputPar, slotType,
                  task.getOutput(step.getKey() + "_" + modified));
            task.addInput(inputCC);

            subtask.addBinding("$" + step.getKey() + "." + slotName,
                  subtask.new Binding(inputPar, step.getKey(), "$this."
                     + inputPar, DecompositionClass.Type.InputInput));
         } else {
            String OutputPar = step.getKey() + "_" + slotName;
            TaskClass.Output outputCC = task.new Output(OutputPar, slotType);
            task.addOutput(outputCC);

            subtask.addBinding("$this." + OutputPar, subtask.new Binding(
                  OutputPar, "this", "$" + step.getKey() + "." + slotName,
                  DecompositionClass.Type.OutputOutput));
         }
      }

   }

   /*
    * Checks all the steps' of a decomposition class, if all of them have the
    * same input and the TaskClass class doesn't have it, then it will be added
    * to the TaskClass class.
    */
   public void transform (TaskModel taskModel) {
      Iterator<TaskClass> tasksIterator = taskModel.getTaskClasses().iterator();
      TaskClass task = null;

      while (tasksIterator.hasNext()) {

         task = tasksIterator.next();
         ArrayList<Input> decompositionInputs = new ArrayList<Input>();

         for (DecompositionClass subtask : task.getDecompositions()) {
            ArrayList<Input> stepsInputs = new ArrayList<Input>();
            for (Entry<String, Step> step : subtask.getSteps().entrySet()) {

               TaskClass taskTemp = taskModel.getTaskClass(step.getValue()
                     .getType().getId());
               if ( taskTemp != null ) {
                  for (Input input : taskTemp.getDeclaredInputs()) {

                     if ( subtask
                           .getBindingStep(step.getKey(), input.getName()) == null ) {
                        stepsInputs.add(input);
                     }
                  }
               }

            }

            for (int i = 0; i < stepsInputs.size(); i++) {
               int num = 0;
               for (int j = i + 1; j < stepsInputs.size(); j++) {
                  if ( stepsInputs.get(i).getName()
                        .equals(stepsInputs.get(j).getName()) ) {
                     boolean contain = false;
                     for (int k = 0; k < decompositionInputs.size(); k++) {
                        if ( decompositionInputs.get(k).getName()
                              .equals(stepsInputs.get(i).getName()) ) {
                           contain = true;
                           break;
                        }
                     }
                     if ( !contain )
                        num++;
                     else
                        break;
                  }
               }
               // If all of the steps have this input, add the binding to them
               if ( num == subtask.getSteps().size() - 1 ) {
                  decompositionInputs.add(stepsInputs.get(i));
                  for (Entry<String, Step> step : subtask.getSteps().entrySet()) {
                     subtask.addBinding("$" + step.getKey() + "."
                        + stepsInputs.get(i).getName(), subtask.new Binding(
                           stepsInputs.get(i).getName(), step.getKey(), "this."
                              + stepsInputs.get(i).getName(), DecompositionClass.Type.InputInput));
                  }
               }
            }
         }
         // If task doesn't have this input add it
         for (int k = 0; k < decompositionInputs.size(); k++) {

            if ( !task.hasInput(decompositionInputs.get(k).getName()) ) {
               task.addInput(task.new Input(decompositionInputs.get(k)
                     .getName(), decompositionInputs.get(k).getType(), null));
            }
         }

      }

   }
}