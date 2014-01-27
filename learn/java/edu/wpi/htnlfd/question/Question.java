package edu.wpi.htnlfd.question;

import org.w3c.dom.*;
import edu.wpi.htnlfd.model.TaskModel;

public abstract class Question {
	public int priority;

	public String question;

	public Question(int priority) {
		super();
		this.priority = priority;
	}

	/**
	 * Sets the question according to the state of taskModel.
	 */
	public abstract Question ask(TaskModel taskModel);

	/**
	 * Converts to DOM Node.
	 */
	public abstract Node toNode(Document document);

}
