package edu.wpi.htnlfd.question;

import org.w3c.dom.*;
import java.util.*;
import java.util.Map.Entry;
import edu.wpi.htnlfd.model.*;
import edu.wpi.htnlfd.model.DecompositionClass.*;
import edu.wpi.htnlfd.model.TaskClass.*;

public class AskRepeatedStep extends Question {

	// String taskName = null;

	String decName = null;

	// String uri = null;

	String stepName = null;

	int times = 0;

	public AskRepeatedStep(int priority) {
		super(priority);
		this.question = "";
	}

	@Override
	public Question ask(TaskModel taskModel) {
		for (TaskClass task : taskModel.getTaskClasses()) {
			Question q = findRepeatedStep(taskModel, task);
			if (q != null)
				return this;
		}
		return null;
	}

	public AskRepeatedStep findRepeatedStep(TaskModel taskModel, TaskClass task) {

		for (DecompositionClass dec : task.getDecompositions()) {
			if (dec.getStepNames().size() <= 1)
				continue;
			Map<Step, List<Step>> steps = new HashMap<Step, List<Step>>();
			Map<Step, List<String>> names = new HashMap<Step, List<String>>();

			{
				int i = dec.getStepNames().size() - 1;
				// Finding more than one consecutive same steps
				while (i > 0) {
					int j = i - 1;
					while (j >= 0) {
						Step stepO1 = dec.getStep(dec.getStepNames().get(i));
						Step stepO2 = dec.getStep(dec.getStepNames().get(j));

						DecompositionClass.Step step1 = dec.new Step(stepO1);
						DecompositionClass.Step step2 = dec.new Step(stepO2);
						step1.removeRequired(dec.getStepNames().get(j));

						boolean contain = false;
						if (step1.isEquivalent(step2, taskModel)) {
							if (dec.checkInputs(dec.getStepNames().get(i),
									step1.getType(), dec.getStepNames().get(j),
									step2.getType(), dec, dec, taskModel)) {

								for (Entry<Step, List<Step>> step : steps
										.entrySet()) {
									if (step.getKey().equals(stepO1)) {
										steps.get(stepO1).add(stepO2);
										names.get(stepO1).add(
												dec.getStepNames().get(j));
										contain = true;
										break;
									} else {
										for (Step st : step.getValue()) {
											if (st.equals(stepO1)) {
												step.getValue().add(stepO2);
												names.get(step.getKey()).add(
														dec.getStepNames().get(
																j));
												contain = true;
												break;
											}
										}
										if (contain) {
											break;
										}
									}
								}

								if (contain) {
									;
								} else {
									List<Step> listStep = new ArrayList<Step>();
									listStep.add(stepO2);
									steps.put(stepO1, listStep);

									List<String> listName = new ArrayList<String>();
									listName.add(dec.getStepNames().get(i));
									listName.add(dec.getStepNames().get(j));
									names.put(stepO1, listName);

								}

								i = j;
								j = i - 1;

							} else {
								i = i - 1;
								j = j - 1;
								break;
							}

						} else {
							i = i - 1;
							j = j - 1;
							break;
						}

					}
				}

			}

			for (Entry<Step, List<Step>> step : steps.entrySet()) {
				List<String> list = names.get(step.getKey());
				int maxOccurs = list.size();

				this.stepName = list.get(maxOccurs - 1);
				this.times = maxOccurs;
				// this.taskName = task.getId();
				this.decName = dec.getId();
				// this.uri = task.getQname().getNamespaceURI();

				this.question = "Is " + this.decName + ":" + this.stepName
						+ " repeated " + this.times + " times?";
				AskQuestion.properties.put("TellMaxOccurs@format",
						this.question);
				return this;
			}
		}
		return null;

	}

	public Node toNode(Document document) {

		Element taskElement = document.createElementNS(TaskModel.xmlnsValue,
				"task");
		Attr idTask = document.createAttribute("id");
		idTask.setValue("TellMaxOccurs");
		taskElement.setAttributeNode(idTask);

		// Inputs
		Element decInput = document.createElementNS(TaskModel.xmlnsValue,
				"input");
		Attr typeDec = document.createAttribute("type");
		typeDec.setValue("string");
		decInput.setAttributeNode(typeDec);

		Attr nameDec = document.createAttribute("name");
		nameDec.setValue("subtask");
		decInput.setAttributeNode(nameDec);

		taskElement.appendChild(decInput);

		Element stepInput = document.createElementNS(TaskModel.xmlnsValue,
				"input");
		Attr typeCondition = document.createAttribute("type");
		typeCondition.setValue("string");
		stepInput.setAttributeNode(typeCondition);

		Attr nameStep = document.createAttribute("name");
		nameStep.setValue("stepName");
		stepInput.setAttributeNode(nameStep);

		taskElement.appendChild(stepInput);

		Element maxInput = document.createElementNS(TaskModel.xmlnsValue,
				"input");
		Attr typeMax = document.createAttribute("type");
		typeCondition.setValue("number");
		maxInput.setAttributeNode(typeMax);

		Attr nameMax = document.createAttribute("name");
		nameMax.setValue("maxOccurs");
		maxInput.setAttributeNode(nameMax);

		taskElement.appendChild(maxInput);

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

		// Bindings
		Element stepBinding = document.createElementNS(TaskModel.xmlnsValue,
				"binding");

		Attr bindingStepSlot = document.createAttribute("slot");

		bindingStepSlot.setValue("$this.subtask");

		stepBinding.setAttributeNode(bindingStepSlot);

		Attr bindingStepValue = document.createAttribute("value");

		bindingStepValue.setValue(this.decName);

		stepBinding.setAttributeNode(bindingStepValue);

		taskElement.appendChild(stepBinding);

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
		Element script = document.createElementNS(TaskModel.xmlnsValue,
				"script");

		script.setTextContent("Packages.edu.wpi.htnlfd.Init.addApplicable ($disco, $this.subtask, $this.condition);");

		taskElement.appendChild(script);

		return taskElement;
	}

}
