package edu.swufe.nxksecdisk.ui.module;

import edu.swufe.nxksecdisk.server.enumeration.LogLevel;
import edu.swufe.nxksecdisk.server.enumeration.VcLevel;
import edu.swufe.nxksecdisk.server.pojo.ExtendStores;
import edu.swufe.nxksecdisk.server.pojo.ServerSetting;
import edu.swufe.nxksecdisk.system.AppSystem;
import edu.swufe.nxksecdisk.ui.callback.GetServerStatus;
import edu.swufe.nxksecdisk.ui.callback.UpdateSetting;
import edu.swufe.nxksecdisk.ui.pojo.FileSystemPath;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * <h2>界面模块——设置</h2>
 * <p>
 * 设置界面类，负责图形化界面下的设置界面显示。
 * </p>
 *
 * @author 青阳龙野(kohgylw)
 * @version 1.0
 */
public class SettingWindow extends DiskDynamicWindow {

    private volatile static SettingWindow instance;

    private final ServerUiModule serverUi = ServerUiModule.getInstance();

    private JDialog window;

    private JTextField portInput;

    private JTextField bufferInput;

    private JComboBox<String> mlInput;

    private JComboBox<String> vcInput;

    private JComboBox<String> logLevelInput;

    private JComboBox<String> changePwdInput;

    private JComboBox<String> showChainInput;

    private JButton cancel;

    private JButton update;

    private JButton changeFileSystemPath;

    private File chooserPath;

    private List<FileSystemPath> extendStores;

    private static final String ML_OPEN = "是(YES)";

    private static final String ML_CLOSE = "否(CLOSE)";

    private static final String VC_STANDARD = "标准(STANDARD)";

    private static final String VC_SIMP = "简化(SIMPLIFIED)";

    private static final String VC_CLOSE = "关闭(CLOSE)";

    private static final String CHANGE_PWD_OPEN = "启用(ALLOW)";

    private static final String CHANGE_PWD_CLOSE = "禁用(PROHIBIT)";

    private static final String SHOW_CHAIN_OPEN = "启用(OPEN)";

    private static final String SHOW_CHAIN_CLOSE = "禁用(CLOSE)";

    private GetServerStatus serverStatus;

    private UpdateSetting updateSetting;

    private FileSystemPathViewer fileSystemPathViewer;

