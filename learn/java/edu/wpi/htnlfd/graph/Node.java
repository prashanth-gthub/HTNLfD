package edu.wpi.htnlfd.graph;

import edu.wpi.htnlfd.model.DecompositionClass.Binding;
import edu.wpi.htnlfd.model.DecompositionClass.Step;
import edu.wpi.htnlfd.model.*;
import edu.wpi.htnlfd.model.TaskClass.*;
import java.util.*;
import java.util.Map.Entry;

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

   public List<ArrayList<Node>> evaluate () {
      ArrayList<Node> newNodes = new ArrayList<Node>();
      List<ArrayList<Node>> nodeList = new ArrayList<ArrayList<Node>>();
      nodeList.add(newNodes);
      eval(this, newNodes, nodeList);
      return nodeList;
   }

   public Node eval (Node root, ArrayList<Node> nodes,
         List<ArrayList<Node>> nodeList) {
      if ( root.children.isEmpty() ) {
         return root;
      } else {
         if ( typeOfChildren == CType.Required ) {
            for (Node child : root.children) {
               Node ret = eval(child, nodes, nodeList);
               if ( ret != null )
                  nodes.add(ret);
            }
         } else if ( typeOfChildren == CType.Alter ) {

            for (Node child : root.children) {
               ArrayList<Node> newNodes = new ArrayList<Node>();
               nodeList.add(newNodes);
               Node ret = eval(child, newNodes, nodeList);
               if ( ret != null )
                  newNodes.add(ret);
            }
         }
      }
      return null;
   }

   public List<ArrayList<Node>> giveSeparateNodes () {
      ArrayList<Node> newNodes = new ArrayList<Node>();
      List<ArrayList<Node>> nodeList = new ArrayList<ArrayList<Node>>();
      nodeList.add(newNodes);
      Node ret = giveSeparateNode(this, newNodes, nodeList);
      if ( ret != null ) {
         newNodes.add(ret);
      }
      return nodeList;
   }

   public Node giveSeparateNode (Node root, ArrayList<Node> nodes,
         List<ArrayList<Node>> nodeList) {
      if ( root.children.isEmpty() ) {
         return root;
      } else {
         if ( typeOfChildren == CType.Required ) {
            for (Node child : root.children) {
               Node ret = eval(child, nodes, nodeList);
               if ( ret != null ) {
                  nodes.add(ret);
                  Node dumyNode = new Node();
                  dumyNode.typeOfNode = NType.Empty;
                  nodes.add(dumyNode);
               }
            }

         } else if ( typeOfChildren == CType.Alter ) {

            for (Node child : root.children) {
               ArrayList<Node> newNodes = new ArrayList<Node>();
               nodeList.add(newNodes);
               Node ret = eval(child, newNodes, nodeList);
               if ( ret != null )
                  newNodes.add(ret);
            }

         }
      }
      return null;
   }

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

   public Node addNode (Node node, CType required, TaskModel taskModel) {

      node.parent.children.add(node.parent.children.indexOf(node) + 1, this);
      this.parent = node.parent;
      // Adding to taskClass
      Step which = (node.parent.children
            .get(node.parent.children.indexOf(node))).step;
      String nameWhich = (node.parent.children.get(node.parent.children
            .indexOf(node))).stepName;
      DecompositionClass.Step stp = which.getDecompositionClass().new Step(
            this.step.getType(), this.step.getMinOccurs(),
            this.step.getMaxOccurs(), null);
      String newName = which.findStepName(this.stepName);
      DecompositionClass currentDec = which.getDecompositionClass();
      DecompositionClass prevDec = this.step.getDecompositionClass();
      TaskClass prevTask = prevDec.getGoal();
      TaskClass currentTask = currentDec.getGoal();

      currentDec.addStep(newName, stp, nameWhich);

      for (Entry<String, Binding> bind : prevDec.getBindings().entrySet()) {
         if ( bind.getValue().getStep().equals(this.stepName) ) {
            Entry<String, Binding> bind2 = prevDec.getBinding(bind, prevDec);
            String value = bind2.getValue().getValue();

            TaskClass.Input inputT = null;

            for (Input in : prevTask.getDeclaredInputs()) {
               if ( in.getName()
                     .equals(bind.getValue().getValue().substring(6)) ) {
                  inputT = in;
                  break;
               }
            }

            int adding = currentTask.getDeclaredInputs().size();

            String modified = (inputT.getModified() == null) ? null : inputT
                  .getModified().getName();

            String inName = currentTask.addInput(taskModel, currentTask,
                  inputT.getName(), inputT.getType(), modified, value,
                  currentDec, newName);

            boolean add = (currentTask.getDeclaredInputs().size() != adding);
            if ( !add ) {
               currentDec.addBinding("$" + newName + "."
                  + bind.getValue().getSlot(), currentDec.new Binding(bind
                     .getValue().getSlot(), newName, "$this." + inName,
                     DecompositionClass.Type.InputInput));

               if ( modified != null ) {
                  String outName = null;
                  outName = currentTask.getInput(inputT.getName())
                        .getModified().getName();

                  String outputTaskName = prevDec.getBindings()
                        .get("$this" + modified).getValue()
                        .substring(2 + this.stepName.length());

                  currentDec.addBinding("$this." + outName,
                        currentDec.new Binding(outName, "this", "$" + newName
                           + "." + outputTaskName,
                              DecompositionClass.Type.OutputOutput));
               }

            } else {
               currentDec.addBinding("$" + newName + "."
                  + bind.getValue().getSlot(), currentDec.new Binding(bind
                     .getValue().getSlot(), newName, "$this." + inName,
                     DecompositionClass.Type.InputInput));
               currentDec.addBinding("$this." + inName, currentDec.new Binding(
                     inName, "this", value, DecompositionClass.Type.Constant));

               if ( modified != null ) {
                  String outName = null;
                  outName = modified;

                  String outputTaskName = prevDec.getBindings()
                        .get("$this." + modified).getValue()
                        .substring(2 + this.stepName.length());
                  TaskClass.Output output = currentTask.new Output(
                        currentTask.findOutputName(newName, outputTaskName),
                        prevTask.getOutput(outName).getType());
                  currentTask.addOutput(output);
                  currentTask.getInput(inName).setModified(output);

                  currentDec.addBinding("$this." + output.getName(),
                        currentDec.new Binding(output.getName(), "this", "$"
                           + newName + "." + outputTaskName,
                              DecompositionClass.Type.OutputOutput));
               }

            }

         }
      }

      // Add Ordering
      currentDec.addOrdering(taskModel);

      this.step = stp;
      this.stepName = newName;
      return this;
   }

   public Node addOptionalStep (Node node, CType required, TaskModel taskModel) {
      this.step.setMinOccurs(0);
      return addNode(node, required, taskModel);
   }

   public void printNode () {
      if ( this.stepName != null )
         System.out.println(this.stepName + " "
            + this.step.getDecompositionClass().getGoal().getId());

   }
}
