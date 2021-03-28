package edu.swufe.nxksecdisk.server.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Administrator
 */
public interface ShowPictureService {

    String getPreviewPictureJson(final HttpServletRequest request);

    /**
     * <h2>获取压缩版图片</h2>
     * <p>该方法用于获取指定图片的压缩流，以便于提高预览速度。</p>
     *
     * @param request HttpServletRequest 请求对象，应包含文件块路径fp，该方法会根据其大小自动判定压缩率。
     * @author 青阳龙野(kohgylw)
     */
    void requireCondensedPicture(final HttpServletRequest request, final HttpServletResponse response);
}
