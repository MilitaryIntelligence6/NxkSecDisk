package kohgylw.kiftd.server.listener;

import kohgylw.kiftd.printer.Out;
import kohgylw.kiftd.server.mapper.FolderMapper;
import kohgylw.kiftd.server.util.*;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.File;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * <h2>服务器初始化监听器</h2>
 * <p>
 * 该监听器用于在服务器启动和关闭时执行一些必要的初始化或清理操作。
 * </p>
 *
 * @author 青阳龙野(kohgylw)
 * @version 1.0
 */
@WebListener
public class ServerInitListener implements ServletContextListener
{
    /**
     * 定时检查失效额外权限的周期，以毫秒为单位;
     */
    private static final int CYVLE_TIME = 30000;

    /**
     * 是否需要重新检查失效额外权限，当出现文件夹删除操作后，应将该属性置为true。
     */
    public static boolean needCheck;
    /**
     * 是否需要继续检查失效额外权限（用以控制检查的停止）
     */
    private static boolean continueCheck;

    /**
     * 用于在服务器启动后动态监听服务器主目录的线程，以便实现某些文件的动态更新
     */
    private Thread pathWatchServiceThread;

    /**
     * 用于在服务器启动后实时清理失效额外权限配置的线程，以便及时清理被删除的文件夹对应的额外权限配置
     */
    private Thread cleanInnvalidAddedAuthThread;

    /**
     * 是否继续监听服务器主目录下的文件改动（用以控制监听的停止）
     */
    private boolean run;

    /*
     *  一些必须用到的工具类（应确保在contextInitialized方法中初始化它们）
     */
    /**
     * 文件块操作工具
     */
    private FileBlockUtil fbu;

    /**
     * 公告信息解析工具
     */
    private NoticeUtil nu;

    /**
     * 文件夹映射表
     */
    private FolderMapper nm;

    /**
     * 日志记录工具
     */
    private LogUtil lu;

    @Override
    public void contextInitialized(final ServletContextEvent sce)
    {
        // 获取IOC容器，用于实例化一些必要的工具
        final ApplicationContext context = (ApplicationContext) WebApplicationContextUtils
                .getWebApplicationContext(sce.getServletContext());
        // 1，初始化文件节点数据库
        FileNodeUtil.initNodeTableToDataBase();
        // 2，校对文件块并清理临时文件夹
        Out.println("文件系统节点信息校对...");
        final String fsp = ConfigureReader.getInstance().getFileSystemPath();
        final File fspf = new File(fsp);
        if (fspf.isDirectory() && fspf.canRead() && fspf.canWrite())
        {
            fbu = context.getBean(FileBlockUtil.class);
            fbu.checkFileBlocks();
            fbu.initTempDir();
            Out.println("校对完成。");
        }
        else
        {
            Out.println("错误：文件系统节点信息校对失败，存储位置无法读写或不存在。");
        }
        // 3，解析公告信息（请确保该操作在校对文件块后进行）
        nu = context.getBean(NoticeUtil.class);
        nu.loadNotice();// 解析公告文件
        // 4，启动文件自动更新监听，对需要自动更新的文件进行实时更新
        doWatch();
        // 5，启动失效额外权限检查线程，对删除的文件夹对应的额外权限设置进行清理工作，避免堆积
        nm = context.getBean(FolderMapper.class);
        lu = context.getBean(LogUtil.class);
        cleanInvalidAddedAuth();
    }

    public void contextDestroyed(final ServletContextEvent sce)
    {
        // 1，关闭动态监听
        run = false;
        // 2，清理临时文件夹
        Out.println("清理临时文件...");
        fbu.initTempDir();
    }

    private void doWatch()
    {
        // 后期的动态监听部分
        run = true;
        // 之后当监听到改动操作时再重载内容
        if (pathWatchServiceThread == null)
        {
            // 对服务器主目录进行监听，主要监听文件改动事件
            Path confPath = Paths.get(ConfigureReader.getInstance().getPath());
            pathWatchServiceThread = new Thread(() ->
            {
                try
                {
                    while (run)
                    {
                        WatchService ws = confPath.getFileSystem().newWatchService();
                        confPath.register(ws, StandardWatchEventKinds.ENTRY_MODIFY,
                                StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_CREATE);
                        WatchKey wk = ws.take();
                        List<WatchEvent<?>> es = wk.pollEvents();
                        for (WatchEvent<?> we : es)
                        {
                            // 根据改动文件的不同调用不同的处理方法
                            switch (we.context().toString())
                            {
                                case NoticeUtil.NOTICE_FILE_NAME:
                                    nu.loadNotice();// 更新公告文件
                                    break;

                                default:
                                    break;
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                    Out.println("错误：服务器文件自动更新失败，该功能已失效。某些文件将无法自动载入最新内容（请尝试重启程序以恢复该功能）。");
                }
            });
            pathWatchServiceThread.start();
        }
    }

    private void cleanInvalidAddedAuth()
    {
        needCheck = true;
        continueCheck = true;
        if (cleanInnvalidAddedAuthThread == null)
        {
            cleanInnvalidAddedAuthThread = new Thread(() ->
            {
                while (continueCheck)
                {
                    if (needCheck)
                    {
                        List<String> invalidIdList = new ArrayList<>();
                        List<String> idList = ConfigureReader.getInstance().getAllAddedAuthFoldersId();
                        for (String id : idList)
                        {
                            if (nm.queryById(id) == null)
                            {
                                invalidIdList.add(id);
                                Out.println("文件夹ID：" + id + "对应的文件夹不存在或已被删除，相关的额外权限设置将被清理。");
                            }
                        }
                        if (ConfigureReader.getInstance().removeAddedAuthByFolderId(invalidIdList))
                        {
                            Out.println("失效的额外权限设置已经清理完成。");
                        }
                        needCheck = false;
                    }
                    try
                    {
                        Thread.sleep(CYVLE_TIME);
                    }
                    catch (InterruptedException e)
                    {
                        continueCheck = false;
                        lu.writeException(e);
                    }
                }
            });
            cleanInnvalidAddedAuthThread.start();
        }
    }
}
