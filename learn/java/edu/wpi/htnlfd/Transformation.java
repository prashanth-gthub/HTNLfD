package edu.wpi.htnlfd;

import edu.wpi.htnlfd.model.TaskModel;


abstract class Transformation {

   /**
    * Transform.
    * This function change our taskmodel
    */
   abstract void transform(TaskModel taskModel);
}
