/* Copyright (c) 2008 Charles Rich and Worcester Polytechnic Institute.
 * All Rights Reserved.  Use is subject to license terms.  See the file
 * "license.terms" for information on usage and redistribution of this
 * file and for a DISCLAIMER OF ALL WARRANTIES.
 */
package edu.wpi.cetask;

import edu.wpi.cetask.ScriptEngineWrapper.Compiled;

import java.text.DateFormat;
import java.util.*;
import javax.script.*;

/**
 * Representation for instances of a task class.
 */
public class Task extends Instance {
 
   protected Task (TaskClass type, TaskEngine engine) { 
      super(type, engine); 
      synchronized (bindings) {
         try { 
            // create JavaScript instance object (no initial slot properties)
            // do not use Instance.eval below because Decomposition.Step includes
            // call to updateBindings, but during constructor, steps are not yet added
            bindings.put("$this", 
                 TaskEngine.INVOCABLE ? ((Invocable) engine.getScriptEngine())
                                        .invokeFunction("edu_wpi_cetask_newObject") : 
                     engine.eval("new Object()", "new Task"));
            type.updateBindings(this); // extension
         } catch (RuntimeException e) { throw e; }  
         catch (Exception e) { throw new RuntimeException(e); }
      }
   }
   
   @Override
   public TaskClass getType () { return (TaskClass) super.getType(); }

   @Override
   public boolean equals (Object object) {
      if ( this == object ) return true;
      if ( !(object instanceof Task) ) return false;
      Task task = (Task) object;
      if ( !task.getType().equals(getType()) ) return false;
      synchronized (bindings) {
         try {
            bindings.put("$$value", task.bindings.get("$this")); 
            return (Boolean) evalCondition(equals, compiledEquals, "equals");
         } finally { bindings.remove("$$value"); }
      }
   }

   @Override 
   public int hashCode () {
      return getType().hashCode() ^
         eval(hashCode, compiledHashCode, "hashCode").hashCode();
   }
   
   // see Javascript function definitions in default.xml
   final static String hashCode = "edu.wpi.cetask_helper.hashCode($this)",
                       equals = "edu.wpi.cetask_helper.equals($this,$$value)";

   static Compiled compiledEquals, compiledHashCode;
   
   /**
    * Return value of the named slot.<br>
    * <br>
    * Note that this method returns a Java object, but the <em>real</em>
    * values are stored in JavaScript and must be converted to Java objects when
    * returned by this method. Refer to Chapter 12 of O'Reilly JavaScript guide
    * for details of conversion, but note following subtleties here:
    * <ul>
    * <li> JavaScript objects and arrays (i.e., everything except for numbers,
    * strings and booleans) convert to Java objects of "opaque" type. These
    * values can be passed back to JavaScript, but have an unpublished API that
    * is not intended for use by Java programs.
    * <li> <em>Both</em> null and undefined in JavaScript convert to null
    * </ul>
    * Since there is no attribute for "optional" slots in CEA-2018, all input
    * slot values must be defined before executing a task and all output slots
    * will be defined after execution. However, a null value can be used unless
    * a pre- or postcondition specifies otherwise.
    * 
    * @see #isDefinedSlot(String)
    * @see #deleteSlotValue(String)
    * @see #isDefinedInputs()
    * @see #isDefinedOutputs()
    */
   public Object getSlotValue (String name) { 
      if ( isScriptable(name) ) {
         Object value = engine.get(bindings.get("$this"), name);
         return engine.isDefined(value) ? value : null;
      } else return eval("$this."+name, "getSlotValue"); 
   }

   private Boolean getSlotValueBoolean (String name) {
      if ( isScriptable(name) ) {
         Object value = engine.get(bindings.get("$this"), name);
         return engine.isDefined(value) ? (Boolean) value : null;
      } else return evalCondition("$this."+name, "getSlotValue"); 
   }
   
   private boolean isScriptable (String name) {
        String type = getType().getSlotType(name);
        // for Java objects, must use LiveConnect
        return TaskEngine.SCRIPTABLE && 
           !(Utils.startsWith(type, "Packages.") || Utils.startsWith(type, "java.")); 
   }

   /**
    * Return the string obtained by calling <em>JavaScript</em> toString method 
    * on value of named slot.   Good for debugging output.
    * 
    * @see TaskEngine#toString(Object)
    */
   public String getSlotValueToString (String name) {
      return engine.toString(getSlotValue(name));
   }

