package edu.wpi.htnlfd.graph;

import java.util.*;
import java.util.Map.Entry;
import edu.wpi.htnlfd.Demonstration;
import edu.wpi.htnlfd.graph.Graph.Pair;
import edu.wpi.htnlfd.model.DecompositionClass.Step;
import edu.wpi.htnlfd.model.*;

public class Graph {

   Node startNode = null;

   Node endNode = null;

   List<Component> components;

   static Map<String, Graph> taskGraphs = new HashMap<String, Graph>();

   public class Triple {
      public Object fisrt;

      public Object second;

      public Object third;

      public Triple (Object first, Object second, Object third) {
         super();
         this.fisrt = first;
         this.second = second;
         this.third = third;
      }

   }

   public class Pair {
      public Object left;

      public Object right;

      public Pair (Object left, Object right) {
         super();
         this.left = left;
         this.right = right;
      }

   }

   public Graph (String taskName) {
      if ( !taskGraphs.containsKey(taskName) ) {

         this.startNode = new Node();
         this.endNode = new Node();
         components = new ArrayList<Component>();
         taskGraphs.put(taskName, this);
      } else {
         this.startNode = taskGraphs.get(taskName).startNode;
         this.endNode = taskGraphs.get(taskName).endNode;
         this.components = taskGraphs.get(taskName).components;
      }
   }

   public static Graph getGraph (String taskName) {
      if ( !taskGraphs.containsKey(taskName) ) {
         return new Graph(taskName);
      } else {
         return taskGraphs.get(taskName);
      }
   }

   /**
    * Finds the longest common subsequence of two list of nodes, and fills the
    * newNodes1 and newNodes2 list by their common subsequence.
    */
   void getLCS (List<Node> nodes1, List<Node> nodes2, TaskModel taskModel,
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

   }

   /**
    * Main function to merge two taskclasses.
    * 
    * @return
    */
   /*
    * public boolean addGraph (Demonstration demonstration, TaskClass task,
    * TaskModel taskModel, TaskClass newTask) { List<ArrayList<Node>> nodesLists
    * = new ArrayList<ArrayList<Node>>(); List<Node> newNodes = new
    * ArrayList<Node>(); List<Node> nodesP = new ArrayList<Node>();
    * nodesP.add(startNode); getNodes(task, taskModel, nodesP); //bfs(nodesP);
    * findPathes(nodesLists, startNode); newNodes.add(startNode); // it should
    * be here getNodes(newTask, taskModel, newNodes); int max = 0; int[][]
    * maxSolution = null; List<Node> chosenNodes = null; List<Node>
    * chosenDemNodes = null; List<ArrayList<Node>> nodesListDem = new
    * ArrayList<ArrayList<Node>>(); giveCombinations(nodesListDem,
    * newNodes.subList(1, newNodes.size())); //
    * nodesListDem.add((ArrayList<Node>) newNodes); for (ArrayList<Node> dem :
    * nodesListDem) { dem.add(0, startNode); for (int i = 0; i <
    * nodesLists.size(); i++) { List<Node> nodes = nodesLists.get(i); int M =
    * nodes.size() - 1; int N = dem.size() - 1; int[][] opt = new int[M + 1][N +
    * 1]; int LCS = findLCS(nodes.subList(1, nodes.size()), dem.subList(1,
    * dem.size()), taskModel, opt); if ( max < LCS ) { chosenNodes = nodes;
    * chosenDemNodes = dem; maxSolution = opt; max = LCS; } //
    * System.out.println("LCS = " + LCS); } } if(max == 0){ return false; }
    * merge(chosenNodes.subList(1, chosenNodes.size()),
    * chosenDemNodes.subList(1, chosenDemNodes.size()), taskModel, maxSolution);
    * bfs(chosenNodes); boolean opt = optional(taskModel); boolean alt =
    * alternativeRecipe(demonstration, taskModel, task, newTask); return alt ||
    * opt; }
    */

