package edu.wpi.htnlfd;

import java.io.*;
import java.util.*;

import javax.xml.transform.*;

import edu.wpi.cetask.*;
import edu.wpi.disco.*;
import edu.wpi.htnlfd.model.TaskModel;

public class Init {

	/** The learned taskmodel. */
	private static TaskModel learnedTaskmodel = null;

	private static Demonstration demonstration = new Demonstration();

	private static DomManipulation DOM = new DomManipulation();
	public static String namespace = "tireRotation";
	private static String filename = "tireRotation1";

	private static String separator = System.getProperty("file.separator");

	private static String fileName = System.getProperty("user.dir") + separator
			+ filename;

	/**
	 * The main method.(Never called)
	 */
	public static void main(String[] args) {
		System.out.println("YUHU");
	}

	/**
	 * Learns demonstrations.
	 */
	public static void learn(Disco disco, String taskName) throws Exception {

		if (demonstration == null) {
			demonstration = new Demonstration();
			DOM = new DomManipulation();
		}

		List<Task> DemonstratedTasks = demonstration.findDemonstration(disco);
		try {
			learnedTaskmodel = demonstration.buildTaskModel(disco, taskName,
					DemonstratedTasks);
			load(disco);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Adds the steps to the specified task and subtask. If it should be added
	 * at the end after should be null or "".
	 */
	public static void addSteps(Disco disco, String subtask, String after)
			throws Exception {
		demonstration.getNewTaskModel();
		learnedTaskmodel = demonstration.addSteps(disco, subtask, after);
		load(disco);
	}

	/**
	 * Makes one step to be optional.
	 */
	public static void addOptionalStep(Disco disco, String subtask,
			String stepName) throws Exception {
		demonstration.getNewTaskModel();
		learnedTaskmodel = demonstration.addOptionalStep(subtask, stepName);
		load(disco);
	}

	/**
	 * Makes the step to be repeated
	 */
	public static void addMaxOccurs(Disco disco, String subtask,
			String stepName, int maxOccurs) throws Exception {
		demonstration.getNewTaskModel();
		learnedTaskmodel = demonstration.addMaxOccurs(subtask, stepName,
				maxOccurs);
		load(disco);
	}

	/**
	 * Adds the alternative recipe.
	 */
	public static void addAlternativeRecipe(Disco disco, String taskName,
			String applicable) throws Exception {
		demonstration.getNewTaskModel();
		learnedTaskmodel = demonstration.addAlternativeRecipe(disco, taskName,
				applicable);
		load(disco);
	}

	public static void addOrderStep(Disco disco, String subtaskId,
			String stepNameDep, String stepNameRef) throws Exception {
		demonstration.getNewTaskModel();
		learnedTaskmodel = demonstration.addOrderStep(subtaskId, stepNameDep,
				stepNameRef);
		load(disco);
	}

	public static void removeBinding(Disco disco, String subtaskId, String name)
			throws Exception {
		demonstration.getNewTaskModel();
		learnedTaskmodel = demonstration.removeBinding(subtaskId, name);
		load(disco);
	}

	public static void addBinding(Disco disco, String subtaskId, String key,
			String value) throws Exception {
		demonstration.getNewTaskModel();
		learnedTaskmodel = demonstration.addBinding(subtaskId, key, value);
		load(disco);
	}

	public static void changeBinding(Disco disco, String subtaskId, String key,
			String value) throws Exception {
		demonstration.getNewTaskModel();
		learnedTaskmodel = demonstration.changeBinding(subtaskId, key, value);
		load(disco);
	}

	public static void connectSteps(Disco disco, String subtaskId,
			String step1, String output, String step2, String input)
			throws Exception {
		demonstration.getNewTaskModel();
		learnedTaskmodel = demonstration.connectSteps(subtaskId, step1, output,
				step2, input);
		load(disco);
	}

	/**
	 * Makes the steps of a subtask completely ordered
	 */
	public static void setOrdered(Disco disco, String subtaskId)
			throws Exception {
		demonstration.getNewTaskModel();
		learnedTaskmodel = demonstration.setOrdered(subtaskId);
		load(disco);

	}

	/**
	 * Adds the applicable condition to a subtask.
	 */
	public static void addApplicable(Disco disco, String subtaskId,
			String condition) throws Exception {
		demonstration.getNewTaskModel();
		learnedTaskmodel = demonstration.addApplicable(subtaskId, condition);
		load(disco);
	}

	/**
	 * Adds the precondition to a task.
	 */
	public static void addPrecondition(Disco disco, String taskName,
			String precondition) throws Exception {
		demonstration.getNewTaskModel();
		learnedTaskmodel = demonstration
				.addPrecondition(taskName, precondition);
		load(disco);

	}

	/**
	 * Adds the postcondition to a task.
	 */
	public static void addPostcondition(Disco disco, String taskName,
			String postcondition, boolean sufficient) throws Exception {
		demonstration.getNewTaskModel();
		learnedTaskmodel = demonstration.addPostcondition(taskName,
				postcondition, sufficient);
		load(disco);
	}

	/**
	 * Adds the output to a task.
	 */
	public static void addOutput(Disco disco, String taskName,
			String outputName, String outputType) throws Exception {
		demonstration.getNewTaskModel();
		learnedTaskmodel = demonstration.addOutput(taskName, outputName,
				outputType);
		load(disco);

	}

	/**
	 * Adds the input to a task.
	 */
	public static void addInput(Disco disco, String taskName, String inputName,
			String type, String modified) throws Exception {
		demonstration.getNewTaskModel();
		learnedTaskmodel = demonstration.addInput(taskName, inputName, type,
				modified);
		load(disco);

	}
	

	public static void addTask(Disco disco, String taskName) throws Exception {
		demonstration.getNewTaskModel();
		learnedTaskmodel = demonstration.addTask(taskName);
		load(disco);

	}

	public static void changeSubtaskName(Disco disco, String subtaskId,
			String newName) throws Exception {
		demonstration.getNewTaskModel();
		learnedTaskmodel = demonstration.changeSubtaskName(subtaskId, newName);
		load(disco);
	}
	
	public static void answerQuestion(Disco disco, String taskName, String input)
			throws Exception {

		learnedTaskmodel = demonstration.answerQuestion(taskName, input);
		load(disco);
	}

	/**
	 * Prints the learned taskmodel.
	 * 
	 * @throws IOException
	 */
	public static void print(PrintStream stream) throws TransformerException,
			IOException {
		DOM.writeDOM(stream, learnedTaskmodel);
	}

	public static void print() throws TransformerException, IOException {
		// C:\Users\User\AppData\Local\Temp\Console.test
		PrintStream stream = new PrintStream(System.out);
		print(stream);
	}

	/**
	 * Load learned taskmodel into disco.
	 * 
	 * @throws Exception
	 */
	public static void load(Disco disco) throws Exception {
		// engine.load(learnedTaskmodel.toNode(), null);
		DOM.writeDOM(fileName, learnedTaskmodel, demonstration.askQuestion);
		demonstration.readDOM(disco, fileName);

	}

	public static void printTasks() {
		for (edu.wpi.htnlfd.model.TaskClass task : learnedTaskmodel
				.getTaskClasses()) {
			System.out.println(task.getId());
		}
	}

}