   /**
    * Test whether slot value is defined. Note if getSlotValue() returns null,
    * isDefinedSlot() may return true <em>or</em> false.
    * 
    * @see #getSlotValue(String)
    * @see #deleteSlotValue(String)
    */
   public boolean isDefinedSlot (String name) {
      return TaskEngine.SCRIPTABLE ?
         engine.isDefined(engine.get(bindings.get("$this"), name)) 
         // note using !== (not ===) below b/c null==undefined in JavaScript
         : evalCondition("$this."+name+" !== undefined", "isDefinedSlot");
   }   
  
   // see Decomposition
   private boolean modified = true;
   boolean isModified () { return modified; }
   void setModified (boolean modified) { this.modified = modified; }
   
   /**
    * Set the value of named slot to given value.<br>
    * <br>
    * Note that the given Java object must be converted to a JavaScript object
    * for storage. Refer to Chapter 12 of O'Reilly JavaScript guide for details
    * of conversion, but note following subtleties here:
    * <ul>
    * <li> Java objects and arrays (i.e., everything but numbers, booleans and 
    * strings) convert to instances of JavaObject and JavaArray 
    * </ul>
    * 
    * @see #setSlotValue(String,Object,boolean)
    * @see #getSlotValue(String)
    * @see #setSlotValueScript(String,String,String)
    */
   public Object setSlotValue (String name, Object value) {
      setSlotValue(name, value, true);
      return value;
   }   
  
   /**
    * Set the value of named slot to given value.<br>
    * 
    * @see #setSlotValue(String,Object)
    */
   public void setSlotValue (String name, Object value, boolean check) {
      checkCircular(name, value);
      String type = getType().getSlotType(name);
      if ( !check || checkSlotValue(type, value) ) {
         if ( TaskEngine.SCRIPTABLE && 
               ( value == null || value instanceof Boolean 
                 || value instanceof String || value instanceof Number 
                 || engine.isScriptable(value) ||
                 value == engine.undefined() ) ) { 
            modified = true;
            engine.put(bindings.get("$this"), name, value);
         } else 
            synchronized (bindings) {
               try {
                  bindings.put("$$value", value); // convert to JavaScript value
                  modified = true;
                  eval("$this."+name+" = $$value;", "setSlotValue"); 
               } finally { bindings.remove("$$value"); }
            }
      } else failCheck(name, value.toString(), "setSlotValue");
   }  
     
   protected void checkCircular (String name, Object value) {
      if ( value == this ) 
         // will cause infinite loop when printing
         throw new RuntimeException("Attempting to store pointer to self in slot "
               +name+" of "+this);
   }
   
   public boolean checkSlotValue (String type, Object value) {
      try {
         if ( type == null || value == null ||
               ( type.equals("boolean") && value instanceof Boolean ) ||
               ( type.equals("string") && value instanceof String ) ||
               ( type.equals("number") && value instanceof Number ) ||
               // for Unity version
               ( Utils.startsWith(type, "Packages.") 
                 && Class.forName(type.substring(9)).isInstance(value) ) ) 
            return true;
      } catch (ClassNotFoundException e) { return false; }
      synchronized (bindings) {
         try {
            bindings.put("$$value", value); // convert to JavaScript object
            return Utils.booleanValue(
                  eval( // workaround bug: "$$value instanceof Date" returns false
                        type.equals("Date") ? "$$value.getUTCDate !== undefined" :
                           ("$$value instanceof "+type),
                        "checkSlotValue"));
         } finally { bindings.remove("$$value"); }
      }
   }

   /**
    * Set the value of named slot to result of evaluating given JavaScript
    * expression. This method avoids issues of conversion from Java to
    * JavaScript objects.
    * 
    * @see #setSlotValue(String,Object)
    */
   public void setSlotValueScript (String name, String expression, String where) {
      // only evaluate expression once (in case of side effects)
      try {
         if ( !evalCondition(makeExpression("$this", getType(), name, expression, false), where) )
            failCheck(name, expression, where);
         else modified = true;
      } finally { bindings.remove("$$value"); }
   }

   void setSlotValueScript (String name, Compiled compiled, String where) {
      if ( !evalCondition(compiled, bindings, where) )
         failCheck(name, "compiled script", where);
      else modified = true;
   }
   
