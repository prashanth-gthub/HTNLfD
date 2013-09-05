/* Copyright (c) 2009 Charles Rich and Worcester Polytechnic Institute.
 * All Rights Reserved.  Use is subject to license terms.  See the file
 * "license.terms" for information on usage and redistribution of this
 * file and for a DISCLAIMER OF ALL WARRANTIES.
 */
package edu.wpi.disco.plugin;

import java.util.Collections;
import java.util.List;

import edu.wpi.cetask.Plan;
import edu.wpi.cetask.Task;
import edu.wpi.disco.Agenda;
import edu.wpi.disco.Disco;

/**
 * Convenient base plugin for defining plugins which return a single task.
 */
public abstract class SingletonPlugin extends BasePlugin {

   @Override
   final protected List<Task> newTaskList (Disco disco, Plan plan) {
      return Collections.singletonList(newTask(disco, plan));
   }
   
   abstract protected Task newTask (Disco disco, Plan plan);
   
   public SingletonPlugin (Agenda agenda, int priority) { 
      super(agenda, priority);
   }
}
