package edu.wpi.disco;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import edu.wpi.cetask.ShellWindow;

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