   static String makeExpression (String self, TaskClass type, String name, 
                                 String value, boolean onlyDefined) {
      StringBuffer buffer = new StringBuffer();
      buffer.append("$$value = ").append(value).append(';');
      if ( onlyDefined ) 
         buffer.append("if ( $$value == undefined ) $$value = true; else ");
      buffer.append("if ( ").append(checkExpression(type, name)).append(" ) {")
         .append(self).append('.').append(name)
         .append(" = $$value; $$value = true; } else $$value = false; $$value");
      return buffer.toString();
   }
   
   void failCheck (String name, String expression, String where) {
      throw new IllegalArgumentException(
            "Type error: Cannot assign result of "+expression+" to "
            +getType().getSlotType(name)+" slot "+name
            +" of "+getType()+" in "+where);
   }
   
   public boolean checkSlotValueScript (String name, String expression) {
      // only evaluate expression once (in case of side effects)
      try { return evalCondition(
               "$$value = "+expression+"; "+checkExpression(getType(), name), 
               "checkSlotValueScript");
      } finally { bindings.remove("$$value"); }
   }

   private static String checkExpression (TaskClass task, String name) {
      String type = task.getSlotType(name);
      return type == null ? "true" :
        ("$$value === undefined || "+ // allow any slot to be set to undefined
            ("boolean".equals(type) ? "typeof $$value == \"boolean\"" :
               "string".equals(type) ? "typeof $$value == \"string\"" :
                  "number".equals(type) ? "typeof $$value == \"number\"" :
                     // work around bug: "$$value instanceof Date" returns false
                     "Date".equals(type) ? "$$value.getUTCDate !== undefined" :
                        "$$value instanceof "+type));
   }
   
   /**
    * Similar to {@link #setSlotValueScript(String,String,String)} but with extra 
    * bindings to use for evaluation of expression (instead of task bindings)
    */
   void setSlotValueScript (String name, String expression, String where,
                            Bindings extra) {
      try {
         extra.put("$$this", bindings.get("$this"));
         if ( !evalCondition(expression, extra, where) )
            failCheck(name, expression, where);
         else  modified = true;
      } 
      finally { extra.remove("$$value"); extra.remove("$$this"); }
   }
   
   void setSlotValueScript (String name, Compiled compiled, String where,
                            Bindings extra) {
      try {
         extra.put("$$this", bindings.get("$this"));
         if ( !evalCondition(compiled, extra, where) )
            failCheck(name, "compiled script", where);
         else  modified = true;

      } 
      finally { extra.remove("$$value"); extra.remove("$$this"); }
   }
   
   /**
    * Make named slot undefined.
    * 
    * @see #isDefinedSlot(String)
    */
   public void deleteSlotValue (String name) {
      eval("delete $this."+name, "deleteSlotValue");
      getType().updateBindings(this);
      modified = true;
   }
   
   public Boolean isApplicable () {
      TaskClass type = getType();
      return evalCondition(type.precondition, type.compiledPrecondition, 
            type+" precondition");
   }
   
   private Boolean achieved;
   
   public void setAchieved (Boolean achieved) { 
      this.achieved = achieved;
      engine.clearLiveAchieved();
   }
   
   public Boolean isAchieved () {
      if ( achieved != null ) return achieved; // overrides condition
      if ( engine.containsAchieved(this) ) { // check cached value
         return engine.isAchieved(this);
      }
      TaskClass type = getType();
      // empty string evaluates to null
      return engine.setAchieved(
            this, // store cache
            evalCondition(type.postcondition, type.compiledPostcondition, 
                  type+" postcondition"));
   }

   /**
    * Test whether all declared input slots (i.e., not including
    * "external") have defined values.
    * 
    * @see #getSlotValue(String)
    */
   public boolean isDefinedInputs() {
      for (String name : getType().getDeclaredInputNames())
         if ( !isDefinedSlot(name) ) return false;
      return true;
   }

   /**
    * Test whether all declared output slots (i.e., not including "success" and
    * "when") have defined values. 
    * 
    * @see #getSlotValue(String)
    */
   public boolean isDefinedOutputs() {
      for (String name : getType().getDeclaredOutputNames())
         if ( !isDefinedSlot(name) ) return false;
      return true;
   }

