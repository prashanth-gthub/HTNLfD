package edu.wpi.htnlfd.graph;

import java.util.*;
import edu.wpi.htnlfd.Demonstration;
import edu.wpi.htnlfd.graph.Node.*;
import edu.wpi.htnlfd.model.*;
import edu.wpi.htnlfd.model.DecompositionClass.Step;

public class Graph {
   public Node root;

   public Demonstration demonstration;

   /**
    * Builds the tree and tries to merge the new demonstration with the current
    * model.
    */
   public void buildTree (TaskClass task, TaskClass newTask,
         TaskModel taskModel, Demonstration demonstration) {
      this.demonstration = demonstration;
      this.root = new Node(null, NType.Root);
      getTree(task, root);
      Node demonstrationRoot = new Node(null, NType.Root);
      getTree(newTask, demonstrationRoot);
      List<Node> demonstrationNodes = demonstrationRoot.evaluate().get(0);
      mergable(root, demonstrationNodes, taskModel, demonstrationRoot);

   }

   /**
    * Gets the tree of given taskClass.
    */
   public Node getTree (TaskClass task, Node root) {

      if ( task.getDecompositions().size() > 1 ) {
         for (DecompositionClass dec : task.getDecompositions()) {

            Node newRoot = null;
            newRoot = new Node(null, NType.Required);

            oneNode(dec, root);
            root.children.add(newRoot);
            root.typeOfChildren = CType.Alter;
         }
      } else {
         DecompositionClass dec = task.getDecompositions().get(0);
         oneNode(dec, root);
         root.typeOfChildren = CType.Required;
      }
      return root;
   }

   /**
    * One node.
    */
   public Node oneNode (DecompositionClass dec, Node root) {
      for (String stepName : dec.getStepNames()) {
         Step step = dec.getStep(stepName);
         /*
          * if ( step.getType().isInternal() ) { Node newRoot = null; if (
          * step.getMinOccurs() != 0 ) { newRoot = new Node(null,
          * NType.Required); newRoot.step = step; newRoot.stepName = stepName; }
          * else { newRoot = new Node(null, NType.Optional); newRoot.step =
          * step; newRoot.stepName = stepName; } getTree(step.getType(),
          * newRoot); root.children.add(newRoot); } else {
          */
         Node newRoot = null;
         if ( step.getMinOccurs() != 0 ) {
            newRoot = new Node(null, NType.Required);
            newRoot.step = step;
            newRoot.stepName = stepName;
         } else {
            newRoot = new Node(null, NType.Optional);
            newRoot.step = step;
            newRoot.stepName = stepName;
         }
         newRoot.parent = root;
         root.children.add(newRoot);
         // }
      }
      return null;
   }

   /**
    * Checks whether the current demonstration is satisfied by the model or not.
    */
   boolean statisfiable (List<ArrayList<Node>> nodesList,
         List<Node> demonstration, TaskModel taskModel) {
      boolean equal = false;
      for (List<Node> nodes : nodesList) {
         equal = false;
         if ( nodes.size() == demonstration.size() ) {
            equal = true;
            for (int i = 0; i < nodes.size(); i++) {
               if ( !nodes.get(i).isEquivalent(demonstration.get(i), taskModel) ) {
                  equal = false;
                  break;
               }
            }

            if ( equal ) {
               break;
            }
         }

      }
      return equal;
   }

