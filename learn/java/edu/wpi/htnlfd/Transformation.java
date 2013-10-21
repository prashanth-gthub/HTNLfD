package edu.wpi.htnlfd;

import edu.wpi.htnlfd.model.TaskModel;

abstract class Transformation {

   /**
    * Changes taskmodel to another valid taskmodel.
    */
   abstract void transform (TaskModel taskModel);
}