    private SettingWindow() {
        // 全局字体设置;
        setUIFont();
        // 窗口主体相关设置
        setWindow(new JDialog(serverUi.getWindow(), "kiftd-设置"));
        getWindow().setModal(true);
        getWindow().setSize(420, 425);
        getWindow().setLocation(150, 150);
        getWindow().setDefaultCloseOperation(1);
        getWindow().setResizable(false);
        getWindow().setLayout(new BoxLayout(
                getWindow().getContentPane(), 3));
        final JPanel titlebox = new JPanel(new FlowLayout(1));
        titlebox.setBorder(new EmptyBorder(0, 0, (int) (7 * proportion), 0));
        final JLabel title = new JLabel("服务器设置 Server Setting");
        title.setFont(new Font("宋体", 1, (int) (20 * proportion)));
        titlebox.add(title);

        getWindow().add(titlebox);
        // 窗口组件排布
        final JPanel settingbox = new JPanel(new GridLayout(8, 1, 0, 0));
        settingbox.setBorder(new EtchedBorder());
        final int interval = 0;
        // 必须登入下拉框
        final JPanel mlbox = new JPanel(new FlowLayout(1));
        mlbox.setBorder(new EmptyBorder(interval, 0, interval, 0));
        final JLabel mltitle = new JLabel("必须登入(must login)：");
        (
                mlInput = new JComboBox<>()).addItem(ML_OPEN);

        mlInput.addItem(ML_CLOSE);

        mlInput.setPreferredSize(new Dimension((int) (170 * proportion), (int) (20 * proportion)));
        mlbox.add(mltitle);
        mlbox.add(
                mlInput);
        // 登录验证码下拉框
        final JPanel vcbox = new JPanel(new FlowLayout(1));
        vcbox.setBorder(new EmptyBorder(interval, 0, interval, 0));
        final JLabel vctitle = new JLabel("登录验证码(VC type)：");
        (
                vcInput = new JComboBox<>()).addItem(VC_STANDARD);

        vcInput.addItem(VC_SIMP);

        vcInput.addItem(VC_CLOSE);

        mlInput.setPreferredSize(new Dimension((int) (170 * proportion), (int) (20 * proportion)));
        vcbox.add(vctitle);
        vcbox.add(
                vcInput);
        // 端口号输入框
        final JPanel portbox = new JPanel(new FlowLayout(1));
        portbox.setBorder(new EmptyBorder(interval, 0, interval, 0));
        final JLabel porttitle = new JLabel("端口(port)：");
        (
                portInput = new JTextField())
                .setPreferredSize(new Dimension((int) (120 * proportion), (int) (25 * proportion)));
        portbox.add(porttitle);
        portbox.add(
                portInput);
        // 缓存大小输入框
        final JPanel bufferbox = new JPanel(new FlowLayout(1));
        bufferbox.setBorder(new EmptyBorder(interval, 0, interval, 0));
        final JLabel buffertitle = new JLabel("缓存大小(buffer)：");
        (
                bufferInput = new JTextField())
                .setPreferredSize(new Dimension((int) (170 * proportion), (int) (25 * proportion)));
        final JLabel bufferUnit = new JLabel("KB");
        bufferbox.add(buffertitle);
        bufferbox.add(
                bufferInput);
        bufferbox.add(bufferUnit);
        // 日志等级选择框
        final JPanel logbox = new JPanel(new FlowLayout(1));
        logbox.setBorder(new EmptyBorder(interval, 0, interval, 0));
        final JLabel logtitle = new JLabel("日志等级(port)：");
        (
                logLevelInput = new JComboBox<>()).addItem("记录全部(ALL)");

        logLevelInput.addItem("仅异常(EXCEPTION)");

        logLevelInput.addItem("不记录(NONE)");

        logLevelInput.setPreferredSize(new Dimension((int) (170 * proportion), (int) (20 * proportion)));
        logbox.add(logtitle);
        logbox.add(
                logLevelInput);
        // 用户修改密码选择框
        final JPanel cpbox = new JPanel(new FlowLayout(1));
        cpbox.setBorder(new EmptyBorder(interval, 0, interval, 0));
        final JLabel cptitle = new JLabel("用户修改密码(change password)：");
        (
                changePwdInput = new JComboBox<>()).addItem(CHANGE_PWD_CLOSE);

        changePwdInput.addItem(CHANGE_PWD_OPEN);

        changePwdInput.setPreferredSize(new Dimension((int) (170 * proportion), (int) (20 * proportion)));
        cpbox.add(cptitle);
        cpbox.add(
                changePwdInput);
        // 用户修改密码选择框
        final JPanel scbox = new JPanel(new FlowLayout(1));
        cpbox.setBorder(new EmptyBorder(interval, 0, interval, 0));
        final JLabel sctitle = new JLabel("永久资源链接(file chain)：");
        (
                showChainInput = new JComboBox<>()).addItem(SHOW_CHAIN_CLOSE);

        showChainInput.addItem(SHOW_CHAIN_OPEN);

        showChainInput.setPreferredSize(new Dimension((int) (170 * proportion), (int) (20 * proportion)));
        scbox.add(sctitle);
        scbox.add(
                showChainInput);
        // 文件系统管理按钮
        final JPanel filePathBox = new JPanel(new FlowLayout(1));
        filePathBox.setBorder(new EmptyBorder(interval, 0, interval, 0));
        final JLabel filePathtitle = new JLabel("文件系统路径(file system path)：");

        changeFileSystemPath = new JButton("管理(Manage)");
        changeFileSystemPath.setPreferredSize(new Dimension((int) (170 * proportion), (int) (32 * proportion)));
        filePathBox.add(filePathtitle);
        filePathBox.add(
                changeFileSystemPath);
        // 界面布局顺序
        settingbox.add(portbox);
        settingbox.add(mlbox);
        settingbox.add(vcbox);
        settingbox.add(bufferbox);
        settingbox.add(logbox);
        settingbox.add(cpbox);
        settingbox.add(scbox);
        settingbox.add(filePathBox);

        getWindow().add(settingbox);
        final JPanel buttonbox = new JPanel(new FlowLayout(1));
        buttonbox.setBorder(new EmptyBorder((int) (0 * proportion), 0, (int) (5 * proportion), 0));

        update = new JButton("应用(Update)");

        cancel = new JButton("取消(Cancel)");
        update.setPreferredSize(new Dimension((int) (155 * proportion), (int) (32 * proportion)));
        cancel.setPreferredSize(new Dimension((int) (155 * proportion), (int) (32 * proportion)));
        buttonbox.add(update);
        buttonbox.add(cancel);

        getWindow().add(buttonbox);
        cancel.addActionListener(e -> getWindow().setVisible(false));
        update.addActionListener(e -> {
            // 仅在服务器停止时才可以进行修改
            if (getServerStatus().getServerStatus()) {
                startServerStatus();
            } else {
                AppSystem.pool.execute(SettingWindow.this::runServerSetting);
            }
        });

        changeFileSystemPath.addActionListener(e -> {
            fileSystemPathViewer = FileSystemPathViewer.getInstance();
            fileSystemPathViewer.show();
        });
        modifyComponentSize(getWindow());
    }

