package edu.swufe.nxksecdisk.server.service.impl;

import edu.swufe.nxksecdisk.printer.Out;
import edu.swufe.nxksecdisk.server.enumeration.AccountAuth;
import edu.swufe.nxksecdisk.server.enumeration.PowerPointType;
import edu.swufe.nxksecdisk.server.mapper.FolderMapper;
import edu.swufe.nxksecdisk.server.mapper.NodeMapper;
import edu.swufe.nxksecdisk.server.model.Node;
import edu.swufe.nxksecdisk.server.pojo.VideoTranscodeThread;
import edu.swufe.nxksecdisk.server.service.ResourceService;
import edu.swufe.nxksecdisk.server.util.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * 资源服务类，所有处理非下载流请求的工作均在此完成
 * @author Administrator
 */
@Service
public class ResourceServiceImpl implements ResourceService
{
    /**
     * 资源缓存的寿命30分钟，正好对应账户的自动注销时间;
     */
    private static final long RESOURCE_CACHE_MAX_AGE = 1800L;

    @Resource
    private NodeMapper nodeMapper;

    @Resource
    private FileBlockUtil fileBlockUtil;

    @Resource
    private LogUtil logUtil;

    @Resource
    private DocxToPdfUtil docxToPdfUtil;

    @Resource
    private TxtToPdfUtil txtToPdfUtil;

    @Resource
    private VideoTranscodeUtil videoTranscodeUtil;

    @Resource
    private PowerPointToPdfUtil pptToPdfUtil;

    @Resource
    private FolderUtil folderUtil;

    @Resource
    private FolderMapper folderMapper;

    @Resource
    private TxtCharsetGetter txtCharsetGetter;

    @Resource
    private NoticeUtil noticeUtil;

    @Resource
    private ContentTypeMap contentTypeMap;

    @Resource
    private DiskFfmpegLocator diskFfmpegLocator;

    @Resource
    private IpAddrGetter ipAddrGetter;

