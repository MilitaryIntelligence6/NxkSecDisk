package edu.swufe.nxksecdisk.server.service.impl;

import com.google.gson.Gson;
import edu.swufe.nxksecdisk.server.enumeration.AccountAuth;
import edu.swufe.nxksecdisk.server.mapper.FolderMapper;
import edu.swufe.nxksecdisk.server.mapper.NodeMapper;
import edu.swufe.nxksecdisk.server.model.Folder;
import edu.swufe.nxksecdisk.server.model.Node;
import edu.swufe.nxksecdisk.server.pojo.FolderView;
import edu.swufe.nxksecdisk.server.pojo.RemainingFolderView;
import edu.swufe.nxksecdisk.server.pojo.SearchView;
import edu.swufe.nxksecdisk.server.service.FolderViewService;
import edu.swufe.nxksecdisk.server.util.ConfigureReader;
import edu.swufe.nxksecdisk.server.util.DiskFfmpegLocator;
import edu.swufe.nxksecdisk.server.util.FolderUtil;
import edu.swufe.nxksecdisk.server.util.ServerTimeUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * @author Administrator
 */
@Service
public class FolderViewServiceImpl implements FolderViewService {

    /**
     * 每次查询的文件或文件夹的最大限额，即查询步进长度;
     */
    private static int SELECT_STEP = 256;

    @Resource
    private FolderUtil folderUtil;

    @Resource
    private FolderMapper folderMapper;

    @Resource
    private NodeMapper nodeMapper;

    @Resource
    private Gson gson;

    @Resource
    private DiskFfmpegLocator diskFfmpegLocator;

    @Override
    public String getFolderViewToJson(final String fid, final HttpSession session, final HttpServletRequest request) {
        final ConfigureReader cr = ConfigureReader.getInstance();
        if (fid == null || fid.length() == 0) {
            return "ERROR";
        }
        Folder vf = this.folderMapper.queryById(fid);
        if (vf == null) {
            // 如果用户请求一个不存在的文件夹，则返回“NOT_FOUND”，令页面回到ROOT视图;
            return "NOT_FOUND";
        }
        final String account = (String) session.getAttribute("ACCOUNT");
        // 检查访问文件夹视图请求是否合法
        if (!ConfigureReader.getInstance().accessFolder(vf, account)) {
            // 如无访问权限则直接返回该字段，令页面回到ROOT视图;
            return "notAccess";
        }
        final FolderView fv = new FolderView();
        // 返回查询步长;
        fv.setSelectStep(SELECT_STEP);
        fv.setFolder(vf);
        fv.setParentList(this.folderUtil.getParentList(fid));
        // 第一波文件夹数据按照最后的记录作为查询偏移量;
        long foldersOffset = this.folderMapper.countByParentId(fid);
        fv.setFoldersOffset(foldersOffset);
        Map<String, Object> keyMap1 = new HashMap<>();
        keyMap1.put("pid", fid);
        long fOffset = foldersOffset - SELECT_STEP;
        // 进行查询;
        keyMap1.put("offset", fOffset > 0L ? fOffset : 0L);
        keyMap1.put("rows", SELECT_STEP);
        List<Folder> folders = this.folderMapper.queryByParentIdSection(keyMap1);
        List<Folder> fs = new LinkedList<>();
        for (Folder f : folders) {
            if (ConfigureReader.getInstance().accessFolder(f, account)) {
                fs.add(f);
            }
        }
        fv.setFolderList(fs);
        // 文件的查询逻辑与文件夹基本相同;
        long filesOffset = this.nodeMapper.countByParentFolderId(fid);
        fv.setFilesOffset(filesOffset);
        Map<String, Object> keyMap2 = new HashMap<>();
        keyMap2.put("pfid", fid);
        long fiOffset = filesOffset - SELECT_STEP;
        keyMap2.put("offset", fiOffset > 0L ? fiOffset : 0L);
        keyMap2.put("rows", SELECT_STEP);
        fv.setFileList(this.nodeMapper.queryByParentFolderIdSection(keyMap2));
        if (account != null) {
            fv.setAccount(account);
        }
        if (ConfigureReader.getInstance().isAllowChangePassword()) {
            fv.setAllowChangePassword("true");
        }
        else {
            fv.setAllowChangePassword("false");
        }
        if (ConfigureReader.getInstance().isAllowSignUp()) {
            fv.setAllowSignUp("true");
        }
        else {
            fv.setAllowSignUp("false");
        }
        final List<String> authList = new ArrayList<String>();
        if (cr.authorized(account, AccountAuth.UPLOAD_FILES, folderUtil.getAllFoldersId(fid))) {
            authList.add("U");
        }
        if (cr.authorized(account, AccountAuth.CREATE_NEW_FOLDER, folderUtil.getAllFoldersId(fid))) {
            authList.add("C");
        }
        if (cr.authorized(account, AccountAuth.DELETE_FILE_OR_FOLDER, folderUtil.getAllFoldersId(fid))) {
            authList.add("D");
        }
        if (cr.authorized(account, AccountAuth.RENAME_FILE_OR_FOLDER, folderUtil.getAllFoldersId(fid))) {
            authList.add("R");
        }
        if (cr.authorized(account, AccountAuth.DOWNLOAD_FILES, folderUtil.getAllFoldersId(fid))) {
            authList.add("L");
            if (cr.isOpenFileChain()) {
                // 显示永久资源链接;
                fv.setShowFileChain("true");
            }
            else {
                fv.setShowFileChain("false");
            }
        }
        if (cr.authorized(account, AccountAuth.MOVE_FILES, folderUtil.getAllFoldersId(fid))) {
            authList.add("M");
        }
        fv.setAuthList(authList);
        fv.setPublishTime(ServerTimeUtil.accurateToMinute());
        fv.setEnableFfmpeg(diskFfmpegLocator.isEnableFFmpeg());
        fv.setEnableDownloadZip(ConfigureReader.getInstance().isEnableDownloadByZip());
        return gson.toJson(fv);
    }

