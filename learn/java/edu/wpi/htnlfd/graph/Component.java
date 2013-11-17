package edu.wpi.htnlfd.graph;

import edu.wpi.htnlfd.model.*;
import java.util.*;

public class Component {
   private Node start = null;

   private Node end = null;

   private int id = 0;

   public Component (int id) {
      super();
      this.id = id;
      start = new Node(this);
      end = new Node(this);
   }

   public int getId () {
      return id;
   }

   public void setId (int id) {
      this.id = id;
   }

   public Component (int i, Node startNode, Node endNode) {
      super();
      this.start = new Node(this);
      this.end = new Node(this);

      start.parents.add(startNode);
      startNode.childs.add(start);

      endNode.parents.add(end);
      end.childs.add(endNode);
   }

   public Node getStart () {
      return start;
   }

   public void setStart (Node start) {
      this.start = start;
   }

   public Node getEnd () {
      return end;
   }

   public void setEnd (Node end) {
      this.end = end;
   }

   public List<Node> findSharedNodes (Graph graph, TaskModel taskModel) {

      List<ArrayList<Node>> nodesLists = new ArrayList<ArrayList<Node>>();
      graph.findPathes(nodesLists, start);
      List<Node> LCS = nodesLists.get(0);
      List<Node> newNodes1 = new ArrayList<Node>();
      List<Node> newNodes2 = new ArrayList<Node>();
      for (int i = 1; i < nodesLists.size(); i++) {
         List<Node> nodes = nodesLists.get(i);
         graph.getLCS(LCS, nodes, taskModel, newNodes1, newNodes2);
         LCS = newNodes1;
      }
      return LCS;
   }

   public boolean canJoin (List<Node> nodes, Graph graph, TaskModel taskModel) {
      if ( start.childs.size() == 0 )
         return true;
      List<Node> LCS = findSharedNodes(graph, taskModel);
      List<Node> newNodes1 = new ArrayList<Node>();
      List<Node> newNodes2 = new ArrayList<Node>();
      graph.getLCS(LCS, nodes, taskModel, newNodes1, newNodes2);
      if ( newNodes1.size() == 0 || newNodes2.size() == 0 )
         return false;
      return true;
   }

   public void join (List<Node> nodes, Graph graph, TaskModel taskModel) {
      boolean satisfy = false;

      List<ArrayList<Node>> nodesLists = new ArrayList<ArrayList<Node>>();
      graph.findPathes(nodesLists, start);

      if ( start.childs.size() == 0 ) {
         start.childs.add(nodes.get(0));
         nodes.get(0).parents.add(start);

         nodes.get(nodes.size() - 1).childs.add(end);
         end.parents.add(nodes.get(nodes.size() - 1));

         for (Node node : nodes) {
            node.setComponent(this);
         }

         return;
      }

      int max = 0;
      List<Node> chosenNodes = null;
      List<Node> chosenDemNodes = null;

      List<ArrayList<Node>> nodesListDem = new ArrayList<ArrayList<Node>>();

      graph.giveCombinations(nodesListDem, nodes);
      ArrayList<Node> demSave = null;

      for (ArrayList<Node> dem : nodesListDem) {

         for (int i = 0; i < nodesLists.size(); i++) {
            List<Node> path = nodesLists.get(i);
            List<Node> newNodes1 = new ArrayList<Node>();
            List<Node> newNodes2 = new ArrayList<Node>();

            List<Node> validPath = new ArrayList<Node>();
            for (Node pa : path) {
               if ( pa.step != null ) {
                  validPath.add(pa);
               }
            }

            graph.getLCS(validPath, dem, taskModel, newNodes1, newNodes2);
            List<Node> LCS = newNodes1;

            if ( max < LCS.size() ) {
               chosenNodes = newNodes1;
               chosenDemNodes = newNodes2;
               max = LCS.size();
               if(max == dem.size()){
                  //return; // satisfy
                  satisfy = true;
               }
               demSave = dem;
            }
         }
      }

      // should be here
      demSave.get(0).parents.add(this.start);
      this.start.childs.add(demSave.get(0));

      demSave.get(demSave.size() - 1).childs.add(this.end);
      this.end.parents.add(demSave.get(demSave.size() - 1));
      // ///

      for (Node node : demSave) {
         node.setComponent(this);
      }

      graph.mergeGraphs(chosenNodes, chosenDemNodes, taskModel, this);

      bfs();
   }

   public void bfs () {

      // System.out.println("...........................");
      Queue<Node> queue = new LinkedList<Node>();
      Node root = start;
      root.printNode();
      queue.add(root);
      root.visited = true;
      root.level = 1;
      List<Node> nodes = new ArrayList<Node>();
      while (!queue.isEmpty()) {
         Node node = (Node) queue.remove();
         nodes.add(node);
         for (Node childNode : node.childs) {
            if ( !childNode.visited ) {
               childNode.visited = true;
               childNode.level = node.level + 1;
               childNode.printNode();
               queue.add(childNode);

            }
         }
      }

      for (Node nod : nodes) {
         nod.visited = false;
      }

   }

   public void dfs (Node root) {

      Stack<Node> stack = new Stack<Node>();

      stack.push(root);
      root.visited = true;
      root.printNode();
      List<Node> nodes = new ArrayList<Node>();
      while (!stack.isEmpty()) {
         Node node = (Node) stack.peek();
         nodes.add(node);
         for (Node childNode : node.childs) {
            if ( !childNode.visited ) {
               childNode.visited = true;
               childNode.printNode();
               stack.push(childNode);
            } else {
               stack.pop();
            }
         }

      }
      for (Node nod : nodes) {
         nod.visited = false;
      }
   }

  

}