   public boolean addGraph (Demonstration demonstration, TaskClass task,
         TaskModel taskModel, TaskClass newTask) {

      List<Node> nodesP = new ArrayList<Node>();

      // nodesP.add(startNode);
      getNodes(newTask, taskModel, nodesP);

      if ( this.components.size() == 0 ) {
         Component component = new Component(1, this.startNode, this.endNode);
         this.components.add(component);
         component.join(nodesP, this, taskModel);

         // component.bfs();
      } else {
         boolean canJoin = false;

         for (int i = 0; i < this.components.size(); i++) {
            if ( this.components.get(i).canJoin(nodesP, this, taskModel) ) {

               this.components.get(i).join(nodesP, this, taskModel);
               canJoin = true;
               break;
            }
         }

         if ( !canJoin ) {
            int max = 0;
            for (Component comp : this.components) {
               max = comp.getId() > max ? comp.getId() : max;
            }
            Component component = new Component(max + 1, this.startNode,
                  this.endNode);
            this.components.add(component);
            component.join(nodesP, this, taskModel);

            // component.bfs();
         }
      }

      boolean opt = optional(taskModel, startNode);

      boolean alt = alternativeRecipe(demonstration, taskModel, task, newTask);

      return false;
   }

   /**
    * Finds pathes.
    */
   void findPathes (List<ArrayList<Node>> nodesLists, Node start) {
      List<Node> newNodes = new ArrayList<Node>();
      nodesLists.add((ArrayList<Node>) newNodes);
      newNodes.add(start); // startNode
      branch(nodesLists, newNodes, start);

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
               /*
                * Node node = nodes.get(k); Node temp = null; if ( node.step !=
                * null ) { temp = new Node(node.step, node.tasks.get(0),
                * node.decompositions.get(0), node.stepNames.get(0)); } else {
                * temp = new Node(node.step, null, null, null); }
                * temp.parents.addAll(node.parents);
                * temp.childs.addAll(node.childs);
                */
               newNodes.add(nodes.get(k));

            }
            nodesLists.add((ArrayList<Node>) newNodes);
            newNodes.add(root.childs.get(i));
            branch(nodesLists, newNodes, root.childs.get(i));

         }
      }

   }

   /**
    * Converts TaskClass to Node.
    */
   public void getNodes (TaskClass task, TaskModel taskModel, List<Node> nodes) {

      // adding optional steps and alternative recipes

      for (DecompositionClass dec : task.getDecompositions()) {

         for (String stepName : dec.getStepNames()) {
            Node newNode1 = new Node(dec.getStep(stepName), task, dec, stepName);
            nodes.add(newNode1);
         }
      }

      for (int i = 0; i < nodes.size(); i++) {

         if ( i - 1 >= 0 ) {
            if ( (nodes.get(i - 1).decompositions.size() != 0
               && nodes.get(i).decompositions.size() != 0 && nodes.get(i).decompositions
                  .get(0).equals(nodes.get(i - 1).decompositions.get(0)))
               || (nodes.get(i - 1).decompositions.size() == 0 && nodes.get(i).decompositions
                     .size() == 0) || nodes.get(i - 1).equals(startNode) )
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
            if ( (nodes.get(i).decompositions.size() != 0
               && nodes.get(i + 1).decompositions.size() != 0 && nodes.get(i).decompositions
                  .get(0).equals(nodes.get(i + 1).decompositions.get(0)))
               || (nodes.get(i).decompositions.size() == 0 && nodes.get(i + 1).decompositions
                     .size() == 0) || nodes.get(i).equals(startNode) )
               nodes.get(i).childs.add(nodes.get(i + 1));

      }

      /*
       * boolean contain = true; while(contain){ contain = false; for(int
       * i=0;i<nodes.size();i++){ Node nod = nodes.get(i); if(nod.step!=null &&
       * (nod.step.getType().isInternal() ||
       * nod.step.getType().getId().startsWith("_"))){ List<Node> chNodes = new
       * ArrayList<Node>(); getNodes ( nod.step.getType(), taskModel, chNodes);
       * for(Node chn:chNodes){ if(chn.parents == null ||
       * chn.parents.size()==0){ chn.parents.addAll(nod.parents); for(Node
       * par:nod.parents){ for(Node pc:par.childs) if(pc.step.equals(nod.step))
       * par.childs.remove(pc); // check step par.childs.add(chn); } } }
       * if(chNodes.size()>=1) for(Node chn:chNodes){ if(chn.childs == null ||
       * chn.childs.size()==0){ chn.childs.addAll(nod.childs); for(Node
       * ch:nod.childs){ for(Node cp:ch.parents) if(cp.step.equals(nod.step))
       * ch.parents.remove(cp); // check step ch.parents.add(chn); } } }
       * nodes.remove(i); nodes.addAll(i,chNodes); contain = true; break; } } }
       */
   }

   /**
    * Merges graphs.
    */
   void mergeGraphs (List<Node> x, List<Node> y, TaskModel taskModel,
         Component component) {

      for (int k = 0; k < x.size(); k++) {
         y.get(k).tasks.get(0).setId("PlaceDishes111");
         x.get(k).tasks.add(y.get(k).tasks.get(0));
         x.get(k).decompositions.add(y.get(k).decompositions.get(0));
         x.get(k).stepNames.add(y.get(k).stepNames.get(0));

         // handling parents
         for (Node ypar : y.get(k).parents) {
            boolean contain = false;
            for (Node xpar : x.get(k).parents) {
               if ( xpar.equals(ypar) || xpar.step.equals(ypar.step) ) {
                  // ypar.printNode();
                  // xpar.printNode();
                  contain = true;
                  break;
               }
            }
            if ( !contain ) {
               boolean contC = false;
               for (int l = 0; l <= k - 1; l++) {
                  if ( y.get(l).equals(ypar) || y.get(l).step.equals(ypar.step) ) {
                     boolean contain1 = false;
                     for (Node xpar : x.get(k).parents) {
                        if ( xpar.equals(x.get(l))
                           || xpar.step.equals(x.get(l).step) ) {
                           contain1 = true;
                           break;
                        }
                     }
                     if ( !contain1 ) {
                        x.get(k).parents.add(x.get(l));

                     }

                     boolean contain2 = false;
                     for (Node xpar : x.get(l).childs) {
                        if ( xpar.equals(x.get(k))
                           || xpar.step.equals(x.get(k).step) ) {
                           contain2 = true;
                           break;
                        }
                     }
                     if ( !contain2 ) {
                        x.get(l).childs.add(x.get(k));
                     }

                     contC = true;
                     break;
                  }
               }
               if ( !contC ) {
                  x.get(k).parents.add(ypar);

               }
               ypar.childs.add(x.get(k));

            }
            for (Node ch : ypar.childs) {
               if ( ch.equals(y.get(k)) || ch.step.equals(y.get(k).step) ) {
                  ypar.childs.remove(ch);

                  break;
               }
            }
         }

         // handling children
         for (Node ych : y.get(k).childs) {
            boolean contain = false;
            for (Node xch : x.get(k).childs) {
               // ych.printNode();
               // xch.printNode();
               if ( xch.equals(ych) || xch.step.equals(ych.step) ) {
                  contain = true;
                  break;
               }
            }
            if ( !contain ) { //
               boolean contC = false;
               for (int l = k + 1; l < y.size(); l++) {
                  if ( y.get(l).equals(ych) || y.get(l).step.equals(ych.step) ) {
                     boolean contain1 = false;
                     for (Node xch : x.get(k).childs) {
                        if ( xch.equals(x.get(l))
                           || xch.step.equals(x.get(l).step) ) {
                           contain1 = true;
                           break;
                        }
                     }
                     if ( !contain1 ) {
                        x.get(k).childs.add(x.get(l));

                     }
                     boolean contain2 = false;
                     for (Node xch : x.get(l).parents) {
                        if ( xch.equals(x.get(k))
                           || xch.step.equals(x.get(k).step) ) {
                           contain2 = true;
                           break;
                        }
                     }
                     if ( !contain2 ) {
                        x.get(l).parents.add(x.get(k));
                     }
                     contC = true;
                     break;
                  }
               }
               if ( !contC ) {

                  boolean cont = false;
                  for (Node par : ych.parents) {
                     if ( par.equals(x.get(k))
                        || par.step.equals(x.get(k).step) ) {
                        cont = true;
                        break;
                     }
                  }
                  if ( !cont ) { //
                     ych.parents.add(x.get(k));
                  }
                  x.get(k).childs.add(ych);
                  for (int l = 0; l < ych.parents.size(); l++) {
                     if ( ych.parents.get(l).step.equals(y.get(k).step) ) {
                        ych.parents.remove(ych.parents.get(l));
                        break;
                     }
                  }
               }

            }

         }

      }
   }

   /**
    * Finds longest comment sequence.
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
    * Gives combinations of the nodes by considering their ordering constraints.
    */
   void giveCombinations (List<ArrayList<Node>> nodesLists, List<Node> dem) {
      combination(nodesLists, dem, new ArrayList<Node>());

      for (List<Node> nodeLists : nodesLists) {
         for (int m = 1; m < nodeLists.size(); m++) {
            nodeLists.get(m).parents.clear();
            nodeLists.get(m).parents.add(nodeLists.get(m - 1));
            nodeLists.get(m - 1).childs.clear();
            nodeLists.get(m - 1).childs.add(nodeLists.get(m));
         }
      }
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

      // checking ordering constraints
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
                  Node temp = new Node(node.step, node.tasks.get(0),
                        node.decompositions.get(0), node.stepNames.get(0));

                  newNodes.add(temp);
               } else {
                  Node temp = new Node(node.step, null, null, null);

                  newNodes.add(temp);
               }
            }
            Node temp = new Node(dem.get(i).step, dem.get(i).tasks.get(0),
                  dem.get(i).decompositions.get(0), dem.get(i).stepNames.get(0));
            newNodes.add(temp);
            combination(nodesLists, dem, newNodes);
         }
      }

   }

   /**
    * Searches for optional steps, and changes their optional attribute.
    * 
    * @return
    */
   boolean optional (TaskModel taskModel, Node root) {
      boolean opt = false;

      while (root.childs.size() != 0) {
         if ( root.childs.size() == 1 ) {
            root = root.childs.get(0);
         } else if ( root.childs.size() > 1 ) {
            boolean find = false;
            for (Node ch1 : root.childs) {
               for (Node ch2 : root.childs) {
                  if ( !ch1.equals(ch2) ) {
                     // not nested
                     Node node = ch2;
                     while (!node.childs.get(0).equals(ch1)) {

                        if ( node.childs.size() > 1 ) {

                           break;
                        }

                        node = node.childs.get(0);

                        if ( node == null || node.childs.size() == 0 )
                           break;
                     }

                     if ( node == null || node.childs.size() == 0 ) {
                        continue;
                     }
                     if ( node.childs.get(0).equals(ch1) ) {
                        node = ch2;
                        find = true;
                        while (!node.equals(ch1)) {
                           node.step.setMinOccurs(0);
                           System.out.println("optional "
                              + node.stepNames.get(0));
                           node = node.childs.get(0);

                        }
                     }

                  }
               }
            }

            for (Node child : root.childs) {
               optional(taskModel, child);
            }

            break;
         }
      }
      return opt;

   }

   /**
    * Find alternative recipe.
    */
   public void findAlternativeRecipe (Node root, Stack<Pair> stack,
         List<Triple> triples) {

      while (root.childs.size() != 0) {

         if ( root.parents.size() > 1 ) {
            if ( stack.size() != 0 ) {
               Pair top = stack.pop();
               Triple tr = new Triple(top.left, top.right, root);
               triples.add(tr);
               if ( ((Node) tr.third).step != null
                  && ((Node) tr.fisrt).step != null
                  && ((Node) tr.second).step != null )
                  System.out.println(((Node) tr.fisrt).stepNames.get(0) + " "
                     + ((Node) tr.second).stepNames.get(0) + "-"
                     + ((Node) tr.second).tasks.get(0).getId() + " "
                     + ((Node) tr.third).stepNames.get(0) + " ");
               else
                  System.out.println("inja");
            }
         }
         if ( root.childs.size() == 1 ) {
            root = root.childs.get(0);
         } else if ( root.childs.size() > 1 ) {

            for (Node child : root.childs) {
               stack = new Stack<Pair>();
               stack.push(new Pair(root, child));
               findAlternativeRecipe(child, stack, triples);
            }

            break;
         }

      }

   }

   /**
    * Searches for alternative recipes and adds them.
    */
   boolean alternativeRecipe (Demonstration demonstration, TaskModel taskModel,
         TaskClass task, TaskClass newTask) {
      TaskClass builtTask = null;
      boolean alt = false;
      boolean first = false;
      for (Component comp : this.components) {
         Map<Pair, ArrayList<Node>> altr = new HashMap<Pair, ArrayList<Node>>();

         List<Triple> triples = new ArrayList<Triple>();
         findAlternativeRecipe(comp.getStart(), null, triples);
         for (Triple tr : triples) {
            if ( !tr.second.equals(tr.third) ) {
               boolean contain = false;
               for (Entry<Pair, ArrayList<Node>> al : altr.entrySet()) {
                  if ( ((Node) al.getKey().left).step
                        .equals(((Node) tr.fisrt).step)
                     && ((Node) al.getKey().right).step
                           .equals(((Node) tr.third).step) ) {
                     al.getValue().add((Node) tr.second);
                     contain = true;
                     break;
                  }
               }
               if ( !contain ) {
                  ArrayList<Node> children = new ArrayList<Node>();
                  children.add((Node) tr.second);
                  altr.put(new Pair((Node) tr.fisrt, (Node) tr.third), children);
               }
            }
         }

         if ( triples.size() != 0 ) {
            Map<Pair, TaskClass> taskPair = addAlternativeRecipes(
                  demonstration, taskModel, task.getDecompositions().get(0),
                  altr);
            TaskClass compTask = replaceAltTask(taskModel,taskPair, comp.getStart(),task.getDecompositions().get(0));
            if ( !first ) {
               builtTask = compTask;
               first = true;
            } else {
               demonstration.addAlternativeRecipe(compTask, null, builtTask);
            }
         }

      }

      if(builtTask!=null){
         taskModel.remove(task);
         taskModel.add(builtTask);
      }
      return alt;

   }

   public TaskClass replaceAltTask (TaskModel taskModel, Map<Pair, TaskClass> taskPair,
          Node root, DecompositionClass dec) {
      List<Step> steps = new ArrayList<Step>();
      
      while (root.childs.size() != 0) {

         
         if ( root.childs.size() == 1 ) {
            steps.add(root.step);
            root = root.childs.get(0);
         } else if ( root.childs.size() > 1 ) {
            steps.add(root.step);
            for (Entry<Pair, TaskClass> pair : taskPair.entrySet()) {
               if ( ((Node) pair.getKey().left).step.equals(root.step) ) {
                  DecompositionClass.Step stp = dec.new Step(pair.getValue(),
                        1, 1, null);
                  steps.add(stp);
                  root = (Node) pair.getKey().right;
                  break;
               }
            }

         }

      }
      
      List<Step> validSteps = new ArrayList<Step>();
      for(Step st:steps){
         if(st!=null){
            validSteps.add(st);
         }
      }
      TaskClass task = dec.getGoal().addInternalTask(taskModel, dec,
            validSteps);
      return task;
   }

   private Map<Pair, TaskClass> addAlternativeRecipes (
         Demonstration demonstration, TaskModel taskModel,
         DecompositionClass dec, Map<Pair, ArrayList<Node>> altr) {
      Map<Pair, TaskClass> taskPair = new HashMap<Pair, TaskClass>();
      List<TaskClass> tasks = new ArrayList<TaskClass>();
      for (Entry<Pair, ArrayList<Node>> al : altr.entrySet()) {
         boolean first = false;
         for (Node child : al.getValue()) {
            List<Step> steps = new ArrayList<Step>();
            while (!child.step.equals(((Node) al.getKey().right).step)) {
               steps.add(child.step);
               child = child.childs.get(0);
            }

            TaskClass task = dec.getGoal().addInternalTask(taskModel, dec,
                  steps);
            if ( !first ) {
               tasks.add(task);
               first = true;
            } else {
               demonstration.addAlternativeRecipe(task, null,
                     tasks.get(tasks.size() - 1));
            }

         }
         taskPair.put(al.getKey(), tasks.get(tasks.size() - 1));
         taskModel.add(tasks.get(tasks.size() - 1));
      }

      return taskPair;
   }

   /**
    * Prints the graph.
    */
   public void printGraph () {

      for (Component comp : this.components) {
         comp.bfs();
      }
   }

}