    protected void show() {
        this.startServerStatus();
        getWindow().setVisible(true);
    }

    private void startServerStatus() {
        AppSystem.pool.execute(this::runServerStatus);
    }

    protected static SettingWindow getInstance() {
        if (instance == null) {
            synchronized (SettingWindow.class) {
                if (instance == null) {
                    instance = new SettingWindow();
                }
            }
        }
        return instance;
    }

    private void runServerSetting() {
        if (getUpdateSetting() != null) {
            try {
                ServerSetting serverSetting = new ServerSetting();
                serverSetting.setPort(Integer.parseInt(portInput.getText()));
                serverSetting.setBuffSize(Integer.parseInt(bufferInput.getText()) * 1024);
                serverSetting.setFsPath(getChooserPath().getAbsolutePath());
                List<ExtendStores> extendStoresList = new ArrayList<>();
                for (FileSystemPath fileSystemPath : getExtendStores()) {
                    ExtendStores extendStores = new ExtendStores();
                    extendStores.setIndex(fileSystemPath.getIndex());
                    extendStores.setPath(fileSystemPath.getPath());
                    extendStoresList.add(extendStores);
                }
                serverSetting.setExtendStores(extendStoresList);
                switch (logLevelInput.getSelectedIndex()) {
                    case 0: {
                        serverSetting.setLog(LogLevel.EVENT);
                        break;
                    }
                    case 1: {
                        serverSetting.setLog(LogLevel.RUNTIME_EXCEPTION);
                        break;
                    }
                    case 2: {
                        serverSetting.setLog(LogLevel.NONE);
                        break;
                    }
                    default: {
                        // 注意，当选择未知的日志等级时，不做任何操作
                        break;
                    }
                }
                switch (mlInput.getSelectedIndex()) {
                    case 0: {
                        serverSetting.setMustLogin(true);
                        break;
                    }
                    case 1: {
                        serverSetting.setMustLogin(false);
                        break;
                    }
                    default: {
                        break;
                    }
                }
                switch (changePwdInput.getSelectedIndex()) {
                    case 0: {
                        serverSetting.setChangePassword(false);
                        break;
                    }
                    case 1: {
                        serverSetting.setChangePassword(true);
                        break;
                    }
                    default: {
                        break;
                    }
                }
                switch (showChainInput.getSelectedIndex()) {
                    case 0: {
                        serverSetting.setFileChain(false);
                        break;
                    }
                    case 1: {
                        serverSetting.setFileChain(true);
                        break;
                    }
                    default: {
                        break;
                    }
                }
                switch (vcInput.getSelectedIndex()) {
                    case 0: {
                        serverSetting.setVc(VcLevel.STANDARD);
                        break;
                    }
                    case 1: {
                        serverSetting.setVc(VcLevel.SIMPLIFIED);
                        break;
                    }
                    case 2: {
                        serverSetting.setVc(VcLevel.CLOSE);
                        break;
                    }
                    default:
                        break;
                }
                if (getUpdateSetting().update(serverSetting)) {
                    ServerUiModule.getInstance().updateServerStatus();
                    getWindow().setVisible(false);
                }
            }
            catch (Exception exc) {
                AppSystem.out.println(exc.getMessage());
                AppSystem.out.println("错误：无法更新服务器设置");
            }
        } else {
            getWindow().setVisible(false);
        }
    }

