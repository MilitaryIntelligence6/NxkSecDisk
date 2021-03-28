package edu.swufe.nxksecdisk.ui.module;

import edu.swufe.nxksecdisk.server.util.ConfigureReader;
import edu.swufe.nxksecdisk.system.AppSystem;
import edu.swufe.nxksecdisk.system.Decorator;
import edu.swufe.nxksecdisk.ui.callback.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Administrator
 */
public class ServerUiModule extends DiskDynamicWindow {

    private volatile static ServerUiModule instance;

    protected static JFrame window;

    private static SystemTray tray;

    private static TrayIcon trayIcon;

    private static final JTextArea out = new JTextArea();

    private static SettingWindow settingWindow;

    private static FsViewer fsViewer;

    private static OnCloseServer closeServer;

    private static OnStartServer startServer;

    private static GetServerStatus serverStatus;

    private static GetServerTime serverTime;

    private static JButton start;

    private static JButton stop;

    private static JButton restart;

    private static JButton setting;

    private static JButton fileIOUtil;

    private static JButton exit;

    private static JLabel serverStatusLab;

    private static JLabel portStatusLab;

    private static JLabel logLevelLab;

    private static JLabel bufferSizeLab;

    private static final String S_STOP = "停止[Stopped]";

    private static final String S_START = "运行[Running]";

    private static final String S_STARTING = "启动中[Starting]...";

    private static final String S_STOPPING = "停止中[Stopping]...";

    protected static final String L_ALL = "记录全部(ALL)";

    protected static final String L_EXCEPTION = "仅异常(EXCEPTION)";

    protected static final String L_NONE = "不记录(NONE)";

    private SimpleDateFormat sdf;

    /**
     * 窗口原始宽度
     */
    private final int originSizeWidth = 300;
    /**
     * 窗口原始高度
     */
    private final int originSizeHeight = 570;

    private static MenuItem filesViewer;

