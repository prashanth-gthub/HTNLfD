package edu.wpi.htnlfd.dto;

import java.util.ArrayList;

public class TaskBlock {
   String id;
   ArrayList<Input> inputs = new ArrayList<Input>();
   ArrayList<Output> outputs = new ArrayList<Output>();
   ArrayList<Subtasks> subtasks = new ArrayList<Subtasks>();
   public String getId () {
      return id;
   }
   public void setId (String id) {
      this.id = id;
   }
   public TaskBlock (String id) {
      super();
      this.id = id;
   }
   
   public void addInput(Input in){
      inputs.add(in);
   }
   
   public void addOutput(Output out){
      outputs.add(out);
   }
   
   public void addSubtask(Subtasks sub){
      subtasks.add(sub);
   }
   public ArrayList<Input> getInputs () {
      return inputs;
   }
   public void setInputs (ArrayList<Input> inputs) {
      this.inputs = inputs;
   }
   public ArrayList<Output> getOutputs () {
      return outputs;
   }
   public void setOutputs (ArrayList<Output> outputs) {
      this.outputs = outputs;
   }
   public ArrayList<Subtasks> getSubtasks () {
      return subtasks;
   }
   public void setSubtasks (ArrayList<Subtasks> subtasks) {
      this.subtasks = subtasks;
   }
   
   
}
