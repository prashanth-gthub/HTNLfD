package edu.wpi.htnlfd;
import java.util.*;
import edu.wpi.htnlfd.model.DecompositionClass.Step;
import edu.wpi.htnlfd.model.*;

public class Graph {

   Node startNode = new Node();

   public class Node {

      boolean visited = false;

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
         this.tasks.add(task);
         this.decompositions.add(decomposition);
         this.stepNames.add(stepName);
      }

      public Node () {
         // TODO Auto-generated constructor stub
      }

      public boolean isEquivalent (Node comp, TaskModel taskModel) {
         if(this.equals(comp)){
            return true;
         }
         if (this.step!=null && comp.step!=null && this.step.isEquivalent(comp.step, taskModel) ) {
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

   public void addGraph (TaskClass task, TaskModel taskModel, TaskClass newTask) {

      List<ArrayList<Node>> nodesLists = new ArrayList<ArrayList<Node>>();
      List<Node> newNodes = new ArrayList<Node>();
      List<Node> nodesP = new ArrayList<Node>();


      nodesP.add(startNode);
      getNodes(task, taskModel, nodesP);

      findPathes(nodesLists);
      
      newNodes.add(startNode);   // it should be here
      getNodes(newTask, taskModel, newNodes);
      
      for (ArrayList<Node> nodL : nodesLists) {
         for (Node node : nodL) {
            printNode(node);
         }
      }
      int max = 0;
      int[][] maxSolution = null;
      List<Node> chosenNodes = null;

      for (int i = 0; i < nodesLists.size(); i++) {
         List<Node> nodes = nodesLists.get(i);
         int M = nodes.size()-1;
         int N = newNodes.size()-1;
         int[][] opt = new int[M + 1][N + 1];

         int LCS = findLCS(nodes.subList(1, nodes.size()), newNodes.subList(1, newNodes.size()), taskModel,
               opt);
         if ( max < LCS ) {
            chosenNodes = nodes;
            maxSolution = opt;
            max = LCS;
         }
         System.out.println("LCS = " + LCS);
      }

      merge(chosenNodes.subList(1, chosenNodes.size()), newNodes.subList(1, newNodes.size()), taskModel,
            maxSolution);

      bfs(chosenNodes);
   }

   void findPathes (List<ArrayList<Node>> nodesLists) {
      List<Node> newNodes = new ArrayList<Node>();
      nodesLists.add((ArrayList<Node>) newNodes);
      newNodes.add(startNode);
      branch(nodesLists, newNodes, startNode);
   }

   void branch (List<ArrayList<Node>> nodesLists, List<Node> nodes, Node root) {

      if ( root.childs.size() == 1 ) {
         nodes.add(root.childs.get(0));
         branch(nodesLists, nodes, root.childs.get(0));
      } else if ( root.childs.size() > 1 ) {
         int size = nodes.size();
         nodes.add(root.childs.get(0));
         branch(nodesLists, nodes, root.childs.get(0));

         for (int i = 1; i < root.childs.size(); i++) {

            List<Node> newNodes = new ArrayList<Node>(nodes.subList(0, size));
            nodesLists.add((ArrayList<Node>) newNodes);
            newNodes.add(root.childs.get(i));
            branch(nodesLists, newNodes, root.childs.get(i));

         }
      }

   }

   public void bfs (List<Node> nodes) {

      System.out.println("...........................");
      Queue<Node> queue = new LinkedList<Node>();
      Node root = nodes.get(1);
      printNode(root);
      queue.add(root);
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

      for (Node nod : nodes) {
         nod.visited = false;
      }

   }

   public void printNode (Node root) {
      System.out.println("-----------------");
      if(root.stepNames == null)
         return;
      for (int i = 0; i < root.stepNames.size(); i++) {
         System.out.println(root.stepNames.get(i) + " "
            + root.tasks.get(i).getId());
      }
      System.out.println("-----------------");
   }

   public void getNodes (TaskClass task, TaskModel taskModel, List<Node> nodes) {

      
      for (DecompositionClass dec : task.getDecompositions()) {

         for (String stepName : dec.getStepNames()) {
            Node newNode1 = new Node(dec.getStep(stepName), task, dec, stepName);
            nodes.add(newNode1);
         }
      }

      for (int i = 0; i < nodes.size(); i++) {

         if ( i - 1 >= 0 ) {
            nodes.get(i).parents.add(nodes.get(i - 1));
            if ( nodes.get(i - 1).stepNames.size() != 0
               && nodes.get(i).stepNames.size() != 0
               && !nodes.get(i).decompositions.get(0).isOptionalStep(
                     nodes.get(i).stepNames.get(0))
               && nodes.get(i - 1).decompositions.get(0).isOptionalStep(
                     nodes.get(i - 1).stepNames.get(0)) ) {
               int j = i - 1;
               while (nodes.get(j).decompositions.get(0).isOptionalStep(
                     nodes.get(j).stepNames.get(0))) {
                  j = j - 1;
               }

               nodes.get(i).parents.add(nodes.get(j));
               nodes.get(j).childs.add(nodes.get(i));
            }
         }

         if ( i + 1 < nodes.size() )
            nodes.get(i).childs.add(nodes.get(i + 1));

      }
   }

   void merge (List<Node> x, List<Node> y, TaskModel taskModel, int[][] opt) {

      int M = x.size();
      int N = y.size();

      int i = 0, j = 0;
      List<Node> newNodes = new ArrayList<Node>(x);
      
      while (i < M && j < N) {
         if ( x.get(i).isEquivalent(y.get(j), taskModel) ) {

            /*x.get(i).tasks.add(y.get(j).tasks.get(0));
            x.get(i).decompositions.add(y.get(j).decompositions.get(0));
            x.get(i).stepNames.add(y.get(j).stepNames.get(0));

            if ( j - 1 >= 0 ) {
               if(y.get(j - 1).stepNames!=null){
                  if(!x.get(i).parents.contains(y.get(j - 1)))
                     x.get(i).parents.add(y.get(j - 1));
                  if(!y.get(j - 1).childs.contains(x.get(i)))
                     y.get(j - 1).childs.add(x.get(i));
               }
            }
            y.set(j, x.get(i));*/

            newNodes.set(i, y.get(j));
            
            i++;
            j++;
         } else {

            if ( opt[i + 1][j] >= opt[i][j + 1] ) {
               newNodes.set(i, null);
               i++;
            } else {

               j++;
            }
         }
      }
      
      for(int k=0;k<newNodes.size();k++){
         if(newNodes.get(k)!=null){
            
            x.get(k).tasks.add(newNodes.get(k).tasks.get(0));
            x.get(k).decompositions.add(newNodes.get(k).decompositions.get(0));
            x.get(k).stepNames.add(newNodes.get(k).stepNames.get(0));
   
            if(!x.get(k).parents.contains(newNodes.get(k).parents.get(0))) 
               x.get(k).parents.add(newNodes.get(k).parents.get(0));
            if(!newNodes.get(k).parents.get(0).childs.contains(x.get(k))){
               newNodes.get(k).parents.get(0).childs.add(x.get(k));
               newNodes.get(k).parents.get(0).childs.remove(newNodes.get(k));
            }
            if(newNodes.get(k).childs.size()>0 && k+1<newNodes.size() && newNodes.get(k+1)==null  && !x.get(k).childs.contains(newNodes.get(k).childs.get(0))) 
               x.get(k).childs.add(newNodes.get(k).childs.get(0));
         }

      }
 
   }

   public int findLCS (List<Node> x, List<Node> y, TaskModel taskModel,
         int[][] opt) {

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

   public void dfs (Node root) {

      Stack<Node> stack = new Stack<Node>();

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
}
