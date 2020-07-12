package de.jeff_media.LumberJack;

import java.io.File;

public class FileUtils {

    public static void renameFileInPluginDir(LumberJack main,String oldName, String newName) {
        File oldFile = new File(main.getDataFolder().getAbsolutePath() + File.separator + oldName);
        File newFile = new File(main.getDataFolder().getAbsolutePath() + File.separator + newName);
        oldFile.getAbsoluteFile().renameTo(newFile.getAbsoluteFile());
    }
}
