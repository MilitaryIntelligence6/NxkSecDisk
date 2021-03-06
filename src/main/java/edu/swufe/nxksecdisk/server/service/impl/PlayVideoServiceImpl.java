package edu.swufe.nxksecdisk.server.service.impl;

import com.google.gson.Gson;
import edu.swufe.nxksecdisk.server.enumeration.AccountAuth;
import edu.swufe.nxksecdisk.server.mapper.FolderMapper;
import edu.swufe.nxksecdisk.server.mapper.NodeMapper;
import edu.swufe.nxksecdisk.server.model.Node;
import edu.swufe.nxksecdisk.server.pojo.VideoInfo;
import edu.swufe.nxksecdisk.server.service.PlayVideoService;
import edu.swufe.nxksecdisk.server.util.*;
import edu.swufe.nxksecdisk.system.AppSystem;
import org.springframework.stereotype.Service;
import ws.schild.jave.MultimediaObject;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;

/**
 * @author Administrator
 */
@Service
public class PlayVideoServiceImpl implements PlayVideoService {

    @Resource
    private NodeMapper nodeMapper;

    @Resource
    private Gson gson;

    @Resource
    private FileBlockUtil fileBlockUtil;

    @Resource
    private LogUtil logUtil;

    @Resource
    private FolderMapper folderMapper;

    @Resource
    private FolderUtil folderUtil;

    @Resource
    private DiskFfmpegLocator diskFfmpegLocator;

    private final ConfigReader config = ConfigReader.getInstance();

    private VideoInfo foundVideo(final HttpServletRequest request) {
        final String fileId = request.getParameter("fileId");
        if (fileId != null && fileId.length() > 0) {
            final Node f = this.nodeMapper.queryById(fileId);
            final VideoInfo vi = new VideoInfo(f);
            if (f != null) {
                final String account = (String) request.getSession().getAttribute("ACCOUNT");
                if (config.authorized(account, AccountAuth.DOWNLOAD_FILES,
                        folderUtil.getAllFoldersId(f.getFileParentFolder()))
                        && config.accessFolder(folderMapper.queryById(f.getFileParentFolder())
                        , account)) {
                    final String fileName = f.getFileName();
                    // 检查视频格式
                    final String suffix = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
                    switch (suffix) {
                        case "mp4":
                            if (diskFfmpegLocator.getFFMPEGExecutablePath() != null) {
                                // 因此对于mp4后缀的视频，进一步检查其编码是否为h264，如果是，则允许直接播放
                                File target = fileBlockUtil.getFileFromBlocks(f);
                                if (target == null || !target.isFile()) {
                                    return null;
                                }
                                MultimediaObject mo = new MultimediaObject(target, diskFfmpegLocator);
                                try {
                                    if (mo.getInfo().getVideo().getDecoder().indexOf("h264") >= 0) {
                                        vi.setNeedEncode("N");
                                        return vi;
                                    }
                                }
                                catch (Exception e) {
                                    AppSystem.out.println(String.format("错误：视频文件“%s”在解析时出现意外错误。详细信息：%s",
                                            f.getFileName(), e.getMessage()));
                                    logUtil.writeException(e);
                                }
                                // 对于其他编码格式，则设定需要转码
                                vi.setNeedEncode("Y");
                            }
                            else {
                                // 如果禁用了ffmpeg，那么怎么都不需要转码;
                                vi.setNeedEncode("N");
                            }
                            return vi;
                        case "mkv":
                        case "mov":
                        case "webm":
                        case "avi":
                        case "wmv":
                        case "flv":
                            if (diskFfmpegLocator.getFFMPEGExecutablePath() != null) {
                                vi.setNeedEncode("Y");
                            }
                            else {
                                vi.setNeedEncode("N");
                            }
                            return vi;
                        default:
                            break;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public String parsePlayVideoJson(final HttpServletRequest request) {
        final VideoInfo v = this.foundVideo(request);
        if (v != null) {
            return gson.toJson((Object) v);
        }
        return "ERROR";
    }
}
