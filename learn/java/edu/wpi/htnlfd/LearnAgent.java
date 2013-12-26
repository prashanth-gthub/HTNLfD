package edu.wpi.htnlfd;

import edu.wpi.disco.Agenda.Plugin;
import edu.wpi.disco.*;
import edu.wpi.disco.lang.*;
import edu.wpi.htnlfd.question.*;

public class LearnAgent extends Agent {

   public LearnAgent (String name) {
      super(name);
   }

   private boolean once = false;

   public Question question = null;

   public static void main (String[] args) {
      Interaction interaction = new Interaction(new LearnAgent("agent"),
            new User("user"), args.length > 0 && args[0].length() > 0 ? args[0]
               : null);
      Disco disco = interaction.getDisco();
      disco.importPackage("Packages.edu.wpi.htnlfd");
      disco.importPackage("Packages.edu.wpi.htnlfd.domain");
      disco.importPackage("Packages.edu.wpi.htnlfd.table");
      disco.importPackage("Packages.edu.wpi.htnlfd.carMaintenance");
      disco.eval("function find (value) { return value.find(); }",
            "LearnAgent.main()");
      interaction.start(true);
   }

   @Override
   public Plugin.Item respondIf (Interaction interaction, boolean guess) {
      if ( !once ) {
         // logic goes in if above to decide when to ask question based on state
         // of learned
         // model and Disco
         once = false;
         Disco disco = interaction.getDisco();
         if ( question instanceof AskAppCondition ) {
            return Agenda.newItem(
                  Propose.Should.newInstance(disco, true,
                        disco.getTaskClass("TellAppCondition").newInstance()),
                  null);
         }
         else if ( question instanceof AskRepeatedStep ) {
            return Agenda.newItem(
                  Propose.Should.newInstance(disco, true,
                        disco.getTaskClass("TellMaxOccurs").newInstance()),
                  null);
         }
         /*
          * case "TellInput": return Agenda.newItem(
          * Propose.Should.newInstance(disco, true,
          * disco.getTaskClass("TellInput").newInstance()), null); case
          * "TellOutput": return Agenda.newItem(
          * Propose.Should.newInstance(disco, true,
          * disco.getTaskClass("TellOutput").newInstance()), null); case
          * "TellPostcondition": return Agenda.newItem(
          * Propose.Should.newInstance(disco, true,
          * disco.getTaskClass("TellPostcondition").newInstance()), null); case
          * "TellPrecondition": return Agenda.newItem(
          * Propose.Should.newInstance(disco, true,
          * disco.getTaskClass("TellPrecondition").newInstance()), null); case
          * "TellOrdered": return Agenda.newItem(
          * Propose.Should.newInstance(disco, true,
          * disco.getTaskClass("TellOrdered").newInstance()), null);
          */

      }
      return super.respondIf(interaction, guess);
   }
   

}