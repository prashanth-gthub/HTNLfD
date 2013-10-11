package edu.wpi.htnlfd.model;

import edu.wpi.htnlfd.model.DecompositionClass.*;
import org.w3c.dom.*;
import java.util.*;
import java.util.Map.Entry;
import javax.xml.namespace.QName;

public class TaskClass extends TaskModel.Member {

   public TaskClass (TaskModel taskModel, String id, QName qname) {
      taskModel.super(id, qname);
   }

   public TaskClass (TaskModel taskModel, String id) {
      taskModel.super(id, null);
      this.setId(id);
   }

   private List<Input> declaredInputs;

   private List<Output> declaredOutputs;

   public boolean hasInput(String inputName){
      for (Input input : this.getDeclaredInputs()) {
         if ( input.getName().equals(inputName) ) {
            return true;
         }
      }
      return false;
   }
   public String addInput (TaskClass task, String inputName, String inputType,
         String modified, String inputBindingValue, DecompositionClass subtask,
         String prefix) {
      boolean contain = false;
      String name = null;
      for (Entry<String, Binding> bind : subtask.getBindings().entrySet()) {
         /*
          * ((inputName.contains(ReferenceFrame) && bind.getKey().contains(
          * ReferenceFrame)) || (!inputName.contains(ReferenceFrame) && !bind
          * .getKey().contains(ReferenceFrame)))
          */

         if ( bind.getKey().contains("this")
            && bind.getValue().isInputInput()
            && !((modified != null) ^ task
                  .isModified(bind.getValue().getSlot())) ) {
            if ( bind.getValue().getValue().equals(inputBindingValue) ) {
               contain = true;
               name = bind.getKey().substring(6);
               break;
            }
         }
      }
      if ( contain ) {
         return name;
      } else {
         if ( name == null )
            name = prefix + "_" + inputName;
         String pName = prefix + name.substring(name.indexOf("_"));

         TaskClass.Input inputC = task.new Input(pName, inputType, null);
         task.addInput(inputC);

         return pName;
      }
   }

   abstract class Slot {

      private String type;

      private String name;

      protected Slot (String name, String type) {

         this.type = type;
         this.name = name;
      }

      public Slot () {
      }

      public TaskClass getTask () {
         return TaskClass.this;
      }

      public String getType () {
         return type;
      }

      public void setType (String type) {
         this.type = type;
      }

      public String getName () {
         return name;
      }

      public void setName (String name) {
         this.name = name;
      }

   }

   public class Input extends Slot {

      private Output modified;

      public Input (String inputName, String primitiveType, Output modifies) {
         super(inputName, primitiveType);
         this.setName(inputName);
         this.setType(primitiveType);
         this.setModified(modifies);
      }

      public Input () {
         super();
      }

      public Output getModified () {
         return modified;
      }

      public void setModified (Output modifies) {
         this.modified = modifies;
      }

      public Node toNode (Document document, String xmlnsValue) {
         Element inputTask = document.createElementNS(xmlnsValue, "input");

         Attr inputNameAttr = document.createAttribute("name");
         inputNameAttr.setValue(this.getName());
         inputTask.setAttributeNode(inputNameAttr);

         Attr inputType = document.createAttribute("type");
         inputType.setValue(this.getType());
         inputTask.setAttributeNode(inputType);

         if ( this.getModified() != null ) {
            Attr modifiedAttr = document.createAttribute("modified");
            modifiedAttr.setValue(this.getModified().getName());
            inputTask.setAttributeNode(modifiedAttr);
         }
         return inputTask;
      }

   }

   public class Output extends Slot {

      public Output (String outputName, String slotType) {
         super(outputName, slotType);
         this.setName(outputName);
         this.setType(slotType);
      }

      public Node toNode (Document document, String xmlnsValue) {
         Element outputTask = document.createElementNS(xmlnsValue, "output");

         Attr inputNameAttr = document.createAttribute("name");
         inputNameAttr.setValue(this.getName());
         outputTask.setAttributeNode(inputNameAttr);
         Attr inputType = document.createAttribute("type");
         inputType.setValue(this.getType());
         outputTask.setAttributeNode(inputType);
         return outputTask;
      }

   }

   public List<Input> getDeclaredInputs () {
      if ( this.declaredInputs == null )
         this.declaredInputs = new ArrayList<Input>();
      return declaredInputs;
   }

   public List<Output> getDeclaredOutputs () {
      if ( this.declaredOutputs == null )
         this.declaredOutputs = new ArrayList<Output>();
      return declaredOutputs;
   }

   public void addInput (Input inputC) {
      if ( this.declaredInputs == null )
         this.declaredInputs = new ArrayList<Input>();
      declaredInputs.add(inputC);

   }

   public void addOutput (Output outputTask) {
      if ( this.declaredOutputs == null )
         this.declaredOutputs = new ArrayList<Output>();
      this.declaredOutputs.add(outputTask);

   }

   public void removeOutput (Output outputTask) {
      if ( this.declaredOutputs != null )
         this.declaredOutputs.remove(outputTask);

   }

   private List<DecompositionClass> decompositions = new ArrayList<DecompositionClass>();

   public List<DecompositionClass> getDecompositions () {
      return decompositions;
   }

   public void addDecompositionClass (DecompositionClass dec) {
      decompositions.add(dec);
   }

   public DecompositionClass getDecomposition (String id) {
      for (DecompositionClass decomp : getDecompositions())
         if ( decomp.getId().equals(id) )
            return decomp;
      return null;
   }

   public boolean isEquivalent (TaskClass next) {
      DecompositionClass temp = null;
      for (DecompositionClass dec1 : this.getDecompositions()) {
         boolean contain = false;
         for (DecompositionClass dec2 : next.getDecompositions()) {
            if ( dec1.isEquivalent(dec2) ) {
               contain = true;
               temp = dec2;
               break;
            }
         }
         if ( !contain )
            return false;
      }
      next.getDecompositions().remove(temp);
      return true;
   }

   public Node toNode (Document document, String xmlnsValue,
         String namespacePrefix, Set<String> namespaces) {

      Element taskElement = document.createElementNS(xmlnsValue, "task");

      Attr idTask = document.createAttribute("id");
      idTask.setValue(this.getId());
      taskElement.setAttributeNode(idTask);

      for (TaskClass.Input input : this.getDeclaredInputs()) {

         taskElement.appendChild(input.toNode(document, xmlnsValue));

      }

      for (TaskClass.Output output : this.getDeclaredOutputs()) {
         taskElement.appendChild(output.toNode(document, xmlnsValue));
      }

      for (DecompositionClass subtask : this.getDecompositions()) {

         taskElement.appendChild(subtask.toNode(document, xmlnsValue,
               namespacePrefix, namespaces));

      }
      return taskElement;
   }

   public boolean isModified (String inputName) {
      for (Input in : declaredInputs) {
         if ( in.getName().equals(inputName) ) {
            if ( in.getModified() != null )
               return true;

         }
      }
      return false;
   }
 

}