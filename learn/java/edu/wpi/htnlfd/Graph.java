package edu.wpi.htnlfd;

import java.util.*;
import edu.wpi.htnlfd.Graph.Node;
import edu.wpi.htnlfd.model.DecompositionClass.Step;
import edu.wpi.htnlfd.model.*;

public class Graph {

   Node startNode = new Node();

   public class Node {

      boolean visited = false;

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

   }

   /**
    * Main function to merge two taskclasses.
    */
   public void addGraph (Demonstration demonstration, TaskClass task,
         TaskModel taskModel, TaskClass newTask) {

      List<ArrayList<Node>> nodesLists = new ArrayList<ArrayList<Node>>();
      List<Node> newNodes = new ArrayList<Node>();
      List<Node> nodesP = new ArrayList<Node>();

      nodesP.add(startNode);
      getNodes(task, taskModel, nodesP);

      findPathes(nodesLists);

      newNodes.add(startNode); // it should be here
      getNodes(newTask, taskModel, newNodes);

      int max = 0;
      int[][] maxSolution = null;
      List<Node> chosenNodes = null;
      List<Node> chosenDemNodes = null;

      List<ArrayList<Node>> nodesListDem = new ArrayList<ArrayList<Node>>();
      giveCombinations(nodesListDem, newNodes.subList(1, newNodes.size()));
      // nodesListDem.add((ArrayList<Node>) newNodes);
      for (ArrayList<Node> dem : nodesListDem) {
         dem.add(0, startNode);
         for (int i = 0; i < nodesLists.size(); i++) {
            List<Node> nodes = nodesLists.get(i);
            int M = nodes.size() - 1;
            int N = dem.size() - 1;
            int[][] opt = new int[M + 1][N + 1];

            int LCS = findLCS(nodes.subList(1, nodes.size()),
                  dem.subList(1, dem.size()), taskModel, opt);
            if ( max < LCS ) {
               chosenNodes = nodes;
               chosenDemNodes = dem;
               maxSolution = opt;
               max = LCS;
            }
            System.out.println("LCS = " + LCS);
         }
      }

      merge(chosenNodes.subList(1, chosenNodes.size()),
            chosenDemNodes.subList(1, chosenDemNodes.size()), taskModel,
            maxSolution);

      bfs(chosenNodes.subList(1, chosenNodes.size()));

      optional(taskModel);
      alternativeRecipe(demonstration, taskModel, task, newTask);
   }

   /**
    * Finds pathes.
    */
   void findPathes (List<ArrayList<Node>> nodesLists) {
      List<Node> newNodes = new ArrayList<Node>();
      nodesLists.add((ArrayList<Node>) newNodes);
      newNodes.add(startNode);
      branch(nodesLists, newNodes, startNode);

   }

   /**
    * Finds pathes with a recursive function.
    */
   void branch (List<ArrayList<Node>> nodesLists, List<Node> nodes, Node root) {

      if ( root.childs.size() == 1 ) {
         nodes.add(root.childs.get(0));
         branch(nodesLists, nodes, root.childs.get(0));
      } else if ( root.childs.size() > 1 ) {
         int size = nodes.size();
         nodes.add(root.childs.get(0));
         branch(nodesLists, nodes, root.childs.get(0));

         for (int i = 1; i < root.childs.size(); i++) {

            List<Node> newNodes = new ArrayList<Node>();
            for (int k = 0; k < size; k++) {
               Node node = nodes.get(k);
               Node temp = null;
               if ( node.step != null ) {
                  temp = new Node(node.step, node.tasks.get(0),
                        node.decompositions.get(0), node.stepNames.get(0));
               } else {
                  temp = new Node(node.step, null, null, null);
               }
               temp.parents.addAll(node.parents);
               temp.childs.addAll(node.childs);
               newNodes.add(temp);

            }
            nodesLists.add((ArrayList<Node>) newNodes);
            newNodes.add(root.childs.get(i));
            branch(nodesLists, newNodes, root.childs.get(i));

         }
      }

   }

   /**
    * Breath first search.
    */
   public void bfs (List<Node> nodes) {

      System.out.println("...........................");
      Queue<Node> queue = new LinkedList<Node>();
      Node root = nodes.get(0);
      printNode(root);
      queue.add(root);
      root.visited = true;
      root.level = 1;
      while (!queue.isEmpty()) {
         Node node = (Node) queue.remove();

         for (Node childNode : node.childs) {
            if ( !childNode.visited ) {
               childNode.visited = true;
               childNode.level = node.level + 1;
               printNode(childNode);
               queue.add(childNode);
            }
         }
      }

      for (Node nod : nodes) {
         nod.visited = false;
      }

   }

   /**
    * Prints the node.
    */
   public void printNode (Node root) {
      System.out.println("-----------------");
      if ( root.stepNames == null )
         return;
      for (int i = 0; i < root.stepNames.size(); i++) {
         System.out.println(root.stepNames.get(i) + " "
            + root.tasks.get(i).getId() + " " + root.level);
      }
      System.out.println("-----------------");
   }