   /**
    * Checks whether the current demonstration is satisfied by the model or not
    * if not, it tries to merge them.
    */
   boolean mergable (Node root, List<Node> demonstration, TaskModel taskModel,
         Node demonstrationRoot) {

      boolean retMerged = true;
      List<ArrayList<Node>> nodesList = root.evaluate();

      boolean equal = statisfiable(nodesList, demonstration, taskModel);

      if ( equal ) {
         return true;
      }
      class Pair implements Comparable<Pair> {
         int LCS;

         ArrayList<Node> nodes;

         public Pair (ArrayList<Node> nodes2, int lCS2) {
            this.LCS = lCS2;
            this.nodes = nodes2;
         }

         public int compareTo (Pair o) {
            return (new Integer(this.LCS)).compareTo(new Integer(o.LCS));
         }

      }
      ;

      List<Pair> nodesLCS = new ArrayList<Pair>();

      for (List<Node> nodes : nodesList) {
         List<Node> newNodes1 = new ArrayList<Node>();
         List<Node> newNodes2 = new ArrayList<Node>();
         int LCS = getLCS(nodes, demonstration, taskModel, newNodes1, newNodes2);
         nodesLCS.add(new Pair((ArrayList<Node>) nodes, LCS));
      }
      Collections.sort(nodesLCS);

      for (Pair pa : nodesLCS) {

         if ( root.typeOfChildren == Node.CType.Alter ) {

            List<Node> newNodes1 = new ArrayList<Node>();
            List<Node> newNodes2 = new ArrayList<Node>();
            getLCS(pa.nodes, demonstration, taskModel, newNodes1, newNodes2);
            if ( !merge(root, demonstration, newNodes1, newNodes2, pa.nodes,
                  taskModel) ) {
               retMerged = false;
            } else {
               retMerged = true;
               break;
            }

         } else if ( root.typeOfChildren == Node.CType.Required ) {

            List<Node> newNodes1 = new ArrayList<Node>();
            List<Node> newNodes2 = new ArrayList<Node>();
            getLCS(pa.nodes, demonstration, taskModel, newNodes1, newNodes2);

            if ( !merge(root, demonstration, newNodes1, newNodes2, pa.nodes,
                  taskModel) ) {
               /*
                * Node newRoot = new Node(null, NType.Root); this.root =
                * newRoot; newRoot.children.add(root); root.typeOfNode = null;
                * root.parent = newRoot;
                * newRoot.children.add(demonstrationRoot);
                */
               retMerged = false;
            } else {
               retMerged = true;
               break;
            }
         }
      }

      return retMerged;
   }

