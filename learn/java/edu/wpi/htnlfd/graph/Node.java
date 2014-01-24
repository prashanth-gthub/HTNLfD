package edu.wpi.htnlfd.graph;

import edu.wpi.htnlfd.model.DecompositionClass.Step;
import edu.wpi.htnlfd.model.*;
import java.util.*;

public class Node {

   public enum CType {
      Alter, Required
   };

   public enum NType {
      Optional, Required, Root, Empty
   };

   static int idCounter = 0;

   public int id;

   public Step step;

   public String stepName;

   public List<Integer> required = new ArrayList<Integer>();

   public Node value;

   public Node parent;

   public List<Node> children = new ArrayList<Node>();

   public CType typeOfChildren = CType.Required;

   public NType typeOfNode = NType.Required;

   public boolean visited;

   public Node (Node value, NType typeOfNode, int id) {
      super();
      this.value = value;
      this.typeOfNode = typeOfNode;
      if ( id == -1 ) {
         idCounter++;
         this.id = idCounter;
      } else {
         this.id = id;
      }
   }

   public Node (Node value, Node parent, List<Node> children,
         CType typeOfChildren, NType typeOfNode, int id, List<Integer> required) {
      super();
      this.value = value;
      this.parent = parent;
      this.children = children;
      this.typeOfChildren = typeOfChildren;
      this.typeOfNode = typeOfNode;
      if ( id == -1 ) {
         idCounter++;
         this.id = idCounter;
      } else {
         this.id = id;
      }
      this.required.addAll(required);
   }

   public Node (Node value, Node parent, List<Node> children,
         CType typeOfChildren, NType typeOfNode, Step step, String stepNam,
         int id, List<Integer> required) {
      super();
      this.value = value;
      this.parent = parent;
      this.children = children;
      this.typeOfChildren = typeOfChildren;
      this.typeOfNode = typeOfNode;
      this.step = step;
      this.stepName = stepNam;
      if ( id == -1 ) {
         idCounter++;
         this.id = idCounter;
      } else {
         this.id = id;
      }
      this.required.addAll(required);
   }

   public Node (int id) {
      if ( id == -1 ) {
         idCounter++;
         this.id = idCounter;
      } else {
         this.id = id;
      }
   }

   /**
    * Evaluate nodes value.
    */
   public List<ArrayList<Node>> getHighestLevel () {

      List<ArrayList<Node>> nodeList = new ArrayList<ArrayList<Node>>();

      if ( this.typeOfChildren == CType.Alter ) {
         for (Node child : this.children) {
            ArrayList<Node> newNodes = new ArrayList<Node>();
            nodeList.add(newNodes);
            eval(child, newNodes, nodeList);
         }
      } else {
         ArrayList<Node> newNodes = new ArrayList<Node>();
         nodeList.add(newNodes);
         eval(this, newNodes, nodeList);
      }
      return nodeList;
   }

   public List<ArrayList<Node>> getLowestLevel () {

      List<ArrayList<Node>> nodeLists = constantEval(this);
      return nodeLists;

   }

