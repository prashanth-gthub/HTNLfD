package edu.wpi.htnlfd.table;

import edu.wpi.htnlfd.KnowledgeBase;
import edu.wpi.htnlfd.model.TaskClass;

public class TableKnowledgeBase extends KnowledgeBase {

	@Override
	public String getApplicable(TaskClass task, TaskClass newTask) {

		String input = "input1";
		// "step" + newTask.getDecompositions().get(0).getStepNames().size() +
		// "Task";
		String applicable = "!this." + input;
		if (task.getDecompositions().get(0).getApplicable() == null) {
			task.getDecompositions().get(0).setApplicable("this." + input);
		}

		TaskClass.Input inputC = task.new Input(input, "boolean", null);
		task.addInput(inputC);
		return applicable;
	}

}
