package edu.wpi.htnlfd;

import edu.wpi.disco.Agenda.Plugin;
import edu.wpi.disco.Agent;
import edu.wpi.disco.Interaction;
import edu.wpi.disco.User;
import edu.wpi.disco.lang.Say;

public class LearnAgent extends Agent {
   
   public LearnAgent (String name) { super(name); }
   
   private boolean once = false;
   
   public static void main (String[] args) {
      new Interaction(
            new LearnAgent("agent"), 
            new User("user"),
            args.length > 0 && args[0].length() > 0 ? args[0] : null)
      .start(true); 
   }
   
   @Override
   public Plugin.Item respondIf (Interaction interaction, boolean guess) {
      if ( !once ) { 
         // logic goes in if above to decide when to ask question based on state of learned
         // model and Disco
         once = true;
         
         return Agenda.newItem(new Say(interaction.getDisco(), false, "What is the name of altrenative recipe?"), null);
      }
      return super.respondIf(interaction, guess);
    }

}