    @Override
    public String getSreachViewToJson(HttpServletRequest request) {
        final ConfigureReader cr = ConfigureReader.getInstance();
        String fid = request.getParameter("fid");
        String keyWorld = request.getParameter("keyworld");
        if (fid == null || fid.length() == 0 || keyWorld == null) {
            return "ERROR";
        }
        // 如果啥么也不查，那么直接返回指定文件夹标准视图
        if (keyWorld.length() == 0) {
            return getFolderViewToJson(fid, request.getSession(), request);
        }
        Folder vf = this.folderMapper.queryById(fid);
        final String account = (String) request.getSession().getAttribute("ACCOUNT");
        // 检查访问文件夹视图请求是否合法
        if (!ConfigureReader.getInstance().accessFolder(vf, account)) {
            // 如无访问权限则直接返回该字段，令页面回到ROOT视图;
            return "notAccess";
        }
        final SearchView sv = new SearchView();
        // 先准备搜索视图的文件夹信息
        Folder sf = new Folder();
        sf.setFolderId(vf.getFolderId());// 搜索视图的主键设置与搜索路径一致
        sf.setFolderName("在“" + vf.getFolderName() + "”内搜索“" + keyWorld + "”的结果...");// 名称就是搜索的描述
        sf.setFolderParent(vf.getFolderId());// 搜索视图的父级也与搜索路径一致
        sf.setFolderCreator("--");// 搜索视图是虚拟的，没有这些
        sf.setFolderCreationDate("--");
        sf.setFolderConstraint(vf.getFolderConstraint());// 其访问等级也与搜索路径一致
        sv.setFolder(sf);// 搜索视图的文件夹信息已经准备就绪
        // 设置上级路径为搜索路径
        List<Folder> pl = this.folderUtil.getParentList(fid);
        pl.add(vf);
        sv.setParentList(pl);
        // 设置所有搜索到的文件夹和文件，该方法迭查找：
        List<Node> ns = new LinkedList<>();
        List<Folder> fs = new LinkedList<>();
        sreachFilesAndFolders(fid, keyWorld, account, ns, fs);
        sv.setFileList(ns);
        sv.setFolderList(fs);
        // 搜索不支持分段加载，所以统计数据直接写入实际查询到的列表大小
        sv.setFoldersOffset(0L);
        sv.setFilesOffset(0L);
        sv.setSelectStep(SELECT_STEP);
        // 账户视图与文件夹相同
        if (account != null) {
            sv.setAccount(account);
        }
        if (ConfigureReader.getInstance().isAllowChangePassword()) {
            sv.setAllowChangePassword("true");
        }
        else {
            sv.setAllowChangePassword("false");
        }
        // 设置操作权限，对于搜索视图而言，只能进行下载操作（因为是虚拟的）
        final List<String> authList = new ArrayList<String>();
        // 搜索结果只接受“下载”操作
        if (cr.authorized(account, AccountAuth.DOWNLOAD_FILES, folderUtil.getAllFoldersId(fid))) {
            authList.add("L");
            if (cr.isOpenFileChain()) {
                // 显示永久资源链接;
                sv.setShowFileChain("true");
            }
            else {
                sv.setShowFileChain("false");
            }
        }
        // 同时额外具备普通文件夹没有的“定位”功能。
        authList.add("O");
        sv.setAuthList(authList);
        // 写入实时系统时间
        sv.setPublishTime(ServerTimeUtil.accurateToMinute());
        // 设置查询字段
        sv.setKeyWorld(keyWorld);
        // 返回公告MD5
        sv.setEnableFfmpeg(diskFfmpegLocator.isEnableFFmpeg());
        sv.setEnableDownloadZip(ConfigureReader.getInstance().isEnableDownloadByZip());
        return gson.toJson(sv);
    }

