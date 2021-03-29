package edu.swufe.nxksecdisk.server.service.impl;

import com.google.gson.Gson;
import edu.swufe.nxksecdisk.server.enumeration.AccountAuth;
import edu.swufe.nxksecdisk.server.mapper.FolderMapper;
import edu.swufe.nxksecdisk.server.mapper.NodeMapper;
import edu.swufe.nxksecdisk.server.model.Node;
import edu.swufe.nxksecdisk.server.pojo.AudioInfoList;
import edu.swufe.nxksecdisk.server.service.PlayAudioService;
import edu.swufe.nxksecdisk.server.util.AudioInfoUtil;
import edu.swufe.nxksecdisk.server.util.ConfigReader;
import edu.swufe.nxksecdisk.server.util.FolderUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author Administrator
 */
@Service
public class PlayAudioServiceImpl implements PlayAudioService {

    @Resource
    private NodeMapper nodeMapper;

    @Resource
    private AudioInfoUtil audioInfoUtil;

    @Resource
    private Gson gson;

    @Resource
    private FolderUtil folderUtil;

    @Resource
    private FolderMapper folderMapper;

    private final ConfigReader config = ConfigReader.getInstance();

    private AudioInfoList foundAudios(final HttpServletRequest request) {
        final String fileId = request.getParameter("fileId");
        if (fileId != null && fileId.length() > 0) {
            Node targetNode = nodeMapper.queryById(fileId);
            if (targetNode != null) {
                final String account = (String) request.getSession().getAttribute("ACCOUNT");
                if (config.authorized(account, AccountAuth.DOWNLOAD_FILES,
                        folderUtil.getAllFoldersId(targetNode.getFileParentFolder()))
                        && config.accessFolder(folderMapper.queryById(targetNode.getFileParentFolder()),
                        account)) {
                    final List<Node> blocks = (List<Node>) this.nodeMapper.queryBySomeFolder(fileId);
                    return this.audioInfoUtil.transformToAudioInfoList(blocks, fileId);
                }
            }
        }
        return null;
    }

    /**
     * <h2>解析播放音频文件</h2>
     * <p>
     * 根据音频文件的ID查询音频文件节点，以及同级目录下所有音频文件组成播放列表，并返回节点JSON信息，以便发起播放请求。
     * </p>
     *
     * @param request javax.servlet.http.HttpServletRequest 请求对象
     * @return String 视频节点的JSON字符串
     * @author kohgylw
     */
    @Override
    public String requireAudioInfoListByJson(final HttpServletRequest request) {
        final AudioInfoList ail = this.foundAudios(request);
        if (ail != null) {
            return gson.toJson((Object) ail);
        }
        return "ERROR";
    }
}