   public List<ArrayList<Node>> constantEval (Node root) {

      if ( root.children == null || root.children.size() == 0 ) {
         List<ArrayList<Node>> nodesLists = new ArrayList<ArrayList<Node>>();
         ArrayList<Node> nodes = new ArrayList<Node>();
         nodes.add(root);
         nodesLists.add(nodes);
         if ( root.step != null
            && root.step.getDecompositionClass().isOptionalStep(root.stepName) ) {
            ArrayList<Node> nodesO = new ArrayList<Node>();
            nodesLists.add(nodesO);
         }

         return nodesLists;
      } else if ( root.typeOfChildren == CType.Required ) {

         List<ArrayList<Node>> nodesListsTemp = new ArrayList<ArrayList<Node>>();
         for (Node child : root.children) {
            List<ArrayList<Node>> list = constantEval(child);
            List<ArrayList<Node>> nodesLists = new ArrayList<ArrayList<Node>>();

            if ( nodesListsTemp.size() != 0 ) {
               for (ArrayList<Node> l : list) {
                  for (ArrayList<Node> li : nodesListsTemp) {
                     ArrayList<Node> temp = new ArrayList<Node>();
                     for (Node lii : li) {
                        temp.add(lii);
                     }
                     for (Node ll : l) {
                        temp.add(ll);
                        if ( root.step != null )
                           ll.required.addAll(root.required);
                     }
                     nodesLists.add(temp);
                  }
               }

               nodesListsTemp = nodesLists;
            } else {
               for (ArrayList<Node> l : list) {
                  nodesListsTemp.add(l);
                  for (Node ll : l) {
                     if ( root.step != null )
                        ll.required.addAll(root.required);
                  }
               }
            }

         }
         if ( root.step != null
            && root.step.getDecompositionClass().isOptionalStep(root.stepName) ) {
            ArrayList<Node> nodesO = new ArrayList<Node>();
            nodesListsTemp.add(nodesO);
         }

         return nodesListsTemp;
      } else if ( root.typeOfChildren == CType.Alter ) {

         List<ArrayList<Node>> nodesListsTemp = new ArrayList<ArrayList<Node>>();
         for (Node child : root.children) {
            List<ArrayList<Node>> list = constantEval(child);
            for (ArrayList<Node> l : list) {

               nodesListsTemp.add(l);
               for (Node ll : l) {
                  if ( root.step != null )
                     ll.required.addAll(root.required);
               }
            }
         }

         if ( root.step != null
            && root.step.getDecompositionClass().isOptionalStep(root.stepName) ) {
            ArrayList<Node> nodesO = new ArrayList<Node>();
            nodesListsTemp.add(nodesO);
         }

         return nodesListsTemp;
      }
      return null;
   }

   /**
    * Evaluate nodes value.
    */
   public Node eval (Node root, ArrayList<Node> nodes,
         List<ArrayList<Node>> nodeList) {
      if ( root.children.isEmpty() ) {
         return root;
      } else {
         if ( root.typeOfChildren == CType.Required ) {
            for (Node child : root.children) {
               nodes.add(child);
            }
         }
      }
      return null;
   }

   public Node getCopy () {
      Node n = new Node(this.id);
      n.step = this.step;
      n.stepName = this.stepName;
      n.parent = this.parent;
      n.typeOfChildren = this.typeOfChildren;
      n.typeOfNode = this.typeOfNode;
      return n;
   }

   /**
    * Give nodes without any empty node between two consecutive steps and with
    * empty node between two non-consecutive steps.
    */
   /*
    * public List<ArrayList<Node>> giveSeparateNodes () { ArrayList<Node>
    * newNodes = new ArrayList<Node>(); List<ArrayList<Node>> nodeList = new
    * ArrayList<ArrayList<Node>>(); nodeList.add(newNodes); Node ret =
    * giveSeparateNode(this, newNodes, nodeList); if ( ret != null ) {
    * newNodes.add(ret); } return nodeList; }
    */

   /**
    * Give nodes without any empty node between two consecutive steps and with
    * empty node between two non-consecutive steps.
    */
   /*
    * public Node giveSeparateNode (Node root, ArrayList<Node> nodes,
    * List<ArrayList<Node>> nodeList) { if ( root.children.isEmpty() ) { return
    * root; } else { if ( root.typeOfChildren == CType.Required ) { for (Node
    * child : root.children) { Node ret = giveSeparateNode(child, nodes,
    * nodeList); if ( ret != null ){ for(ArrayList<Node> li:nodeList){
    * li.add(ret); Node dumyNode = new Node(); dumyNode.typeOfNode =
    * NType.Empty; li.add(dumyNode); } } } } else if ( root.typeOfChildren ==
    * CType.Alter ) { int size = nodes.size(); for (int
    * ch=0;ch<root.children.size();ch++) { Node child = root.children.get(ch);
    * if(ch == 0){ Node ret = eval(child, nodes, nodeList); if ( ret != null ){
    * nodes.add(ret); Node dumyNode = new Node(); dumyNode.typeOfNode =
    * NType.Empty; nodes.add(dumyNode); } } else { ArrayList<Node> newNodes =
    * new ArrayList<Node>(); for(int s=0;s<size;s++){
    * newNodes.add(nodes.get(s).getCopy()); } nodeList.add(newNodes); Node ret =
    * giveSeparateNode(child, newNodes, nodeList); if ( ret != null ){
    * newNodes.add(ret); Node dumyNode = new Node(); dumyNode.typeOfNode =
    * NType.Empty; newNodes.add(dumyNode); } } } } } return null; }
    */
   /**
    * Checks if two nodes are equivalent. Checks one node's step with another.
    */
   public boolean isEquivalent (Node comp, TaskModel taskModel) {
      if ( this.equals(comp) ) {
         return true;
      }
      Step stepFake = this.step;
      Step stepReal = stepFake.getDecompositionClass().getStep(this.stepName);

      Step stepFakeD = comp.step;
      Step stepRealD = stepFakeD.getDecompositionClass().getStep(comp.stepName);

      if ( stepReal != null && stepRealD != null
         && stepReal.isEquivalentNoRequired(stepRealD, taskModel) ) {
         Map.Entry<String, Step> entry1 = new AbstractMap.SimpleEntry<String, Step>(
               this.stepName, this.step);
         Map.Entry<String, Step> entry2 = new AbstractMap.SimpleEntry<String, Step>(
               comp.stepName, stepRealD);
         if ( checkRequired(this, comp, taskModel) ) {
            if ( stepReal.getDecompositionClass().checkStepInputs(entry1,
                  stepReal.getDecompositionClass().getGoal(), entry2,
                  stepRealD.getDecompositionClass().getGoal(),
                  stepReal.getDecompositionClass(),
                  stepRealD.getDecompositionClass(), taskModel) )

               return true;
         }
      }
      return false;
   }