    private ServerUiModule() {
        setUIFont();
        window = new JFrame("kiftd-服务器控制台");
        window.setSize(originSizeWidth, originSizeHeight);
        window.setLocation(100, 100);
        window.setResizable(false);
        try {
            window.setIconImage(
                    ImageIO.read(this.getClass().getResourceAsStream("/icon/icon.png")));
        }
        catch (NullPointerException | IOException e) {
            e.printStackTrace();
        }
        if (SystemTray.isSupported()) {
            window.setDefaultCloseOperation(1);
            tray = SystemTray.getSystemTray();
            String iconType = "/icon/icon_tray.png";
            if (System.getProperty("os.name").toLowerCase().indexOf("window") >= 0) {
                iconType = "/icon/icon_tray_w.png";
            }
            try {
                trayIcon = new TrayIcon(ImageIO.read(this.getClass().getResourceAsStream(iconType)));
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            trayIcon.setToolTip("青阳网络文件系统-kiftd");
            trayIcon.setImageAutoSize(true);
            final PopupMenu pMenu = new PopupMenu();
            final MenuItem exit = new MenuItem("退出(Exit)");
            filesViewer = new MenuItem("文件...(Files)");
            final MenuItem show = new MenuItem("显示主窗口(Show)");
            trayIcon.addMouseListener(new MouseListener() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    // TODO 自动生成的方法存根
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    // TODO 自动生成的方法存根
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    // TODO 自动生成的方法存根
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    // TODO 自动生成的方法存根
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    // TODO 自动生成的方法存根
                    if (e.getClickCount() == 2) {
                        show();
                    }
                }
            });
            exit.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    // TODO 自动生成的方法存根
                    exit();
                }
            });
            filesViewer.addActionListener((e) ->
            {
                filesViewer.setEnabled(false);
                fileIOUtil.setEnabled(false);
                AppSystem.pool.execute(() -> {
                    try {
                        fsViewer = FsViewer.getInstance();
                        fsViewer.show();
                    }
                    catch (SQLException e1) {
                        // TODO 自动生成的 catch 块
                        JOptionPane.showMessageDialog(window, "错误：无法打开文件，文件系统可能已损坏，您可以尝试重启应用。", "错误",
                                JOptionPane.ERROR_MESSAGE);
                    }
                    filesViewer.setEnabled(true);
                    fileIOUtil.setEnabled(true);
                });
//                Thread t = new Thread(
//               () -> {
//                    try {
//                        fsViewer = FsViewer.getInstance();
//                        fsViewer.show();
//                    }
//                    catch (SQLException e1) {
//                        // TODO 自动生成的 catch 块
//                        JOptionPane.showMessageDialog(window, "错误：无法打开文件，文件系统可能已损坏，您可以尝试重启应用。", "错误",
//                                JOptionPane.ERROR_MESSAGE);
//                    }
//                    filesViewer.setEnabled(true);
//                    fileIOUtil.setEnabled(true);
//                });
//                t.start();
            });
            show.addActionListener(e -> show());
            pMenu.add(exit);
            pMenu.addSeparator();
            pMenu.add(filesViewer);
            pMenu.add(show);
            trayIcon.setPopupMenu(pMenu);
            try {
                tray.add(trayIcon);
            }
            catch (AWTException e) {
                e.printStackTrace();
            }
        } else {
            window.setDefaultCloseOperation(1);
        }
        window.setLayout(new BoxLayout(window.getContentPane(), 3));
        final JPanel titleBox = new JPanel(new FlowLayout(1));
        titleBox.setBorder(new EmptyBorder(0, 0, (int) (-25 * proportion), 0));
        final JLabel title = new JLabel("kiftd");
        title.setFont(new Font("宋体", 1, (int) (30 * proportion)));
        titleBox.add(title);
        window.add(titleBox);
        final JPanel subtitleBox = new JPanel(new FlowLayout(1));
        subtitleBox.setBorder(new EmptyBorder(0, 0, (int) (-20 * proportion), 0));
        final JLabel subtitle = new JLabel("青阳网络文件系统-服务器");
        subtitle.setFont(new Font("宋体", 0, (int) (13 * proportion)));
        subtitleBox.add(subtitle);
        window.add(subtitleBox);
        final JPanel statusBox = new JPanel(new GridLayout(4, 1));
        statusBox.setBorder(BorderFactory.createEtchedBorder());
        final JPanel serverStatusPanel = new JPanel(new FlowLayout());
        serverStatusPanel.setBorder(new EmptyBorder(0, 0, (int) (-8 * proportion), 0));
        serverStatusPanel.add(new JLabel("服务器状态(Status)："));
        serverStatusPanel.add(serverStatusLab = new JLabel("--"));
        statusBox.add(serverStatusPanel);
        final JPanel portStatus = new JPanel(new FlowLayout());
        portStatus.setBorder(new EmptyBorder(0, 0, (int) (-8 * proportion), 0));
        portStatus.add(new JLabel("端口号(Port)："));
        portStatus.add(portStatusLab = new JLabel("--"));
        statusBox.add(portStatus);
        final JPanel addrStatus = new JPanel(new FlowLayout());
        addrStatus.setBorder(new EmptyBorder(0, 0, (int) (-8 * proportion), 0));
        addrStatus.add(new JLabel("日志等级(LogLevel)："));
        addrStatus.add(logLevelLab = new JLabel("--"));
        statusBox.add(addrStatus);
        final JPanel bufferStatus = new JPanel(new FlowLayout());
        bufferStatus.setBorder(new EmptyBorder(0, 0, (int) (-8 * proportion), 0));
        bufferStatus.add(new JLabel("下载缓冲区(Buffer)："));
        bufferStatus.add(bufferSizeLab = new JLabel("--"));
        statusBox.add(bufferStatus);
        window.add(statusBox);
        final JPanel buttonBox = new JPanel(new GridLayout(6, 1));
        buttonBox.add(start = new JButton("开启(Start)>>"));
        buttonBox.add(stop = new JButton("关闭(Stop)||"));
        buttonBox.add(restart = new JButton("重启(Restart)~>"));
        buttonBox.add(fileIOUtil = new JButton("文件(Files)[*]"));
        buttonBox.add(setting = new JButton("设置(Setting)[/]"));
        buttonBox.add(exit = new JButton("退出(Exit)[X]"));
        window.add(buttonBox);
        final JPanel outputBox = new JPanel(new FlowLayout(1));
        outputBox.add(new JLabel("[输出信息(Server Message)]："));

        out.setLineWrap(true);
        out.setRows(3 + (int) (proportion));
        out.setSize((int) (292 * proportion), 100);
        out.setEditable(false);
        out.setForeground(Color.RED);
        out.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void removeUpdate(DocumentEvent e) {
                // TODO 自动生成的方法存根
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                // TODO 自动生成的方法存根
                AppSystem.pool.execute(() -> {
                            if (out.getLineCount() >= 1000) {
                                int end = 0;
                                try {
                                    end = out.getLineEndOffset(100);
                                }
                                catch (Exception exc) {
                                    exc.printStackTrace();
                                }
                                out.replaceRange("", 0, end);
                            }
                            out.setCaretPosition(out.getText().length());
                        }
                );
//                Thread t = new Thread(() ->
//                {
//                    if (out.getLineCount() >= 1000) {
//                        int end = 0;
//                        try {
//                            end = out.getLineEndOffset(100);
//                        }
//                        catch (Exception exc) {
//                            exc.printStackTrace();
//                        }
//                        out.replaceRange("", 0, end);
//                    }
//                    out.setCaretPosition(out.getText().length());
//                });
//                t.start();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // TODO 自动生成的方法存根
                out.selectAll();
                out.setCaretPosition(out.getSelectedText().length());
                out.requestFocus();
            }
        });
        outputBox.add(new JScrollPane(out));
        window.add(outputBox);
        final JPanel bottomBox = new JPanel(new FlowLayout(1));
        bottomBox.setBorder(new EmptyBorder(0, 0, (int) (-30 * proportion), 0));
        bottomBox.add(new JLabel("--青阳龙野@kohgylw--"));
        window.add(bottomBox);
        start.setEnabled(false);
        stop.setEnabled(false);
        restart.setEnabled(false);
        setting.setEnabled(false);
        start.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO 自动生成的方法存根
                start.setEnabled(false);
                setting.setEnabled(false);
                fileIOUtil.setEnabled(false);
                if (filesViewer != null) {
                    filesViewer.setEnabled(false);
                }
                println("启动服务器...");
                if (startServer != null) {
                    serverStatusLab.setText(S_STARTING);
                    AppSystem.pool.execute(() -> {
                        if (startServer.start()) {
                            println("启动完成。正在检查服务器状态...");
                            if (serverStatus.getServerStatus()) {
                                println("KIFT服务器已经启动，可以正常访问了。");
                            } else {
                                println("KIFT服务器未能成功启动，请检查设置或查看异常信息。");
                            }
                        } else {
                            if (ConfigureReader.getInstance().getPropertiesStatus() != 0) {
                                switch (ConfigureReader.getInstance().getPropertiesStatus()) {
                                    case ConfigureReader.INVALID_PORT: {
                                        println("KIFT无法启动：端口设置无效。");
                                        break;
                                    }
                                    case ConfigureReader.INVALID_BUFFER_SIZE: {
                                        println("KIFT无法启动：缓存设置无效。");
                                        break;
                                    }
                                    case ConfigureReader.INVALID_FILE_SYSTEM_PATH: {
                                        println("KIFT无法启动：文件系统路径或某一扩展存储区设置无效。");
                                        break;
                                    }
                                    case ConfigureReader.INVALID_LOG: {
                                        println("KIFT无法启动：日志设置无效。");
                                        break;
                                    }
                                    case ConfigureReader.INVALID_VC: {
                                        println("KIFT无法启动：登录验证码设置无效。");
                                        break;
                                    }
                                    case ConfigureReader.INVALID_MUST_LOGIN_SETTING: {
                                        println("KIFT无法启动：必须登入设置无效。");
                                        break;
                                    }
                                    case ConfigureReader.INVALID_CHANGE_PASSWORD_SETTING: {
                                        println("KIFT无法启动：用户修改账户密码设置无效。");
                                        break;
                                    }
                                    case ConfigureReader.INVALID_FILE_CHAIN_SETTING: {
                                        println("KIFT无法启动：永久资源链接设置无效。");
                                        break;
                                    }
                                    default: {
                                        println("KIFT无法启动，请检查设置或查看异常信息。");
                                        break;
                                    }
                                }
                            } else {
                                println("KIFT无法启动，请检查设置或查看异常信息。");
                            }
                            serverStatusLab.setText(S_STOP);
                        }
                        updateServerStatus();
                    });

//                    Thread t = new Thread(() ->
//                    {
//                        if (startServer.start()) {
//                            println("启动完成。正在检查服务器状态...");
//                            if (serverStatus.getServerStatus()) {
//                                println("KIFT服务器已经启动，可以正常访问了。");
//                            }
//                            else {
//                                println("KIFT服务器未能成功启动，请检查设置或查看异常信息。");
//                            }
//                        }
//                        else {
//                            if (ConfigureReader.getInstance().getPropertiesStatus() != 0) {
//                                switch (ConfigureReader.getInstance().getPropertiesStatus()) {
//                                    case ConfigureReader.INVALID_PORT: {
//                                        println("KIFT无法启动：端口设置无效。");
//                                        break;
//                                    }
//                                    case ConfigureReader.INVALID_BUFFER_SIZE: {
//                                        println("KIFT无法启动：缓存设置无效。");
//                                        break;
//                                    }
//                                    case ConfigureReader.INVALID_FILE_SYSTEM_PATH: {
//                                        println("KIFT无法启动：文件系统路径或某一扩展存储区设置无效。");
//                                        break;
//                                    }
//                                    case ConfigureReader.INVALID_LOG: {
//                                        println("KIFT无法启动：日志设置无效。");
//                                        break;
//                                    }
//                                    case ConfigureReader.INVALID_VC: {
//                                        println("KIFT无法启动：登录验证码设置无效。");
//                                        break;
//                                    }
//                                    case ConfigureReader.INVALID_MUST_LOGIN_SETTING: {
//                                        println("KIFT无法启动：必须登入设置无效。");
//                                        break;
//                                    }
//                                    case ConfigureReader.INVALID_CHANGE_PASSWORD_SETTING: {
//                                        println("KIFT无法启动：用户修改账户密码设置无效。");
//                                        break;
//                                    }
//                                    case ConfigureReader.INVALID_FILE_CHAIN_SETTING: {
//                                        println("KIFT无法启动：永久资源链接设置无效。");
//                                        break;
//                                    }
//                                    default: {
//                                        println("KIFT无法启动，请检查设置或查看异常信息。");
//                                        break;
//                                    }
//                                }
//                            }
//                            else {
//                                println("KIFT无法启动，请检查设置或查看异常信息。");
//                            }
//                            serverStatusLab.setText(S_STOP);
//                        }
//                        updateServerStatus();
//                    });
//                    t.start();
                }
            }
        });
        stop.addActionListener(e -> {
            // TODO 自动生成的方法存根
            stop.setEnabled(false);
            restart.setEnabled(false);
            fileIOUtil.setEnabled(false);
            if (filesViewer != null) {
                filesViewer.setEnabled(false);
            }
            println("关闭服务器...");
            AppSystem.pool.execute(() -> {
                if (closeServer != null) {
                    serverStatusLab.setText(S_STOPPING);
                    if (closeServer.close()) {
                        println("关闭完成。正在检查服务器状态...");
                        if (serverStatus.getServerStatus()) {
                            println("KIFT服务器未能成功关闭，如有需要，可以强制关闭程序（不安全）。");
                        } else {
                            println("KIFT服务器已经关闭，停止所有访问。");
                        }
                    } else {
                        println("KIFT服务器无法关闭，请手动结束本程序。");
                    }
                    updateServerStatus();
                }
            });
//                Thread t = new Thread(() ->
//                {
//                    if (closeServer != null) {
//                        serverStatusLab.setText(S_STOPPING);
//                        if (closeServer.close()) {
//                            println("关闭完成。正在检查服务器状态...");
//                            if (serverStatus.getServerStatus()) {
//                                println("KIFT服务器未能成功关闭，如有需要，可以强制关闭程序（不安全）。");
//                            }
//                            else {
//                                println("KIFT服务器已经关闭，停止所有访问。");
//                            }
//                        }
//                        else {
//                            println("KIFT服务器无法关闭，请手动结束本程序。");
//                        }
//                        updateServerStatus();
//                    }
//                });
//                t.start();
        });
        exit.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO 自动生成的方法存根
                fileIOUtil.setEnabled(false);
                if (filesViewer != null) {
                    filesViewer.setEnabled(false);
                }
                exit();
            }
        });
        restart.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO 自动生成的方法存根
                stop.setEnabled(false);
                restart.setEnabled(false);
                fileIOUtil.setEnabled(false);
                if (filesViewer != null) {
                    filesViewer.setEnabled(false);
                }
                AppSystem.pool.execute(() -> {
                    println("正在重启服务器...");
                    if (closeServer.close()) {
                        if (startServer.start()) {
                            println("重启成功，可以正常访问了。");
                        } else {
                            println("错误：服务器已关闭但未能重新启动，请尝试手动启动服务器。");
                        }
                    } else {
                        println("错误：无法关闭服务器，请尝试手动关闭。");
                    }
                    updateServerStatus();
                });
