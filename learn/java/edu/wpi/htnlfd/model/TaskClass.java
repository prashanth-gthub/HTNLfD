package edu.wpi.htnlfd.model;


import edu.wpi.disco.Disco;
import edu.wpi.htnlfd.ApplicationSpecificClass;
import org.w3c.dom.*;
import java.util.*;
import javax.script.*;
import javax.xml.namespace.QName;

public class TaskClass extends TaskModel.Member {

   /**
    * Instantiates a new task class.
    */
   public TaskClass (TaskModel taskModel, String id, QName qname) {
      taskModel.super(id, qname);

   }

   /**
    * Instantiates a new task class.
    */
   public TaskClass (TaskModel taskModel, String id) {
      taskModel.super(id, null);
      this.setId(id);
   }

   /**
    * Copy Constructor for TaskClass.
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
            if ( in.getModified() != null && in.getModified().equals(out) ) {
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

   private boolean primitive;

   private String precondition;

   private String postcondition;

   private boolean sufficient = false;

   public void setSufficient (boolean sufficient) {
      this.sufficient = sufficient;
   }

   /**
    * Returns true iff postcondition is provided and is sufficient.
    */
   public boolean isSufficient () {
      return sufficient;
   }

   public String getPrecondition () {
      return precondition;
   }

   public void setPrecondition (String precondition) {
      this.precondition = precondition;
   }

   public String getPostcondition () {
      return postcondition;
   }

   public void setPostcondition (String postcondition) {
      this.postcondition = postcondition;
   }

   /**
    * Checks if is primitive.
    */
   public boolean isPrimitive () {
      return primitive;
   }

   /**
    * Sets the primitive.
    */
   public void setPrimitive (boolean primitive) {
      this.primitive = primitive;
   }

   private List<Input> declaredInputs;

   private List<Output> declaredOutputs;

   /**
    * Checks for input.
    */
   public boolean hasInput (String inputName) {
      for (Input input : this.getDeclaredInputs()) {
         if ( input.getName().equals(inputName) ) {
            return true;
         }
      }
      return false;
   }
   
   public Input getInput(String id){
      for (Input input : this.getDeclaredInputs()) {
         if ( input.getName().equals(id) ) {
            return input;
         }
      }
      return null;
   }
   

