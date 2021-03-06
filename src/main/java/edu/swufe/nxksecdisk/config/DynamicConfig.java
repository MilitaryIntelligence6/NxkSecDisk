package edu.swufe.nxksecdisk.config;

import edu.swufe.nxksecdisk.constant.EnumLaunchMode;

/**
 * @author Military Intelligence 6 root
 * @version 1.0.0
 */
public final class DynamicConfig {

    private static EnumLaunchMode launcherMode = EnumLaunchMode.CONSOLE;

    public static EnumLaunchMode getLauncherMode() {
        return launcherMode;
    }

    public static void setLauncherMode(EnumLaunchMode launcherMode) {
        DynamicConfig.launcherMode = launcherMode;
    }
}
