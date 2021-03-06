package edu.swufe.nxksecdisk.server.controller;

import edu.swufe.nxksecdisk.server.service.ResourceService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 转为在线资源打造的控制器，针对在线播放等功能的适配;
 *
 * @author Administrator
 */
@Controller
@RequestMapping("/resourceController")
public class ResourceController {

    /**
     * 资源相关的服务层，详见具体实现;
     */
    @Resource
    private ResourceService resourceService;

    /**
     * 以严格的HTTP响应格式返回在线资源流，适用于多数浏览器;
     *
     * @param fileId
     * @param request
     * @param response
     */
    @RequestMapping("/getResource/{fileId}")
    public void getResource(@PathVariable("fileId") String fileId, HttpServletRequest request,
                            HttpServletResponse response) {
        resourceService.requireResource(fileId, request, response);
    }

    /**
     * 以PDF格式获取word预览视图;
     *
     * @param fileId
     * @param request
     * @param response
     */
    @RequestMapping("/getWordView/{fileId}")
    public void getWordView(@PathVariable("fileId") String fileId, HttpServletRequest request,
                            HttpServletResponse response) {
        resourceService.getWordView(fileId, request, response);
    }

    /**
     * 以PDF格式获取TXT预览视图;
     *
     * @param fileId
     * @param request
     * @param response
     */
    @RequestMapping("/getTxtView/{fileId}")
    public void getTxtView(@PathVariable("fileId") String fileId, HttpServletRequest request,
                           HttpServletResponse response) {
        resourceService.getTxtView(fileId, request, response);
    }

    /**
     * 以PDF格式获取PPT预览视图;
     *
     * @param fileId
     * @param request
     * @param response
     */
    @RequestMapping("/getPPTView/{fileId}")
    public void getPPTView(@PathVariable("fileId") String fileId, HttpServletRequest request,
                           HttpServletResponse response) {
        resourceService.getPPTView(fileId, request, response);
    }

    /**
     * 得到UTF-8编码格式的LRC歌词（文本）流;
     *
     * @param fileId
     * @param request
     * @param response
     */
    @RequestMapping("/getLRContext/{fileId}")
    public void getLRContext(@PathVariable("fileId") String fileId, HttpServletRequest request,
                             HttpServletResponse response) {
        resourceService.getLRContextByUTF8(fileId, request, response);
    }

    /**
     * 获取视频转码状态，如指定视频未开始转码则开始，如已经开始则返回进度，如已经完成则返回FIN;
     *
     * @param request
     * @return
     */
    @RequestMapping("/getVideoTranscodeStatus.ajax")
    public @ResponseBody
    String getVideoTranscodeStatus(HttpServletRequest request) {
        return resourceService.getVideoTranscodeStatus(request);
    }

    /**
     * 获取公告新的的MD5，如果没有公告信息，则返回null;
     *
     * @return
     */
    @RequestMapping("/getNoticeMD5.ajax")
    public @ResponseBody
    String getNoticeMD5() {
        return resourceService.requireNoticeMD5();
    }

    /**
     * 获取公告信息的HTML文本流;
     *
     * @param request
     * @param response
     */
    @RequestMapping("/getNoticeContext.do")
    public void getNoticeContext(HttpServletRequest request, HttpServletResponse response) {
        resourceService.getNoticeContext(request, response);
    }

}
