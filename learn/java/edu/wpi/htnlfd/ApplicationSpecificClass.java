package edu.wpi.htnlfd;

public abstract class ApplicationSpecificClass {

   /**
    * returns Javascript expression that evaluates to 
    * an object that is equal to this instance of class
    */
   public abstract String find ();
   
   // forces developer to override and think about these methods
   // even though inherited from Object
   
   public abstract int hashCode ();
   public abstract boolean equals (Object obj);
   public abstract String toString ();
   
}