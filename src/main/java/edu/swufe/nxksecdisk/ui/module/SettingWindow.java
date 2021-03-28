package edu.swufe.nxksecdisk.ui.module;

import edu.swufe.nxksecdisk.system.AppSystem;
import edu.swufe.nxksecdisk.server.enumeration.LogLevel;
import edu.swufe.nxksecdisk.server.enumeration.VcLevel;
import edu.swufe.nxksecdisk.server.pojo.ExtendStores;
import edu.swufe.nxksecdisk.server.pojo.ServerSetting;
import edu.swufe.nxksecdisk.ui.callback.GetServerStatus;
import edu.swufe.nxksecdisk.ui.callback.UpdateSetting;
import edu.swufe.nxksecdisk.ui.pojo.FileSystemPath;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
public class SettingWindow extends DiskDynamicWindow
{
    private static SettingWindow instance;

    protected static JDialog window;

    private static JTextField portInput;

    private static JTextField bufferInput;

    private static JComboBox<String> mlInput;

    private static JComboBox<String> vcInput;

    private static JComboBox<String> logLevelInput;

    private static JComboBox<String> changePwdInput;

    private static JComboBox<String> showChainInput;

    private static JButton cancel;

    private static JButton update;

    private static JButton changeFileSystemPath;

    protected static File chooserPath;

    protected static List<FileSystemPath> extendStores;

    private static final String ML_OPEN = "是(YES)";

    private static final String ML_CLOSE = "否(CLOSE)";

    private static final String VC_STANDARD = "标准(STANDARD)";

    private static final String VC_SIMP = "简化(SIMPLIFIED)";

    private static final String VC_CLOSE = "关闭(CLOSE)";

    private static final String CHANGE_PWD_OPEN = "启用(ALLOW)";

    private static final String CHANGE_PWD_CLOSE = "禁用(PROHIBIT)";

    private static final String SHOW_CHAIN_OPEN = "启用(OPEN)";

    private static final String SHOW_CHAIN_CLOSE = "禁用(CLOSE)";

    protected static GetServerStatus serverStatus;

    protected static UpdateSetting updateSetting;

    private static FileSystemPathViewer fileSystemPathViewer;

