package edu.swufe.nxksecdisk.server.util;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

/**
 * <h2>时间相关处理工具</h2>
 * <p>
 * 该工具提供获取服务器时间等与时间相关的操作方法，全部为静态的，本身无需实例化。
 * </p>
 *
 * @author 青阳龙野(kohgylw)
 * @version 1.0
 */
public final class ServerTimeUtil {

    private ServerTimeUtil() {
    }

    public static String accurateToSecond() {
        return accurateFormat("yyyy年MM月dd日 HH:mm:ss");
    }

    public static String accurateToMinute() {
        return accurateFormat("yyyy年MM月dd日 HH:mm");
    }

    public static String accurateToDay() {
        return accurateFormat("yyyy年MM月dd日");
    }

    public static String accurateToLogName() {
        return accurateFormat("yyyy_MM_dd");
    }

    private static String accurateFormat(String format) {
        return DateTimeFormatter
                .ofPattern(format)
                .format(LocalDateTime.now());
    }

    public static Date serverTime() {
        return new Date();
    }

    /**
     * <h2>从文件块生成“最后修改时间”标签</h2>
     * <p>
     * 该方法用于生成符合HTTP规范的Last-Modified格式时间截，用于判定资源的最后修改日期。
     * </p>
     *
     * @param block java.io.File 要生成的文件块对象，应该是文件，但也支持文件夹，或者null
     * @return java.lang.String 记录最后修改日期的时间截，格式类似于“Wed, 29 Apr 2020 08:18:43
     * GMT”，若传入文件不存在或为null，则返回当前时间
     * @author 青阳龙野(kohgylw)
     */
    public static String getLastModifiedFormBlock(File block) {
        ZonedDateTime longToTime;
        if (block != null && block.exists()) {
            longToTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(block.lastModified()), ZoneId.of("GMT"));
        }
        else {
            longToTime = ZonedDateTime.now(ZoneId.of("GMT"));
        }
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z", Locale.US)
                .withZone(ZoneId.of("GMT"));
        return longToTime.format(dtf).trim();
    }
}