   /**
    * Merges the new demonstration with the model.
    */
   private boolean merge (Node root, List<Node> demonstration,
         List<Node> newNodes1, List<Node> newNodes2, ArrayList<Node> nodes,
         TaskModel taskModel) {
      boolean merged = true;

      List<ArrayList<Node>> eval = root.giveSeparateNodes();

      ArrayList<Node> evl = null;
      for (ArrayList<Node> ev : eval) {
         ArrayList<Node> copyEv = new ArrayList<Node>(ev);
         Iterator<Node> next = copyEv.iterator();
         while (next.hasNext()) {
            if ( next.next().typeOfNode == NType.Empty )
               next.remove();
         }
         evl = ev;
         for (int i = 0; i < nodes.size(); i++) {
            if ( !nodes.get(i).equals(copyEv.get(i)) ) {
               evl = null;
               break;
            }
         }
      }

      int[] indices = new int[newNodes2.size()];

      for (int i = 0; i < newNodes2.size(); i++) {
         int index = -1;
         for (int j = 0; j < evl.size(); j++) {
            Node e = evl.get(j);
            if ( e.equals(newNodes1.get(i)) ) {
               index = j;
               break;
            }
         }
         int tIndex = index;
         boolean yes = false;
         while (tIndex < evl.size()
            && evl.get(tIndex).typeOfNode != NType.Empty) {
            Node e = evl.get(tIndex);

            for (int j = 0; j < newNodes1.size(); j++) {

               if ( e.equals(newNodes1.get(j)) ) {
                  yes = true;
                  break;
               }
            }
            if ( !yes ) {
               break;
            }

            tIndex++;
         }
         if ( !yes ) {
            indices[i] = -1;
         } else {
            indices[i] = 1;
         }

      }

      int[] indicesDem = new int[demonstration.size()];

      for (int i = 0; i < demonstration.size(); i++) {
         indicesDem[i] = -1;
      }
      for (int i = 0; i < demonstration.size(); i++) {
         for (int j = 0; j < newNodes2.size(); j++) {
            if ( demonstration.get(i).equals(newNodes2.get(j)) ) {
               indicesDem[i] = indices[j];
            }
         }
      }

      int[] indicesMod = new int[nodes.size()];
      for (int i = 0; i < nodes.size(); i++) {
         indicesMod[i] = -1;
      }
      for (int i = 0; i < nodes.size(); i++) {
         for (int j = 0; j < newNodes1.size(); j++) {
            if ( nodes.get(i).equals(newNodes1.get(j)) ) {
               indicesMod[i] = indices[j];
            }
         }
      }

      int firstD = -1;
      int firstM = -1;
      int secondD = -1;
      int secondM = -1;
      while (secondD <= indicesDem.length - 1
         && secondM <= indicesMod.length - 1) {
         boolean set = false;
         for (int i = secondD + 1; i < indicesDem.length; i++) {

            int inD = indicesDem[i];

            if ( inD == 1 ) {
               secondD = i;
               set = true;
               break;
            }
         }
         if ( !set ) {
            secondD = indicesDem.length;
         }
         set = false;
         for (int j = secondM + 1; j < indicesMod.length; j++) {
            int inM = indicesMod[j];
            if ( inM == 1 ) {
               secondM = j;
               set = true;
               break;
            }
         }
         if ( !set ) {
            secondM = indicesMod.length;
         }

         if ( firstM + 1 != secondM && firstD + 1 != secondD ) {

            // alternative
            List<Step> steps = new ArrayList<Step>();

            steps = new ArrayList<Step>();
            for (int m = firstM + 1; m < secondM; m++) {
               steps.add(nodes.get(m).step);
            }
            TaskClass newTask2 = nodes.get(firstM + 1).step
                  .getDecompositionClass()
                  .getGoal()
                  .addInternalTask(taskModel,
                        nodes.get(firstM + 1).step.getDecompositionClass(),
                        steps);
            taskModel.add(newTask2);

            steps = new ArrayList<Step>();
            for (int m = firstD + 1; m < secondD; m++) {
               steps.add(demonstration.get(m).step);
            }
            TaskClass newTask = nodes.get(firstM + 1).step
                  .getDecompositionClass()
                  .getGoal()
                  .addInternalTask(taskModel,
                        nodes.get(firstM + 1).step.getDecompositionClass(),
                        steps);
            // taskModel.add(newTask);

            this.demonstration.addAlternativeRecipe(newTask, null, newTask2);

         } else if ( firstM + 1 == secondM && firstD + 1 != secondD ) {
            // optional
            int where = firstM;
            Node retnode = nodes.get(where);
            for (int i = firstD + 1; i < secondD; i++) {
               demonstration.get(i).typeOfNode = NType.Optional;
               demonstration.get(i).step.setMinOccurs(0);

               retnode = demonstration.get(i).addOptionalStep(retnode,
                     CType.Required, taskModel);

            }
         } else if ( firstD + 1 == secondD && firstM + 1 != secondM ) {
            // optional
            for (int i = firstM + 1; i < secondM; i++) {
               nodes.get(i).typeOfNode = NType.Optional;
               nodes.get(i).step.setMinOccurs(0);
            }
         } else if ( firstD + 1 == secondD && firstM + 1 == secondM ) {
            ;
         }
         firstD = secondD;
         firstM = secondM;
      }

      return merged;
   }

   /**
    * Finds the longest common subsequent.
    */
   int getLCS (List<Node> nodes1, List<Node> nodes2, TaskModel taskModel,
         List<Node> newNodes1, List<Node> newNodes2) {
      int M = nodes1.size();
      int N = nodes2.size();
      int[][] opt = new int[M + 1][N + 1];

      int LCS = findLCS(nodes1, nodes2, taskModel, opt);

      int i = 0, j = 0;

      while (i < M && j < N) {
         if ( nodes1.get(i).isEquivalent(nodes2.get(j), taskModel) ) {

            newNodes1.add(nodes1.get(i));
            newNodes2.add(nodes2.get(j));

            i++;
            j++;
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
    * Find the value of longest common subsequent.
    */
   public static int findLCS (List<Node> x, List<Node> y, TaskModel taskModel,
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
    * Breath first search.
    */
   public void bfs () {

      // System.out.println("...........................");
      Queue<Node> queue = new LinkedList<Node>();
      Node root = this.root;
      root.printNode();
      queue.add(root);
      root.visited = true;
      List<Node> nodes = new ArrayList<Node>();
      while (!queue.isEmpty()) {
         Node node = (Node) queue.remove();
         nodes.add(node);
         for (Node childNode : node.children) {
            if ( !childNode.visited ) {
               childNode.visited = true;
               childNode.printNode();
               queue.add(childNode);

            }
         }
      }

      for (Node nod : nodes) {
         nod.visited = false;
      }

   }

}
