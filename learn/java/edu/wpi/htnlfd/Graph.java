package edu.wpi.htnlfd;

import java.util.*;
import edu.wpi.htnlfd.model.DecompositionClass.Step;
import edu.wpi.htnlfd.model.*;

public class Graph {

   List<Node> graph;

   List<Node> roots = new ArrayList<Node>();

   public class Node {

      boolean visited = false;

      Step step;

      List<TaskClass> tasks = new ArrayList<TaskClass>();

      List<DecompositionClass> decompositions = new ArrayList<DecompositionClass>();;

      List<String> stepNames = new ArrayList<String>();;

      List<Node> childs = new ArrayList<Node>();

      List<Node> parents = new ArrayList<Node>();

      public Node (Step step, TaskClass task, DecompositionClass decomposition,
            String stepName) {
         super();
         this.step = step;
         this.tasks.add(task);
         this.decompositions.add(decomposition);
         this.stepNames.add(stepName);
      }

      public boolean isEquivalent (Node comp, TaskModel taskModel) {
         if ( this.step.isEquivalent(comp.step, taskModel) ) {
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

   }

   public void addGraph(TaskClass task, DecompositionClass dec,
          TaskModel taskModel){
      List<Node> nodes = new ArrayList<Node>();
      
      getNodes(task, dec, taskModel, nodes);
      
      for(int i=0;i<nodes.size();i++){
         
         if(i-1>=0)
            nodes.get(i).parents.add(nodes.get(i-1));
         else
            roots.add(nodes.get(i));
         if(i+1<nodes.size())
            nodes.get(i).childs.add(nodes.get(i+1));
      }
      
      if(graph == null){
         graph = nodes;         
      }
      else
         merge(graph, nodes, taskModel);
      
      for(Node node:nodes){
         // add to graph
         getNode(node);
      }
      
      bfs();
   }
   public void test (TaskClass task1, DecompositionClass dec1,
         TaskClass task2, DecompositionClass dec2, TaskModel taskModel) {

      List<Node> nodes1 = new ArrayList<Node>();
      List<Node> nodes2 = new ArrayList<Node>();
      getNodes(task1, dec1, taskModel, nodes1);

      getNodes(task2, dec2, taskModel, nodes2);

      merge(nodes1, nodes2, taskModel);
      
      bfs();

   }

   public void bfs () {

      for(Node root:roots){
         System.out.println(".......................");
         Queue<Node> queue = new LinkedList<Node>();
         queue.add(root);
         printNode(root);
         root.visited = true;
         while (!queue.isEmpty()) {
            Node node = (Node) queue.remove();
   
            for (Node childNode : node.childs) {
               if ( !childNode.visited ) {
                  childNode.visited = true;
                  printNode(childNode);
                  queue.add(childNode);
               }
            }
         }
         
         for(Node node:graph){
            node.visited = false;
         }
         
      }
      
   }

   public void printNode (Node root) {
      System.out.println("-----------------");
      for(int i=0;i<root.stepNames.size();i++){
         System.out.println(root.stepNames.get(i) + " "
            + root.tasks.get(i).getId());
      }
      System.out.println("-----------------");
   }

   public void getNodes (TaskClass task, DecompositionClass dec,
         TaskModel taskModel, List<Node> nodes) {

      for (String stepName : dec.getStepNames()) {
         if ( taskModel.getTaskClass(dec.getStep(stepName).getType().getId()) == null ) {
            Node newNode1 = new Node(dec.getStep(stepName), task, dec, stepName);
            
            nodes.add(newNode1);
            
         } else {
            // Which DecompositionClass
            getNodes(dec.getStep(stepName).getType(), dec.getStep(stepName)
                  .getType().getDecompositions().get(0), taskModel, nodes);
         }
      }

   }

   void merge(List<Node> x, List<Node> y, TaskModel taskModel){
      
      int M = x.size();
      int N = y.size();
      int[][] opt = new int[M + 1][N + 1];
      
      int LCS = findLCS (x, y, taskModel, opt);
      
      System.out.println("LCS = "+LCS);
      int i = 0, j = 0;
      while (i < M && j < N) {
         if ( x.get(i).isEquivalent(y.get(j), taskModel) ) {

            x.get(i).tasks.add(y.get(j).tasks.get(0));
            x.get(i).decompositions.add(y.get(j).decompositions.get(0));
            x.get(i).stepNames.add(y.get(j).stepNames.get(0));

            if ( j - 1 >= 0 ) {
               x.get(i).parents.add(y.get(j - 1));
               y.get(j - 1).childs.add(x.get(i));
            }
            if(i==0 && j==0){
               roots.remove(roots.size()-1);
            }

            y.set(j, x.get(i));

            i++;
            j++;
         } else {

            if ( opt[i + 1][j] >= opt[i][j + 1] ) {
               i++;
            } else {

               //graph.add(y.get(j));
               j++;
            }
         }
      }
      
   }
   public int findLCS (List<Node> x, List<Node> y, TaskModel taskModel, int[][] opt) {

      int M = x.size();
      int N = y.size();
      int LCS = 0;
      for (int i = M - 1; i >= 0; i--) {
         for (int j = N - 1; j >= 0; j--) {
            if ( x.get(i).isEquivalent(y.get(j), taskModel) ) {
               opt[i][j] = opt[i + 1][j + 1] + 1;
            } else
               opt[i][j] = Math.max(opt[i + 1][j], opt[i][j + 1]);
         }
      }

      int i = 0, j = 0;
      while (i < M && j < N) {
         if ( x.get(i).isEquivalent(y.get(j), taskModel) ) {
            i++;
            j++;
            LCS++;
         } else {

            if ( opt[i + 1][j] >= opt[i][j + 1] ) {
               i++;
            } else {
               j++;
            }
         }
      }
      return LCS;
   }

   public void dfs () {

      Stack<Node> stack = new Stack<Node>();
      Node root = roots.get(0);
      stack.push(root);
      root.visited = true;
      printNode(root);
      while (!stack.isEmpty()) {
         Node node = (Node) stack.peek();
         for (Node childNode : node.childs) {
            if ( !childNode.visited ) {
               childNode.visited = true;
               printNode(childNode);
               stack.push(childNode);
            } else {
               stack.pop();
            }
         }

      }
      // Clear visited property of nodes
   }
   
   void getNode(Node node){
      boolean contain = false;
      for(Node n:graph){
         if(n.equals(node)){
            contain = true;
            break;
         }
      }
      if(!contain){
         graph.add(node);
      }
   }

}