   /**
    * Test whether this task starts given plan.  (For extensions.)
    * 
    * @see Plan#isStarted()
    */
   public boolean isStarter (Plan plan) { return true; };
   
   /**
    * Test whether this task is instance of primitive type.
    */
   public boolean isPrimitive () { return getType().isPrimitive(); }
   
   /**
    * Test whether this task is instance of an internal type.
    */
   public boolean isInternal () { return getType().isInternal(); }
   
   // predefined slots
   
   /**
    * Get time when this task occurred, if it did.
    *
    * @return milliseconds or 0 (if not occurred)
    * @see #setWhen(long)
    */
   public long getWhen () {
      try { 
         return // for some reason, millis get converted to double first 
               engine.evalDouble("$this.when.getTime()", bindings, "getWhen").longValue(); 
      } catch (RuntimeException e) { throw e; }  
        catch (Exception e) { throw new RuntimeException(e); }
   }

   /**
    * Set the time when this task occurred. Javascript instance of Date object
    * is created with given milliseconds.
    * 
    * @see #getWhen()
    */
   public void setWhen (long milliseconds) {
      setSlotValueScript("when", "new Date("+Long.toString(milliseconds)+")", 
            "setWhen");
   }
   
   /**
    * Test whether this task instance occurred.
    */
   public boolean occurred () { return isDefinedSlot("when"); }
      
   /**
    * Get the value of the predefined 'success' slot 
    * @return TRUE, FALSE or null
    */
   public Boolean getSuccess () { return getSlotValueBoolean("success");  }

   /**
    * Set the predefined 'success' output slot of this task.
    * 
    * @param success - note type boolean so converts to JavaScript boolean
    */
   public void setSuccess (boolean success) {
      setSlotValue("success", success);
      engine.clearLive();  // not clearLivedAchieved (infinite loop)
   }
   
   protected Boolean checkAchieved () {
      Boolean success = isAchieved();
      if ( success != null ) {
         // if condition is unknown, then respect existing value of 'success'
         // slot, since it may come from communication
         if ( Utils.isFalse(success) ) setSuccess(false);
         else if ( getType().isSufficient() ) setSuccess(true);
      }
      return getSuccess();
   }
   
   /**
    * Get the value of the predefined 'external' input slot.
    *  
    * @return TRUE, FALSE or null
    */
   public Boolean getExternal () {
      return (Boolean) getSlotValue("external"); 
   }
   
   /**
    * Set the predefined 'external' input slot of this task.
    * 
    * @param external - note type boolean so converts to JavaScript boolean
    */
   public void setExternal (boolean external) {
      setSlotValue("external", external);
   }

   /**
    * Test whether this task <em>must</em> be executed by user.  It must be
    * be executed by the user iff the 'external' slot is true. 
    * Always returns false for non-primitive tasks.  
    *  <p>
    * Note that
    * both {@link #isUser()} and {@link #isSystem()} may return false, but
    * both may not return true.
    * 
    * @see #canUser()
    */
   public boolean isUser () {  return Utils.isTrue(getExternal()); }

   /**
    * Test whether this task <em>must</em> be executed by system. It must be
    * executed by system iff the 'external' slot is false. 
    * Always returns false for non-primitive tasks.
    * <p> 
    * Note that
    * both {@link #isUser()} and {@link #isSystem()} may return false, but 
    * both may not return true.
    * 
    * @see #canSystem()
    */
   public boolean isSystem () { return Utils.isFalse(getExternal()); } 

   /**
    * Test whether this task <em>can</em> be executed by user.  It can be
    * executed by user iff it is primitive and it is not true that it must 
    * be executed by system.  Always returns false for non-primitive tasks.  
    * <p>
    * Note that both {@link #canUser()} and {@link #canSystem()} 
    * may return true, but both may not return false.
    * 
    * @see #isUser()
    */
   public boolean canUser() { return !isSystem() && isPrimitive(); }
   
   /**
    * Test whether this task <em>can</em> be executed by system. It can be
    * executed by system iff it is primitive and it is not true that it must
    * be executed by user.  Always returns false for non-primitive tasks.    
    * <p> 
    * Note that both 
    * {@link #canUser()} and {@link #canSystem()} may return true, but both may 
    * not return false.
    * 
    * @see #isSystem()
    */
   public boolean canSystem() {  return !isUser() && isPrimitive(); }
   
   private boolean unexplained;
   