    /**
     * 提供资源的输出流，原理与下载相同，但是个别细节有区别;
     * @param fid      java.lang.String 目标资源的fid，用于指定文件节点
     * @param request  javax.servlet.http.HttpServletRequest 请求对象
     * @param response javax.servlet.http.HttpServletResponse 响应对象
     */
    @Override
    public void requireResource(String fid, HttpServletRequest request, HttpServletResponse response)
    {
        final String account = (String) request.getSession().getAttribute("ACCOUNT");
        if (fid != null)
        {
            Node n = nodeMapper.queryById(fid);
            if (n != null)
            {
                if (ConfigureReader.getInstance().authorized(account, AccountAuth.DOWNLOAD_FILES,
                        folderUtil.getAllFoldersId(n.getFileParentFolder()))
                        && ConfigureReader.getInstance().accessFolder(folderMapper.queryById(n.getFileParentFolder()), account))
                {
                    File file = fileBlockUtil.getFileFromBlocks(n);
                    if (file != null && file.isFile())
                    {
                        String suffix = "";
                        if (n.getFileName().indexOf(".") >= 0)
                        {
                            suffix = n.getFileName().substring(n.getFileName().lastIndexOf(".")).trim().toLowerCase();
                        }
                        String contentType = contentTypeMap.getContentType(suffix);
                        switch (suffix)
                        {
                            case ".mp4":
                            case ".webm":
                            case ".mov":
                            case ".avi":
                            case ".wmv":
                            case ".mkv":
                            case ".flv":
                                if (diskFfmpegLocator.getFFMPEGExecutablePath() != null)
                                {
                                    contentType = "video/mp4";
                                    synchronized (VideoTranscodeUtil.videoTranscodeThreads)
                                    {
                                        VideoTranscodeThread vtt = VideoTranscodeUtil.videoTranscodeThreads.get(fid);
                                        if (vtt != null)
                                        {// 针对需要转码的视频（在转码列表中存在）
                                            File f = new File(ConfigureReader.getInstance().getTemporaryfilePath(),
                                                    vtt.getOutputFileName());
                                            if (f.isFile() && vtt.getProgress().equals("FIN"))
                                            {
                                                // 判断是否转码成功
                                                // 成功，则播放它;
                                                file = f;
                                            }
                                            else
                                            {
                                                try
                                                {
                                                    // 否则，返回处理失败;
                                                    response.sendError(500);
                                                }
                                                catch (IOException e)
                                                {
                                                }
                                                return;
                                            }
                                        }
                                    }
                                }
                                break;
                            default:
                                break;
                        }
                        String ip = ipAddrGetter.getIpAddr(request);
                        String range = request.getHeader("Range");
                        int status = sendResource(file, n.getFileName(), contentType, request, response);
                        if (status == HttpServletResponse.SC_OK || (range != null && range.startsWith("bytes=0-")))
                        {
                            this.logUtil.writeDownloadFileEvent(account, ip, n);
                        }
                        return;
                    }
                }
                else
                {
                    // 处理资源未被授权的问题
                    try
                    {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                    catch (IOException e)
                    {
                    }
                }
            }
        }
        try
        {
            // 处理无法下载的资源
            response.sendError(404);
        }
        catch (IOException e)
        {
        }
    }

    /**
     * <h2>返回资源</h2>
     * <p>
     * 该方法用于回传某个文件资源，支持断点续传。
     * </p>
     *
     * @param resource    java.io.File 要发送的文件资源
     * @param fname       java.lang.String 要传递给客户端的文件名，会加入到响应头中
     * @param contentType java.lang.String 返回资源的CONTENT_TYPE标识名，例如“text/html”
     * @param request     javax.servlet.http.HttpServletRequest 请求对象
     * @param response    javax.servlet.http.HttpServletResponse 响应对象
     * @return int 操作完毕后返回的状态码，例如“200”
     * @author 青阳龙野(kohgylw)
     */
    private int sendResource(File resource, String fname, String contentType, HttpServletRequest request,
                             HttpServletResponse response)
    {
        // 状态码，初始为200
        int status = HttpServletResponse.SC_OK;
        // 开始资源传输
        try (RandomAccessFile randomFile = new RandomAccessFile(resource, "r"))
        {
            long contentLength = randomFile.length();
            final String lastModified = ServerTimeUtil.getLastModifiedFormBlock(resource);
            // 如果请求中包含了对本地缓存的过期判定参数，则执行过期判定
            final String eTag = this.fileBlockUtil.getETag(resource);
            final String ifModifiedSince = request.getHeader("If-Modified-Since");
            final String ifNoneMatch = request.getHeader("If-None-Match");
            if (ifModifiedSince != null || ifNoneMatch != null)
            {
                if (ifNoneMatch != null)
                {
                    if (ifNoneMatch.trim().equals(eTag))
                    {
                        status = HttpServletResponse.SC_NOT_MODIFIED;
                        // 304;
                        response.setStatus(status);
                        return status;
                    }
                }
                else
                {
                    if (ifModifiedSince.trim().equals(lastModified))
                    {
                        status = HttpServletResponse.SC_NOT_MODIFIED;
                        // 304
                        response.setStatus(status);
                        return status;
                    }
                }
            }
            // 检查断点续传请求是否过期
            String ifUnmodifiedSince = request.getHeader("If-Unmodified-Since");
            if (ifUnmodifiedSince != null && !(ifUnmodifiedSince.trim().equals(lastModified)))
            {
                status = HttpServletResponse.SC_PRECONDITION_FAILED;
                // 412;
                response.setStatus(status);
                return status;
            }
            String ifMatch = request.getHeader("If-Match");
            if (ifMatch != null && !(ifMatch.trim().equals(eTag)))
            {
                status = HttpServletResponse.SC_PRECONDITION_FAILED;
                // 412;
                response.setStatus(status);
                return status;
            }
            // 如果缓存过期或无缓存，则按请求参数返回数据
            String range = request.getHeader("Range");
            long start = 0, end = 0;
            if (range != null && range.startsWith("bytes="))
            {
                String[] values = range.split("=")[1].split("-");
                start = Long.parseLong(values[0]);
                if (values.length > 1)
                {
                    end = Long.parseLong(values[1]);
                }
            }
            long requestSize = 0;
            if (end != 0 && end > start)
            {
                requestSize = end - start + 1;
            }
            else
            {
                requestSize = Long.MAX_VALUE;
            }
            byte[] buffer = new byte[ConfigureReader.getInstance().getBuffSize()];
            response.setContentType(contentType);
            response.setHeader("Accept-Ranges", "bytes");
            response.setHeader("ETag", this.fileBlockUtil.getETag(resource));
            response.setHeader("Last-Modified", ServerTimeUtil.getLastModifiedFormBlock(resource));
            response.setHeader("Cache-Control", "max-age=" + RESOURCE_CACHE_MAX_AGE);
            // 第一次请求只返回content length来让客户端请求多次实际数据
            final String ifRange = request.getHeader("If-Range");
            if (range != null && range.startsWith("bytes=")
                    && (ifRange == null || ifRange.trim().equals(eTag) || ifRange.trim().equals(lastModified)))
            {
                // 以后的多次以断点续传的方式来返回视频数据
                status = HttpServletResponse.SC_PARTIAL_CONTENT;
                // 206;
                response.setStatus(status);
                long requestStart = 0, requestEnd = 0;
                String[] ranges = range.split("=");
                if (ranges.length > 1)
                {
                    String[] rangeDatas = ranges[1].split("-");
                    requestStart = Long.parseLong(rangeDatas[0]);
                    if (rangeDatas.length > 1)
                    {
                        requestEnd = Long.parseLong(rangeDatas[1]);
                    }
                }
                long length = 0;
                if (requestEnd > 0)
                {
                    length = requestEnd - requestStart + 1;
                    response.setHeader("Content-length", "" + length);
                    response.setHeader("Content-Range",
                            "bytes " + requestStart + "-" + requestEnd + "/" + contentLength);
                }
                else
                {
                    length = contentLength - requestStart;
                    response.setHeader("Content-length", "" + length);
                    response.setHeader("Content-Range",
                            "bytes " + requestStart + "-" + (contentLength - 1) + "/" + contentLength);
                }
            }
            else
            {
                response.setHeader("Content-length", contentLength + "");
            }
            ServletOutputStream out = response.getOutputStream();
            long needSize = requestSize;
            randomFile.seek(start);
            while (needSize > 0)
            {
                int len = randomFile.read(buffer);
                if (needSize < buffer.length)
                {
                    out.write(buffer, 0, (int) needSize);
                }
                else
                {
                    out.write(buffer, 0, len);
                    if (len < buffer.length)
                    {
                        break;
                    }
                }
                needSize -= buffer.length;
            }
            out.close();
            return status;
        }
        catch (Exception e)
        {
            status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            return status;
        }
    }

    /**
     * 对docx的预览实现
     * @param fileId   java.lang.String 要读取的文件节点ID
     * @param request  javax.servlet.http.HttpServletRequest 请求对象
     * @param response javax.servlet.http.HttpServletResponse 响应对象
     */
    @Override
    public void getWordView(String fileId, HttpServletRequest request, HttpServletResponse response)
    {
        final String account = (String) request.getSession().getAttribute("ACCOUNT");
        // 权限检查
        if (fileId != null)
        {
            Node n = nodeMapper.queryById(fileId);
            if (n != null)
            {
                if (ConfigureReader.getInstance().authorized(account, AccountAuth.DOWNLOAD_FILES,
                        folderUtil.getAllFoldersId(n.getFileParentFolder()))
                        && ConfigureReader.getInstance().accessFolder(folderMapper.queryById(n.getFileParentFolder()), account))
                {
                    File file = fileBlockUtil.getFileFromBlocks(n);
                    if (file != null && file.isFile())
                    {
                        // 后缀检查
                        String suffix = "";
                        if (n.getFileName().indexOf(".") >= 0)
                        {
                            suffix = n.getFileName().substring(n.getFileName().lastIndexOf(".")).trim().toLowerCase();
                        }
                        if (".docx".equals(suffix))
                        {
                            String contentType = contentTypeMap.getContentType(".pdf");
                            response.setContentType(contentType);
                            String ip = ipAddrGetter.getIpAddr(request);
                            // 执行转换并写出输出流
                            try
                            {
                                docxToPdfUtil.convertPdf(new FileInputStream(file), response.getOutputStream());
                                logUtil.writeDownloadFileEvent(account, ip, n);
                                return;
                            }
                            catch (IOException e)
                            {
                            }
                            catch (Exception e)
                            {
                                Out.println(e.getMessage());
                                logUtil.writeException(e);
                            }
                        }
                    }
                }
            }
        }
        try
        {
            response.sendError(500);
        }
        catch (Exception e1)
        {
        }
    }

    /**
     * 对TXT预览的实现;
     * @param fileId   java.lang.String 要读取的文件节点ID
     * @param request  javax.servlet.http.HttpServletRequest 请求对象
     * @param response javax.servlet.http.HttpServletResponse 响应对象
     */
    @Override
    public void getTxtView(String fileId, HttpServletRequest request, HttpServletResponse response)
    {
        final String account = (String) request.getSession().getAttribute("ACCOUNT");
        // 权限检查
        if (fileId != null)
        {
            Node n = nodeMapper.queryById(fileId);
            if (n != null)
            {
                if (ConfigureReader.getInstance().authorized(account, AccountAuth.DOWNLOAD_FILES,
                        folderUtil.getAllFoldersId(n.getFileParentFolder()))
                        && ConfigureReader.getInstance().accessFolder(folderMapper.queryById(n.getFileParentFolder()), account))
                {
                    File file = fileBlockUtil.getFileFromBlocks(n);
                    if (file != null && file.isFile())
                    {
                        // 后缀检查
                        String suffix = "";
                        if (n.getFileName().indexOf(".") >= 0)
                        {
                            suffix = n.getFileName().substring(n.getFileName().lastIndexOf(".")).trim().toLowerCase();
                        }
                        if (".txt".equals(suffix))
                        {
                            String contentType = contentTypeMap.getContentType(".pdf");
                            response.setContentType(contentType);
                            String ip = ipAddrGetter.getIpAddr(request);
                            // 执行转换并写出输出流
                            try
                            {
                                txtToPdfUtil.convertPdf(file, response.getOutputStream());
                                logUtil.writeDownloadFileEvent(account, ip, n);
                                return;
                            }
                            catch (Exception e)
                            {
                                Out.println(e.getMessage());
                                logUtil.writeException(e);
                            }
                        }
                    }
                }
            }
        }
        try
        {
            response.sendError(500);
        }
        catch (Exception e1)
        {
        }
    }

    @Override
    public String getVideoTranscodeStatus(HttpServletRequest request)
    {
        if (diskFfmpegLocator.getFFMPEGExecutablePath() != null)
        {
            String fId = request.getParameter("fileId");
            if (fId != null)
            {
                try
                {
                    return videoTranscodeUtil.getTranscodeProcess(fId);
                }
                catch (Exception e)
                {
                    Out.println("错误：视频转码功能出现意外错误。详细信息：" + e.getMessage());
                    logUtil.writeException(e);
                }
            }
        }
        return "ERROR";
    }

    /**
     * 对PPT预览的实现;
     * @param fileId   java.lang.String 要读取的文件节点ID
     * @param request  javax.servlet.http.HttpServletRequest 请求对象
     * @param response javax.servlet.http.HttpServletResponse 响应对象
     */
    @Override
    public void getPPTView(String fileId, HttpServletRequest request, HttpServletResponse response)
    {
        final String account = (String) request.getSession().getAttribute("ACCOUNT");
        // 权限检查
        if (fileId != null)
        {
            Node n = nodeMapper.queryById(fileId);
            if (n != null)
            {
                if (ConfigureReader.getInstance().authorized(account, AccountAuth.DOWNLOAD_FILES,
                        folderUtil.getAllFoldersId(n.getFileParentFolder()))
                        && ConfigureReader.getInstance().accessFolder(folderMapper.queryById(n.getFileParentFolder()), account))
                {
                    File file = fileBlockUtil.getFileFromBlocks(n);
                    if (file != null && file.isFile())
                    {
                        // 后缀检查
                        String suffix = "";
                        if (n.getFileName().indexOf(".") >= 0)
                        {
                            suffix = n.getFileName().substring(n.getFileName().lastIndexOf(".")).trim().toLowerCase();
                        }
                        switch (suffix)
                        {
                            case ".ppt":
                            case ".pptx":
                                String contentType = contentTypeMap.getContentType(".pdf");
                                response.setContentType(contentType);
                                String ip = ipAddrGetter.getIpAddr(request);
                                // 执行转换并写出输出流
                                try
                                {
                                    pptToPdfUtil.convertPdf(new FileInputStream(file), response.getOutputStream(),
                                            ".ppt".equals(suffix) ? PowerPointType.PPT : PowerPointType.PPTX);
                                    logUtil.writeDownloadFileEvent(account, ip, n);
                                    return;
                                }
                                catch (IOException e)
                                {
                                }
                                catch (Exception e)
                                {
                                    Out.println(e.getMessage());
                                    logUtil.writeException(e);
                                }
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        }
        try
        {
            response.sendError(500);
        }
        catch (Exception e1)
        {
            e1.printStackTrace();
        }
    }

    @Override
    public void getLRContextByUTF8(String fileId, HttpServletRequest request, HttpServletResponse response)
    {
        final String account = (String) request.getSession().getAttribute("ACCOUNT");
        final String ip = ipAddrGetter.getIpAddr(request);
        // 权限检查
        if (fileId != null)
        {
            Node n = nodeMapper.queryById(fileId);
            if (n != null)
            {
                if (ConfigureReader.getInstance().authorized(account, AccountAuth.DOWNLOAD_FILES,
                        folderUtil.getAllFoldersId(n.getFileParentFolder()))
                        && ConfigureReader.getInstance().accessFolder(folderMapper.queryById(n.getFileParentFolder()), account))
                {
                    File file = fileBlockUtil.getFileFromBlocks(n);
                    if (file != null && file.isFile())
                    {
                        // 检查是否有可用缓存
                        String ifModifiedSince = request.getHeader("If-Modified-Since");
                        if (ifModifiedSince != null
                                && ifModifiedSince.trim().equals(ServerTimeUtil.getLastModifiedFormBlock(file)))
                        {
                            response.setStatus(304);
                            return;
                        }
                        String ifNoneMatch = request.getHeader("If-None-Match");
                        if (ifNoneMatch != null && ifNoneMatch.trim().equals(this.fileBlockUtil.getETag(file)))
                        {
                            response.setStatus(304);
                            return;
                        }
                        // 如无，则返回新数据
                        // 后缀检查
                        String suffix = "";
                        if (n.getFileName().indexOf(".") >= 0)
                        {
                            suffix = n.getFileName().substring(n.getFileName().lastIndexOf(".")).trim().toLowerCase();
                        }
                        if (".lrc".equals(suffix))
                        {
                            String contentType = "text/plain";
                            response.setContentType(contentType);
                            response.setCharacterEncoding("UTF-8");
                            response.setHeader("ETag", this.fileBlockUtil.getETag(file));
                            response.setHeader("Last-Modified", ServerTimeUtil.getLastModifiedFormBlock(file));
                            response.setHeader("Cache-Control", "max-age=" + RESOURCE_CACHE_MAX_AGE);
                            // 执行转换并写出输出流
                            try
                            {
                                String inputFileEncode = txtCharsetGetter.getTxtCharset(new FileInputStream(file));
                                BufferedReader bufferedReader = new BufferedReader(
                                        new InputStreamReader(new FileInputStream(file), inputFileEncode));
                                BufferedWriter bufferedWriter = new BufferedWriter(
                                        new OutputStreamWriter(response.getOutputStream(), "UTF-8"));
                                String line;
                                while ((line = bufferedReader.readLine()) != null)
                                {
                                    bufferedWriter.write(line);
                                    bufferedWriter.newLine();
                                }
                                bufferedWriter.close();
                                bufferedReader.close();
                                this.logUtil.writeDownloadFileEvent(account, ip, n);
                                return;
                            }
                            catch (IOException e)
                            {
                            }
                            catch (Exception e)
                            {
                                Out.println(e.getMessage());
                                logUtil.writeException(e);
                            }

                        }
                    }
                }
            }
        }
        try
        {
            response.sendError(500);
        }
        catch (Exception e1)
        {
            e1.printStackTrace();
        }
    }

    @Override
    public void getNoticeContext(HttpServletRequest request, HttpServletResponse response)
    {
        File noticeHTML = new File(ConfigureReader.getInstance().getTemporaryfilePath(), NoticeUtil.NOTICE_OUTPUT_NAME);
        String contentType = "text/html";
        if (noticeHTML.isFile() && noticeHTML.canRead())
        {
            sendResource(noticeHTML, NoticeUtil.NOTICE_FILE_NAME, contentType, request, response);
        }
        else
        {
            try
            {
                response.setContentType(contentType);
                response.setCharacterEncoding("UTF-8");
                PrintWriter writer = response.getWriter();
                writer.write("<p class=\"lead\">暂无新公告。</p>");
                writer.flush();
                writer.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String requireNoticeMD5()
    {
        return noticeUtil.getMd5();
    }
}
