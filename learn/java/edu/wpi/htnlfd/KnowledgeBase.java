package edu.wpi.htnlfd;

import edu.wpi.htnlfd.model.TaskClass;

public abstract class KnowledgeBase {
   
   public abstract String getApplicable(TaskClass task, TaskClass newTask);
}
