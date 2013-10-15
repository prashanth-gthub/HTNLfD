package edu.wpi.htnlfd.model;

import edu.wpi.htnlfd.DomManipulation;
import edu.wpi.htnlfd.model.DecompositionClass.*;
import org.w3c.dom.*;
import java.util.*;
import java.util.Map.Entry;
import javax.xml.namespace.QName;

// TODO: Auto-generated Javadoc
/**
 * The Class TaskClass.
 */
public class TaskClass extends TaskModel.Member {

   /**
    * Instantiates a new task class.
    *
    * @param taskModel the task model
    * @param id the id
    * @param qname the qname
    */
   public TaskClass (TaskModel taskModel, String id, QName qname) {
      taskModel.super(id, qname);
   }

   /**
    * Instantiates a new task class.
    *
    * @param taskModel the task model
    * @param id the id
    */
   public TaskClass (TaskModel taskModel, String id) {
      taskModel.super(id, null);
      this.setId(id);
   }

   /**
    * Instantiates a new task class.
    *
    * @param taskModel the task model
    * @param oldTaskClass the old task class
    */
   public TaskClass (TaskModel taskModel, TaskClass oldTaskClass) {
      taskModel.super(oldTaskClass.getId(), oldTaskClass.getQname());
      this.declaredOutputs = new ArrayList<Output>(
            oldTaskClass.declaredOutputs.size());
      this.declaredInputs = new ArrayList<Input>(
            oldTaskClass.declaredInputs.size());

      for (Output out : oldTaskClass.declaredOutputs) {
         Output output = new Output(out);
         declaredOutputs.add(output);
         for (Input in : oldTaskClass.declaredInputs) {
            if (in.getModified()!=null && in.getModified().equals(out) ) {
               declaredInputs.add(new Input(in, output));
            }
         }
      }

      for (Input in : oldTaskClass.declaredInputs) {
         if ( in.getModified() == null )
            declaredInputs.add(new Input(in, null));
      }

      this.primitive = oldTaskClass.isPrimitive();

      this.decompositions = new ArrayList<DecompositionClass>(oldTaskClass
            .getDecompositions().size());
      for (DecompositionClass oldDecomposition : oldTaskClass
            .getDecompositions()) {
         this.decompositions.add(new DecompositionClass(taskModel,
               oldDecomposition, this));
      }

   }

   /** The primitive. */
   private boolean primitive;

   /**
    * Checks if is primitive.
    *
    * @return true, if is primitive
    */
   public boolean isPrimitive () {
      return primitive;
   }

   /**
    * Sets the primitive.
    *
    * @param primitive the new primitive
    */
   public void setPrimitive (boolean primitive) {
      this.primitive = primitive;
   }

   /** The declared inputs. */
   private List<Input> declaredInputs;

   /** The declared outputs. */
   private List<Output> declaredOutputs;

   /**
    * Checks for input.
    *
    * @param inputName the input name
    * @return true, if successful
    */
   public boolean hasInput (String inputName) {
      for (Input input : this.getDeclaredInputs()) {
         if ( input.getName().equals(inputName) ) {
            return true;
         }
      }
      return false;
   }

   /**
    * Adds the input.
    *
    *This function checks the input value of a task; if it is the same as the input of demonstrated task, it will return the name of the 
      existed input. If it can't find it, it will add the input to the TaskClass.
    *
    * @param task the task
    * @param inputName the input name
    * @param inputType the input type
    * @param modified the modified
    * @param inputBindingValue the input binding value
    * @param subtask the subtask
    * @param prefix the prefix
    * @return the string
    */
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

   /**
    * The Class Slot.
    */
   abstract class Slot {

      /** The type. */
      private String type;

      /** The name. */
      private String name;

      /**
       * Instantiates a new slot.
       *
       * @param name the name
       * @param type the type
       */
      protected Slot (String name, String type) {

         this.type = type;
         this.name = name;
      }

      /**
       * Instantiates a new slot.
       */
      public Slot () {
      }

      /**
       * Gets the task.
       *
       * @return the task
       */
      public TaskClass getTask () {
         return TaskClass.this;
      }

      /**
       * Gets the type.
       *
       * @return the type
       */
      public String getType () {
         return type;
      }

