package edu.swufe.nxksecdisk.server.controller;

import edu.swufe.nxksecdisk.server.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * <h2>主控制器</h2>
 * <p>
 * 该控制器用于负责处理kiftd主页（home.html）的所有请求，具体过程请见各个方法注释。
 * </p>
 *
 * @author 青阳龙野(kohgylw)
 * @version 1.0
 */
@Controller
@RequestMapping({"/homeController"})
public class HomeController {

    private static final String CHARSET_BY_AJAX = "text/html; charset=utf-8";

    @Resource
    private ServerInfoService serverInfoService;

    @Resource
    private AccountService accountService;

    @Resource
    private FolderViewService folderViewService;

    @Resource
    private FolderService folderService;

    @Resource
    private FileService fileService;

    @Resource
    private PlayVideoService playVideoService;

    @Resource
    private ShowPictureService showPictureService;

    @Resource
    private PlayAudioService playAudioService;

    @Resource
    private FileChainService fileChainService;

    @RequestMapping({"/getServerOS.ajax"})
    @ResponseBody
    public String getServerOS() {
        return this.serverInfoService.requireOsName();
    }

    @RequestMapping(value = {"/getPublicKey.ajax"}, produces = {CHARSET_BY_AJAX})
    @ResponseBody
    public String getPublicKey() {
        return this.accountService.getPublicKey();
    }

    @RequestMapping({"/doLogin.ajax"})
    @ResponseBody
    public String doLogin(final HttpServletRequest request, final HttpSession session) {
        return this.accountService.checkLoginRequest(request, session);
    }

    /**
     * 获取一个新验证码并存入请求者的Session中;
     *
     * @param request
     * @param response
     * @param session
     */
    @RequestMapping({"/getNewVerCode.do"})
    public void getNewVerCode(final HttpServletRequest request, final HttpServletResponse response,
                              final HttpSession session) {
        accountService.getNewLoginVerCode(request, response, session);
    }

    /**
     * 修改密码;
     *
     * @param request
     * @return
     */
    @RequestMapping(value = {"/doChangePassword.ajax"}, produces = {CHARSET_BY_AJAX})
    @ResponseBody
    public String doChangePassword(final HttpServletRequest request) {
        return accountService.changePassword(request);
    }

    @RequestMapping(value = {"/getFolderView.ajax"}, produces = {CHARSET_BY_AJAX})
    @ResponseBody
    public String getFolderView(final String fid, final HttpSession session, final HttpServletRequest request) {
        return folderViewService.getFolderViewToJson(fid, session, request);
    }

    @RequestMapping(value = {"/getRemainingFolderView.ajax"}, produces = {CHARSET_BY_AJAX})
    @ResponseBody
    public String getRemainingFolderView(final HttpServletRequest request) {
        return folderViewService.getRemainingFolderViewToJson(request);
    }

    @RequestMapping({"/doLogout.ajax"})
    public @ResponseBody
    String doLogout(final HttpSession session) {
        this.accountService.logout(session);
        return "SUCCESS";
    }

    @RequestMapping({"/newFolder.ajax"})
    @ResponseBody
    public String newFolder(final HttpServletRequest request) {
        return this.folderService.newFolder(request);
    }

    @RequestMapping({"/deleteFolder.ajax"})
    @ResponseBody
    public String deleteFolder(final HttpServletRequest request) {
        return this.folderService.deleteFolder(request);
    }

    @RequestMapping({"/renameFolder.ajax"})
    @ResponseBody
    public String renameFolder(final HttpServletRequest request) {
        return this.folderService.renameFolder(request);
    }

    @RequestMapping(value = {"/douploadFile.ajax"}, produces = {CHARSET_BY_AJAX})
    @ResponseBody
    public String douploadFile(final HttpServletRequest request, final HttpServletResponse response,
                               final MultipartFile file) {
        return this.fileService.doUploadFile(request, response, file);
    }

    @RequestMapping(value = {"/checkUploadFile.ajax"}, produces = {CHARSET_BY_AJAX})
    @ResponseBody
    public String checkUploadFile(final HttpServletRequest request, final HttpServletResponse response) {
        return this.fileService.checkUploadFile(request, response);
    }