   /**
    * Test whether this task occurrence is explained (e.g., contributes) to
    * current discourse state.
    */
   public boolean isUnexplained () { return unexplained; }
   
   /**
    * Set whether this task occurrence is explained (e.g., contributes) to
    * current discourse state.@param unexplained
    */
   public void setUnexplained (boolean unexplained) { this.unexplained = unexplained; }

   /**
    * Return modifiable list of decomposition classes <em>applicable</em> to this task
    * and not rejected.
    * 
    * @see TaskClass#getDecompositions()
    */
   public List<DecompositionClass> getDecompositions () {
      List<DecompositionClass> decomps = new ArrayList<DecompositionClass>(
            getType().getDecompositions());
      for (Iterator<DecompositionClass> i = decomps.iterator(); i.hasNext();) {
         DecompositionClass next = i.next();
         // remove decompositions not applicable to or rejected for this task instance
         if ( rejected.contains(next) || Utils.isFalse(next.isApplicable(this)) )      
            i.remove();
      }
      return decomps; 
   }

   /**
    * Update engine state based on occurrence of this task.
    * 
    * @param contributes plan to which this occurrence has been found to contribute
    *                    or null if no such plan could be found
    * @param continuation true if contributes started before this task matched                   
    * @return false iff this task is unexplained
    */
   public boolean interpret (Plan contributes, boolean continuation) { 
      // continuation unused here but needed in Disco
      if ( contributes != null ) {
         contributes.match(this);
         return true;
      } else return false;
   }
   
   /**
    * Test whether given task instance matches this task instance. Two task
    * instances match (symmetric relationship) iff they are both instances of the
    * same type and the values of each corresponding slot are either equal or
    * one is undefined.
    * 
    * @see #copySlotValues(Task)
    */
   public boolean matches (Task goal) {
      if ( goal == this ) return true;
      if ( !getType().equals(goal.getType()) ) return false;
      for (String name : getType().getInputNames())
         if ( !matchesSlot(goal, name) ) return false;
      for (String name : getType().getOutputNames())
         if ( !matchesSlot(goal, name) ) return false;
      return true;
   }
   
   private boolean matchesSlot (Task goal, String name) {
      return !isDefinedSlot(name) || !goal.isDefinedSlot(name) 
        || Utils.equals(getSlotValue(name), goal.getSlotValue(name));
   }

   /**
    * Test whether this task either matches goal of given plan or could
    * be viewed as a part of the plan for given task, i.e., whether
    * given plan explains this task.  
    * <p>
    * This method is for use in extensions.  In CETask, it is equivalent 
    * to {@link #matches(Task)}.   
    */
   public boolean contributes (Plan plan) {
      return matches(plan.getGoal());
   }
   
   /**
    * Test whether this task can directly contribute to a task of given type.
    * (For extension to plan recognition.)
    */
   public boolean contributes (TaskClass type) {
      return getType() == type;
   }
   
   /**
    * Test whether this task can indirectly contribute to a task of given type.  Returns
    * false if {@link #contributes(TaskClass)} returns true.
    * (For extension to plan recognition.)
    */
   public boolean isPathFrom (TaskClass type) {
      return getType().isPathFrom(type);
   }
   
   /**
    * Copies the values of defined slots <em>from</em> given task to this task. Note this
    * side-effects this task, overwriting existing slot values.
    * 
    * @param from - task of same type
    * @return true if any slots of this task overwritten (were defined)
    * 
    * @see #matches(Task)
    */
   public boolean copySlotValues (Task from) {
      if ( !getType().equals(from.getType()) ) 
         throw new IllegalArgumentException("Cannot copy slot values from "+
               from.getType()+" to "+getType()); 
      boolean overwrite = false;
      for (String name : getType().getInputNames())
         overwrite = copySlotValue(from, name, name, true, false) || overwrite;
      for (String name : getType().getOutputNames())
         overwrite = copySlotValue(from, name, name, true, false) || overwrite;
      return overwrite;
   }
   
   /**
    * If named slot of given task is defined, copy value <em>from</em> 
    * slot name of given task to given slot of this task.
    *
    * @return true if slot of this task is overwritten (was defined)
    */
   public boolean copySlotValue (Task from, String fromSlot, String thisSlot) {
      return copySlotValue(from, fromSlot, thisSlot, true, true);
   }
   
