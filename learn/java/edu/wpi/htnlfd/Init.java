package edu.wpi.htnlfd;

import java.util.*;
import edu.wpi.cetask.*;
import edu.wpi.disco.*;
import edu.wpi.disco.lang.*;

public class Init {

   public static void main (String[] args) {
      System.out.println("YUHU");
   }

   public static void demonstration (Disco disco, String taskName) {
      System.out.println(taskName);
      Segment parent = disco.getStack().get(0);
      for (Iterator<Object> children = parent.children(); children.hasNext();) {
         Object child = children.next();
         if ( child instanceof Task ) {
            Task task = (Task) child;
            if ( !(task instanceof Utterance) ) {
               System.out.println(task.getType());
            }
         }
      }
   }
}
