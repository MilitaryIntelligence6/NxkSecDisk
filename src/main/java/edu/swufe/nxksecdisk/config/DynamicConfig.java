package edu.swufe.nxksecdisk.config;

import edu.swufe.nxksecdisk.constant.EnumLauncherMode;

/**
 * @author Military Intelligence 6 root
 * @version 1.0.0
 * @ClassName DynamicConfig
 * @Description TODO
 * @CreateTime 2021年03月28日 13:00:00
 */
public final class DynamicConfig {

    private static EnumLauncherMode launcherMode = EnumLauncherMode.CONSOLE;

    public static EnumLauncherMode getLauncherMode() {
        return launcherMode;
    }

    public static void setLauncherMode(EnumLauncherMode launcherMode) {
        DynamicConfig.launcherMode = launcherMode;
    }
}
