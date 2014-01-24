package edu.wpi.htnlfd.question;

import org.w3c.dom.*;
import edu.wpi.htnlfd.model.*;

public class AskAppCondition extends Question {

   String decName = null;

   // String uri = null;

   public AskAppCondition (int priority) {
      super(priority);
      this.question = "";
   }

   @Override
   public Question ask (TaskModel taskModel) {
      for (TaskClass task : taskModel.getTaskClasses()) {
         if ( task.getDecompositions().size() > 1 ) {
            for (DecompositionClass dec : task.getDecompositions()) {
               if ( dec.getApplicable() == null || dec.getApplicable() == "" ) {
                  this.question = "tell the applicability condition of %s is %s";

                  this.decName = dec.getId();
                  // this.uri = task.getQname().getNamespaceURI() ;
                  AskQuestion.properties.put("TellAppCondition@format",
                        this.question);
                  return this;
               }
            }
         }
      }
      return null;
   }

   public Node toNode (Document document) {

      Element taskElement = document.createElementNS(TaskModel.xmlnsValue,
            "task");
      Attr idTask = document.createAttribute("id");
      idTask.setValue("TellAppCondition");
      taskElement.setAttributeNode(idTask);

      // Inputs
      Element decInput = document
            .createElementNS(TaskModel.xmlnsValue, "input");
      Attr typeDec = document.createAttribute("type");
      typeDec.setValue("string");
      decInput.setAttributeNode(typeDec);

      Attr nameDec = document.createAttribute("name");
      nameDec.setValue("subtask");
      decInput.setAttributeNode(nameDec);

      taskElement.appendChild(decInput);

      Element conditionInput = document.createElementNS(TaskModel.xmlnsValue,
            "input");
      Attr typeCondition = document.createAttribute("type");
      typeCondition.setValue("string");
      conditionInput.setAttributeNode(typeCondition);

      Attr nameCond = document.createAttribute("name");
      nameCond.setValue("condition");
      conditionInput.setAttributeNode(nameCond);

      taskElement.appendChild(conditionInput);

      // Bindings
      Element subtaskBinding = document.createElementNS(TaskModel.xmlnsValue,
            "binding");

      Attr bindingSlot = document.createAttribute("slot");

      bindingSlot.setValue("$this.subtask");

      subtaskBinding.setAttributeNode(bindingSlot);

      Attr bindingValue = document.createAttribute("value");

      bindingValue.setValue(this.decName);

      subtaskBinding.setAttributeNode(bindingValue);

      taskElement.appendChild(subtaskBinding);

      // External Binding
      Element extSubtaskBinding = document.createElementNS(
            TaskModel.xmlnsValue, "binding");

      Attr extBindingSlot = document.createAttribute("slot");

      extBindingSlot.setValue("$this.external");

      extSubtaskBinding.setAttributeNode(extBindingSlot);

      Attr extBindingValue = document.createAttribute("value");

      extBindingValue.setValue("true");

      extSubtaskBinding.setAttributeNode(extBindingValue);

      taskElement.appendChild(extSubtaskBinding);
      // Script
      Element script = document.createElementNS(TaskModel.xmlnsValue, "script");

      script.setTextContent("Packages.edu.wpi.htnlfd.Init.addApplicable ($disco, $this.subtask, $this.condition);");

      taskElement.appendChild(script);

      return taskElement;
   }

}