   /**
    * Converts TaskClass to Node.
    */
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

   /**
    * Merges two different demonstrations.
    */
   void merge (List<Node> x, List<Node> y, TaskModel taskModel, int[][] opt) {

      int M = x.size();
      int N = y.size();

      int i = 0, j = 0;
      List<Node> newNodes = new ArrayList<Node>();
      for (int k = 0; k < M; k++) {
         newNodes.add(null);
      }

      while (i < M && j < N) {
         if ( x.get(i).isEquivalent(y.get(j), taskModel) ) {

            newNodes.set(i, y.get(j));

            i++;
            j++;
         } else {

            if ( opt[i + 1][j] >= opt[i][j + 1] ) {
               // newNodes.set(i, null);
               i++;
            } else {

               j++;
            }
         }
      }

      for (int k = 0; k < newNodes.size(); k++) {
         if ( newNodes.get(k) != null ) {

            if ( k == 0 ) {
               startNode.childs.remove(1);
            }

            x.get(k).tasks.add(newNodes.get(k).tasks.get(0));
            x.get(k).decompositions.add(newNodes.get(k).decompositions.get(0));
            x.get(k).stepNames.add(newNodes.get(k).stepNames.get(0));

            // adding parents
            boolean contain = false;
            for (int l = 0; l < k; l++) {
               if ( newNodes.get(l) != null ) {
                  if ( newNodes.get(k).parents.get(0).step.equals(newNodes
                        .get(l).step) ) {
                     contain = true;
                     if ( !x.get(k).parents.contains(x.get(l)) ) {
                        x.get(k).parents.add(x.get(l));
                     }
                     break;
                  }
               }
            }
            if ( !contain ) {
               if ( !x.get(k).parents.contains(newNodes.get(k).parents.get(0)) ) {
                  x.get(k).parents.add(newNodes.get(k).parents.get(0));
               }
               if ( !newNodes.get(k).parents.get(0).childs.contains(x.get(k)) ) {
                  newNodes.get(k).parents.get(0).childs.add(x.get(k));
                  newNodes.get(k).parents.get(0).childs.remove(newNodes.get(k));
               }
            }

            if ( newNodes.get(k).childs.size() > 0
               && !x.get(k).childs.contains(newNodes.get(k).childs.get(0)) ) {
               boolean contain1 = false;
               for (int v = k + 1; v < M; v++) {
                  if ( newNodes.get(v) != null
                     && newNodes.get(v).step != null
                     && newNodes.get(v).step.equals(newNodes.get(k).childs
                           .get(0).step) ) {
                     x.get(k).childs.add(x.get(v));
                     if ( !x.get(v).parents.contains(x.get(k)) )
                        x.get(v).parents.add(x.get(k));
                     contain1 = true;
                     break;
                  }
               }
               if ( !contain1 )
                  x.get(k).childs.add(newNodes.get(k).childs.get(0));
            }

         }

      }

   }

   /**
    * Finds longest comment sequence.
    */
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

   /**
    * Find alternative recipe.
    */
   public int[][] findAlternativeRecipe (List<Node> x, List<Node> y,
         TaskModel taskModel) {

      int M = x.size();
      int N = y.size();
      int[][] interval = new int[2][2];

      int i = 0, j = 0;
      int startF = -1;
      int endF = -1;
      int startS = -1;
      int endS = -1;
      while (i < M && j < N) {
         if ( x.get(i).isEquivalent(y.get(j), taskModel) ) {
            startF = i;
            startS = j;
            i++;
            j++;
         } else
            break;
      }
      i = M - 1;
      j = N - 1;
      while (i >= 0 && j >= 0) {
         if ( x.get(i).isEquivalent(y.get(j), taskModel) ) {
            endF = i;
            endS = j;
            i--;
            j--;
         } else
            break;
      }
      i = startF + 1;
      j = startS + 1;

      int M1 = x.size();
      int N1 = y.size();
      int[][] opt = new int[M1 + 1][N1 + 1];

      int LCS = findLCS(x, y, taskModel, opt);
      if ( LCS != startF + x.size() - endF + 1 ) {
         interval[0][0] = -1;
         interval[0][1] = -1;
         interval[1][0] = -1;
         interval[1][1] = -1;
         return interval;
      }

      interval[0][0] = startF;
      interval[0][1] = endF;
      interval[1][0] = startS;
      interval[1][1] = endS;

      return interval;
   }

   /**
    * Depth first search
    */
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

   /**
    * Gives combinations of the nodes by considering their ordering constraints.
    */
   void giveCombinations (List<ArrayList<Node>> nodesLists, List<Node> dem) {
      combination(nodesLists, dem, new ArrayList<Node>());
   }

