package edu.wpi.htnlfd.model;

import org.w3c.dom.*;

import edu.wpi.htnlfd.Init;

import java.util.*;

import javax.xml.namespace.QName;

public class TaskModel {

	/** Saves different versions of TaskModel. */
	public static List<TaskModel> taskmodels = new ArrayList<TaskModel>();

	public static final String xmlnsValue = "http://www.cs.wpi.edu/~rich/cetask/cea-2018-ext";

	public String namespace;

	public static String namespacePrefix;

	public static Properties properties = new Properties();

	/**
	 * Instantiates a new task model.
	 */
	public TaskModel() {

		namespace = "urn:disco.wpi.edu:htnlfd:"+Init.namespace;

		int count = 1;
		if (taskmodels.size() == 0) {
			count = 1;
		} else {
			//taskmodels.get(taskmodels.size() - 1); // ??
         String nameS = TaskModel.namespacePrefix; 
			nameS = nameS.replaceAll("[^0-9]*", "");
			count = Integer.parseInt(nameS) + 1;

		}

		// count = 1;

		namespace = namespace + count;
		String[] dNSNameArray = namespace.split(":");
		namespacePrefix = dNSNameArray[dNSNameArray.length - 1];

		taskmodels.add(this);
	}

	/**
	 * Copy constructor for taskmodel.
	 */
	public TaskModel(TaskModel oldModel) {
		/*
		 * List<TaskClass> tasks = new
		 * ArrayList<TaskClass>(oldModel.tasks.size()); for (TaskClass
		 * oldTaskClass : oldModel.getTaskClasses()) { tasks.add(new
		 * TaskClass(this, oldTaskClass)); }
		 */
	}

	private List<TaskClass> tasks = new ArrayList<TaskClass>();

	/**
	 * finds the taskclass with it's id.
	 */
	public TaskClass getTaskClass(String id) {
		for (TaskClass task : tasks) {
			if (task.getId().equals(id))
				return task;
		}
		return null;
	}

	/**
	 * Gets the decomposition class.
	 */
	public DecompositionClass getDecompositionClass(String id) {
		for (TaskClass task : tasks) {
			DecompositionClass dec = task.getDecomposition(id);
			if (dec != null)
				return dec;
		}
		return null;
	}

	/**
	 * Gets the task classes.
	 */
	public List<TaskClass> getTaskClasses() {
		return tasks;// return unmodifiableList

	}

	/**
	 * Adds task to our taskmodel.
	 */
	public void add(TaskClass task) {
		tasks.add(task);
	}

	/**
	 * Removes task from our taskmodel.
	 */
	public boolean remove(TaskClass task) {
		return tasks.remove(task);
	}

	/**
	 * Checks for equivalent TaskClass classes recursively.
	 */
	public boolean isEquivalent() {
		Iterator<TaskClass> iterator = tasks.iterator();
		for (TaskClass c : getTaskClasses()) {
			while (iterator.hasNext()) {
				TaskClass next = iterator.next();
				if (!next.equals(c)) {
					if (c.isEquivalent(next, this)) {
						iterator.remove();
						System.out.println(c.getId() + " and " + next.getId());
					} /*else if (next.getDecompositions().isEmpty()) {
						// not just remove, change their parents
						// ?????????????????
						iterator.remove();
					}*/
				}
			}
		}
		return tasks.isEmpty();
	}

	abstract class Member {

		/**
		 * Instantiates a new member.
		 */
		public Member(String id, QName qname) {
			super();
			this.id = id;
			this.qname = qname;
		}

		private String id;

		private QName qname;

		public void setId(String id) {
			this.id = id;
		}

		public void setQname(QName qname) {
			this.qname = qname;
		}

		public String getId() {
			return id;
		}

		public QName getQname() {
			return qname;
		}

		public TaskModel getModel() {
			return TaskModel.this;
		}

	}

	/**
	 * Makes the TaskModel's DOM element recursively.
	 * 
	 * @param properties
	 */
	public Node toNode(Document document) {
		Element taskModelElement = null;

		taskModelElement = document.createElementNS(TaskModel.xmlnsValue,
				"taskModel");
		document.appendChild(taskModelElement);

		Attr about = document.createAttribute("about");
		about.setValue(this.namespace);
		taskModelElement.setAttributeNode(about);
		Attr xmlns = document.createAttribute("xmlns");
		xmlns.setValue(TaskModel.xmlnsValue);
		taskModelElement.setAttributeNode(xmlns);

		Set<String> namespaces = new HashSet<String>();
		// namespaces.add("urn:disco.wpi.edu:htnlfd:std");
		for (TaskClass task : tasks) {

			taskModelElement.appendChild(task.toNode(document, namespaces,
					properties));
		}

		for (String namespaceOfTasks : namespaces) {

			String[] namespaceOfTaskArray = namespaceOfTasks.split(":");
			String namespaceOfTask = namespaceOfTaskArray[namespaceOfTaskArray.length - 1];

			Attr xmlnsReference = document.createAttribute("xmlns:"
					+ namespaceOfTask);
			xmlnsReference.setValue(namespaceOfTasks);
			taskModelElement.setAttributeNode(xmlnsReference);
		}
		return taskModelElement;

	}

	/**
	 * Adds an attribute to the element.
	 */
	static Attr addAttribute(Document document, Element element, String name,
			String value) {
		Attr attr = document.createAttribute(name);
		attr.setValue(value);
		element.setAttributeNode(attr);
		return attr;
	}

}
