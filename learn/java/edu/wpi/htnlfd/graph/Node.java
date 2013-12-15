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

   public Step step;

   public String stepName;

   public Node value;

   public Node parent;

   public List<Node> children = new ArrayList<Node>();

   public CType typeOfChildren = CType.Required;

   public NType typeOfNode = NType.Required;

   public boolean visited;

   public Node (Node value, NType typeOfNode) {
      super();
      this.value = value;
      this.typeOfNode = typeOfNode;
   }

   public Node (Node value, Node parent, List<Node> children,
         CType typeOfChildren, NType typeOfNode) {
      super();
      this.value = value;
      this.parent = parent;
      this.children = children;
      this.typeOfChildren = typeOfChildren;
      this.typeOfNode = typeOfNode;
   }

   public Node () {

   }

   /**
    * Evaluate nodes value.
    */
   public List<ArrayList<Node>> evaluate () {
      
      List<ArrayList<Node>> nodeList = new ArrayList<ArrayList<Node>>();
      
      if(this.typeOfChildren == CType.Alter){
         for(Node child:this.children){
            ArrayList<Node> newNodes = new ArrayList<Node>();
            nodeList.add(newNodes);
            eval(child, newNodes, nodeList);
         }
      }
      else{
         ArrayList<Node> newNodes = new ArrayList<Node>();
         nodeList.add(newNodes);
         eval(this, newNodes, nodeList);
      }
      return nodeList;
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
               //Node ret = eval(child, nodes, nodeList);
               //if ( ret != null ){
                  nodes.add(child);
               //}
                  /*for(ArrayList<Node> li:nodeList){
                     li.add(ret);
                  }*/
               
            }
         } 
         /*else if ( root.typeOfChildren == CType.Alter ) {

            int size = nodes.size();
            
            for (int ch=0;ch<root.children.size();ch++) {
               Node child = root.children.get(ch);
               
               if(ch == 0){
                  Node ret = eval(child, nodes, nodeList);
                  if ( ret != null )
                     nodes.add(ret);
               }
               else {
                  ArrayList<Node> newNodes = new ArrayList<Node>();
                  for(int s=0;s<size;s++){
                     newNodes.add(nodes.get(s).getCopy());
                  }
                  
                  nodeList.add(newNodes);
                  Node ret = eval(child, newNodes, nodeList);
                  if ( ret != null )
                     newNodes.add(ret);
               }
            }
         }*/
      }
            return null;
   }
   
   public Node getCopy(){
      Node n = new Node();
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
   /*public List<ArrayList<Node>> giveSeparateNodes () {
      ArrayList<Node> newNodes = new ArrayList<Node>();
      List<ArrayList<Node>> nodeList = new ArrayList<ArrayList<Node>>();
      nodeList.add(newNodes);
      Node ret = giveSeparateNode(this, newNodes, nodeList);
      if ( ret != null ) {
         newNodes.add(ret);
      }
      return nodeList;
   }
*/
   
   /**
    * Give nodes without any empty node between two consecutive steps and with
    * empty node between two non-consecutive steps.
    */
   /*public Node giveSeparateNode (Node root, ArrayList<Node> nodes,
         List<ArrayList<Node>> nodeList) {
      if ( root.children.isEmpty() ) {
         return root;
      } else {
         if ( root.typeOfChildren == CType.Required ) {
            for (Node child : root.children) {
               Node ret = giveSeparateNode(child, nodes, nodeList);
               if ( ret != null ){
                  for(ArrayList<Node> li:nodeList){
                     
                     li.add(ret);
                     Node dumyNode = new Node();
                     dumyNode.typeOfNode = NType.Empty;
                     li.add(dumyNode);
                  }
               }
            }
         } else if ( root.typeOfChildren == CType.Alter ) {

            int size = nodes.size();
            
            for (int ch=0;ch<root.children.size();ch++) {
               Node child = root.children.get(ch);
               
               if(ch == 0){
                  Node ret = eval(child, nodes, nodeList);
                  if ( ret != null ){
                     nodes.add(ret);
                     Node dumyNode = new Node();
                     dumyNode.typeOfNode = NType.Empty;
                     nodes.add(dumyNode);
                  }
               }
               else {
                  ArrayList<Node> newNodes = new ArrayList<Node>();
                  for(int s=0;s<size;s++){
                     newNodes.add(nodes.get(s).getCopy());
                  }
                  
                  nodeList.add(newNodes);
                  Node ret = giveSeparateNode(child, newNodes, nodeList);
                  if ( ret != null ){
                     newNodes.add(ret);
                     Node dumyNode = new Node();
                     dumyNode.typeOfNode = NType.Empty;
                     newNodes.add(dumyNode);
                  }
               }
            }
         }
      }
      return null;
   }
*/
   /**
    * Checks if two nodes are equivalent. Checks one node's step with another.
    */
   public boolean isEquivalent (Node comp, TaskModel taskModel) {
      if ( this.equals(comp) ) {
         return true;
      }
      if ( this.step != null && comp.step != null
         && this.step.isEquivalent(comp.step, taskModel) ) {
         Map.Entry<String, Step> entry1 = new AbstractMap.SimpleEntry<String, Step>(
               this.stepName, this.step);
         Map.Entry<String, Step> entry2 = new AbstractMap.SimpleEntry<String, Step>(
               comp.stepName, comp.step);
         if ( this.step.getDecompositionClass().checkStepInputs(entry1,
               this.step.getDecompositionClass().getGoal(), entry2,
               comp.step.getDecompositionClass().getGoal(),
               this.step.getDecompositionClass(),
               comp.step.getDecompositionClass(), taskModel) )
            return true;
      }
      return false;
   }

   /**
    * Adds the node to the tree and adds the step to the taskClass.
    */
   public Node addNode (Node node, CType required, TaskModel taskModel) {

      node.parent.children.add(node.parent.children.indexOf(node) + 1, this);
      this.parent = node.parent;
      // Adding to taskClass
      Step which = (node.parent.children
            .get(node.parent.children.indexOf(node))).step;
      String nameWhich = (node.parent.children.get(node.parent.children
            .indexOf(node))).stepName;

      String newName = this.step.getDecompositionClass().addStep(which,
            this.stepName, this.step, nameWhich, taskModel);

      this.step = this.step.getDecompositionClass().getStep(newName);
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
}