    /**
     * 迭代查找所有匹配项，
     * 参数分别是：从哪找、找啥、谁要找、添加的前缀是啥（便于分辨不同路径下的同名文件）、找到的文件放哪、找到的文件夹放哪;
     *
     * @param fid
     * @param key
     * @param account
     * @param ns
     * @param fs
     */
    private void sreachFilesAndFolders(String fid, String key, String account, List<Node> ns, List<Folder> fs) {
        for (Folder f : this.folderMapper.queryByParentId(fid)) {
            if (ConfigureReader.getInstance().accessFolder(f, account)) {
                if (f.getFolderName().indexOf(key) >= 0) {
                    f.setFolderName(f.getFolderName());
                    fs.add(f);
                }
                sreachFilesAndFolders(f.getFolderId(), key, account, ns, fs);
            }
        }
        for (Node n : this.nodeMapper.queryByParentFolderId(fid)) {
            if (n.getFileName().indexOf(key) >= 0) {
                n.setFileName(n.getFileName());
                ns.add(n);
            }
        }
    }

    @Override
    public String getRemainingFolderViewToJson(HttpServletRequest request) {
        final String fid = request.getParameter("fid");
        final String foldersOffset = request.getParameter("foldersOffset");
        final String filesOffset = request.getParameter("filesOffset");
        if (fid == null || fid.length() == 0) {
            return "ERROR";
        }
        Folder vf = this.folderMapper.queryById(fid);
        if (vf == null) {
            // 如果用户请求一个不存在的文件夹，则返回“NOT_FOUND”，令页面回到ROOT视图;
            return "NOT_FOUND";
        }
        final String account = (String) request.getSession().getAttribute("ACCOUNT");
        // 检查访问文件夹视图请求是否合法
        if (!ConfigureReader.getInstance().accessFolder(vf, account)) {
            // 如无访问权限则直接返回该字段，令页面回到ROOT视图;
            return "notAccess";
        }
        final RemainingFolderView fv = new RemainingFolderView();
        if (foldersOffset != null) {
            try {
                long newFoldersOffset = Long.parseLong(foldersOffset);
                if (newFoldersOffset > 0L) {
                    Map<String, Object> keyMap1 = new HashMap<>();
                    keyMap1.put("pid", fid);
                    long nfOffset = newFoldersOffset - SELECT_STEP;
                    keyMap1.put("offset", nfOffset > 0L ? nfOffset : 0L);
                    keyMap1.put("rows", nfOffset > 0L ? SELECT_STEP : newFoldersOffset);
                    List<Folder> folders = this.folderMapper.queryByParentIdSection(keyMap1);
                    List<Folder> fs = new LinkedList<>();
                    for (Folder f : folders) {
                        if (ConfigureReader.getInstance().accessFolder(f, account)) {
                            fs.add(f);
                        }
                    }
                    fv.setFolderList(fs);
                }
            }
            catch (NumberFormatException e) {
                return "ERROR";
            }
        }
        if (filesOffset != null) {
            try {
                long newFilesOffset = Long.parseLong(filesOffset);
                if (newFilesOffset > 0L) {
                    Map<String, Object> keyMap2 = new HashMap<>();
                    keyMap2.put("pfid", fid);
                    long nfiOffset = newFilesOffset - SELECT_STEP;
                    keyMap2.put("offset", nfiOffset > 0L ? nfiOffset : 0L);
                    keyMap2.put("rows", nfiOffset > 0L ? SELECT_STEP : newFilesOffset);
                    fv.setFileList(this.nodeMapper.queryByParentFolderIdSection(keyMap2));
                }
            }
            catch (NumberFormatException e) {
                return "ERROR";
            }
        }
        return gson.toJson(fv);
    }
}
