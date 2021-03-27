package edu.swufe.nxksecdisk.server.service;

import javax.servlet.http.HttpServletRequest;

public interface PlayVideoService
{
    /**
     * <h2>解析播放视频文件</h2>
     * <p>
     * 根据视频文件的ID查询视频文件节点、判断是否需要进行转码并返回节点的JSON信息，以便页面发起播放请求。
     * </p>
     *
     * @param request javax.servlet.http.HttpServletRequest 请求对象
     * @return String 视频节点的JSON字符串，由kohgylw.kiftd.server.pojo.VideoInfo对象转化而来。
     * @author kohgylw
     */
    String parsePlayVideoJson(final HttpServletRequest request);
}
