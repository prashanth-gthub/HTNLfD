package edu.wpi.htnlfd;

import edu.wpi.cetask.*;
import edu.wpi.cetask.DecompositionClass.Binding;
import edu.wpi.disco.*;
import edu.wpi.disco.lang.Utterance;
import org.w3c.dom.*;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.script.*;

public class Demonstration {

   private DocumentBuilderFactory factory;

   private DocumentBuilder builder;

   private Document document;

   TaskModel taskModel = null;

   private final String ReferenceFrame = "referenceFrame";

   private final String xmlnsValue = "http://www.cs.wpi.edu/~rich/cetask/cea-2018-ext";

   private final String namespace = "urn:disco.wpi.edu:htnlfd:setTable1";

   private final String namespacePrefix;

   private ArrayList<String[]> OrderedTasks = new ArrayList<String[]>();

   private TaskClass recipeTaskClass = null;

   public Demonstration () {
      factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);

      String[] dNSNameArray = namespace.split(":");
      namespacePrefix = dNSNameArray[dNSNameArray.length - 1];
      try {
         builder = factory.newDocumentBuilder();
         // document = builder.newDocument();
      } catch (ParserConfigurationException e) {
         throw new RuntimeException(
               "An error occured while creating the document builder", e);
      }
   }

   public List<Task> findDemonstration (Disco disco, String taskName) {
      List<Task> demonstratedTasks = new ArrayList<Task>();
      List<Task> demonstratedTasksReversed = new ArrayList<Task>();
      Object parent = (disco.getStack().get(0).getChildren().get(disco
            .getStack().get(0).getChildren().size() - 1));
      if ( parent instanceof Segment ) {

         for (int i = ((Segment) parent).getChildren().size() - 1; i >= 0; i--) {
            Object child = ((Segment) parent).getChildren().get(i);
            if ( (child instanceof Task) ) {
               Task task = (Task) child;
               if ( !(task instanceof Utterance) )
                  ;// demonstratedTasks.add(task);
            } else if ( child instanceof Segment ) {
               Segment segment = (Segment) child;
               demonstratedTasks.add(segment.getPurpose());
            }
         }
         
         for(int i=demonstratedTasks.size()-1;i>=0;i--){
            Task myTask = demonstratedTasks.get(i);
            demonstratedTasksReversed.add(myTask);
         }
         return demonstratedTasksReversed;
      } else {
         return null;
      }
   }

   public void writeDOM (Disco disco, String fileName, String taskName,
         List<Task> steps, String input) throws Exception {

      // Writing document into xml file
      document = builder.newDocument();
      DOMSource domSource = new DOMSource(document);
      File demonstrationFile = new File(fileName);
      if ( !demonstrationFile.exists() )
         demonstrationFile.createNewFile();

      try (FileOutputStream fileOutputStream = new FileOutputStream(
            demonstrationFile, false)) {

         StreamResult streamResult = new StreamResult(fileOutputStream);
         TransformerFactory tf = TransformerFactory.newInstance();
         Transformer transformer = tf.newTransformer();

         buildDOM(disco, taskName, steps, input);

         // Adding indentation and omitting xml declaration
         transformer.setOutputProperty(OutputKeys.INDENT, "yes");
         transformer.setOutputProperty(
               "{http://xml.apache.org/xslt}indent-amount", "2");
         transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
         transformer.transform(domSource, streamResult);

      } catch (Exception e) {
         
         throw e;
      }

   }

   public void readDOM (Disco disco, String fileName) {
      taskModel = disco.getInteraction().load(fileName);
   }

   private void buildDOM (Disco disco, String taskName, List<Task> steps,
         String input) throws NoSuchMethodException, ScriptException {

      Element taskModelElement = null;

      taskModelElement = document.createElementNS(xmlnsValue, "taskModel");
      document.appendChild(taskModelElement);

      Attr about = document.createAttribute("about");
      about.setValue(namespace);
      taskModelElement.setAttributeNode(about);
      Attr xmlns = document.createAttribute("xmlns");
      xmlns.setValue(xmlnsValue);
      taskModelElement.setAttributeNode(xmlns);
      Element recipe = null;

      List<String> namespaces = new ArrayList<String>();

      if ( this.taskModel != null ) {
         recipe = learnedTaskmodelToDom(namespaces, taskName, steps,
               taskModelElement, input);
      }
      Element taskElement = null;

      if ( recipe == null ) {
         taskElement = document.createElementNS(xmlnsValue, "task");
         taskModelElement.appendChild(taskElement);
         Attr idTask = document.createAttribute("id");
         idTask.setValue(taskName);
         taskElement.setAttributeNode(idTask);
      } else {
         taskElement = recipe;

         Element inputTask = document.createElementNS(xmlnsValue, "input");
         taskElement.insertBefore(inputTask, taskElement.getFirstChild());
         Attr inputName = document.createAttribute("name");
         inputName.setValue(input);
         inputTask.setAttributeNode(inputName);
         Attr inputType = document.createAttribute("type");
         inputType.setValue("boolean");
         inputTask.setAttributeNode(inputType);

      }

      demonstratedTaskToDom(disco, taskElement, taskName, steps, namespaces,
            recipe, input);

      for (String namespaceOfTasks : namespaces) {

         String[] namespaceOfTaskArray = namespaceOfTasks.split(":");
         String namespaceOfTask = namespaceOfTaskArray[namespaceOfTaskArray.length - 1];

         Attr xmlnsReference = document.createAttribute("xmlns:"
            + namespaceOfTask);
         xmlnsReference.setValue(namespaceOfTasks);
         taskModelElement.setAttributeNode(xmlnsReference);
      }

   }

   public void demonstratedTaskToDom (Disco disco, Element taskElement,
         String taskName, List<Task> steps, List<String> namespaces,
         Element recipe, String input) throws NoSuchMethodException, ScriptException {

      Element subtasks = document.createElementNS(xmlnsValue, "subtasks");
      taskElement.appendChild(subtasks);
      Attr idSubtask = document.createAttribute("id");
      int countSubtask = 0;
      NodeList nodes = taskElement.getChildNodes();
      for (int i = 0; i < nodes.getLength(); i++) {
         if ( nodes.item(i).getNodeName().equals("subtasks") )
            countSubtask++;
      }
      subtasks.setAttributeNode(idSubtask);
      countSubtask = taskElement.getChildNodes().getLength() == 1 ? 1
         : countSubtask + 1;
      idSubtask.setValue(Character.toLowerCase(taskName.charAt(0))
         + (taskName.length() > 1 ? taskName.substring(1) : "") + countSubtask);
      Map<String, Integer> StepNames = new HashMap<String, Integer>();
      Map<String, String> bindings = new HashMap<String, String>();

      Map<String, List<String[]>> inputs = new HashMap<String, List<String[]>>();
      List<String> inputsNumbers = new ArrayList<String>();

      for (Task step : steps) {
         
         String stepName = step.getType().getId();
         int count = 1;
         Map.Entry<String, Integer> stepEntry = null;
         for (Map.Entry<String, Integer> entry : StepNames.entrySet()) {
            if ( stepName.equals(entry.getKey()) ) {
               entry.setValue(entry.getValue() + 1);
               stepEntry = entry;
               count = stepEntry.getValue();
               break; // breaking because its one to one map
            }
         }

         if ( stepEntry == null ) {
            StepNames.put(stepName, count);
         }

         if ( !namespaces.contains(step.getType().getNamespace())
            && !step.getType().getNamespace().equals(namespace) ) {
            namespaces.add(step.getType().getNamespace());
         }

         Element subtaskStep = document.createElementNS(xmlnsValue, "step");
         subtasks.appendChild(subtaskStep);
         Attr nameSubtaskStep = document.createAttribute("name");

         String stepStrValue = Character.toLowerCase(stepName.charAt(0))
            + (stepName.length() > 1 ? stepName.substring(1) : "") + count;
         nameSubtaskStep.setValue(stepStrValue);
         subtaskStep.setAttributeNode(nameSubtaskStep);
         Attr valueSubtaskStep = document.createAttribute("task");
         String namespaceName = step.getType().getNamespace();

         String[] namespaceOfTaskArray = namespaceName.split(":");
         String namespaceOfTask = namespaceOfTaskArray[namespaceOfTaskArray.length - 1];

         if ( namespacePrefix.compareTo(namespaceOfTask) != 0 )
            valueSubtaskStep.setValue(namespaceOfTask + ":" + stepName);
         else
            valueSubtaskStep.setValue(stepName);
         subtaskStep.setAttributeNode(valueSubtaskStep);

         // //////////////////////////////////////////////////

         // ///////////////////////////////////////////////

         for (String inputName : step.getType().getDeclaredInputNames()) {

            String bindingSlotvalue = "$" + stepStrValue + "." + inputName;

             
            //String temp2 = step.getSlotValueToString(inputName);
            
            Object inputBinding = 
                  (((Invocable) disco.getScriptEngine()).invokeFunction("find",
                  step.getSlotValue(inputName)));

            String inputBindingValue = (String) inputBinding;
            
            if ( inputs.get(inputBindingValue) != null ) {
               boolean contain = false;
               for (String[] str : inputs.get(inputBindingValue)) {
                  if ( str[0].contains(inputName) ) {
                     contain = true;
                     break;
                  }
               }
               if ( !contain ) {
                  String[] nameType = new String[2];
                  nameType[0] = inputName;
                  nameType[1] = step.getType().getSlotType(inputName);
                  inputs.get(inputBindingValue).add(nameType);
                  System.out.println(inputBindingValue + " " + nameType[0]);
               }

            } else {
               String[] nameType = new String[2];
               List<String[]> name = new ArrayList<String[]>();
               nameType[0] = inputName;
               nameType[1] = step.getType().getSlotType(inputName);
               name.add(nameType);
               inputs.put(inputBindingValue, name);
               System.out.println(inputBindingValue + " " + nameType[0]);
            }

            bindings.put(bindingSlotvalue, inputBindingValue);
         }

         if ( recipe != null ) {

         }

      }

      if ( recipe != null ) {
         taskElement = recipe;
         Element applicable = document
               .createElementNS(xmlnsValue, "applicable");
         applicable.setTextContent("!$this." + input);
         subtasks.appendChild(applicable);

         for (Entry<String, List<String[]>> inputEntry : inputs.entrySet()) {
            for (String[] inputListName : inputEntry.getValue()) {
               for (String ins : recipeTaskClass.getDeclaredInputNames()) {
                  boolean change = false;
                  if ( recipeTaskClass.getSlotType(ins)
                        .equals(inputListName[1])
                     && ins.contains(inputListName[0]) ) {
                     String findParent = findValueOfInput(ins,
                           recipeTaskClass.getId());
                     if ( findParent != null
                        && findParent.equals(inputEntry.getKey()) ) {
                        inputListName[0] = ins;
                        change = true;
                        System.out.println("---" + inputEntry.getKey() + " "
                           + ins);
                     }

                     // if we cannot find the value in it's parents, it may be
                     // in it's siblings
                     if ( !change ) {

                        List<DecompositionClass> decompositions = recipeTaskClass
                              .getDecompositions();
                        for (DecompositionClass subtaskDecomposition : decompositions) {

                           boolean breaking = false;

                           Collection<Entry<String, Binding>> bindingsSubtask = subtaskDecomposition
                                 .getBindings().entrySet();
                           for (Entry<String, Binding> binding : bindingsSubtask) {

                              if ( binding.getKey().equals("$this." + ins) ) {

                                 if ( binding.getValue().value
                                       .equals(inputEntry.getKey()) ) {
                                    inputListName[0] = ins;
                                    System.out
                                          .println("---" + inputListName[0]);
                                 }

                              }

                           }
                        }
                     }
                  }
               }
            }
         }

      }

      for (Entry<String, List<String[]>> inputEntry : inputs.entrySet()) {
         for (String[] inputListName : inputEntry.getValue()) {
            System.out.println("- " + inputEntry.getKey() + " "
               + inputListName[0] + " " + inputListName[1]);
         }
      }
      // taskElement.appendChild();
      if ( recipe == null ) {
         for (Entry<String, List<String[]>> inputEntry : inputs.entrySet()) {
            for (String[] inputListName : inputEntry.getValue()) {

               Element inputTask = document
                     .createElementNS(xmlnsValue, "input");
               taskElement.insertBefore(inputTask, taskElement.getFirstChild());
               Attr inputName = document.createAttribute("name");
               boolean contain = false;
               for (String str : inputsNumbers) {
                  if ( str.contains(inputListName[0] + "1") ) {
                     contain = true;
                     break;
                  }
               }
               if ( !contain ) {
                  if ( recipe != null ) {
                     ;
                  } else {
                     inputName.setValue(inputListName[0] + "1");
                     inputsNumbers.add(inputListName[0] + "1");
                     inputListName[0] = inputListName[0] + "1";
                  }
               } else {
                  for (int i = inputsNumbers.size() - 1; i >= 0; i--) {
                     if ( inputsNumbers.get(i).contains(inputListName[0]) ) {
                        int start = inputsNumbers.get(i).lastIndexOf(
                              inputsNumbers.get(i))
                           + inputsNumbers.get(i).length() - 1;
                        int end = inputsNumbers.get(i).length();
                        String number = inputsNumbers.get(i).substring(start,
                              end);
                        int num = Integer.parseInt(number);
                        num++;
                        inputsNumbers.add(inputListName[0] + num);
                        inputName.setValue(inputListName[0] + num);
                        inputListName[0] = inputListName[0] + num;
                        break;
                     }
                  }
               }
               inputTask.setAttributeNode(inputName);
               Attr inputType = document.createAttribute("type");
               inputType.setValue(inputListName[1]);
               inputTask.setAttributeNode(inputType);
            }

         }

         for (Entry<String, List<String[]>> inputEntry : inputs.entrySet()) {
            for (String[] inputListName : inputEntry.getValue()) {

               Element subtaskBinding = document.createElementNS(xmlnsValue,
                     "binding");
               // subtasks.appendChild(subtaskBinding);
               Attr bindingSlot = document.createAttribute("slot");
               bindingSlot.setValue("$this." + inputListName[0]);
               subtaskBinding.setAttributeNode(bindingSlot);

               Attr bindingValue = document.createAttribute("value");

               bindingValue.setValue(inputEntry.getKey());
               subtaskBinding.setAttributeNode(bindingValue);

               subtasks.appendChild(subtaskBinding);

            }
         }
      }
      for (Entry<String, String> binding : bindings.entrySet()) {

         for (Entry<String, List<String[]>> inputEntry : inputs.entrySet()) {
            for (String[] inputListName : inputEntry.getValue()) {

               // System.out.println(binding.getValue() + ": "+
               // inputEntry.getKey());
               // System.out.println(binding.getKey() + ": "
               // +inputListName[0].replaceAll("[0-9]*$", ""));

               if ( inputEntry.getKey().equals(binding.getValue())
                  && binding.getKey().endsWith(
                        inputListName[0].replaceAll("[0-9]$", "")) ) {
                  Element subtaskBinding = document.createElementNS(xmlnsValue,
                        "binding");
                  // subtasks.appendChild(subtaskBinding);
                  Attr bindingSlot = document.createAttribute("slot");
                  bindingSlot.setValue(binding.getKey());
                  subtaskBinding.setAttributeNode(bindingSlot);

                  Attr bindingValue = document.createAttribute("value");

                  bindingValue.setValue("$this." + inputListName[0]);
                  subtaskBinding.setAttributeNode(bindingValue);

                  subtasks.appendChild(subtaskBinding);
               }
            }
         }

      }
   }

   private String findValueOfInput (String in, String parent) {

      Iterator<TaskClass> tasksIterator = this.taskModel.getTaskClasses()
            .iterator();
      while (tasksIterator.hasNext()) {
         TaskClass taskclass = tasksIterator.next();
         List<DecompositionClass> decompositions = taskclass
               .getDecompositions();
         for (DecompositionClass subtaskDecomposition : decompositions) {

            for (String stepName : subtaskDecomposition.getStepNames()) {

               if ( subtaskDecomposition.getStepType(stepName).getId()
                     .equals(parent) ) { // I should add namespace
                  Collection<Entry<String, Binding>> bindingsSubtask = subtaskDecomposition
                        .getBindings().entrySet();
                  for (Entry<String, Binding> binding : bindingsSubtask) {

                     if ( binding.getKey().contains(stepName)
                        && binding.getKey().contains(in) ) {
                        Collection<Entry<String, Binding>> bindingsSubtask2 = subtaskDecomposition
                              .getBindings().entrySet();
                        String val = binding.getValue().value;
                        for (Entry<String, Binding> binding2 : bindingsSubtask2) {

                           if ( binding2.getKey().contains(val) ) {
                              if ( !binding2.getValue().value.contains("$this") ) {
                                 return binding2.getValue().value;
                              } else {
                                 return findValueOfInput(val.substring(6),
                                       subtaskDecomposition.getGoal().getId());
                              }
                           }

                        }
                        return findValueOfInput(val.substring(6),
                              subtaskDecomposition.getGoal().getId());
                     }

                  }
               }
            }

         }
      }
      return null;
   }

   public Element learnedTaskmodelToDom (List<String> namespaces,
         String taskName, List<Task> steps, Element taskModelElement,
         String input) {

      Element recipe = null;
      boolean recipeOccured = true;
      Iterator<TaskClass> tasksIterator = this.taskModel.getTaskClasses()
            .iterator();
      ArrayList<Task> changedTasks = new ArrayList<Task>();

      while (tasksIterator.hasNext()) {

         TaskClass task = tasksIterator.next();

         Element taskElement = document.createElementNS(xmlnsValue, "task");
         taskModelElement.appendChild(taskElement);
         Attr idTask = document.createAttribute("id");
         idTask.setValue(task.getId());
         taskElement.setAttributeNode(idTask);
         if ( task.getId().equals(taskName) ) {
            recipe = taskElement;
            recipeTaskClass = task;
         }

         boolean changedTask = false;
         for (Task step : steps) {
            String stepType = step.getType().getId();
            if ( task.getId().equals(stepType) ) {
               changedTasks.add(step);
               changedTask = true;
               break;
            }
         }

         for (String inputName : task.getDeclaredInputNames()) {
            Element inputTask = document.createElementNS(xmlnsValue, "input");
            taskElement.appendChild(inputTask);
            Attr inputNameAttr = document.createAttribute("name");
            inputNameAttr.setValue(inputName);
            inputTask.setAttributeNode(inputNameAttr);
            Attr inputType = document.createAttribute("type");
            inputType.setValue(task.getSlotType(inputName));
            inputTask.setAttributeNode(inputType);
         }

         for (String outputName : task.getDeclaredOutputNames()) {
            Element inputTask = document.createElementNS(xmlnsValue, "output");
            taskElement.appendChild(inputTask);
            Attr inputNameAttr = document.createAttribute("name");
            inputNameAttr.setValue(outputName);
            inputTask.setAttributeNode(inputNameAttr);
            Attr inputType = document.createAttribute("type");
            inputType.setValue(task.getSlotType(outputName));
            inputTask.setAttributeNode(inputType);
         }

         List<DecompositionClass> decompositions = task.getDecompositions();
         for (DecompositionClass subtaskDecomposition : decompositions) {

            Element subtasks = document.createElementNS(xmlnsValue, "subtasks");
            taskElement.appendChild(subtasks);
            Attr idSubtask = document.createAttribute("id");
            String name = subtaskDecomposition.getId();

            for (String stepName : subtaskDecomposition.getStepNames()) {

               Element subtaskStep = document.createElementNS(xmlnsValue,
                     "step");
               subtasks.appendChild(subtaskStep);
               Attr nameSubtaskStep = document.createAttribute("name");

               String stepStrValue = stepName;
               nameSubtaskStep.setValue(stepStrValue);
               subtaskStep.setAttributeNode(nameSubtaskStep);
               Attr valueSubtaskStep = document.createAttribute("task");
               String namespaceDec = subtaskDecomposition.getStepType(stepName)
                     .getNamespace();
               String[] dNSNameArrayDec = namespaceDec.split(":");
               String dNSNameDec = dNSNameArrayDec[dNSNameArrayDec.length - 1];

               if ( dNSNameDec.compareTo(namespacePrefix) != 0 )
                  valueSubtaskStep.setValue(dNSNameDec + ":"
                     + subtaskDecomposition.getStepType(stepName).getId());
               else
                  valueSubtaskStep.setValue(subtaskDecomposition.getStepType(
                        stepName).getId());
               subtaskStep.setAttributeNode(valueSubtaskStep);

               if ( !namespaces.contains(subtaskDecomposition.getStepType(
                     stepName).getNamespace())
                  && !subtaskDecomposition.getStepType(stepName).getNamespace()
                        .equals(namespace) ) {
                  namespaces.add(subtaskDecomposition.getStepType(stepName)
                        .getNamespace());
               }
            }

            Collection<Entry<String, Binding>> bindingsSubtask = subtaskDecomposition
                  .getBindings().entrySet();

            if ( recipe != null && recipeOccured ) {
               Element applicable = document.createElementNS(xmlnsValue,
                     "applicable");
               applicable.setTextContent("$this." + input);
               subtasks.appendChild(applicable);
               recipeOccured = false;
            }

            if ( subtaskDecomposition.applicable != null
               && subtaskDecomposition.applicable != "" ) {
               Element applicable = document.createElementNS(xmlnsValue,
                     "applicable");
               applicable.setTextContent(subtaskDecomposition.applicable);
               subtasks.appendChild(applicable);
            }

            for (Entry<String, Binding> binding : bindingsSubtask) {
               Element subtaskBinding = document.createElementNS(xmlnsValue,
                     "binding");
               // subtasks.appendChild(subtaskBinding);
               Attr bindingSlot = document.createAttribute("slot");

               bindingSlot.setValue(binding.getKey());

               subtaskBinding.setAttributeNode(bindingSlot);

               Attr bindingValue = document.createAttribute("value");

               bindingValue.setValue(binding.getValue().value);
               // else
               // bindingValue.setValue("$this."+);
               subtaskBinding.setAttributeNode(bindingValue);
               if ( changedTask == false || !binding.getKey().contains("this") )
                  subtasks.appendChild(subtaskBinding);
            }

            subtasks.setAttributeNode(idSubtask);
            idSubtask.setValue(name);

         }

      }
      return recipe;
   }

   public void partialOrderring (List<Task> tasks, String taskName) {
      if ( this.taskModel != null ) {
         Iterator<TaskClass> tasksIterator = this.taskModel.getTaskClasses()
               .iterator();
         // Checking each task with all other tasks
         while (tasksIterator.hasNext()) {
            TaskClass task = tasksIterator.next();
            List<DecompositionClass> decompositions = task.getDecompositions();
            for (DecompositionClass subtaskDecomposition : decompositions) {

               Collection<Entry<String, Binding>> bindingsSubtask = subtaskDecomposition
                     .getBindings().entrySet();

               for (Entry<String, Binding> binding : bindingsSubtask) {

                  String nameOfDec = binding.getKey();
                  String valueOfDec = binding.getValue().value;
                  String typeOfDec = binding.getValue().slot;
                  if ( (nameOfDec != null || nameOfDec != "")
                     && (typeOfDec != null || typeOfDec != "")
                     && (valueOfDec != null || valueOfDec != "") )

                     for (Task step : tasks) {

                        for (String inputName : step.getType()
                              .getDeclaredInputNames()) {

                           if ( (nameOfDec.contains(ReferenceFrame) && !inputName
                                 .contains(ReferenceFrame))
                              || (!nameOfDec.contains(ReferenceFrame) && inputName
                                    .contains(ReferenceFrame)) ) {
                              String valueOfTask = step
                                    .getSlotValueToString(inputName);

                              String typeOfTask = step.getType().getSlotType(
                                    inputName);
                              String bindingTask = typeOfTask + "."
                                 + valueOfTask;
                              if ( bindingTask.equals(valueOfDec) ) {
                                 if ( nameOfDec.contains(ReferenceFrame) ) {
                                    System.out.println("------------ "
                                       + task.getId() + " is dependent on "
                                       + taskName);
                                    String[] order = new String[2];
                                    order[0] = task.getId();
                                    order[1] = taskName;
                                    OrderedTasks.add(order);

                                 } else {
                                    System.out.println("------------ "
                                       + taskName + " is dependent on "
                                       + task.getId());

                                    String[] order = new String[2];
                                    order[0] = task.getId();
                                    order[1] = taskName;
                                    OrderedTasks.add(order);
                                 }

                              }
                           }
                        }

                     }
               }
            }
         }
      }

      // Checking each step with all other steps

      for (int i = 0; i < tasks.size(); i++) {
         Task step1 = tasks.get(i);
         for (int j = i + 1; j < tasks.size(); j++) {
            Task step2 = tasks.get(j);
            for (String inputName1 : step1.getType().getDeclaredInputNames()) {
               for (String inputName2 : step2.getType().getDeclaredInputNames()) {
                  // System.out.println("names: "+inputName1+" "+inputName2);
                  if ( (inputName1.contains(ReferenceFrame) && !inputName2
                        .contains(ReferenceFrame))
                     || (!inputName1.contains(ReferenceFrame) && inputName2
                           .contains(ReferenceFrame)) ) {
                     // /////////////////////////////////////////
                     String valueOfTask1 = step1
                           .getSlotValueToString(inputName1);

                     String typeOfTask1 = step1.getType().getSlotType(
                           inputName1);
                     String bindingTask1 = typeOfTask1 + "." + valueOfTask1;
                     // ////////////////////////////////////////
                     // /////////////////////////////////////////
                     String valueOfTask2 = step2
                           .getSlotValueToString(inputName2);

                     String typeOfTask2 = step2.getType().getSlotType(
                           inputName2);
                     String bindingTask2 = typeOfTask2 + "." + valueOfTask2;
                     // ////////////////////////////////////////
                     // System.out.println("input values: "+bindingTask1+" "+bindingTask2);

                     if ( bindingTask1.equals(bindingTask2) ) {
                        if ( bindingTask1.contains(ReferenceFrame) ) {
                           System.out.println("------------ "
                              + step1.getType().getId() + ":" + bindingTask1
                              + " is dependent on " + step2.getType().getId()
                              + ":" + bindingTask2);
                        } else {
                           System.out.println("------------ "
                              + step2.getType().getId() + ":" + bindingTask2
                              + " is dependent on " + step1.getType().getId()
                              + ":" + bindingTask1);
                        }
                     }
                  }
               }
            }
         }

      }

   }

   public void findParentsOfTasks () {

      ArrayList<TaskClass> parent1 = new ArrayList<TaskClass>();
      ArrayList<TaskClass> parent2 = new ArrayList<TaskClass>();

      for (int i = 0; i < OrderedTasks.size(); i++) {
         findParent(OrderedTasks.get(i)[0], parent1);
         findParent(OrderedTasks.get(i)[1], parent2);
      }

      for (int i = 0; i < parent1.size(); i++) {
         for (int j = 0; i < parent2.size(); j++) {
            if ( parent1.get(i).getId().equals(parent2.get(j).getId()) ) {
               System.out.println("... " + parent1.get(i).getId());
            }
         }
      }

   }

   private int findParent (String task, ArrayList<TaskClass> parent) {

      Iterator<TaskClass> tasksIterator = this.taskModel.getTaskClasses()
            .iterator();
      while (tasksIterator.hasNext()) {
         TaskClass taskclass = tasksIterator.next();
         List<DecompositionClass> decompositions = taskclass
               .getDecompositions();
         for (DecompositionClass subtaskDecomposition : decompositions) {
            for (String stepName : subtaskDecomposition.getStepNames()) {

               if ( subtaskDecomposition.getStepType(stepName).getId()
                     .equals(task) ) { // I should add namespace
                  parent.add(taskclass);

                  findParent(taskclass.getId(), parent);
                  return 0; // maybe exception
               }
            }

         }
      }
      return 0;
   }
}