    private SettingWindow()
    {
        // 全局字体设置;
        setUIFont();
        // 窗口主体相关设置
        (SettingWindow.window = new JDialog(ServerUiModule.window, "kiftd-设置")).setModal(true);
        SettingWindow.window.setSize(420, 425);
        SettingWindow.window.setLocation(150, 150);
        SettingWindow.window.setDefaultCloseOperation(1);
        SettingWindow.window.setResizable(false);
        SettingWindow.window.setLayout(new BoxLayout(SettingWindow.window.getContentPane(), 3));
        final JPanel titlebox = new JPanel(new FlowLayout(1));
        titlebox.setBorder(new EmptyBorder(0, 0, (int) (7 * proportion), 0));
        final JLabel title = new JLabel("服务器设置 Server Setting");
        title.setFont(new Font("宋体", 1, (int) (20 * proportion)));
        titlebox.add(title);
        SettingWindow.window.add(titlebox);
        // 窗口组件排布
        final JPanel settingbox = new JPanel(new GridLayout(8, 1, 0, 0));
        settingbox.setBorder(new EtchedBorder());
        final int interval = 0;
        // 必须登入下拉框
        final JPanel mlbox = new JPanel(new FlowLayout(1));
        mlbox.setBorder(new EmptyBorder(interval, 0, interval, 0));
        final JLabel mltitle = new JLabel("必须登入(must login)：");
        (SettingWindow.mlInput = new JComboBox<String>()).addItem(ML_OPEN);
        SettingWindow.mlInput.addItem(ML_CLOSE);
        SettingWindow.mlInput.setPreferredSize(new Dimension((int) (170 * proportion), (int) (20 * proportion)));
        mlbox.add(mltitle);
        mlbox.add(SettingWindow.mlInput);
        // 登录验证码下拉框
        final JPanel vcbox = new JPanel(new FlowLayout(1));
        vcbox.setBorder(new EmptyBorder(interval, 0, interval, 0));
        final JLabel vctitle = new JLabel("登录验证码(VC type)：");
        (SettingWindow.vcInput = new JComboBox<>()).addItem(VC_STANDARD);
        SettingWindow.vcInput.addItem(VC_SIMP);
        SettingWindow.vcInput.addItem(VC_CLOSE);
        SettingWindow.mlInput.setPreferredSize(new Dimension((int) (170 * proportion), (int) (20 * proportion)));
        vcbox.add(vctitle);
        vcbox.add(SettingWindow.vcInput);
        // 端口号输入框
        final JPanel portbox = new JPanel(new FlowLayout(1));
        portbox.setBorder(new EmptyBorder(interval, 0, interval, 0));
        final JLabel porttitle = new JLabel("端口(port)：");
        (SettingWindow.portInput = new JTextField())
                .setPreferredSize(new Dimension((int) (120 * proportion), (int) (25 * proportion)));
        portbox.add(porttitle);
        portbox.add(SettingWindow.portInput);
        // 缓存大小输入框
        final JPanel bufferbox = new JPanel(new FlowLayout(1));
        bufferbox.setBorder(new EmptyBorder(interval, 0, interval, 0));
        final JLabel buffertitle = new JLabel("缓存大小(buffer)：");
        (SettingWindow.bufferInput = new JTextField())
                .setPreferredSize(new Dimension((int) (170 * proportion), (int) (25 * proportion)));
        final JLabel bufferUnit = new JLabel("KB");
        bufferbox.add(buffertitle);
        bufferbox.add(SettingWindow.bufferInput);
        bufferbox.add(bufferUnit);
        // 日志等级选择框
        final JPanel logbox = new JPanel(new FlowLayout(1));
        logbox.setBorder(new EmptyBorder(interval, 0, interval, 0));
        final JLabel logtitle = new JLabel("日志等级(port)：");
        (SettingWindow.logLevelInput = new JComboBox<String>()).addItem("记录全部(ALL)");
        SettingWindow.logLevelInput.addItem("仅异常(EXCEPTION)");
        SettingWindow.logLevelInput.addItem("不记录(NONE)");
        SettingWindow.logLevelInput.setPreferredSize(new Dimension((int) (170 * proportion), (int) (20 * proportion)));
        logbox.add(logtitle);
        logbox.add(SettingWindow.logLevelInput);
        // 用户修改密码选择框
        final JPanel cpbox = new JPanel(new FlowLayout(1));
        cpbox.setBorder(new EmptyBorder(interval, 0, interval, 0));
        final JLabel cptitle = new JLabel("用户修改密码(change password)：");
        (SettingWindow.changePwdInput = new JComboBox<String>()).addItem(CHANGE_PWD_CLOSE);
        SettingWindow.changePwdInput.addItem(CHANGE_PWD_OPEN);
        SettingWindow.changePwdInput.setPreferredSize(new Dimension((int) (170 * proportion), (int) (20 * proportion)));
        cpbox.add(cptitle);
        cpbox.add(SettingWindow.changePwdInput);
        // 用户修改密码选择框
        final JPanel scbox = new JPanel(new FlowLayout(1));
        cpbox.setBorder(new EmptyBorder(interval, 0, interval, 0));
        final JLabel sctitle = new JLabel("永久资源链接(file chain)：");
        (SettingWindow.showChainInput = new JComboBox<String>()).addItem(SHOW_CHAIN_CLOSE);
        SettingWindow.showChainInput.addItem(SHOW_CHAIN_OPEN);
        SettingWindow.showChainInput.setPreferredSize(new Dimension((int) (170 * proportion), (int) (20 * proportion)));
        scbox.add(sctitle);
        scbox.add(SettingWindow.showChainInput);
        // 文件系统管理按钮
        final JPanel filePathBox = new JPanel(new FlowLayout(1));
        filePathBox.setBorder(new EmptyBorder(interval, 0, interval, 0));
        final JLabel filePathtitle = new JLabel("文件系统路径(file system path)：");
        SettingWindow.changeFileSystemPath = new JButton("管理(Manage)");
        changeFileSystemPath.setPreferredSize(new Dimension((int) (170 * proportion), (int) (32 * proportion)));
        filePathBox.add(filePathtitle);
        filePathBox.add(SettingWindow.changeFileSystemPath);
        // 界面布局顺序
        settingbox.add(portbox);
        settingbox.add(mlbox);
        settingbox.add(vcbox);
        settingbox.add(bufferbox);
        settingbox.add(logbox);
        settingbox.add(cpbox);
        settingbox.add(scbox);
        settingbox.add(filePathBox);
        SettingWindow.window.add(settingbox);
        final JPanel buttonbox = new JPanel(new FlowLayout(1));
        buttonbox.setBorder(new EmptyBorder((int) (0 * proportion), 0, (int) (5 * proportion), 0));
        SettingWindow.update = new JButton("应用(Update)");
        SettingWindow.cancel = new JButton("取消(Cancel)");
        update.setPreferredSize(new Dimension((int) (155 * proportion), (int) (32 * proportion)));
        cancel.setPreferredSize(new Dimension((int) (155 * proportion), (int) (32 * proportion)));
        buttonbox.add(SettingWindow.update);
        buttonbox.add(SettingWindow.cancel);
        SettingWindow.window.add(buttonbox);
        SettingWindow.cancel.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                window.setVisible(false);
            }
        });
        SettingWindow.update.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                // 仅在服务器停止时才可以进行修改
                if (serverStatus.getServerStatus())
                {
                    getServerStatus();
                }
                else
                {
                    Thread t = new Thread(() ->
                    {
                        if (updateSetting != null)
                        {
                            try
                            {
                                ServerSetting ss = new ServerSetting();
                                ss.setPort(Integer.parseInt(portInput.getText()));
                                ss.setBuffSize(Integer.parseInt(bufferInput.getText()) * 1024);
                                ss.setFsPath(chooserPath.getAbsolutePath());
                                List<ExtendStores> ess = new ArrayList<>();
                                for (FileSystemPath fsp : extendStores)
                                {
                                    ExtendStores es = new ExtendStores();
                                    es.setIndex(fsp.getIndex());
                                    es.setPath(fsp.getPath());
                                    ess.add(es);
                                }
                                ss.setExtendStores(ess);
                                switch (logLevelInput.getSelectedIndex())
                                {
                                    case 0:
                                        ss.setLog(LogLevel.EVENT);
                                        break;
                                    case 1:
                                        ss.setLog(LogLevel.RUNTIME_EXCEPTION);
                                        break;
                                    case 2:
                                        ss.setLog(LogLevel.NONE);
                                        break;

                                    default:
                                        // 注意，当选择未知的日志等级时，不做任何操作
                                        break;
                                }
                                switch (mlInput.getSelectedIndex())
                                {
                                    case 0:
                                        ss.setMustLogin(true);
                                        break;
                                    case 1:
                                        ss.setMustLogin(false);
                                        break;
                                    default:
                                        break;
                                }
                                switch (changePwdInput.getSelectedIndex())
                                {
                                    case 0:
                                        ss.setChangePassword(false);
                                        break;
                                    case 1:
                                        ss.setChangePassword(true);
                                        break;
                                    default:
                                        break;
                                }
                                switch (showChainInput.getSelectedIndex())
                                {
                                    case 0:
                                        ss.setFileChain(false);
                                        break;
                                    case 1:
                                        ss.setFileChain(true);
                                        break;
                                    default:
                                        break;
                                }
                                switch (vcInput.getSelectedIndex())
                                {
                                    case 0:
                                    {
                                        ss.setVc(VcLevel.STANDARD);
                                        break;
                                    }
                                    case 1:
                                    {
                                        ss.setVc(VcLevel.SIMPLIFIED);
                                        break;
                                    }
                                    case 2:
                                    {
                                        ss.setVc(VcLevel.CLOSE);
                                        break;
                                    }
                                    default:
                                        break;
                                }
                                if (updateSetting.update(ss))
                                {
                                    ServerUiModule.getInstance().updateServerStatus();
                                    window.setVisible(false);
                                }
                            }
                            catch (Exception exc)
                            {
                                AppSystem.out.println(exc.getMessage());
                                AppSystem.out.println("错误：无法更新服务器设置");
                            }
                        }
                        else
                        {
                            window.setVisible(false);
                        }
                    });
                    t.start();
                }
            }
        });
        SettingWindow.changeFileSystemPath.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                fileSystemPathViewer = FileSystemPathViewer.getInstance();
                fileSystemPathViewer.show();
            }
        });
        modifyComponentSize(window);
    }

    protected void show()
    {
        this.getServerStatus();
        SettingWindow.window.setVisible(true);
    }

    private void getServerStatus()
    {
        final Thread t = new Thread(() ->
        {
            if (SettingWindow.serverStatus != null)
            {
                SettingWindow.bufferInput
                        .setText(SettingWindow.serverStatus.getBufferSize() == 0 ? SettingWindow.serverStatus.getInitBufferSize()
                                : SettingWindow.serverStatus.getBufferSize() / 1024 + "");
                SettingWindow.portInput.setText(SettingWindow.serverStatus.getPort() == 0 ? SettingWindow.serverStatus.getInitProt() + ""
                        : SettingWindow.serverStatus.getPort() + "");
                if (SettingWindow.serverStatus.getFileSystemPath() != null)
                {
                    chooserPath = new File(SettingWindow.serverStatus.getFileSystemPath());
                }
                else
                {
                    chooserPath = new File(SettingWindow.serverStatus.getInitFileSystemPath());
                }
                extendStores = SettingWindow.serverStatus.getExtendStores();
                if (serverStatus.getLogLevel() != null)
                {
                    switch (serverStatus.getLogLevel())
                    {
                        case EVENT:
                        {
                            SettingWindow.logLevelInput.setSelectedIndex(0);
                            break;
                        }
                        case RUNTIME_EXCEPTION:
                        {
                            SettingWindow.logLevelInput.setSelectedIndex(1);
                            break;
                        }
                        case NONE:
                        {
                            SettingWindow.logLevelInput.setSelectedIndex(2);
                            break;
                        }
                    }
                }
                else
                {
                    switch (serverStatus.getInitLogLevel())
                    {
                        case EVENT:
                        {
                            SettingWindow.logLevelInput.setSelectedIndex(0);
                            break;
                        }
                        case RUNTIME_EXCEPTION:
                        {
                            SettingWindow.logLevelInput.setSelectedIndex(1);
                            break;
                        }
                        case NONE:
                        {
                            SettingWindow.logLevelInput.setSelectedIndex(2);
                            break;
                        }
                    }
                }
                if (SettingWindow.serverStatus.getMustLogin())
                {
                    SettingWindow.mlInput.setSelectedIndex(0);
                }
                else
                {
                    SettingWindow.mlInput.setSelectedIndex(1);
                }
                if (SettingWindow.serverStatus.isAllowChangePassword())
                {
                    SettingWindow.changePwdInput.setSelectedIndex(1);
                }
                else
                {
                    SettingWindow.changePwdInput.setSelectedIndex(0);
                }
                if (SettingWindow.serverStatus.isOpenFileChain())
                {
                    SettingWindow.showChainInput.setSelectedIndex(1);
                }
                else
                {
                    SettingWindow.showChainInput.setSelectedIndex(0);
                }
                if (SettingWindow.serverStatus.getVCLevel() != null)
                {
                    switch (SettingWindow.serverStatus.getVCLevel())
                    {
                        case STANDARD:
                        {
                            SettingWindow.vcInput.setSelectedIndex(0);
                            break;
                        }
                        case SIMPLIFIED:
                        {
                            SettingWindow.vcInput.setSelectedIndex(1);
                            break;
                        }
                        case CLOSE:
                        {
                            SettingWindow.vcInput.setSelectedIndex(2);
                            break;
                        }
                    }
                }
                else
                {
                    switch (SettingWindow.serverStatus.getInitVCLevel())
                    {
                        case STANDARD:
                        {
                            SettingWindow.vcInput.setSelectedIndex(0);
                            break;
                        }
                        case SIMPLIFIED:
                        {
                            SettingWindow.vcInput.setSelectedIndex(1);
                            break;
                        }
                        case CLOSE:
                        {
                            SettingWindow.vcInput.setSelectedIndex(2);
                            break;
                        }
                    }
                }
            }
            return;
        });
        t.start();
    }

    protected static SettingWindow getInstance()
    {
        if (instance == null)
        {
            synchronized (SettingWindow.class)
            {
                if (instance == null)
                {
                    instance = new SettingWindow();
                }
            }
        }
        return instance;
    }
}
