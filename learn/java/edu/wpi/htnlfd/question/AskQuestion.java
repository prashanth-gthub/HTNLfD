package edu.wpi.htnlfd.question;

import org.w3c.dom.*;
import java.util.*;
import edu.wpi.htnlfd.model.*;

public class AskQuestion {
   public static final String filename = "models\\Tell";

   public String namespace = "urn:disco.wpi.edu:htnlfd:tell";
   public static Properties properties = new Properties();

   private List<Question> questions = new ArrayList<Question>(); // final

   public AskQuestion (List<Question> questions) {
      super();
      this.questions = questions;
   }

   public AskQuestion () {
      Question q1 = new AskAppCondition(2);
      addQuestion(q1);
      Question q2 = new AskRepeatedStep(1);
      addQuestion(q2);
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
   
   public Node toNode(TaskModel taskModel, Document document){
      
      Element taskModelElement = null;

      taskModelElement = document.createElementNS(TaskModel.xmlnsValue,
            "taskModel");
      document.appendChild(taskModelElement);

      Attr about = document.createAttribute("about");
      about.setValue(this.namespace);
      taskModelElement.setAttributeNode(about);
      Attr xmlns = document.createAttribute("xmlns");
      xmlns.setValue(TaskModel.xmlnsValue);
      taskModelElement.setAttributeNode(xmlns);
      

      for (Question question : this.questions) {
         Question q = question.ask(taskModel);
         if ( q != null ){
            taskModelElement.appendChild(q.toNode(document));
         }
      }
      return taskModelElement;
   }
}