      /**
       * Sets the type.
       *
       * @param type the new type
       */
      public void setType (String type) {
         this.type = type;
      }

      /**
       * Gets the name.
       *
       * @return the name
       */
      public String getName () {
         return name;
      }

      /**
       * Sets the name.
       *
       * @param name the new name
       */
      public void setName (String name) {
         this.name = name;
      }

   }

   /**
    * The Class Input.
    */
   public class Input extends Slot {

      /** The modified. */
      private Output modified;

      /**
       * Instantiates a new input.
       *
       * @param inputName the input name
       * @param primitiveType the primitive type
       * @param modifies the modifies
       */
      public Input (String inputName, String primitiveType, Output modifies) {
         super(inputName, primitiveType);
         this.setName(inputName);
         this.setType(primitiveType);
         this.setModified(modifies);
      }

      /**
       * Instantiates a new input.
       */
      public Input () {
         super();
      }

      /**
       * Instantiates a new input.
       *
       * @param oldIn the old in
       * @param modified the modified
       */
      public Input (Input oldIn, Output modified) {
         super(oldIn.getName(), oldIn.getType());
         this.modified = modified;
      }

      /**
       * Gets the modified.
       *
       * @return the modified
       */
      public Output getModified () {
         return modified;
      }

      /**
       * Sets the modified.
       *
       * @param modifies the new modified
       */
      public void setModified (Output modifies) {
         this.modified = modifies;
      }

      /**
       * To node.
       *
       *This function makes the input's DOM element.
       *
       * @param document the document
       * @return the node
       */
      public Node toNode (Document document) {
         Element inputTask = document.createElementNS(
               DomManipulation.xmlnsValue, "input");

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
      
      /**
       * Checks if is equivalent.
       *
       *Checks whether two inputs are equivalent by considering 
       *their type and if they have modified output
       *
       * @param in the in
       * @return true, if is equivalent
       */
      public boolean isEquivalent (Input in){
         if(this.getType().equals(in.getType()) && (this.getModified()!=null && in.getModified()!=null
               && this.getModified().getType().equals(in.getModified().getType())) || (this.getModified()==null && in.getModified()==null
                     ) ){
            return true;
         }
         return false;
      }

   }

   /**
    * The Class Output.
    */
   public class Output extends Slot {

      /**
       * Instantiates a new output.
       *
       * @param outputName the output name
       * @param slotType the slot type
       */
      public Output (String outputName, String slotType) {
         super(outputName, slotType);
         this.setName(outputName);
         this.setType(slotType);
      }

      /**
       * Instantiates a new output.
       *
       * @param oldOut the old out
       */
      public Output (Output oldOut) {
         super(oldOut.getName(), oldOut.getType());
      }

      /**
       * To node.
       *
       *This function makes the output's DOM element.
       *
       * @param document the document
       * @return the node
       */
      public Node toNode (Document document) {
         Element outputTask = document.createElementNS(
               DomManipulation.xmlnsValue, "output");

         Attr inputNameAttr = document.createAttribute("name");
         inputNameAttr.setValue(this.getName());
         outputTask.setAttributeNode(inputNameAttr);
         Attr inputType = document.createAttribute("type");
         inputType.setValue(this.getType());
         outputTask.setAttributeNode(inputType);
         return outputTask;
      }
      
      /**
       * Checks if is equivalent.
       *
       *Checks whether two outputs are equivalent by considering their type
       *
       * @param out the out
       * @return true, if is equivalent
       */
      public boolean isEquivalent (Output out){
         if(this.getType().equals(out.getType())){
            return true;
         }
         return false;
      }
      

   }

   /**
    * Gets the declared inputs.
    *
    * @return the declared inputs
    */
   public List<Input> getDeclaredInputs () {
      if ( this.declaredInputs == null )
         this.declaredInputs = new ArrayList<Input>();
      return declaredInputs;
   }

   /**
    * Sets the declared inputs.
    *
    * @param declaredInputs the new declared inputs
    */
   public void setDeclaredInputs (List<Input> declaredInputs) {
      this.declaredInputs = declaredInputs;
   }

   /**
    * Gets the declared outputs.
    *
    * @return the declared outputs
    */
   public List<Output> getDeclaredOutputs () {
      if ( this.declaredOutputs == null )
         this.declaredOutputs = new ArrayList<Output>();
      return declaredOutputs;
   }