   boolean copySlotValue (Task from, String fromSlot, String thisSlot, 
                          boolean onlyDefined, boolean check) {
      if ( TaskEngine.SCRIPTABLE ) {
         Object value = engine.get(from.bindings.get("$this"), fromSlot);
         checkCircular(thisSlot, value);
         if ( onlyDefined && !engine.isDefined(value) ) return false;
         if ( check ) checkSlotValue(getType().getSlotType(thisSlot), value);
         Object task = bindings.get("$this");
         Object thisValue = engine.get(task, thisSlot);
         boolean overwrite = engine.isDefined(thisValue);
         engine.put(task, thisSlot, value);
         modified = true;
         return overwrite;
      } else {
         if ( onlyDefined && !from.isDefinedSlot(fromSlot) ) return false;
         boolean overwrite = isDefinedSlot(thisSlot);
         setSlotValueScript(thisSlot, "$this."+fromSlot, "copySlotValue", 
               from.bindings);
         return overwrite;
      }
   }

   /**
    * Execute primitive <em>system</em> task. 
    * 
    * @see TaskEngine#done(Task)
    */
   void execute (Plan plan) {
      done(false); // before script executed
      eval(plan);
   }
   
   protected  void evalIf (Plan plan) { if ( isSystem() ) eval(plan); }
   
   protected void eval (Plan plan) {
      Script script = getScript();
      if ( script != null ) 
         synchronized (bindings) {
            try { 
               bindings.put("$plan", plan);
               script.eval(this);      
            } finally { bindings.remove("$plan"); }
         }
   }
   
   public void done (boolean external) {
      synchronized (engine.synchronizer) {
         setExternal(external); 
         done();
      }
   }
   
   public void done () {
      synchronized (engine.synchronizer) {
         setWhen(System.currentTimeMillis());
         engine.tick();
      }
   }

   /**
    * Return appropriate and applicable script to ground this task, or null if none.
    */
   public Script getScript () {
      TaskClass type = getType();
      List<Script> candidates = getTaskScripts();
      if ( candidates.isEmpty() ) { 
         // look for default script for model and then overall (must be toplevel)
         Script global = null;
         for (TaskModel model : engine.getModels()) {
            for (Script script : model.getScripts()) {
               if ( script.isInit() ) continue;
               TaskClass task = script.getTask();
               if ( task == null ) {
                  String ns = script.getModel();
                  // not checking for multiple global scripts
                  if ( ns == null ) global = script; 
                  else if ( ns.equals(type.getNamespace()) )
                     candidates.add(script);
               } else if ( task.equals(type) ) candidates.add(script);
            }
         }
         if ( candidates.isEmpty() && global != null )
            candidates.add(global);
      }
      if ( candidates.isEmpty() ) return null;
      if ( candidates.size() > 1 ) 
         getOut().println("Ignoring multiple scripts for "+this);
      Script script = (Script) candidates.get(0);
      return Utils.isFalse(script.isApplicable(this)) ? null : script;
   }
   
   private List<Script> getTaskScripts () {
      TaskClass type = getType();
      List<Script> candidates = new ArrayList<Script>(type.scripts);
      for (Iterator<Script> i = candidates.iterator(); i.hasNext();) {
         Script script = i.next();
         String platform = script.getPlatform(),
            deviceType = script.getDeviceType();
         // remove scripts not appropriate to current platform and deviceType
         if ( !( (platform == null || platform.equals(engine.getPlatform(this)))
                 && (deviceType == null || deviceType.equals(engine.getDeviceType(this))) ) )
            i.remove();
      }
      return candidates;
   }

