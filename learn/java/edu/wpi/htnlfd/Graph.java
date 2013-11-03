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
            Map.Entry<String,Step> entry1 =
                  new AbstractMap.SimpleEntry<String, Step>(this.stepNames.get(0), this.step);
            Map.Entry<String,Step> entry2 =
                  new AbstractMap.SimpleEntry<String, Step>(comp.stepNames.get(0), comp.step);
            if(this.decompositions.get(0).checkStepInputs (entry1, this.tasks.get(0),
                  entry2, comp.tasks.get(0), this.decompositions.get(0), comp.decompositions.get(0),
                  taskModel))
                     return true;
         }         
         return false;        
      }      

   }
   
   public void addGraph(TaskClass task1, DecompositionClass dec1,TaskClass task2, DecompositionClass dec2, TaskModel taskModel){
      
      List<Node> nodes1 = new ArrayList<Node>();
      List<Node> nodes2 =  new ArrayList<Node>();
      
      getNodes(task1, dec1, taskModel, nodes1);
      
      getNodes(task2, dec2, taskModel, nodes2);
      
      findLCS(nodes1,nodes2,taskModel);
      
   }
   
   public void getNodes(TaskClass task, DecompositionClass dec, TaskModel taskModel, List<Node> nodes){
      
      
      for(String stepName:dec.getStepNames()){
         if(taskModel.getTaskClass(dec.getStep(stepName).getType().getId()) == null){
            Node newNode1 = new Node(dec.getStep(stepName), task, dec, stepName);
            nodes.add(newNode1);
         }
         else{
            // Which DecompositionClass
            getNodes(dec.getStep(stepName).getType(), dec.getStep(stepName).getType().getDecompositions().get(0)
                  , taskModel, nodes);
         }
      }
      
   }
   
   public void findLCS(List<Node> x, List<Node> y, TaskModel taskModel){

      int M = x.size();
      int N = y.size();

      // opt[i][j] = length of LCS of x[i..M] and y[j..N]
      int[][] opt = new int[M+1][N+1];

      // compute length of LCS and all subproblems via dynamic programming
      for (int i = M-1; i >= 0; i--) {
          for (int j = N-1; j >= 0; j--) {
              if (x.get(i).isEquivalent(y.get(j),taskModel)){
                  opt[i][j] = opt[i+1][j+1] + 1;
              }
              else 
                  opt[i][j] = Math.max(opt[i+1][j], opt[i][j+1]);
          }
      }

      // recover LCS itself and print it to standard output
      int i = 0, j = 0;
      while(i < M && j < N) {
          if (x.get(i).isEquivalent(y.get(j),taskModel)) {      
             System.out.println(x.get(i).stepNames.get(0)+ " "+ x.get(i).tasks.get(0).getId());
             System.out.println(y.get(j).stepNames.get(0)+ " "+ y.get(j).tasks.get(0).getId());
              i++;
              j++;
          }
          else if (opt[i+1][j] >= opt[i][j+1]) i++;
          else                                 j++;
      }
   }
   
   
   
}
