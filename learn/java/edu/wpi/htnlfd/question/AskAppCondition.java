package edu.wpi.htnlfd.question;

import edu.wpi.htnlfd.model.*;

public class AskAppCondition extends Question {

   String taskName = null;
   String decName = null;
   String uri = null;
   
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
                  this.question = "What is " + task.getQname().getNamespaceURI() + ": "+task.getId() + " " + dec.getId()
                     + " applicability condition?";
                  this.taskName = task.getId();
                  this.decName = dec.getId();
                  this.uri = task.getQname().getNamespaceURI() ;
                  AskQuestion.properties.put("TellAppCondition@format",this.question);
                  return this;
               }
            }
         }
      }
      return null;
   }

}
