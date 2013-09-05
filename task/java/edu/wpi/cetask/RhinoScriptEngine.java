/* Copyright (c) 2008 Charles Rich and Worcester Polytechnic Institute.
 * All Rights Reserved.  Use is subject to license terms.  See the file
 * "license.terms" for information on usage and redistribution of this
 * file and for a DISCLAIMER OF ALL WARRANTIES.
 */
package edu.wpi.cetask;

import javax.script.*;

// simple wrapper for Rhino script engine include in JDK

class RhinoScriptEngine extends ScriptEngineWrapper 
                        implements Invocable, Compilable {
   
   private final ScriptEngine rhino;
   
   public RhinoScriptEngine (ScriptEngine rhino) {
      this.rhino = rhino;
   }

   private enum Platform { OSX, Windows, Ubuntu }
   private static Platform platform;
   private static Object notFound, undefined;
   
   static {
      // note lib/js.jar contains classes for compilation on all platforms
      // but not execution classes
      try { 
         undefined = sun.org.mozilla.javascript.internal.Context.getUndefinedValue();
         notFound = sun.org.mozilla.javascript.internal.Scriptable.NOT_FOUND;
         platform = Platform.OSX;
      } catch (Throwable e1) {
         try { 
            undefined = org.mozilla.javascript.Context.getUndefinedValue();
            notFound = org.mozilla.javascript.Scriptable.NOT_FOUND;
            platform = Platform.Windows;
         } catch (Throwable e2) {
            try { 
               undefined = sun.org.mozilla.javascript.Context.getUndefinedValue();
               notFound = sun.org.mozilla.javascript.Scriptable.NOT_FOUND;
               platform = Platform.Ubuntu;
            } catch (Throwable e3) {}
         }
      }
   }
   @Override
   boolean isScriptable () { return platform != null; }
   
   @Override
   boolean isScriptable (Object value) {
      switch (platform) {
      case OSX: return value instanceof sun.org.mozilla.javascript.internal.Scriptable;
      case Windows: return value instanceof org.mozilla.javascript.Scriptable;
      case Ubuntu: return value instanceof sun.org.mozilla.javascript.Scriptable;
      default: return false;
      }
   }
   
   @Override
   boolean isDefined (Object value) {
      return !( value == notFound || value == undefined );
   }
   
   @Override
   Object undefined () { return undefined; }
   
   @Override
   Object get (Object object, String field) {
      switch (platform) {
      case OSX:
         sun.org.mozilla.javascript.internal.Scriptable scriptable1 = 
         (sun.org.mozilla.javascript.internal.Scriptable) object;
         return scriptable1.get(field, scriptable1);
      case Windows:
         org.mozilla.javascript.Scriptable scriptable2= 
         (org.mozilla.javascript.Scriptable) object;
         return scriptable2.get(field, scriptable2);
      case Ubuntu:
         sun.org.mozilla.javascript.Scriptable scriptable3= 
         (sun.org.mozilla.javascript.Scriptable) object;
         return scriptable3.get(field, scriptable3);
      default: throw new IllegalArgumentException("Cannot perform get on "+object);
      }
   }
   
   @Override
   void put (Object object, String field, Object value) {
      switch (platform) {
      case OSX:
         sun.org.mozilla.javascript.internal.Scriptable scriptable1 = 
            (sun.org.mozilla.javascript.internal.Scriptable) object;
         scriptable1.put(field, scriptable1, value);
         break;
      case Windows:
         org.mozilla.javascript.Scriptable scriptable2 = 
            (org.mozilla.javascript.Scriptable) object;
         scriptable2.put(field, scriptable2, value);
         break;
      case Ubuntu:
         sun.org.mozilla.javascript.Scriptable scriptable3 = 
            (sun.org.mozilla.javascript.Scriptable) object;
         scriptable3.put(field, scriptable3, value);
         break;
      default: throw new IllegalArgumentException("Cannot perform put on "+object);
      }
   }
   
   @Override
   public Bindings getBindings (int scope) { return rhino.getBindings(scope); }
   
   @Override
   public ScriptContext getContext () { return rhino.getContext(); }

   @Override
   public void setContext (ScriptContext context) { rhino.setContext(context); }

   @Override
   public Object eval (String script, Bindings bindings) throws ScriptException {
      return rhino.eval(script, bindings);
   }

   @Override
   public Object eval (String script) throws ScriptException {
      return rhino.eval(script);
   }
   
   @Override
   public Boolean evalBoolean (String script, Bindings bindings) 
                  throws ScriptException {
      return (Boolean) eval(script, bindings);
   }

   @Override
   public Double evalDouble (String script, Bindings bindings) 
                  throws ScriptException {
      return (Double) eval(script, bindings);
   }
   
   @Override
   public Compiled compile (String script) throws ScriptException {
      return new RhinoCompiled(script);
   }

   private class RhinoCompiled extends Compiled {

      private final CompiledScript compiled;

      public RhinoCompiled (String script) throws ScriptException {
         compiled = ((Compilable) rhino).compile(script);
      }

      @Override
      public Object eval (Bindings bindings) throws ScriptException {
         synchronized (bindings) { return compiled.eval(bindings); }
      }
      
      @Override
      public Boolean evalBoolean (Bindings bindings) throws ScriptException {
         return (Boolean) eval(bindings);
      }
   }
   
   @Override
   public Object invokeFunction (String name, Object... args)
         throws ScriptException, NoSuchMethodException {
      return ((Invocable) rhino).invokeFunction(name, args);
   }

}