   /**
    * Adds the node to the tree and adds the step to the taskClass.
    */
   public Node addNode (Node node, CType required, TaskModel taskModel) {

      if ( node.step == null )
         return null;
      node.parent.children.add(node.parent.children.indexOf(node) + 1, this);
      this.parent = node.parent;
      // Adding to taskClass
      Step which = (node.parent.children
            .get(node.parent.children.indexOf(node))).step;
      String nameWhich = (node.parent.children.get(node.parent.children
            .indexOf(node))).stepName;

      String newName = this.step.getDecompositionClass().addStep(which,
            this.stepName, this.step, nameWhich, taskModel);

      this.step = which.getDecompositionClass().getStep(newName);
      this.stepName = newName;
      return this;
   }

   /**
    * Adds the optional step.
    */
   public Node addOptionalStep (Node node, CType required, TaskModel taskModel) {
      this.step.setMinOccurs(0);
      return addNode(node, required, taskModel);
   }

   /**
    * Prints the node's step's id.
    */
   public void printNode () {
      if ( this.stepName != null )
         System.out.println(this.stepName + " "
            + this.step.getDecompositionClass().getGoal().getId());

   }

   public Node getChild (String name) {
      for (int i = 0; i < this.children.size(); i++) {
         if ( this.children.get(i).stepName.equals(name) ) {
            return this.children.get(i);
         }
      }
      return null;
   }

   public void setRequired (List<String> req) {
      for (String r : req) {
         Node node = this.parent.getChild(r);
         if ( node != null ) {
            this.required.add(node.id);
         }
      }
   }

   public boolean checkRequired (Node node1, Node node2, TaskModel taskModel) {
      if ( (node1.required == null && node2.required == null)
         || (node1.required.size() == 0 && node2.required.size() == 0) ) {
         return true;
      } else if ( node1.required.size() == node2.required.size() ) {

         ArrayList<Integer> temp1 = new ArrayList<Integer>(node1.required);
         ArrayList<Integer> temp2 = new ArrayList<Integer>(node2.required);

         if ( temp1.size() == temp2.size() ) {
            for (int i = 0; i < temp1.size(); i++) {
               Node step1 = node1.parent.getChildById(temp1.get(i));
               int where = -1;
               boolean contain = false;
               for (int j = 0; j < temp2.size(); j++) {
                  Node step2 = node2.parent.getChildById(temp2.get(j));
                  if ( step1.isEquivalent(step2, taskModel) ) {
                     contain = true;
                     where = j;
                  }
               }
               if ( !contain )
                  return false;
               else
                  temp2.remove(where);
            }

            return true;
         }
         return false;

      } else {
         return false;
      }
   }

   public Node getChildById (int idd) {
      for (Node child : this.children) {
         if ( child.id == idd ) {
            return child;
         }
      }
      return null;
   }
}
