package edu.wpi.htnlfd.question;

import java.util.*;
import java.util.Map.Entry;
import edu.wpi.htnlfd.model.*;
import edu.wpi.htnlfd.model.DecompositionClass.*;
import edu.wpi.htnlfd.model.TaskClass.*;

public class AskRepeatedStep extends Question {

   String taskName = null;

   String decName = null;

   String uri = null;

   String stepName = null;

   int times = 0;

   public AskRepeatedStep (int priority) {
      super(priority);
      this.question = "";
   }

   @Override
   public Question ask (TaskModel taskModel) {
      for (TaskClass task : taskModel.getTaskClasses()) {
         Question q = findRepeatedStep(taskModel, task);
         if ( q != null )
            return this;
      }
      return null;
   }

   public AskRepeatedStep findRepeatedStep (TaskModel taskModel, TaskClass task) {

      for (DecompositionClass dec : task.getDecompositions()) {
         if ( dec.getStepNames().size() <= 1 )
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

         for (Entry<Step, List<Step>> step : steps.entrySet()) {
            List<String> list = names.get(step.getKey());
            int maxOccurs = list.size();
            
            this.stepName = list.get(maxOccurs - 1);
            this.times = maxOccurs;
            this.taskName = task.getId();
            this.decName = dec.getId();
            this.uri = task.getQname().getNamespaceURI();
            
            this.question = "Is "+this.uri+ ":" + this.taskName+":"+this.decName
                  +":"+this.stepName + " repeated "
                  + this.times + " times?";
            AskQuestion.properties.put("TellMaxOccurs@format",this.question);
            return this;
         }
      }
      return null;

   }

}