   @Override
   protected String toStringVerbose () { 
      StringBuffer buffer = new StringBuffer(super.toStringVerbose()).append("={ ");
      boolean first = true;
      for (String name : getType().getInputNames()) 
         first = appendSlot(buffer, name, first);
      for (String name : getType().getOutputNames()) 
         if ( !name.equals("when") ) first = appendSlot(buffer, name, first);
      // handle 'when' specially (but not 'success')
      if ( TaskEngine.DEBUG && isDefinedSlot("when") ) {
         long start = engine.getStart();
         long when = getWhen();
         first = appendSlot(buffer, "when", 
               start == 0 ? 
                  DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM)
                     .format(new Date(when)) : 
                  Long.toString((when-start)/100), // tenth second accuracy enough
               first);
      }
      return buffer.append(" }").toString();
   }

   private boolean appendSlot (StringBuffer buffer, String name, boolean first) {
      return isDefinedSlot(name) ? 
         appendSlot(buffer, name, getSlotValueToString(name), first) :
            first;
   }
   
   private boolean appendSlot (StringBuffer buffer, String name, String value, 
         boolean first) {
      if ( !first ) buffer.append(", "); else first = false;
      buffer.append(name).append(':').append(value);
      return first;
   }
   
   /**
    * Return compact human-readable string for this task. 
    */
   public final String format () { return engine.format(this); }
   
   /**
    * Return compact human-readable string for this task.  For overriding
    * by extensions of Task. 
    */
   public String formatTask () { 
      return formatTask(isPrimitive() ? "execute@word" : "achieve@word");
   }
   
   protected String formatTask (String key) { 
      return formatTask(engine.getFormat(this), key);
   }

   protected String formatTask (String format, String key) { 
      TaskClass type = getType();
      List<String> inputs = type.getDeclaredInputNames(),
         outputs = type.getDeclaredOutputNames();
      int inputsSize = inputs.size(), outputsSize = outputs.size();
      if ( format != null ) {
         Object[] slots = new Object[inputsSize+outputsSize];
         int i = 0;
         for (String name : inputs) slots[i++] = formatSlot(name);
         for (String name : outputs) slots[i++] = formatSlot(name);
         return String.format(format, slots);
      } 
      // default formatting
      String and = engine.getProperty("and@word");
      StringBuffer buffer = new StringBuffer();
      if ( key != null ) 
         buffer.append(engine.getProperty(key)).append(' ');
      buffer.append(type)
        .append(formatSlots(inputs, inputsSize, "on@word", and))
        .append(formatSlots(outputs, outputsSize, "producing@word", and));
      return buffer.toString();
   }
   
   private String formatSlot (String name) {
      return formatSlot(name, isDefinedSlot(name));
   }
   
   protected String formatSlot (String name, boolean defined) {
      if ( !defined ) return getType().formatSlot(name, false);      
      Object value = getSlotValue(name);
      return value instanceof Task ? ((Task) value).formatTask() :
         getSlotValueToString(name);
   }
   
   private StringBuffer formatSlots (List<String> slots, int size, 
                                     String key, String and) {
      StringBuffer buffer = new StringBuffer();
      boolean firstDefined = false;
      // need to process slots backwards to drop trailing undefineds
      for (int i = size; i-- > 0;) {
         String name = slots.get(i);
         boolean defined = isDefinedSlot(name);
         if ( defined || firstDefined ) {
            firstDefined = true;
            buffer.insert(0, formatSlot(name));
            if ( i > 0 ) 
               buffer.insert(0, ' ').insert(0, and).insert(0, ' '); 
         }
      }
      if ( buffer.length() > 0 )
         buffer.insert(0, ' ').insert(0, engine.getProperty(key)).insert(0, ' ');
      return buffer;
   }
   
   public String getProperty (String key) { return getType().getProperty(key); }
   
   public Boolean getProperty (String key, Boolean defaultValue) { 
      return getType().getProperty(key, defaultValue);
   }
      
   public String getPropertySlot () { return null; }
   
   protected String getFormat () {
      String format = getProperty("@format"); 
      // allow empty string to undo format
      return format != null && format.length() > 0 ? format : null;
   }
   
   // code below here is for Disco extension--not used in CE Task engine
   
   private Boolean should;
   
   /**
    * Tests whether this task should be executed (default null).  
    * 
    * @return true if task has been accepted, false if rejected, otherwise null 
    */
   public Boolean getShould () { return should; }

   /**
    * Sets belief as to whether this task should be executed (default null).
    * 
    * @see #getShould()
    */
   public void setShould (Boolean should) { this.should = should; }
   
   private List<DecompositionClass> rejected = Collections.emptyList();
   
   /**
    * Return list of decomposition classes rejected for this task.
    * 
    * @see #reject(DecompositionClass)
    */
   public List<DecompositionClass> getRejected () { return rejected; }
   
   /**
    * Add given decomposition class to list of rejects for this task.
    * 
    * @see #getRejected()
    */
   public void reject (DecompositionClass decomp) {
      if ( rejected.isEmpty() ) rejected = new ArrayList<DecompositionClass>();
      rejected.add(decomp);
   }
}