    // 上传文件夹的前置检查流程
    @RequestMapping(value = {"/checkImportFolder.ajax"}, produces = {CHARSET_BY_AJAX})
    @ResponseBody
    public String checkImportFolder(final HttpServletRequest request) {
        return this.fileService.checkImportFolder(request);
    }

    // 执行文件夹上传操作
    @RequestMapping(value = {"/doImportFolder.ajax"}, produces = {CHARSET_BY_AJAX})
    @ResponseBody
    public String doImportFolder(final HttpServletRequest request, final MultipartFile file) {
        return fileService.doImportFolder(request, file);
    }

    // 上传文件夹时，若存在同名文件夹并选择覆盖，则应先执行该方法，执行成功后再上传新的文件夹
    @RequestMapping(value = {"/deleteFolderByName.ajax"}, produces = {CHARSET_BY_AJAX})
    @ResponseBody
    public String deleteFolderByName(final HttpServletRequest request) {
        return folderService.deleteFolderByName(request);
    }

    // 上传文件夹时，若存在同名文件夹并选择保留两者，则应先执行该方法，执行成功后使用返回的新文件夹名进行上传
    @RequestMapping(value = {"/createNewFolderByName.ajax"}, produces = {CHARSET_BY_AJAX})
    @ResponseBody
    public String createNewFolderByName(final HttpServletRequest request) {
        return folderService.createNewFolderByName(request);
    }

    @RequestMapping({"/deleteFile.ajax"})
    @ResponseBody
    public String deleteFile(final HttpServletRequest request) {
        return this.fileService.deleteFile(request);
    }

    @RequestMapping({"/downloadFile.do"})
    public void downloadFile(final HttpServletRequest request, final HttpServletResponse response) {
        this.fileService.doDownloadFile(request, response);
    }

    @RequestMapping({"/renameFile.ajax"})
    @ResponseBody
    public String renameFile(final HttpServletRequest request) {
        return this.fileService.doRenameFile(request);
    }

    @RequestMapping(value = {"/playVideo.ajax"}, produces = {CHARSET_BY_AJAX})
    @ResponseBody
    public String playVideo(final HttpServletRequest request, final HttpServletResponse response) {
        return this.playVideoService.parsePlayVideoJson(request);
    }

    /**
     * <h2>预览图片请求</h2>
     * <p>
     * 该方法用于处理预览图片请求。配合Viewer.js插件，返回指定格式的JSON数据。
     * </p>
     *
     * @param request HttpServletRequest 请求对象
     * @return String 预览图片的JSON信息
     * @author 青阳龙野(kohgylw)
     */
    @RequestMapping(value = {"/getPrePicture.ajax"}, produces = {CHARSET_BY_AJAX})
    @ResponseBody
    public String getPrePicture(final HttpServletRequest request) {
        return this.showPictureService.getPreviewPictureJson(request);
    }

    /**
     * <h2>获取压缩的预览图片</h2>
     * <p>
     * 该方法用于预览较大图片时获取其压缩版本以加快预览速度，该请求会根据预览目标的大小自动决定压缩等级。
     * </p>
     *
     * @param request  HttpServletRequest 请求对象，其中应包含fileId指定预览图片的文件块ID。
     * @param response HttpServletResponse 响应对象，用于写出压缩后的数据流。
     * @author 青阳龙野(kohgylw)
     */
    @RequestMapping({"/showCondensedPicture.do"})
    public void showCondensedPicture(final HttpServletRequest request, final HttpServletResponse response) {
        showPictureService.requireCondensedPicture(request, response);
    }

    @RequestMapping({"/deleteCheckedFiles.ajax"})
    @ResponseBody
    public String deleteCheckedFiles(final HttpServletRequest request) {
        return this.fileService.deleteCheckedFiles(request);
    }

    @RequestMapping({"/getPackTime.ajax"})
    @ResponseBody
    public String getPackTime(final HttpServletRequest request) {
        return this.fileService.getPackTime(request);
    }

