package edu.wpi.htnlfd.question;

import edu.wpi.htnlfd.model.TaskModel;

public abstract class Question {
   public int priority;

   public String question;

   public Question (int priority) {
      super();
      this.priority = priority;
   }

   public abstract Question ask (TaskModel taskModel);
}