   /**
    * Adds the input.
    *
    * @param inputC the input c
    */
   public void addInput (Input inputC) {
      if ( this.declaredInputs == null )
         this.declaredInputs = new ArrayList<Input>();
      declaredInputs.add(inputC);

   }

   /**
    * Adds the output.
    *
    * @param outputTask the output task
    */
   public void addOutput (Output outputTask) {
      if ( this.declaredOutputs == null )
         this.declaredOutputs = new ArrayList<Output>();
      this.declaredOutputs.add(outputTask);

   }

   /**
    * Removes the output.
    *
    * @param outputTask the output task
    */
   public void removeOutput (Output outputTask) {
      if ( this.declaredOutputs != null )
         this.declaredOutputs.remove(outputTask);

   }

   /** The decompositions. */
   private List<DecompositionClass> decompositions = new ArrayList<DecompositionClass>();

   /**
    * Gets the decompositions.
    *
    * @return the decompositions
    */
   public List<DecompositionClass> getDecompositions () {
      return decompositions;
   }

   /**
    * Adds the decomposition class.
    *
    * @param dec the dec
    */
   public void addDecompositionClass (DecompositionClass dec) {
      decompositions.add(dec);
   }

   /**
    * Gets the decomposition.
    *
    * @param id the id
    * @return the decomposition
    */
   public DecompositionClass getDecomposition (String id) {
      for (DecompositionClass decomp : getDecompositions())
         if ( decomp.getId().equals(id) )
            return decomp;
      return null;
   }

   /**
    * Checks if is equivalent.
    *
    *This function checks for equivalent DecompositionClass classes recursively.
    *
    * @param next the next
    * @param taskModel the task model
    * @return true, if is equivalent
    */
   public boolean isEquivalent (TaskClass next, TaskModel taskModel) {
      
      TaskClass fakeTask = new TaskClass(taskModel, next);
      
      if(this.getDeclaredInputs().size() == fakeTask.getDeclaredInputs().size()){
         for(Input thisIn : this.getDeclaredInputs()){
            boolean contain = false;
            Iterator<Input> iterate= fakeTask.getDeclaredInputs().iterator();
            while(iterate.hasNext()){
               Input input = iterate.next();
               if(thisIn.isEquivalent(input)){
                  contain = true;
                  fakeTask.getDeclaredInputs().remove(input);
                  break;
               }
            }
            if(!contain){
               return false;
            }
         }
      }
      
      if(this.getDeclaredOutputs().size() == next.getDeclaredOutputs().size()){
         for(Output thisOut : this.getDeclaredOutputs()){
            boolean contain = false;
            Iterator<Output> iterate= fakeTask.getDeclaredOutputs().iterator();
            while(iterate.hasNext()){
               Output output = iterate.next();
               if(thisOut.isEquivalent(output)){
                  contain = true;
                  fakeTask.getDeclaredOutputs().remove(output);
                  break;
               }
            }
            if(!contain){
               return false;
            }
         }
      }
      
      DecompositionClass temp = null;
      for (DecompositionClass dec1 : this.getDecompositions()) {
         boolean contain = false;
         for (DecompositionClass dec2 : next.getDecompositions()) {
            if ( dec1.isEquivalent(dec2, taskModel) ) {
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

   /**
    * To node.
    *
    *This function makes the TaskClass's DOM element recursively.
    *
    * @param document the document
    * @param namespaces the namespaces
    * @return the node
    */
   public Node toNode (Document document, Set<String> namespaces) {

      Element taskElement = document.createElementNS(
            DomManipulation.xmlnsValue, "task");

      Attr idTask = document.createAttribute("id");
      idTask.setValue(this.getId());
      taskElement.setAttributeNode(idTask);

      for (TaskClass.Input input : this.getDeclaredInputs()) {

         taskElement.appendChild(input.toNode(document));

      }

      for (TaskClass.Output output : this.getDeclaredOutputs()) {
         taskElement.appendChild(output.toNode(document));
      }

      for (DecompositionClass subtask : this.getDecompositions()) {

         taskElement.appendChild(subtask.toNode(document, namespaces));

      }
      return taskElement;
   }

   /**
    * Checks if is modified.
    *
    * @param inputName the input name
    * @return true, if is modified
    */
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