    @RequestMapping({"/downloadCheckedFiles.ajax"})
    @ResponseBody
    public String downloadCheckedFiles(final HttpServletRequest request) {
        return this.fileService.downloadCheckedFiles(request);
    }

    @RequestMapping({"/downloadCheckedFilesZip.do"})
    public void downloadCheckedFilesZip(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        this.fileService.downloadCheckedFilesZip(request, response);
    }

    @RequestMapping(value = {"/playAudios.ajax"}, produces = {CHARSET_BY_AJAX})
    @ResponseBody
    public String playAudios(final HttpServletRequest request) {
        return this.playAudioService.requireAudioInfoListByJson(request);
    }

    /**
     * <h2>移动文件操作前置确认</h2>
     * <p>该逻辑用于在执行移动或复制前确认目标文件夹是否合法以及是否会产生文件名冲突。</p>
     *
     * @param request javax.servlet.http.HttpServletRequest 请求对象
     * @return java.lang.String 判断结果，详情请见具体实现
     * @author 青阳龙野(kohgylw)
     */
    @RequestMapping(value = {"/confirmMoveFiles.ajax"}, produces = {CHARSET_BY_AJAX})
    @ResponseBody
    public String confirmMoveFiles(final HttpServletRequest request) {
        return fileService.confirmMoveFiles(request);
    }

    /**
     * <h2>执行移动文件操作</h2>
     * <p>该逻辑用于正式执行移动或复制操作，在调用之前应先执行判断操作。</p>
     *
     * @param request javax.servlet.http.HttpServletRequest 请求对象
     * @return java.lang.String 执行结果，详情请见具体实现
     * @author 青阳龙野(kohgylw)
     */
    @RequestMapping({"/moveCheckedFiles.ajax"})
    @ResponseBody
    public String moveCheckedFiles(final HttpServletRequest request) {
        return fileService.doMoveFiles(request);
    }

    /**
     * <h2>执行全局查询</h2>
     * <p>该逻辑用于进行全局搜索，将会迭代搜索目标文件夹及其全部子文件夹以查找符合关键字的结果，并返回单独的搜索结果视图。</p>
     *
     * @param request javax.servlet.http.HttpServletRequest 请求对象
     * @return java.lang.String 搜索结果，详情请见具体实现
     * @author 青阳龙野(kohgylw)
     */
    @RequestMapping(value = {"/sreachInCompletePath.ajax"}, produces = {CHARSET_BY_AJAX})
    @ResponseBody
    public String sreachInCompletePath(final HttpServletRequest request) {
        return folderViewService.getSreachViewToJson(request);
    }

    /**
     * <h2>应答机制</h2>
     * <p>
     * 该机制旨在防止某些长耗时操作可能导致Session失效的问题（例如上传、视频播放等），方便用户持续操作。
     * </p>
     *
     * @return String “pong”或“”
     * @author 青阳龙野(kohgylw)
     */
    @RequestMapping(value = {"/ping.ajax"}, produces = {CHARSET_BY_AJAX})
    @ResponseBody
    public String pong(final HttpServletRequest request) {
        return accountService.doPong(request);
    }

    // 询问是否开启自由注册新账户功能
    @RequestMapping(value = {"/askForAllowSignUpOrNot.ajax"}, produces = {CHARSET_BY_AJAX})
    @ResponseBody
    public String askForAllowSignUpOrNot(final HttpServletRequest request) {
        return accountService.isAllowSignUp();
    }

    // 处理注册新账户请求
    @RequestMapping(value = {"/doSigUp.ajax"}, produces = {CHARSET_BY_AJAX})
    @ResponseBody
    public String doSigUp(final HttpServletRequest request) {
        return accountService.doSignUp(request);
    }

    // 获取永久资源链接的对应ckey
    @RequestMapping(value = {"/getFileChainKey.ajax"}, produces = {CHARSET_BY_AJAX})
    @ResponseBody
    public String getFileChainKey(final HttpServletRequest request) {
        return fileChainService.getChainKeyByFid(request);
    }
}
