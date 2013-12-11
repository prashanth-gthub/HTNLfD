package edu.wpi.htnlfd.question;

import edu.wpi.htnlfd.model.*;

public class AskAppCondition extends Question {

   public AskAppCondition (int priority) {
      super(priority);
      this.question = "";
   }

   @Override
   public Question ask (TaskModel taskModel) {
      for (TaskClass task : taskModel.getTaskClasses()) {
         if ( task.getDecompositions().size() > 1 ) {
            for (DecompositionClass dec : task.getDecompositions()) {
               if ( dec.getApplicable() == null || dec.getApplicable() == "" ) {
                  this.question = "What is" + task.getId() + " " + dec.getId()
                     + " applicability condition?";
                  AskQuestion.properties.put("TellAppCondition@format", "What is "
                        + task.getQname().getNamespaceURI() + ":" + task.getId() + "'s "
                        + "applicability condition?");
                  return this;
               }
            }
         }
      }
      return null;
   }

}
