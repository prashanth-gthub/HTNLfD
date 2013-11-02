package edu.wpi.htnlfd;

import java.util.*;
import edu.wpi.htnlfd.model.DecompositionClass.Step;
import edu.wpi.htnlfd.model.*;

public class Graph {

   Node root;
   public class Node{
      
      Step step;
      List<TaskClass> tasks = new ArrayList<TaskClass>();
      List<DecompositionClass> decompositions = new ArrayList<DecompositionClass>();;
      List<String> stepNames = new ArrayList<String>();;
      List<Node> childs = new ArrayList<Node>();
      List<Node> parents =  new ArrayList<Node>();



      public Node (Step step, TaskClass task,
            DecompositionClass decomposition, String stepName) {
         super();
         this.step = step;
         this.tasks.add(task);
         this.decompositions.add(decomposition);
         this.stepNames.add(stepName);
      }


      public boolean isEquivalent (Node comp, TaskModel taskModel) {
         if(this.step.isEquivalent(comp.step, taskModel)){  
            if(this.decompositions.get(0).checkInputs (this.stepNames.get(0), this.tasks.get(0),
                  comp.stepNames.get(0), comp.tasks.get(0), this.decompositions.get(0), comp.decompositions.get(0),
                  taskModel))
                     return true;
         }         
         return false;        
      }      

   }
   
   public void addGraph(TaskClass task1, DecompositionClass dec1,TaskClass task2, DecompositionClass dec2, TaskModel taskModel){
      
      List<Node> nodes1 = new ArrayList<Node>();
      List<Node> nodes2 =  new ArrayList<Node>();
      
      for(String stepName:dec1.getStepNames()){
         Node newNode1 = new Node(dec1.getStep(stepName), task1, dec1, stepName);
         nodes1.add(newNode1);
      }
      
      for(String stepName:dec2.getStepNames()){
         Node newNode2 = new Node(dec2.getStep(stepName), task2, dec2, stepName);
         nodes2.add(newNode2);
      }
      
      findLCS(nodes1,nodes2,taskModel);
      
   }
   
   public void findLCS(List<Node> x, List<Node> y, TaskModel taskModel){

      int M = x.size();
      int N = y.size();

      // opt[i][j] = length of LCS of x[i..M] and y[j..N]
      int[][] opt = new int[M+1][N+1];

      // compute length of LCS and all subproblems via dynamic programming
      for (int i = M-1; i >= 0; i--) {
          for (int j = N-1; j >= 0; j--) {
              if (x.get(i).isEquivalent(y.get(j),taskModel))
                  opt[i][j] = opt[i+1][j+1] + 1;
              else 
                  opt[i][j] = Math.max(opt[i+1][j], opt[i][j+1]);
          }
      }

      // recover LCS itself and print it to standard output
      int i = 0, j = 0;
      while(i < M && j < N) {
          if (x.get(i).isEquivalent(y.get(j),taskModel)) {      
             System.out.println(x.get(i).stepNames.get(0));
             System.out.println(y.get(j).stepNames.get(0));
              i++;
              j++;
          }
          else if (opt[i+1][j] >= opt[i][j+1]) i++;
          else                                 j++;
      }
   }
   
   
   
}
