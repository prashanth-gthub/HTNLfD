package edu.wpi.htnlfd.graph;

import edu.wpi.htnlfd.model.*;
import edu.wpi.htnlfd.model.DecompositionClass.Step;
import java.util.*;

public class Node {

   boolean visited = false;

   Component component;

   int level = 0;

   Step step;

   List<TaskClass> tasks = new ArrayList<TaskClass>();

   List<DecompositionClass> decompositions = new ArrayList<DecompositionClass>();;

   List<String> stepNames = new ArrayList<String>();

   List<Node> childs = new ArrayList<Node>();

   List<Node> parents = new ArrayList<Node>();

   public Node (Step step, TaskClass task, DecompositionClass decomposition,
         String stepName) {
      super();
      this.step = step;
      if ( task != null )
         this.tasks.add(task);
      if ( decomposition != null )
         this.decompositions.add(decomposition);
      if ( stepName != null )
         this.stepNames.add(stepName);
   }

   public Node () {
   }

   public Node (Component component) {
      super();
      this.component = component;
   }

   public Component getComponent () {
      return component;
   }

   public void setComponent (Component component) {
      this.component = component;
   }

   /**
    * Checks if two nodes are equivalent.
    */
   public boolean isEquivalent (Node comp, TaskModel taskModel) {
      if ( this.equals(comp) ) {
         return true;
      }
      if ( this.step != null && comp.step != null
         && this.step.isEquivalent(comp.step, taskModel) ) {
         Map.Entry<String, Step> entry1 = new AbstractMap.SimpleEntry<String, Step>(
               this.stepNames.get(0), this.step);
         Map.Entry<String, Step> entry2 = new AbstractMap.SimpleEntry<String, Step>(
               comp.stepNames.get(0), comp.step);
         if ( this.decompositions.get(0).checkStepInputs(entry1,
               this.tasks.get(0), entry2, comp.tasks.get(0),
               this.decompositions.get(0), comp.decompositions.get(0),
               taskModel) )
            return true;
      }
      return false;
   }

   /**
    * Prints the node.
    */
   public void printNode () {
      System.out.println("-----------------");
      Node root = this;
      if ( root.stepNames == null )
         return;
      for (int i = 0; i < root.stepNames.size(); i++) {
         System.out.print(root.stepNames.get(i) + " "
            + root.tasks.get(i).getId() + " " + root.level + "      ");
         for (int j = 0; j < root.childs.size(); j++)
            if ( root.childs.get(j).tasks.size() > 0 )
               System.out.print(root.childs.get(j).tasks.get(0).getId() + " "
                  + root.childs.get(j).tasks.size() + " ");
         System.out.print("Par:  ");
         for (int j = 0; j < root.parents.size(); j++)
            if ( root.parents.get(j).tasks.size() > 0 )
               System.out.print(root.parents.get(j).tasks.get(0).getId() + " "
                  + root.parents.get(j).tasks.size() + " ");
         System.out.println();
      }
      System.out.println("-----------------");
   }

}