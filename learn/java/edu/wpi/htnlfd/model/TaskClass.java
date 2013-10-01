package edu.wpi.htnlfd.model;

import java.util.*;
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


   abstract class Slot {

      private String type;

      private String name;
      

      protected Slot (String name,String type) {
         super();
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
         super(inputName,primitiveType);
         this.setName(inputName);
         this.setType(primitiveType);
         this.setModified(modified);
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

   }

   public class Output extends Slot {

      public Output (String outputName, String slotType) {
         super(outputName, slotType);
         this.setName(outputName);
         this.setType(slotType);
      }

   }

   public List<Input> getDeclaredInputs () {
      if ( this.declaredInputs == null )
         this.declaredInputs = new ArrayList<Input>();
      return declaredInputs;
   }

   public void setDeclaredInputs (List<Input> declaredInputs) {
      this.declaredInputs = declaredInputs;
   }

   public List<Output> getDeclaredOutputs () {
      if ( this.declaredOutputs == null )
         this.declaredOutputs = new ArrayList<Output>();
      return declaredOutputs;
   }

   public void setDeclaredOutputs (List<Output> declaredOutputs) {
      this.declaredOutputs = declaredOutputs;
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

}