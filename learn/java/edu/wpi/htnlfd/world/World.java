package edu.wpi.htnlfd.world;

import java.io.PrintStream;
import java.util.Iterator;

public abstract class World {
   
   public interface Printable {  void print (PrintStream stream); }

   

   public abstract void print (PrintStream stream);

}
