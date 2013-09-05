/* Copyright (c) 2008 Charles Rich and Worcester Polytechnic Institute.
 * All Rights Reserved.  Use is subject to license terms.  See the file
 * "license.terms" for information on usage and redistribution of this
 * file and for a DISCLAIMER OF ALL WARRANTIES.
 */
package edu.wpi.cetask;

import org.w3c.dom.Node;

import java.io.PrintStream;
import java.util.*;

import javax.xml.namespace.QName;
import javax.xml.xpath.*;

public class TaskModel extends Description {
   
   TaskModel (Node node, TaskEngine engine, XPath xpath) { 
      super(node, engine, xpath);     
   }
   
   Map<String,TaskClass> tasks = new HashMap<String,TaskClass>();
   
   public Collection<TaskClass> getTaskClasses () { return tasks.values(); }
   
   public TaskClass getTaskClass (String id) { return tasks.get(id); }

   List<Script> scripts = Collections.emptyList();
   
   public List<Script> getScripts () { return scripts; }
   
   @Override
   public String toString () { return getNamespace(); }
   
   private final Properties properties = new Properties(engine.properties);
   
   public Properties getProperties () { return properties; }
   
   public String getProperty (String key) {
      String property = properties.getProperty(key);
      return property == null ? null : property.trim();  // remove accidental trailing spaces
   }
   
   public boolean getProperty (String key, boolean defaultValue) {
      String value = getProperty(key);
      return value == null ? defaultValue : Utils.parseBoolean(value);
   }
   
   public void setProperty (String key, String value) {
      properties.setProperty(key,value);
   }
   
   public static class Error extends RuntimeException {
      public Error (Description description, String error) { 
         super(description+": "+error);
      }
   }
   
   private final List<String> ids = new ArrayList<String>();
   
   /**
    * Base class for members of task model with id's
    */
   abstract class Member extends Description {
      
      final private String id; 
      final private QName qname;
      
      /**
       * @return the raw id string from the definition of this member.
       * 
       * @see #getQName()
       */
      public String getId () { return id; }


      /**
       * @return the qualified name of this member
       */
      public QName getQName () { return qname; }
      
      protected Member (Node node, XPath xpath) { 
         super(node, TaskModel.this.engine, xpath);
         String id = xpath("./@id");
         this.id = id.length() > 0 ? id : "**ROOT**";
         qname = new QName(getNamespace(), id);
         ids.add(id);
      }
      
      public TaskModel getModel () { return TaskModel.this; }   
      
      public String getPropertyId () {
         return (id.startsWith("edu.wpi.disco.lang.") ?
            // spare users typing this prefix in properties files
            id.substring(19) : id).replace('$', '.');
      }
      
      public String getProperty (String key) {
         return properties.getProperty(getPropertyId()+key);
      }
      
      public Boolean getProperty (String key, Boolean defaultValue) {
         String value = getProperty(key);
         return value == null ? (Boolean) defaultValue : 
            (Boolean) Utils.parseBoolean(value);
      }
      
      public void setProperty (String key, String value) {
         properties.setProperty(getPropertyId()+key, value);
      }
    
      @Override
      public boolean equals (Object object) {
         return object instanceof Member
           // works across different engines
           && namespace.equals(((Member) object).getNamespace()) 
           && id.equals(((Member) object).getId());
      }
      
      @Override
      public int hashCode () { return node.hashCode(); }
      
      /**
       * A task or decomposition class is internal if its id starts with an underscore.  
       * Can be overridden with @internal property.
       * Internal types are suppressed in various places, such as history printing,
       * ttsay, etc.  Also the default key values that control plugins, such
       * as @ProposeHow are false for internal tasks, rather than true.
       */
      public boolean isInternal () { 
         return getProperty("@internal", getId().charAt(0) == '_');
      }
      
      protected PrintStream getOut () { return engine.getOut(); }
      
      protected PrintStream getErr () { return engine.getErr(); }


   }
  
}
