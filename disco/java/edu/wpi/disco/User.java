/* Copyright (c) 2009 Charles Rich and Worcester Polytechnic Institute.
 * All Rights Reserved.  Use is subject to license terms.  See the file
 * "license.terms" for information on usage and redistribution of this
 * file and for a DISCLAIMER OF ALL WARRANTIES.
 */
package edu.wpi.disco;

import java.util.List;

import edu.wpi.disco.Agenda.Plugin;
import edu.wpi.disco.lang.Ok;
import edu.wpi.disco.lang.Propose;
import edu.wpi.disco.plugin.DecompositionPlugin;
import edu.wpi.disco.plugin.ImplicitAcceptPlugin;
import edu.wpi.disco.plugin.ProposeGlobalPlugin;
import edu.wpi.disco.plugin.ProposeHowPlugin;
import edu.wpi.disco.plugin.ProposeShouldNotPlugin;
import edu.wpi.disco.plugin.ProposeShouldOptionalPlugin;
import edu.wpi.disco.plugin.ProposeShouldOtherPlugin;
import edu.wpi.disco.plugin.ProposeShouldSelfPlugin;
import edu.wpi.disco.plugin.ProposeWhatPlugin;
import edu.wpi.disco.plugin.ProposeWhoPlugin;
import edu.wpi.disco.plugin.RejectProposeWhatPlugin;
import edu.wpi.disco.plugin.RespondPlugin;
import edu.wpi.disco.plugin.TopsPlugin;
import edu.wpi.disco.plugin.UtterancePlugin;

public class User extends Actor {
  
   // TTSay plugins
   public User (String name) {
      super(name);
      new RespondPlugin.Accept(agenda, 120);
      new UtterancePlugin(agenda, 100, true); // excludeAcceptShould 
      new ProposeGlobalPlugin(agenda, 95);
      new ProposeShouldSelfPlugin(agenda, 90, false);
      new ProposeShouldOtherPlugin(agenda, 70);
      new ProposeWhoPlugin(agenda, 50);
      new ProposeWhatPlugin(agenda, 50);
      new RejectProposeWhatPlugin(agenda, 45); // after ProposeWhatPlugin (re enumerations)
      new RespondPlugin.Reject(agenda, 30);
      new ProposeHowPlugin(agenda, 30);
      new DecompositionPlugin(agenda, 25, true, true); // suppressFormatted, focusOnly
      new ImplicitAcceptPlugin(agenda, 25);
      new ProposeShouldOptionalPlugin(agenda, 20);
      new ProposeShouldNotPlugin(agenda, 5);
      new TopsPlugin(agenda, 0, true); // interrupt (note lowest priority)
   }
   
   /**
    * Generate list of items for user utterance menu (TTSay).
    * 
    * Note interaction@ok property (default true) controls whether instance of Ok added
    * when menu is either empty of consists exclusively of Propose.ShouldNot
    * utterances.  This behavior is useful in system with rigid turn-taking,
    * where the user has to select something in order to return control to the
    * agent.  
    * 
    * @see Interaction#isOk()
    */
   @Override
   public List<Plugin.Item> generate (Interaction interaction) {
      List<Plugin.Item> items = super.generate(interaction);
      Disco disco = interaction.getDisco();
      if ( disco.getProperty("interaction@ok", interaction.isOk()) ) {
         boolean ok = true;
         for (Plugin.Item item : items)
            if ( !(item.task instanceof Propose.ShouldNot) ) {
               ok = false;
               break;
            }
         if ( ok ) items.add(0, Agenda.newItem(new Ok(disco, true), null));
      }
      return items;
   }
 
   @Override // default definition of dummy user (using console)
   protected boolean synchronizedRespond (Interaction interaction, boolean ok, boolean guess) {
      return true;
   }
}
            
     