   /**
    * Recursive function for finding combinations.
    */
   void combination (List<ArrayList<Node>> nodesLists, List<Node> dem,
         List<Node> nodes) {

      if ( nodes.size() == dem.size() ) {
         nodesLists.add((ArrayList<Node>) nodes);

         return;
      }

      if ( nodes.size() != 0 && nodes.get(nodes.size() - 1).step != null
         && nodes.get(nodes.size() - 1).step.getRequired() != null
         && nodes.get(nodes.size() - 1).step.getRequired().size() != 0 ) {
         for (String reqStep : nodes.get(nodes.size() - 1).step.getRequired()) {
            boolean contain = false;
            for (Node req : nodes) {
               if ( req.stepNames.get(0).equals(reqStep) ) {
                  contain = true;
               }
            }
            if ( !contain ) {
               return;
            }
         }
      }

      for (int i = 0; i < dem.size(); i++) {
         if ( !nodes.contains(dem.get(i)) ) {
            List<Node> newNodes = new ArrayList<Node>();
            for (Node node : nodes) {
               if ( node.step != null ) {
                  Node temp = new Node(node.step, node.tasks.get(0),
                        node.decompositions.get(0), node.stepNames.get(0));
                  temp.parents.addAll(node.parents);
                  temp.childs.addAll(node.childs);
                  newNodes.add(temp);
               } else {
                  Node temp = new Node(node.step, null, null, null);
                  temp.parents.addAll(node.parents);
                  temp.childs.addAll(node.childs);
                  newNodes.add(temp);
               }
            }
            newNodes.add(dem.get(i));
            combination(nodesLists, dem, newNodes);
         }
      }

   }

   /**
    * Searches for optional steps, and changes their optional attribute.
    */
   void optional (TaskModel taskModel) {

      List<ArrayList<Node>> nodesLists = new ArrayList<ArrayList<Node>>();

      findPathes(nodesLists);
      for (ArrayList<Node> nodes1 : nodesLists) {
         for (ArrayList<Node> nodes2 : nodesLists) {
            if ( !nodes1.equals(nodes2) ) {
               int[][] interval = findAlternativeRecipe(
                     nodes1.subList(1, nodes1.size()),
                     nodes2.subList(1, nodes2.size()), taskModel);
               if ( interval[0][0] == interval[0][1] - 1 ) {
                  for (int i = interval[1][0] + 2; i < interval[1][1] + 1; i++) {
                     nodes2.get(i).step.setMinOccurs(0);
                  }
               } else if ( interval[1][0] == interval[1][1] - 1 ) {
                  for (int i = interval[0][0] + 2; i < interval[0][1] + 1; i++) {
                     nodes1.get(i).step.setMinOccurs(0);
                  }
               }
            }

         }
      }

   }

   /**
    * Searches for alternative recipes and adds them.
    */
   void alternativeRecipe (Demonstration demonstration, TaskModel taskModel,
         TaskClass task, TaskClass newTask) {
      List<ArrayList<Node>> nodesLists = new ArrayList<ArrayList<Node>>();

      findPathes(nodesLists);
      List<ArrayList<Node>> pairs = new ArrayList<ArrayList<Node>>();
      for (ArrayList<Node> nodes1 : nodesLists) {
         for (ArrayList<Node> nodes2 : nodesLists) {
            if ( !nodes1.equals(nodes2) ) {
               boolean contain = false;
               for (int i = 0; i < pairs.size(); i = i + 1) {
                  if ( pairs.get(i).equals(nodes2)
                     && pairs.get(i + 1).equals(nodes1) ) {
                     contain = true;
                     break;
                  }
               }

               if ( !contain ) {
                  pairs.add(nodes1);
                  pairs.add(nodes2);

                  int[][] interval = findAlternativeRecipe(
                        nodes1.subList(1, nodes1.size()),
                        nodes2.subList(1, nodes2.size()), taskModel);
                  List<Step> steps = new ArrayList<Step>();
                  List<Step> stepsAlt = new ArrayList<Step>();
                  if ( (interval[0][0] != interval[0][1] - 1)
                     && (interval[1][0] != interval[1][1] - 1) ) {
                     for (int i = interval[0][0] + 2; i < interval[0][1] + 1; i++) {
                        steps.add(nodes1.get(i).step);
                     }
                     TaskClass intTask = task.addInternalTask(taskModel, task
                           .getDecompositions().get(0), steps); // //////
                     taskModel.add(intTask);

                     for (int i = interval[1][0] + 2; i < interval[1][1] + 1; i++) {
                        stepsAlt.add(nodes2.get(i).step);
                     }

                     TaskClass recipeTask = task.addInternalTask(taskModel,
                           task.getDecompositions().get(0), stepsAlt);
                     // adding binding to constant
                     demonstration.addAlternativeRecipe(intTask, null,
                           recipeTask);
                  }
               }

            }

         }
      }
   }

}
