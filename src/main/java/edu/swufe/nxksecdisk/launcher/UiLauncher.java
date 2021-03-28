package edu.swufe.nxksecdisk.launcher;

import com.formdev.flatlaf.FlatDarculaLaf;
import edu.swufe.nxksecdisk.config.DynamicConfig;
import edu.swufe.nxksecdisk.constant.EnumLauncherMode;
import edu.swufe.nxksecdisk.server.app.DiskAppController;
import edu.swufe.nxksecdisk.server.enumeration.LogLevel;
import edu.swufe.nxksecdisk.server.enumeration.VcLevel;
import edu.swufe.nxksecdisk.server.pojo.ExtendStores;
import edu.swufe.nxksecdisk.server.util.ConfigureReader;
import edu.swufe.nxksecdisk.server.util.ServerTimeUtil;
import edu.swufe.nxksecdisk.ui.callback.GetServerStatus;
import edu.swufe.nxksecdisk.ui.module.ServerUiModule;
import edu.swufe.nxksecdisk.ui.pojo.FileSystemPath;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * <h2>UI界面模式启动器</h2>
 * <p>
 * 该启动器将以界面模式启动kiftd，请执行静态build()方法开启界面并初始化kiftd服务器引擎。
 * </p>
 *
 * @author 青阳龙野(kohgylw)
 * @version 1.0
 */
public class UiLauncher {

    private volatile static UiLauncher instance;

    /**
     * 实例化图形界面并显示它，同时将图形界面的各个操作与服务器控制器对应起来;
     *
     * @throws Exception
     */
    private UiLauncher() throws Exception {
        initSkin();
        DynamicConfig.setLauncherMode(EnumLauncherMode.UI);
        final ServerUiModule serverUi = ServerUiModule.getInstance();
        // 服务器控制层，用于连接UI与服务器内核;
        DiskAppController appController = new DiskAppController();
        ServerUiModule.setStartServer(appController::start);
        ServerUiModule.setOnCloseServer(appController::stop);
        ServerUiModule.setGetServerTime(ServerTimeUtil::serverTime);
        ServerUiModule.setGetServerStatus(new GetServerStatus() {
            @Override
            public boolean getServerStatus() {
                // TODO 自动生成的方法存根
                return appController.started();
            }

            @Override
            public int getPropertiesStatus() {
                // TODO 自动生成的方法存根
                return ConfigureReader.getInstance().getPropertiesStatus();
            }

            @Override
            public int getPort() {
                // TODO 自动生成的方法存根
                return ConfigureReader.getInstance().getPort();
            }

            @Override
            public boolean getMustLogin() {
                // TODO 自动生成的方法存根
                return ConfigureReader.getInstance().mustLogin();
            }

            @Override
            public LogLevel getLogLevel() {
                // TODO 自动生成的方法存根
                return ConfigureReader.getInstance().getLogLevel();
            }

            @Override
            public String getFileSystemPath() {
                // TODO 自动生成的方法存根
                return ConfigureReader.getInstance().getFileSystemPath();
            }

            @Override
            public int getBufferSize() {
                // TODO 自动生成的方法存根
                return ConfigureReader.getInstance().getBuffSize();
            }

            @Override
            public VcLevel getVCLevel() {
                // TODO 自动生成的方法存根
                return ConfigureReader.getInstance().getVCLevel();
            }

            @Override
            public List<FileSystemPath> getExtendStores() {
                List<FileSystemPath> fileSystemPathList = new ArrayList<>();
                for (ExtendStores es : ConfigureReader.getInstance().getExtendStores()) {
                    FileSystemPath fileSystemPath = new FileSystemPath();
                    fileSystemPath.setIndex(es.getIndex());
                    fileSystemPath.setPath(es.getPath());
                    fileSystemPath.setType(FileSystemPath.EXTEND_STORES_NAME);
                    fileSystemPathList.add(fileSystemPath);
                }
                return fileSystemPathList;
            }

            @Override
            public LogLevel getInitLogLevel() {
                // TODO 自动生成的方法存根
                return ConfigureReader.getInstance().getInitLogLevel();
            }

            @Override
            public VcLevel getInitVCLevel() {
                // TODO 自动生成的方法存根
                return ConfigureReader.getInstance().getInitVCLevel();
            }

            @Override
            public String getInitFileSystemPath() {
                // TODO 自动生成的方法存根
                return ConfigureReader.getInstance().getInitFileSystemPath();
            }

            @Override
            public String getInitProt() {
                // TODO 自动生成的方法存根
                return ConfigureReader.getInstance().getInitPort();
            }

            @Override
            public String getInitBufferSize() {
                // TODO 自动生成的方法存根
                return ConfigureReader.getInstance().getInitBuffSize();
            }

            @Override
            public boolean isAllowChangePassword() {
                // TODO 自动生成的方法存根
                return ConfigureReader.getInstance().isAllowChangePassword();
            }

            @Override
            public boolean isOpenFileChain() {
                // TODO 自动生成的方法存根
                return ConfigureReader.getInstance().isOpenFileChain();
            }

            @Override
            public int getMaxExtendStoresNum() {
                // TODO 自动生成的方法存根
                return ConfigureReader.getInstance().getMaxExtendstoresNum();
            }
        });
        ServerUiModule.setUpdateSetting(s ->
        {
            // TODO 自动生成的方法存根
            return ConfigureReader.getInstance().doUpdate(s);
        });
        serverUi.show();
    }

    public static UiLauncher getInstance() throws Exception {
        if (instance == null) {
            synchronized (UiLauncher.class) {
                if (instance == null) {
                    instance = new UiLauncher();
                }
            }
        }
        return instance;
    }

    /**
     * <h2>以UI模式运行kiftd</h2>
     * <p>
     * 执行该方法后，kiftd将立即显示服务器主界面（需要操作系统支持图形界面）并初始化服务器引擎，等待用户点击按钮并触发相应的操作。
     * 该方法将返回本启动器的唯一实例。
     * </p>
     *
     * @return kohgylw.kiftd.mc.UIRunner 本启动器唯一实例
     * @throws Exception
     * @author 青阳龙野(kohgylw)
     */
    public static UiLauncher build() throws Exception {
        return getInstance();
    }

    private void initSkin() {
        try {
            UIManager.setLookAndFeel(new FlatDarculaLaf());
        }
        catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
    }
}
