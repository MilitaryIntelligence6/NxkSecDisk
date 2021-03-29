package edu.swufe.nxksecdisk.launcher;

import com.formdev.flatlaf.FlatDarculaLaf;
import edu.swufe.nxksecdisk.config.DynamicConfig;
import edu.swufe.nxksecdisk.constant.EnumLauncherMode;
import edu.swufe.nxksecdisk.server.app.DiskAppController;
import edu.swufe.nxksecdisk.server.enumeration.LogLevel;
import edu.swufe.nxksecdisk.server.enumeration.VcLevel;
import edu.swufe.nxksecdisk.server.pojo.ExtendStores;
import edu.swufe.nxksecdisk.server.util.ConfigReader;
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
     */
    private UiLauncher() {
        initSkin();
        DynamicConfig.setLauncherMode(EnumLauncherMode.UI);
        final ServerUiModule serverUi = ServerUiModule.getInstance();
        // 服务器控制层，用于连接UI与服务器内核;
        DiskAppController appController = new DiskAppController();
        ServerUiModule.setStartServer(appController::start);
        ServerUiModule.setOnCloseServer(appController::stop);
        ServerUiModule.setGetServerTime(ServerTimeUtil::serverTime);
        // 这个别放在类中, 因为类加载时机会加载ConfigReader, 其识别了ui模式和控制台模式;
        // 初始化时模式为控制台模式, 会在控制台打印信息, 所以要放在这里;
        final ConfigReader config = ConfigReader.getInstance();

        ServerUiModule.setGetServerStatus(new GetServerStatus() {
            @Override
            public boolean getServerStatus() {
                return appController.started();
            }

            @Override
            public int getPropertiesStatus() {
                return config.getPropertiesStatus();
            }

            @Override
            public int getPort() {
                return config.getPort();
            }

            @Override
            public boolean getMustLogin() {
                return config.mustLogin();
            }

            @Override
            public LogLevel getLogLevel() {
                return config.getLogLevel();
            }

            @Override
            public String getFileSystemPath() {
                return config.getFileSystemPath();
            }

            @Override
            public int getBufferSize() {
                return config.getBuffSize();
            }

            @Override
            public VcLevel getVCLevel() {
                return config.getVCLevel();
            }

            @Override
            public List<FileSystemPath> getExtendStores() {
                List<FileSystemPath> fileSystemPathList = new ArrayList<>();
                for (ExtendStores es : config.getExtendStores()) {
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
                return config.getInitLogLevel();
            }

            @Override
            public VcLevel getInitVCLevel() {
                return config.getInitVCLevel();
            }

            @Override
            public String getInitFileSystemPath() {
                return config.getInitFileSystemPath();
            }

            @Override
            public String getInitProt() {
                return config.getInitPort();
            }

            @Override
            public String getInitBufferSize() {
                return config.getInitBuffSize();
            }

            @Override
            public boolean isAllowChangePassword() {
                return config.isAllowChangePassword();
            }

            @Override
            public boolean isOpenFileChain() {
                return config.isOpenFileChain();
            }

            @Override
            public int getMaxExtendStoresNum() {
                return config.getMaxExtendstoresNum();
            }
        });
        ServerUiModule.setUpdateSetting(config::doUpdate);
        serverUi.show();
    }

    public static UiLauncher getInstance() {
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
    public static UiLauncher build() {
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