   /**
    * Adds the input. This function checks the input value of a task; if it is
    * the same as the input of demonstrated task, it will return the name of the
    * existed input. If it can't find it, it will add the input to the
    * TaskClass.
    * 
    * @param taskModel
    */
   public String addInput (TaskModel taskModel, TaskClass task,
         String inputName, String inputType, String modified,
         Object inputBindingValue, DecompositionClass subtask, String prefix) {
      boolean contain = false;
      String name = null;
      for (Input input : task.getDeclaredInputs()) {
         if ( !((modified != null) ^ input.getModified() != null)
            && input.getType().equals(inputType) ) {

            String value = subtask.findValueInParents(taskModel, null, task,
                  subtask, input.getName());
            if ( value.equals(inputBindingValue) ) {
               return input.getName();
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

   public abstract class Slot {

      private String type;

      private String name;

      /**
       * Instantiates a new slot.
       */
      protected Slot (String name, String type) {

         this.type = type;
         this.name = name;
      }

      /**
       * Gets the task.
       */
      public TaskClass getTask () {
         return TaskClass.this;
      }

      /**
       * Gets the type of this slot that this slot is an input or output to.
       */
      public String getType () {
         return type;
      }

      /**
       * Sets the type of this slot.
       */
      public void setType (String type) {
         this.type = type;
      }

      /**
       * Gets the name.
       */
      public String getName () {
         return name;
      }

      /**
       * Sets the name of this slot.
       */
      public void setName (String name) {
         this.name = name;
      }

      protected Element toNode (Document document, String slot) {
         Element element = document.createElementNS(TaskModel.xmlnsValue, slot);
         if ( name == null || type == null )

            throw new IllegalArgumentException("Name or type of slot is null: "
               + this);
         TaskModel.addAttribute(document, element, "name", name);
         TaskModel.addAttribute(document, element, "type", type);
         return element;
      }

   }

   public class Input extends Slot {

      private Output modified;
      private boolean optional = false;

      /**
       * Instantiates a new input.
       */
      public Input (String inputName, String primitiveType, Output modifies) {
         super(inputName, primitiveType);
         this.setName(inputName);
         this.setType(primitiveType);
         this.setModified(modifies);
      }

      /**
       * Copy constructor for inputs.
       */
      public Input (Input oldIn, Output modified) {
         super(oldIn.getName(), oldIn.getType());
         this.modified = modified;
      }

      
      public boolean isOptional () {
         return optional;
      }

      public void setOptional (boolean optional) {
         this.optional = optional;
      }

      /**
       * Gets the modified.
       */
      public Output getModified () {
         return modified;
      }

      /**
       * Sets the modified.
       */
      public void setModified (Output modifies) {
         this.modified = modifies;
      }

      /**
       * This function makes the input's DOM element.
       */
      public Element toNode (Document document,Properties properties) {
         Element element = toNode(document, "input");
         if ( modified != null )
            TaskModel.addAttribute(document, element, "modified",
                  modified.getName());
         if(optional)
            properties.put(getId()+"."+this.getName()+"@optional", "true");
         return element;
      }

      /**
       * Checks if is equivalent. Checks whether two inputs are equivalent by
       * considering their type and if they have modified output
       */
      public boolean isEquivalent (Input in) {
         if ( this.getType().equals(in.getType())
            && (this.getModified() != null && in.getModified() != null && this
                  .getModified().getType().equals(in.getModified().getType()))
            || (this.getModified() == null && in.getModified() == null) ) {
            return true;
         }
         return false;
      }

   }

   public class Output extends Slot {

      /**
       * Instantiates a new output.
       */
      public Output (String outputName, String slotType) {
         super(outputName, slotType);
         this.setName(outputName);
         this.setType(slotType);
      }

      /**
       * Copy constructor for outputs
       * 
       * @param oldOut the old out
       */
      public Output (Output oldOut) {
         super(oldOut.getName(), oldOut.getType());
      }

      /**
       * Checks if is equivalent. Checks whether two outputs are equivalent by
       * considering their type
       */
      public boolean isEquivalent (Output out) {
         if ( this.getType().equals(out.getType()) ) {
            return true;
         }
         return false;
      }

   }

   /**
    * Gets the declared inputs.
    */
   public List<Input> getDeclaredInputs () {
      if ( this.declaredInputs == null )
         this.declaredInputs = new ArrayList<Input>();
      return declaredInputs;
   }

   /**
    * Sets the declared inputs.
    */
   public void setDeclaredInputs (List<Input> declaredInputs) {
      this.declaredInputs = declaredInputs;
   }

   /**
    * Gets the declared outputs.
    */
   public List<Output> getDeclaredOutputs () {
      if ( this.declaredOutputs == null )
         this.declaredOutputs = new ArrayList<Output>();
      return declaredOutputs;
   }

   /**
    * Adds the input.
    */
   public void addInput (Input inputC) {
      if ( this.declaredInputs == null )
         this.declaredInputs = new ArrayList<Input>();
      declaredInputs.add(inputC);

   }

   /**
    * Removes the input and it's modified output.
    */
   public void removeInput (String name) {
      Iterator<Input> iter = this.declaredInputs.iterator();
      while (iter.hasNext()) {
         Input in = iter.next();
         if ( in.getName().equals(name) ) {
            iter.remove();
            if ( in.getModified() != null )
               this.removeOutput(in.getModified());
            break;
         }
      }
   }

   /**
    * Adds the output.
    */
   public void addOutput (Output outputTask) {
      if ( this.declaredOutputs == null )
         this.declaredOutputs = new ArrayList<Output>();
      this.declaredOutputs.add(outputTask);

   }

   /**
    * Removes the output.
    */
   public void removeOutput (Output outputTask) {
      if ( this.declaredOutputs != null )
         this.declaredOutputs.remove(outputTask);

   }

   /**
    * Removes the output by name.
    */
   public void removeOutput (String name) {
      Iterator<Output> iter = this.declaredOutputs.iterator();
      while (iter.hasNext()) {
         Output out = iter.next();
         if ( out.getName().equals(name) ) {
            iter.remove();
            break;
         }
      }
   }

   private List<DecompositionClass> decompositions = new ArrayList<DecompositionClass>();

   public List<DecompositionClass> getDecompositions () {
      return decompositions;
   }

   public void addDecompositionClass (DecompositionClass dec) {
      decompositions.add(dec);
   }
   
   public void removeDecompositionClass (DecompositionClass dec) {
      decompositions.remove(dec);
   }

   public DecompositionClass getDecomposition (String id) {
      for (DecompositionClass decomp : getDecompositions())
         if ( decomp.getId().equals(id) )
            return decomp;
      return null;
   }

   /**
    * Checks if is equivalent. This function checks for equivalent
    * DecompositionClass classes recursively.
    */
   public boolean isEquivalent (TaskClass next, TaskModel taskModel) {

      TaskClass fakeTask = new TaskClass(taskModel, next);

      if ( this.getDeclaredInputs().size() == fakeTask.getDeclaredInputs()
            .size() ) {
         for (Input thisIn : this.getDeclaredInputs()) {
            boolean contain = false;
            Iterator<Input> iterate = fakeTask.getDeclaredInputs().iterator();
            while (iterate.hasNext()) {
               Input input = iterate.next();
               if ( thisIn.isEquivalent(input) ) {
                  contain = true;
                  fakeTask.getDeclaredInputs().remove(input);
                  break;
               }
            }
            if ( !contain ) {
               return false;
            }
         }
      }

      if ( this.getDeclaredOutputs().size() == next.getDeclaredOutputs().size() ) {
         for (Output thisOut : this.getDeclaredOutputs()) {
            boolean contain = false;
            Iterator<Output> iterate = fakeTask.getDeclaredOutputs().iterator();
            while (iterate.hasNext()) {
               Output output = iterate.next();
               if ( thisOut.isEquivalent(output) ) {
                  contain = true;
                  fakeTask.getDeclaredOutputs().remove(output);
                  break;
               }
            }
            if ( !contain ) {
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
    * This function makes the TaskClass's DOM element recursively.
    * @param properties 
    */
   public Node toNode (Document document, Set<String> namespaces, Properties properties) {

      Element taskElement = document.createElementNS(TaskModel.xmlnsValue,
            "task");

      Attr idTask = document.createAttribute("id");
      idTask.setValue(this.getId());
      taskElement.setAttributeNode(idTask);

      for (TaskClass.Input input : this.getDeclaredInputs()) {

         taskElement.appendChild(input.toNode(document,properties));

      }

      for (TaskClass.Output output : this.getDeclaredOutputs()) {
         taskElement.appendChild(output.toNode(document, "output"));
      }

      if ( this.precondition != null && this.precondition != "" ) {
         Element prec = document.createElementNS(TaskModel.xmlnsValue,
               "precondition");
         prec.setTextContent(precondition);
         taskElement.appendChild(prec);
      }

      if ( this.postcondition != null && this.postcondition != "" ) {
         Element post = document.createElementNS(TaskModel.xmlnsValue,
               "postcondition");
         post.setTextContent(postcondition);

         if ( isSufficient() ) {
            Attr sufficient = document.createAttribute("sufficient");
            sufficient.setValue("true");
            post.setAttributeNode(sufficient);
         }

         taskElement.appendChild(post);
      }

      for (DecompositionClass subtask : this.getDecompositions()) {

         taskElement.appendChild(subtask.toNode(document, namespaces));

      }

      return taskElement;
   }

   /**
    * Checks if is modified.
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

   /**
    * Adds the outputs and bindings to a new task.
    */
   public void addOutputsBindings (edu.wpi.cetask.Task step, String stepNameR,
         DecompositionClass subtask) {

      for (String outputName : step.getType().getDeclaredOutputNames()) {

         String bindingSlot = "$" + stepNameR + "." + outputName;

         String bindingSlotValue = null;
         bindingSlotValue = stepNameR + "_" + outputName;

         subtask.addBinding("$this." + bindingSlotValue, subtask.new Binding(
               bindingSlotValue, "this", bindingSlot,
               DecompositionClass.Type.OutputOutput));
         this.addOutput(this.new Output(bindingSlotValue, step.getType()
               .getSlotType(outputName)));
      }

   }

   /**
    * Adds the inputs and bindings to a new task.
    */
   public List<String> addInputsBindings (TaskModel taskModel,
         edu.wpi.cetask.Task step, String stepNameR,
         DecompositionClass subtask, Disco disco) throws NoSuchMethodException,
         ScriptException {

      List<String> inputs = new ArrayList<String>();

      for (String inputName : step.getType().getDeclaredInputNames()) {

         String bindingSlotvalue = "$" + stepNameR + "." + inputName;

         inputs.add(inputName);

         Object inputBinding = null;
         String inputBindingValue = null;
         
         inputBinding = (((Invocable) disco.getScriptEngine())
               .invokeFunction("find", step.getSlotValue(inputName)));

         if(inputBinding == null){
            inputBinding = step.getSlotValue(inputName);
            inputBindingValue = ((ApplicationSpecificClass)inputBinding).find();
         }
         else
            inputBindingValue = (String) inputBinding;

         int inputNum1 = this.getDeclaredInputs().size();
         String changedName = this.addInput(taskModel, this, inputName, step
               .getType().getSlotType(inputName),
               step.getType().getModified(inputName), inputBindingValue,
               subtask, stepNameR);
         int inputNum2 = this.getDeclaredInputs().size();
         subtask.addBinding(bindingSlotvalue, subtask.new Binding(inputName,
               stepNameR, "$this." + changedName,
               DecompositionClass.Type.InputInput));

         if ( inputNum1 != inputNum2 ) {
            subtask.addBinding("$this." + changedName, subtask.new Binding(
                  changedName, "this",inputBindingValue,
                  DecompositionClass.Type.Constant));

            for (int i = this.getDeclaredOutputs().size() - 1; i >= (this
                  .getDeclaredOutputs().size() - step.getType()
                  .getDeclaredOutputNames().size()); i--) {
               if ( step.getType().getModified(inputName) != null
                  && this.getDeclaredOutputs().get(i).getName()
                        .contains(step.getType().getModified(inputName)) ) {
                  this.getDeclaredInputs()
                        .get(this.getDeclaredInputs().size() - 1)
                        .setModified(this.getDeclaredOutputs().get(i));
                  break;
               }
            }
         }

      }
      return inputs;
   }

   /**
    * Gets the output. Finds the output with it's name
    */
   public Output getOutput (String outName) {
      for (Output out : this.getDeclaredOutputs()) {
         if ( out.getName().equals(outName) ) {
            return out;
         }
      }
      return null;
   }

   public void changeNameSpace (TaskClass newTask) {
  
      this.setQname(newTask.getQname());
      
   }
   

}