//                Thread t = new Thread(() ->
//                {
//                    println("正在重启服务器...");
//                    if (closeServer.close()) {
//                        if (startServer.start()) {
//                            println("重启成功，可以正常访问了。");
//                        }
//                        else {
//                            println("错误：服务器已关闭但未能重新启动，请尝试手动启动服务器。");
//                        }
//                    }
//                    else {
//                        println("错误：无法关闭服务器，请尝试手动关闭。");
//                    }
//                    updateServerStatus();
//                });
//                t.start();
            }
        });
        setting.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO 自动生成的方法存根
                settingWindow = SettingWindow.getInstance();
                AppSystem.pool.execute(settingWindow::show);
//                Thread t = new Thread(settingWindow::show);
//                t.start();
            }
        });
        fileIOUtil.addActionListener((e) ->
        {
            fileIOUtil.setEnabled(false);
            if (filesViewer != null) {
                filesViewer.setEnabled(false);
            }
            AppSystem.pool.execute(() -> {
                try {
                    fsViewer = FsViewer.getInstance();
                    fsViewer.show();
                }
                catch (SQLException e1) {
                    // TODO 自动生成的 catch 块
                    AppSystem.out.println("错误：无法读取文件，文件系统可能已经损坏，您可以尝试重启应用。");
                }
                fileIOUtil.setEnabled(true);
                if (filesViewer != null) {
                    filesViewer.setEnabled(true);
                }
            });
//            Thread t = new Thread(() ->
//            {
//                try {
//                    fsViewer = FsViewer.getInstance();
//                    fsViewer.show();
//                }
//                catch (SQLException e1) {
//                    // TODO 自动生成的 catch 块
//                    AppSystem.out.println("错误：无法读取文件，文件系统可能已经损坏，您可以尝试重启应用。");
//                }
//                fileIOUtil.setEnabled(true);
//                if (filesViewer != null) {
//                    filesViewer.setEnabled(true);
//                }
//            });
//            t.start();
        });
        modifyComponentSize(window);
        init();
    }

    private void init() {
        initDateFormat();
    }

    private void initDateFormat() {
        sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    }

    public void show() {
        window.setVisible(true);
        updateServerStatus();
    }

    public static void setOnCloseServer(final OnCloseServer cs) {
        closeServer = cs;
    }

    public static ServerUiModule getInstance() {
        if (instance == null) {
            synchronized (ServerUiModule.class) {
                if (instance == null) {
                    instance = new ServerUiModule();
                }
            }
        }
        return instance;
    }

    public static void setStartServer(final OnStartServer ss) {
        startServer = ss;
    }

    public static void setGetServerStatus(final GetServerStatus st) {
        serverStatus = st;
        SettingWindow.serverStatus = st;
    }

    public void updateServerStatus() {
        if (serverStatus != null) {
            AppSystem.pool.execute(() -> {
                if (serverStatus.getServerStatus()) {
                    serverStatusLab.setText(S_START);
                    start.setEnabled(false);
                    stop.setEnabled(true);
                    restart.setEnabled(true);
                    setting.setEnabled(false);
                } else {
                    serverStatusLab.setText(S_STOP);
                    start.setEnabled(true);
                    stop.setEnabled(false);
                    restart.setEnabled(false);
                    setting.setEnabled(true);
                }
                fileIOUtil.setEnabled(true);
                if (filesViewer != null) {
                    filesViewer.setEnabled(true);
                }
                portStatusLab.setText(serverStatus.getPort() + "");
                if (serverStatus.getLogLevel() != null) {
                    switch (serverStatus.getLogLevel()) {
                        case EVENT: {
                            logLevelLab.setText(L_ALL);
                            break;
                        }
                        case NONE: {
                            logLevelLab.setText(L_NONE);
                            break;
                        }
                        case RUNTIME_EXCEPTION: {
                            logLevelLab.setText(L_EXCEPTION);
                            break;
                        }
                        default: {
                            logLevelLab.setText("无法获取(?)");
                            break;
                        }
                    }
                }
                bufferSizeLab.setText(serverStatus.getBufferSize() / 1024 + " KB");
            });
//            Thread t = new Thread(() ->
//            {
//                if (serverStatus.getServerStatus()) {
//                    serverStatusLab.setText(S_START);
//                    start.setEnabled(false);
//                    stop.setEnabled(true);
//                    restart.setEnabled(true);
//                    setting.setEnabled(false);
//                }
//                else {
//                    serverStatusLab.setText(S_STOP);
//                    start.setEnabled(true);
//                    stop.setEnabled(false);
//                    restart.setEnabled(false);
//                    setting.setEnabled(true);
//                }
//                fileIOUtil.setEnabled(true);
//                if (filesViewer != null) {
//                    filesViewer.setEnabled(true);
//                }
//                portStatusLab.setText(serverStatus.getPort() + "");
//                if (serverStatus.getLogLevel() != null) {
//                    switch (serverStatus.getLogLevel()) {
//                        case EVENT: {
//                            logLevelLab.setText(L_ALL);
//                            break;
//                        }
//                        case NONE: {
//                            logLevelLab.setText(L_NONE);
//                            break;
//                        }
//                        case RUNTIME_EXCEPTION: {
//                            logLevelLab.setText(L_EXCEPTION);
//                            break;
//                        }
//                        default: {
//                            logLevelLab.setText("无法获取(?)");
//                            break;
//                        }
//                    }
//                }
//                bufferSizeLab.setText(serverStatus.getBufferSize() / 1024 + " KB");
//            });
//            t.start();
        }
    }

    private void exit() {
        start.setEnabled(false);
        stop.setEnabled(false);
        exit.setEnabled(false);
        restart.setEnabled(false);
        setting.setEnabled(false);
        this.println("退出程序...");
        if (closeServer != null) {
            AppSystem.pool.execute(() -> {
                if (serverStatus.getServerStatus()) {
                    closeServer.close();
                }
                System.exit(0);
                return;
            });
//            final Thread t = new Thread(() ->
//            {
//                if (serverStatus.getServerStatus()) {
//                    closeServer.close();
//                }
//                System.exit(0);
//                return;
//            });
//            t.start();
        } else {
            System.exit(0);
        }
    }

    public void println(final String context) {
        out.append(Decorator.decorateDate(context));
    }

    public void printf(final String context, Object... args) {
        out.append(String.format(Decorator.decorateDate(context), args));
    }

    private String requireFormatDate() {
        if (serverTime != null) {
            final Date d = serverTime.get();
            return sdf.format(d);
        }
        return sdf.format(new Date());
    }

    public static void setGetServerTime(final GetServerTime serverTime) {
        ServerUiModule.serverTime = serverTime;
    }

    public static void setUpdateSetting(final UpdateSetting updateSetting) {
        SettingWindow.updateSetting = updateSetting;
    }
}
