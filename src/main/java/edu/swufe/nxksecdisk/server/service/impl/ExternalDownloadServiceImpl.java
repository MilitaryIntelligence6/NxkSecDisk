package edu.swufe.nxksecdisk.server.service.impl;

import edu.swufe.nxksecdisk.server.enumeration.AccountAuth;
import edu.swufe.nxksecdisk.server.mapper.FolderMapper;
import edu.swufe.nxksecdisk.server.mapper.NodeMapper;
import edu.swufe.nxksecdisk.server.model.Node;
import edu.swufe.nxksecdisk.server.service.ExternalDownloadService;
import edu.swufe.nxksecdisk.server.util.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

/**
 * @author Administrator
 */
@Service
public class ExternalDownloadServiceImpl
        extends RangeFileStreamWriter
        implements ExternalDownloadService {

    private static final String CONTENT_TYPE = "application/octet-stream";

    /**
     * 凭证池，用于存储生成好的下载凭证;
     */
    private static Map<String, String> downloadKeyMap = new HashMap<>();

    @Resource
    private NodeMapper nodeMapper;

    @Resource
    private LogUtil logUtil;

    @Resource
    private FileBlockUtil fileBlockUtil;

    @Resource
    private FolderUtil folderUtil;

    @Resource
    private FolderMapper folderMapper;

    private final ConfigReader config = ConfigReader.getInstance();

    @Override
    public String getDownloadKey(HttpServletRequest request) {
        // 首先进行权限检查
        final String account = (String) request.getSession().getAttribute("ACCOUNT");
        // 找到要下载的文件节点
        final String fileId = request.getParameter("fId");
        if (fileId != null) {
            final Node f = this.nodeMapper.queryById(fileId);
            if (f != null) {
                // 权限检查
                if (config.authorized(account, AccountAuth.DOWNLOAD_FILES,
                        folderUtil.getAllFoldersId(f.getFileParentFolder()))
                        && config.accessFolder(folderMapper.queryById(f.getFileParentFolder())
                        , account)) {
                    // 获取凭证
                    synchronized (downloadKeyMap) {
                        // 查找该资源是否已经生成了一个凭证，如有，则直接使用，否则，新生成一个加入到凭证表。
                        this.logUtil.writeShareFileURLEvent(request, f);
                        if (downloadKeyMap.containsValue(f.getFileId())) {
                            Entry<String, String> k = downloadKeyMap.entrySet().parallelStream()
                                    .filter((e) -> e.getValue().equals(f.getFileId())).findFirst().get();
                            return k.getKey();
                        }
                        else {
                            String dKey = UUID.randomUUID().toString();
                            downloadKeyMap.put(dKey, f.getFileId());
                            return dKey;
                        }
                    }
                }
            }
        }
        return "ERROR";
    }

    @Override
    public void downloadFileByKey(HttpServletRequest request, HttpServletResponse response) {
        final String dkey = request.getParameter("dkey");
        // 权限凭证有效性并确认其对应的资源
        if (dkey != null) {
            // 找到要下载的文件节点
            String fId = null;
            synchronized (downloadKeyMap) {
                fId = downloadKeyMap.get(dkey);
            }
            if (fId != null) {
                Node f = this.nodeMapper.queryById(fId);
                if (f != null) {
                    File target = this.fileBlockUtil.getFileFromBlocks(f);
                    if (target != null && target.isFile()) {
                        String range = request.getHeader("Range");
                        int status = writeRangeFileStream(request, response, target, f.getFileName(), CONTENT_TYPE,
                                config.getDownloadMaxRate(null), fileBlockUtil.getETag(target)
                                , true);
                        if (status == HttpServletResponse.SC_OK || (range != null && range.startsWith("bytes=0-"))) {
                            this.logUtil.writeDownloadFileByKeyEvent(request, f);
                        }
                        return;
                    }
                }
            }
        }
        try {
            // 处理无法下载的资源
            response.sendError(404);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
