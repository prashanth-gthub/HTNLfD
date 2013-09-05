package edu.wpi.disco;

import edu.wpi.cetask.ShellWindow;

import java.awt.event.*;

public class ConsoleWindow extends ShellWindow {

   public ConsoleWindow (final Interaction interaction, int width, int height, int fontSize) {
      super(new Console(null, interaction), width, height, fontSize);
      setTitle("Disco");
      appendOutput("\n");
      interaction.setDaemon(true);
      addWindowListener(new WindowAdapter() {
         @Override
         public void windowClosed (WindowEvent e) { interaction.interrupt(); }
      });
      interaction.start(true);
   }

}