    private void runServerStatus() {
        if (getServerStatus() != null) {
            bufferInput
                    .setText(getServerStatus().getBufferSize() == 0 ?
                            getServerStatus().getInitBufferSize()
                            : String.format("%d", getServerStatus().getBufferSize() / 1024));
            portInput.setText(getServerStatus().getPort() == 0 ?
                    String.format("%s", getServerStatus().getInitProt())
                    : String.format("%d", getServerStatus().getPort()));
            if (getServerStatus().getFileSystemPath() != null) {
                setChooserPath(new File(getServerStatus().getFileSystemPath()));
            } else {
                setChooserPath(new File(getServerStatus().getInitFileSystemPath()));
            }
            setExtendStores(getServerStatus().getExtendStores());
            if (getServerStatus().getLogLevel() != null) {
                switch (getServerStatus().getLogLevel()) {
                    case EVENT: {
                        logLevelInput.setSelectedIndex(0);
                        break;
                    }
                    case RUNTIME_EXCEPTION: {
                        logLevelInput.setSelectedIndex(1);
                        break;
                    }
                    case NONE: {
                        logLevelInput.setSelectedIndex(2);
                        break;
                    }
                    default: {
                        break;
                    }
                }
            } else {
                switch (getServerStatus().getInitLogLevel()) {
                    case EVENT: {
                        logLevelInput.setSelectedIndex(0);
                        break;
                    }
                    case RUNTIME_EXCEPTION: {
                        logLevelInput.setSelectedIndex(1);
                        break;
                    }
                    case NONE: {
                        logLevelInput.setSelectedIndex(2);
                        break;
                    }
                    default: {
                        break;
                    }
                }
            }
            if (getServerStatus().getMustLogin()) {
                mlInput.setSelectedIndex(0);
            } else {
                mlInput.setSelectedIndex(1);
            }
            if (getServerStatus().isAllowChangePassword()) {
                changePwdInput.setSelectedIndex(1);
            } else {
                changePwdInput.setSelectedIndex(0);
            }
            if (getServerStatus().isOpenFileChain()) {
                showChainInput.setSelectedIndex(1);
            } else {
                showChainInput.setSelectedIndex(0);
            }
            if (getServerStatus().getVCLevel() != null) {
                switch (getServerStatus().getVCLevel()) {
                    case STANDARD: {
                        vcInput.setSelectedIndex(0);
                        break;
                    }
                    case SIMPLIFIED: {
                        vcInput.setSelectedIndex(1);
                        break;
                    }
                    case CLOSE: {
                        vcInput.setSelectedIndex(2);
                        break;
                    }
                    default: {
                        break;
                    }
                }
            } else {
                switch (getServerStatus().getInitVCLevel()) {
                    case STANDARD: {
                        vcInput.setSelectedIndex(0);
                        break;
                    }
                    case SIMPLIFIED: {
                        vcInput.setSelectedIndex(1);
                        break;
                    }
                    case CLOSE: {
                        vcInput.setSelectedIndex(2);
                        break;
                    }
                    default: {
                        break;
                    }
                }
            }
        }
    }

    public UpdateSetting getUpdateSetting() {
        return updateSetting;
    }

    public void setUpdateSetting(UpdateSetting updateSetting) {
        this.updateSetting = updateSetting;
    }

    public GetServerStatus getServerStatus() {
        return serverStatus;
    }

    public void setServerStatus(GetServerStatus serverStatus) {
        this.serverStatus = serverStatus;
    }

    public JDialog getWindow() {
        return window;
    }

    public void setWindow(JDialog window) {
        this.window = window;
    }

    public List<FileSystemPath> getExtendStores() {
        return extendStores;
    }

    public void setExtendStores(List<FileSystemPath> extendStores) {
        this.extendStores = extendStores;
    }

    public File getChooserPath() {
        return chooserPath;
    }

    public void setChooserPath(File chooserPath) {
        this.chooserPath = chooserPath;
    }
}
