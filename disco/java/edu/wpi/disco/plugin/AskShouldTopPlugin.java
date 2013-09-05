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
import edu.wpi.disco.Agenda.Plugin;
import edu.wpi.disco.Disco;
import edu.wpi.disco.lang.Ask;

/**
 * Plugin for agent to ask about first live toplevel plan iff
 * Ask.Should property for task is true (default) and stack is empty.
 */
public class AskShouldTopPlugin extends DefaultPlugin {

   @Override 
   public List<Plugin.Item> apply () {
      Disco disco = getAgenda().getDisco();
      if ( !disco.isEmpty() ) return null;
      for (Plan top : disco.getTops()) {
         Task goal = top.getGoal();
         if ( getGenerateProperty(Ask.Should.class, goal) && top.isLive() )
            return Collections.singletonList(
                  new Plugin.Item(new Ask.Should(disco, self(), goal), top));
      }
      return null;
   }
   
   public AskShouldTopPlugin (Agenda agenda, int priority) { 
      super(agenda, priority);
   }
}
