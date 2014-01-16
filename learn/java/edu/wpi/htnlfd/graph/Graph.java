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
   public void mergeTasks (TaskClass task, TaskClass newTask,
         TaskModel taskModel, Demonstration demonstration) {
      this.demonstration = demonstration;
      this.root = new Node(null, NType.Root, -1);
      getTree(task, root);
      Node demonstrationRoot = new Node(null, NType.Root, -1);
      getTree(newTask, demonstrationRoot);
      List<Node> demonstrationNodes = demonstrationRoot.getHighestLevel()
            .get(0);
      boolean merged = mergable(root, demonstrationNodes, taskModel,
            demonstrationRoot);
      if ( !merged ) {
         demonstration.addAlternativeRecipe(newTask, null, task);
      }

   }
   
   /**
    * Gets the tree of given taskClass.
    */
   public Node getTree (TaskClass task, Node root) {

      if ( task.getDecompositions().size() > 1 ) {
         for (DecompositionClass dec : task.getDecompositions()) {

            Node newRoot = new Node(null, NType.Required, -1);
            oneNode(dec, newRoot);
            newRoot.typeOfChildren = CType.Required;
            root.typeOfChildren = CType.Alter;
            root.children.add(newRoot);

         }
      } else {
         DecompositionClass dec = task.getDecompositions().get(0);
         oneNode(dec, root);
         root.typeOfChildren = CType.Required;
      }
      return root;
   }

   /**
    * Makes one Node recursively.
    */
   public Node oneNode (DecompositionClass dec, Node root) {
      for (String stepName : dec.getStepNames()) {
         Step stepReal = dec.getStep(stepName);
         Step step = dec.new Step(stepReal);
         if ( step.getType().isInternal() ) {
            Node newRoot = null;
            if ( step.getMinOccurs() != 0 ) {
               newRoot = new Node(null, NType.Required, -1);
               newRoot.step = step;
               newRoot.stepName = stepName;
            } else {
               newRoot = new Node(null, NType.Optional, -1);
               newRoot.step = step;
               newRoot.stepName = stepName;
            }
            getTree(step.getType(), newRoot);

            newRoot.parent = root;
            root.children.add(newRoot);
            if ( step.getRequired() != null && step.getRequired().size() != 0 )
               newRoot.setRequired(step.getRequired());
         } else {

            Node newRoot = null;
            if ( step.getMinOccurs() != 0 ) {
               newRoot = new Node(null, NType.Required, -1);
               newRoot.step = step;
               newRoot.stepName = stepName;
            } else {
               newRoot = new Node(null, NType.Optional, -1);
               newRoot.step = step;
               newRoot.stepName = stepName;
            }
            newRoot.parent = root;
            root.children.add(newRoot);
            if ( step.getRequired() != null && step.getRequired().size() != 0 )
               newRoot.setRequired(step.getRequired());
         }
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
      List<ArrayList<Node>> nodesListAllPathes = root.getLowestLevel();
      // Problem with ordering in permutations
      List<ArrayList<Node>> nodesListPermutation = new ArrayList<ArrayList<Node>>();
      for (ArrayList<Node> nodesL : nodesListAllPathes) {
         getPermutations(nodesListPermutation, nodesL);
      }
      boolean equal = statisfiable(nodesListPermutation, demonstration,
            taskModel);

      if ( equal ) {
         return true;
      }
      List<ArrayList<Node>> nodesList = root.getHighestLevel();

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
      Collections.sort(nodesLCS, Collections.reverseOrder());

      if ( nodesLCS.size() == 0 || nodesLCS.get(0).LCS == 0 ) {
         return false;
      }
      for (Pair pa : nodesLCS) {

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

      ArrayList<Node> evl = nodes;

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

         if ( (firstM == -1 && secondM == indicesMod.length)
            || (firstD == -1 && secondD == indicesDem.length) ) {
            return false;
         }

         else if ( firstM + 1 != secondM && firstD + 1 != secondD ) {

            // alternative
            List<Step> steps = new ArrayList<Step>();
            List<Node> nodeSteps = new ArrayList<Node>();

            steps = new ArrayList<Step>();
            for (int m = firstM + 1; m < secondM; m++) {

               Step stepFake = nodes.get(m).step;
               Step stepReal = stepFake.getDecompositionClass().getStep(
                     nodes.get(m).stepName);

               steps.add(stepReal);

            }

            List<Step> stepsD = new ArrayList<Step>();

            for (int m = firstD + 1; m < secondD; m++) {

               Step stepFake = demonstration.get(m).step;
               Step stepReal = stepFake.getDecompositionClass().getStep(
                     demonstration.get(m).stepName);
               stepsD.add(stepReal);
               nodeSteps.add(demonstration.get(m));
            }

            TaskClass originalTask = steps.get(0).getDecompositionClass()
                  .getGoal();

            TaskClass originalTaskCopy = new TaskClass(taskModel, originalTask);

            TaskClass newTask2 = originalTaskCopy.addInternalTask(
                  taskModel,
                  originalTaskCopy.getDecomposition(steps.get(0)
                        .getDecompositionClass().getId()), steps);

            Node tempRoot = new Node(null, NType.Root, root.id);
            getTree(newTask2, tempRoot);

            List<ArrayList<Node>> nodesList = tempRoot.getLowestLevel();

            List<ArrayList<Node>> nodesListPermutation = new ArrayList<ArrayList<Node>>();
            for (ArrayList<Node> nodesL : nodesList) {
               getPermutations(nodesListPermutation, nodesL);
            }

            taskModel.add(newTask2);
            if ( !statisfiable(nodesListPermutation, nodeSteps, taskModel) ) {

               taskModel.remove(newTask2);

               TaskClass newTask2Original = originalTask.addInternalTask(
                     taskModel, steps.get(0).getDecompositionClass(), steps);
               taskModel.add(newTask2Original);

               TaskClass newTask = steps
                     .get(0)
                     .getDecompositionClass()
                     .getGoal()
                     .addInternalTask(taskModel,
                           steps.get(0).getDecompositionClass(), stepsD);
               // taskModel.add(newTask);

               this.demonstration.addAlternativeRecipe(newTask, null,
                     newTask2Original);
            } else {

               taskModel.remove(newTask2);
            }

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
               Step stepFake = nodes.get(i).step;
               Step stepReal = stepFake.getDecompositionClass().getStep(
                     nodes.get(i).stepName);
               stepReal.setMinOccurs(0);
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

   /**
    * Gets different permutations of the list of nodes.
    */
   void getPermutations (List<ArrayList<Node>> nodesLists, List<Node> dem) {
      permutation(nodesLists, dem, new ArrayList<Node>());

      for (List<Node> nodeLists : nodesLists) {
         for (int m = 1; m < nodeLists.size(); m++) {
            nodeLists.get(m).parent = null;
            nodeLists.get(m).parent = nodeLists.get(m - 1);
            nodeLists.get(m - 1).children.clear();
            nodeLists.get(m - 1).children.add(nodeLists.get(m));
         }
      }
   }

   void permutation (List<ArrayList<Node>> nodesLists, List<Node> dem,
         List<Node> nodes) {

      // checking ordering constraints
      if ( nodes.size() != 0 && nodes.get(nodes.size() - 1).step != null
         && nodes.get(nodes.size() - 1).required != null
         && nodes.get(nodes.size() - 1).required.size() != 0 ) {
         for (int reqStep : nodes.get(nodes.size() - 1).required) {
            boolean contain = false;
            for (Node req : nodes) {
               if ( req.id == reqStep ) {
                  contain = true;
               }
            }
            if ( !contain ) {
               return;
            }
         }
      }
      
      if ( nodes.size() == dem.size() ) {
          nodesLists.add((ArrayList<Node>) nodes);

          return;
       }

      for (int i = 0; i < dem.size(); i++) {
         boolean contain = false;
         for (int j = 0; j < nodes.size(); j++) {
            if ( dem.get(i).step.equals(nodes.get(j).step) ) {
               contain = true;
               break;
            }
         }
         if ( !contain ) {
            List<Node> newNodes = new ArrayList<Node>();
            for (Node node : nodes) {
               if ( node.step != null ) {
                  Node temp = new Node(node.value, node.parent, node.children,
                        node.typeOfChildren, node.typeOfNode, node.step,
                        node.stepName, node.id, node.required);
                  newNodes.add(temp);
               } else {
                  Node temp = new Node(node.id);
                  newNodes.add(temp);
               }
            }
            Node temp = new Node(dem.get(i).value, dem.get(i).parent,
                  dem.get(i).children, dem.get(i).typeOfChildren,
                  dem.get(i).typeOfNode, dem.get(i).step, dem.get(i).stepName,
                  dem.get(i).id, dem.get(i).required);
            newNodes.add(temp);
            permutation(nodesLists, dem, newNodes);
         }
      }

   }

}
