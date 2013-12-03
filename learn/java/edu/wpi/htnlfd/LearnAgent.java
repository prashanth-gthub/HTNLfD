package edu.wpi.htnlfd;

import edu.wpi.disco.Agenda.Plugin;
import edu.wpi.disco.*;
import edu.wpi.disco.lang.Say;

public class LearnAgent extends Agent {
   
   public LearnAgent (String name) { super(name); }
   
   private boolean once = true;
   
   public static void main (String[] args) {
      Interaction interaction = new Interaction(
            new LearnAgent("agent"), 
            new User("user"),
            args.length > 0 && args[0].length() > 0 ? args[0] : null);
      Disco disco = interaction.getDisco();
      disco.importPackage("Packages.edu.wpi.htnlfd");
      disco.importPackage("Packages.edu.wpi.htnlfd.table");
      disco.importPackage("Packages.edu.wpi.htnlfd.carMaintenance");
      disco.eval("function find (value) { return value.find(); }", "LearnAgent.main()");
      interaction.start(true); 
   }
   
   @Override
   public Plugin.Item respondIf (Interaction interaction, boolean guess) {
      if ( !once ) { 
         // logic goes in if above to decide when to ask question based on state of learned
         // model and Disco
         once = false;
         
         return Agenda.newItem(new Say(interaction.getDisco(), false, "What is the name of altrenative recipe?"), null);
      }
      return super.respondIf(interaction, guess);
    }

}