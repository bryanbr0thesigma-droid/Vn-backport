package com.vnap.config;

import com.vnap.VillagerNewsAddonPort;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class VillagerNewsBuildSettings {
   private static final boolean DIALOGUE_TEST_COMMAND = loadDialogueTestCommand();

   private VillagerNewsBuildSettings() {
   }

   public static boolean dialogueTestCommand() {
      return DIALOGUE_TEST_COMMAND;
   }

   private static boolean loadDialogueTestCommand() {
      Properties properties = new Properties();

      try {
         boolean var2;
         try (InputStream stream = VillagerNewsBuildSettings.class.getResourceAsStream("/villager-news-addon-port-build.properties")) {
            if (stream == null) {
               return false;
            }

            properties.load(stream);
            var2 = Boolean.parseBoolean(properties.getProperty("dialogue_test_command", "false"));
         }

         return var2;
      } catch (IOException var6) {
         VillagerNewsAddonPort.LOGGER.warn("Could not load Villager News build settings", var6);
         return false;
      }
   }
}
