package edu.wpi.htnlfd.question;

import java.util.*;
import edu.wpi.htnlfd.model.TaskModel;

public class AskQuestion {
   public static Properties properties = new Properties();
   private List<Question> questions = new ArrayList<Question>(); // final

   public AskQuestion (List<Question> questions) {
      super();
      this.questions = questions;
   }

   public AskQuestion () {
      Question q1 = new AskAppCondition(1);
      addQuestion (q1);
   }

   public void addQuestion (Question q) {
      if ( this.questions.size() == 0 ) {
         this.questions.add(q);
      } else {
         for (int i = 0; i < this.questions.size(); i++) {
            Question question = this.questions.get(i);
            if ( question.priority > q.priority ) {
               this.questions.add(i, q);
               break;
            }
         }
      }
   }

   public Question Ask (TaskModel taskModel) {
      for (Question question : this.questions) {
         Question q = question.ask(taskModel);
         if ( q != null )
            return q;
      }
      return null;
   }
}
